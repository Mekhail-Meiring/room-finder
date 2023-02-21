package com.za.roomfinder.datasource;

import com.za.roomfinder.dto.BookedRoom;
import com.za.roomfinder.dto.BookingRequest;
import com.za.roomfinder.dto.Client;
import com.za.roomfinder.exceptions.ClientNotFoundException;
import com.za.roomfinder.exceptions.ClientRegistrationException;
import com.za.roomfinder.exceptions.RoomNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private BookingService bookingService;

    @BeforeEach
    public void setUp() {
        bookingService = new BookingService();
    }

    @Test
    @DisplayName("Should be able to book a room")
    public void testBookRoomSuccess() {
        BookingRequest bookingRequest = new BookingRequest(1, LocalDate.now().plusDays(5).toString());
        bookingService.bookRoom(bookingRequest);

        assertEquals(1, bookingService.getBookedRooms().size());
    }

    @Test
    @DisplayName("Client should be able to register")
    public void clientShouldBeAbleToRegister (){

        // Given:
        Client client = new Client(
                1234,
                "John",
                "Doe",
                "example@email.com",
                "123456789");


        // When:
        bookingService.registerClient(client);

        // Then:
        assertEquals(1, bookingService.getClients().size());
    }

    @Test
    @DisplayName("Client should not be able to register with same id number")
    public void clientShouldNotBeAbleToRegisterTwice(){

        // Given:
        Client client = new Client(
                1234,
                "John",
                "Doe",
                "example@email.com",
                "123456789");

        bookingService.registerClient(client);
        assertEquals(1, bookingService.getClients().size());

        // When:
        Client client1 = new Client(
                1234,
                "Johnny",
                "Doe",
                "example1@email.com",
                "223456789");

        // Then:
        assertThrows(ClientRegistrationException.class, () -> bookingService.registerClient(client1));
        assertEquals(1, bookingService.getClients().size());
    }

    @Test
    @DisplayName("Client should be able to login after registration")
    public void clientShouldBeAbleToLoginAfterRegistration (){

        // Given:
        Client client = new Client(
                1234,
                "John",
                "Doe",
                "example@email.com",
                "123456789");

        bookingService.registerClient(client);
        assertEquals(1, bookingService.getClients().size());

        // When:
        Client loggedInClient = bookingService.clientLogin(client);

        // Then:
        assertEquals(client, loggedInClient);

    }


    @Test
    @DisplayName("Client should not be able to login if not registered")
    public void clientShouldNotBeAbleToLoginIfNotRegistered (){

        // Given:
        assertEquals(0, bookingService.getClients().size());

        // When/Then:
        Client client = new Client(
                1234,
                "John",
                "Doe",
                "example@email.com",
                "123456789");

        assertThrows(ClientNotFoundException.class, () -> bookingService.clientLogin(client));

    }


    @Test
    @DisplayName("Should not be able to make a booking on a day that is already taken")
    public void shouldNotBeAbleToMakeABookingOnADayThatIsAlreadyTaken() {

        // Given
        BookingRequest bookingRequest1 = new BookingRequest(1, LocalDate.now().toString());
        bookingService.bookRoom(bookingRequest1);

        // When/Then
        BookingRequest bookingRequest2 = new BookingRequest(1, LocalDate.now().toString());
        assertThrows(RoomNotAvailableException.class, () -> bookingService.bookRoom(bookingRequest2));
        assertEquals(1, bookingService.getBookedRooms().size());
    }


    @Test
    @DisplayName("Should not be able to make a booking on a day that is in the past")
    public void testBookRoomStartDateInPast(){

        // Given
        BookingRequest bookingRequest = new BookingRequest(1, LocalDate.now().minusDays(1).toString());

        // When/Then
        assertThrows(RoomNotAvailableException.class, () -> bookingService.bookRoom(bookingRequest));
        assertEquals(0, bookingService.getBookedRooms().size());
    }


    @Test
    @DisplayName("Should not be able to make a booking on a day that is in the past")
    public void testCalculatePrice() {

        // Given
        String fortyFirstDayOfTheYear = LocalDate.of(2023, 2, 10).toString();

        // When
        BookingRequest bookingRequest = new BookingRequest(1, fortyFirstDayOfTheYear);
        Double price = bookingService.calculatePrice(bookingRequest);


        // Then

        // price for room formula = (day of year + 100) / 12
        // February 10th = 41st day of the year
        // (41 + 100) / 12 = 11.75
        assertEquals(price, 11.75);
    }


    @Test
    @DisplayName("Should be able to get the number of days until a day is booked")
    public void shouldBeAbleGetNumberOfDaysUntilADayIsBooked(){
        LocalDate mockDate = LocalDate.now();
        BookedRoom bookedRoom = new BookedRoom(1, 1, mockDate.plusDays(2).toString(), mockDate.toString(), 0);
        long daysUntilBooked = bookingService.getAmountOfDaysBeforeBookedDate(bookedRoom);
        assertEquals(2, daysUntilBooked);
    }

    @Test
    @DisplayName("Should be able to get the percentage of the price that should be refunded")
    public void shouldBeAbleToGetPercentageDiscountForAmountOfDays(){
        assertEquals(0, bookingService.getPercentageFee(1));
        assertEquals(0.25, bookingService.getPercentageFee(4));
        assertEquals(0.5, bookingService.getPercentageFee(8));
        assertEquals(1.0, bookingService.getPercentageFee(15));
    }


    @Test
    @DisplayName("Should be able to cancel a booking two days before and get no refund")
    public void shouldBeAbleCancelBookingTwoDaysBeforeAndGetNoRefund() throws ExecutionException, InterruptedException {

        // Booked for two days after today
        String bookedForDate = LocalDate.now().plusDays(2).toString();

        BookingRequest bookingRequest = new BookingRequest(1, bookedForDate);
        bookingService.bookRoom(bookingRequest);

        assertEquals(1, bookingService.getBookedRooms().size());

        double price = bookingService.getBookedRooms().get(0).price();
        assertEquals(price, 12.83);

        double refundAmount = bookingService.cancelBooking(1, 1);


        assertEquals(0, bookingService.getBookedRooms().size());

        // 0% of the price refunded = 0.0
        assertEquals(0.0, refundAmount);

    }


    @Test
    @DisplayName("Should be able to cancel a booking four days before and get a 25% refund")
    public void shouldBeAbleCancelBookingFourDaysBeforeAndGet25percentRefund() throws ExecutionException, InterruptedException {

        // Booked for four days after today
        String bookedForDate = LocalDate.now().plusDays(4).toString();

        BookingRequest bookingRequest = new BookingRequest(1, bookedForDate);
        bookingService.bookRoom(bookingRequest);

        assertEquals(1, bookingService.getBookedRooms().size());

        double price = bookingService.getBookedRooms().get(0).price();
        assertEquals(13.0, price);

        double refundAmount = bookingService.cancelBooking(1, 1);


        assertEquals(0, bookingService.getBookedRooms().size());

        // 25% of the price refunded = 3.23
        assertEquals(3.25, refundAmount);
    }


    @Test
    @DisplayName("Should be able to cancel a booking four days before and get a 50% refund")
    public void shouldBeAbleCancelBookingEightDaysBeforeAndGet50percentRefund() throws ExecutionException, InterruptedException {

        // Booked for eight days after today
        String bookedForDate = LocalDate.now().plusDays(8).toString();

        BookingRequest bookingRequest = new BookingRequest(1, bookedForDate);

        bookingService.bookRoom(bookingRequest);


        assertEquals(1, bookingService.getBookedRooms().size());

        double price = bookingService.getBookedRooms().get(0).price();
        assertEquals(13.33, price);

        double refundAmount = bookingService.cancelBooking(1, 1);


        assertEquals(0, bookingService.getBookedRooms().size());

        // 50% of the price refunded = 6.63
        assertEquals(6.67, refundAmount);
    }


    @Test
    public void testCancelBookingFifteenDaysBefore() throws ExecutionException, InterruptedException {

        // Booked for fifteen days after today
        String bookedForDate = LocalDate.now().plusDays(15).toString();

        BookingRequest bookingRequest = new BookingRequest(1, bookedForDate);
        bookingService.bookRoom(bookingRequest);

        assertEquals(1, bookingService.getBookedRooms().size());

        double price = bookingService.getBookedRooms().get(0).price();
        assertEquals(13.92,price);

        double refundAmount = bookingService.cancelBooking(1, 1);


        assertEquals(0, bookingService.getBookedRooms().size());

        // 100% of the price refunded = 13.83
        assertEquals(13.92, refundAmount);
    }


    @Test
    @DisplayName("Should be charged 0% extra of original price for booking after 1 days")
    public void shouldBeAbleToGetTheRescheduleFee(){

        // Given:

        double bookedRoomPrice = 15.0;

        BookedRoom bookedRoom = new BookedRoom(
                1,
                1,
                LocalDate.now().plusDays(5).toString(),
                LocalDate.now().minusDays(1).toString(),
                bookedRoomPrice);

        // When:

        double rescheduleFee =  bookingService.getRescheduleFee(bookedRoom);

        // Then:

        // 0% of bookedRoomPrice = 0
        assertEquals(0.0, rescheduleFee);

    }


    @Test
    @DisplayName("Should be charged 25% extra of original price for booking after 3 days")
    public void shouldBeAbleToTestRescheduleFee (){

        // Given:

        double bookedRoomPrice = 15.0;

        BookedRoom bookedRoom = new BookedRoom(
                1,
                1,
                LocalDate.now().plusDays(5).toString(),
                LocalDate.now().minusDays(4).toString(),
                bookedRoomPrice);

        // When:
        double rescheduleFee =  bookingService.getRescheduleFee(bookedRoom);

        // Then:

        // 25% of bookedRoomPrice
        assertEquals(3.75, rescheduleFee);
    }


    @Test
    @DisplayName("Should be charged 50% extra of original price for booking after 7 days")
    public void shouldBeCharged50percentExtraForBookingAfter7days (){

        // Given:

        double bookedRoomPrice = 15.0;

        BookedRoom bookedRoom = new BookedRoom(
                1,
                1,
                LocalDate.now().plusDays(5).toString(),
                LocalDate.now().minusDays(8).toString(),
                bookedRoomPrice);

        // When:

        double rescheduleFee =  bookingService.getRescheduleFee(bookedRoom);

        // Then:

        // 50% of bookedRoomPrice
        assertEquals(7.5, rescheduleFee);

    }


    @Test
    @DisplayName("Should be charged 100% extra of original price for booking after 7 days")
    public void shouldBeCharged100percentExtraForBookingAfter14days (){

        // Given:

        double bookedRoomPrice = 15.0;

        BookedRoom bookedRoom = new BookedRoom(
                1,
                1,
                LocalDate.now().plusDays(5).toString(),
                LocalDate.now().minusDays(15).toString(),
                bookedRoomPrice);

        // When:
        double rescheduleFee =  bookingService.getRescheduleFee(bookedRoom);

        // Then:

        // 100% of bookedRoomPrice
        assertEquals(bookedRoomPrice, rescheduleFee);

    }

    @Test
    @DisplayName("Should be able to successfully book and reschedule a booked room")
    public void shouldBeAbleToRescheduleABookedDate(){

        // Given:
        String bookedForDate = LocalDate.now().plusDays(5).toString();
        BookingRequest bookingRequest = new BookingRequest(1, bookedForDate);
        bookingService.bookRoom(bookingRequest);

        assertEquals(1, bookingService.getBookedRooms().size());
        assertEquals(bookedForDate, bookingService.getBookedRooms().get(0).date());
        assertEquals(1, bookingService.getBookedRooms().get(0).bookingId());
        assertTrue(bookingService.getBookedRooms().get(0).price() > 0.0);

        // When:
        String rescheduleDate = LocalDate.now().plusDays(6).toString();
        BookingRequest rescheduleBookingRequest = new BookingRequest(1, rescheduleDate);

        bookingService.rescheduleBooking(1, rescheduleBookingRequest);
        assertEquals(rescheduleDate, bookingService.getBookedRooms().get(0).date());


        // Then:

        // no fee for same day reschedule
        assertEquals(0.0, bookingService.getBookedRooms().get(0).price());
    }



}
