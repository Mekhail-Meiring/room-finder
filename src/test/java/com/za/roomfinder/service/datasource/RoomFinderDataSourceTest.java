package com.za.roomfinder.service.datasource;

import com.za.roomfinder.service.datasource.dto.*;
import com.za.roomfinder.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

public class RoomFinderDataSourceTest {

    private RoomFinderDataSource dataSource;

    @BeforeEach
    public void setUp() {
        dataSource = new RoomFinderDataSource();
    }

    @Test
    @DisplayName("Should be able to book a room")
    public void testBookRoomSuccess() {
        BookingRequest bookingRequest = new BookingRequest(1, LocalDate.now().plusDays(5).toString());
        RoomPrice roomPrice = dataSource.bookRoom(bookingRequest);
        RoomPaymentRequest roomPaymentRequest = new RoomPaymentRequest(1, bookingRequest.date(), roomPrice.price());
        dataSource.payForBooking(roomPaymentRequest);

        assertEquals(1, dataSource.getBookedRooms().size());
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
        dataSource.registerClient(client);

        // Then:
        assertEquals(1, dataSource.getClients().size());
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

        dataSource.registerClient(client);
        assertEquals(1, dataSource.getClients().size());

        // When:
        Client client1 = new Client(
                1234,
                "Johnny",
                "Doe",
                "example1@email.com",
                "223456789");

        // Then:
        assertThrows(ClientRegistrationException.class, () -> dataSource.registerClient(client1));
        assertEquals(1, dataSource.getClients().size());
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

        dataSource.registerClient(client);
        assertEquals(1, dataSource.getClients().size());

        // When:
        Client loggedInClient = dataSource.clientLogin(client.idNumber());

        // Then:
        assertEquals(client, loggedInClient);

    }


    @Test
    @DisplayName("Client should not be able to login if not registered")
    public void clientShouldNotBeAbleToLoginIfNotRegistered (){

        // Given:
        assertEquals(0, dataSource.getClients().size());

        // When/Then:
        assertThrows(ClientNotFoundException.class, () -> dataSource.clientLogin(1234));

    }


    @Test
    @DisplayName("Should not be able to make a booking on a day that is already taken")
    public void shouldNotBeAbleToMakeABookingOnADayThatIsAlreadyTaken() {

        // Given
        BookingRequest bookingRequest1 = new BookingRequest(1, LocalDate.now().toString());
        RoomPrice roomPrice = dataSource.bookRoom(bookingRequest1);
        RoomPaymentRequest roomPaymentRequest = new RoomPaymentRequest(1, bookingRequest1.date(), roomPrice.price());
        dataSource.payForBooking(roomPaymentRequest);

        // When/Then
        BookingRequest bookingRequest2 = new BookingRequest(1, LocalDate.now().toString());
        assertThrows(RoomNotAvailableException.class, () -> dataSource.bookRoom(bookingRequest2));
        assertEquals(1, dataSource.getBookedRooms().size());
    }


    @Test
    @DisplayName("Should not be able to make a booking on a day that is in the past")
    public void testBookRoomStartDateInPast(){

        // Given
        BookingRequest bookingRequest = new BookingRequest(1, LocalDate.now().minusDays(1).toString());

        // When/Then
        assertThrows(RoomNotAvailableException.class, () -> dataSource.bookRoom(bookingRequest));
        assertEquals(0, dataSource.getBookedRooms().size());
    }


    @Test
    @DisplayName("Should not be able to make a booking on a day that is in the past")
    public void testCalculatePrice() {

        // Given
        String fortyFirstDayOfTheYear = LocalDate.of(2023, 2, 10).toString();

        // When
        BookingRequest bookingRequest = new BookingRequest(1, fortyFirstDayOfTheYear);
        Double price = dataSource.calculatePrice(bookingRequest);


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
        long daysUntilBooked = dataSource.getAmountOfDaysBeforeBookedDate(bookedRoom);
        assertEquals(2, daysUntilBooked);
    }

    @Test
    @DisplayName("Should be able to get the percentage of the price that should be refunded")
    public void shouldBeAbleToGetPercentageDiscountForAmountOfDays(){
        assertEquals(0, dataSource.getPercentageFee(1));
        assertEquals(0.25, dataSource.getPercentageFee(4));
        assertEquals(0.5, dataSource.getPercentageFee(8));
        assertEquals(1.0, dataSource.getPercentageFee(15));
    }


    @Test
    @DisplayName("Should be able to cancel a booking two days before and get no refund")
    public void shouldBeAbleCancelBookingTwoDaysBeforeAndGetNoRefund() {

        // Booked for two days after today
        String bookedForDate = LocalDate.now().plusDays(2).toString();

        BookingRequest bookingRequest = new BookingRequest(1, bookedForDate);
        RoomPrice priceOfRoom = dataSource.bookRoom(bookingRequest);
        RoomPaymentRequest roomPaymentRequest = new RoomPaymentRequest(1, bookingRequest.date(), priceOfRoom.price());
        dataSource.payForBooking(roomPaymentRequest);

        assertEquals(1, dataSource.getBookedRooms().size());

        double price = dataSource.getBookedRooms().get(0).price();
        assertTrue(price > 0);

        double refundAmount = dataSource.cancelBooking(1, 1);


        assertEquals(0, dataSource.getBookedRooms().size());

        // 0% of the price refunded = 0.0
        assertEquals(0.0, refundAmount);

    }


    @Test
    @DisplayName("Should be able to cancel a booking four days before and get a 25% refund")
    public void shouldBeAbleCancelBookingFourDaysBeforeAndGet25percentRefund(){

        // Booked for four days after today
        String bookedForDate = LocalDate.now().plusDays(4).toString();

        BookingRequest bookingRequest = new BookingRequest(1, bookedForDate);
        RoomPrice priceOfRoom = dataSource.bookRoom(bookingRequest);
        RoomPaymentRequest roomPaymentRequest = new RoomPaymentRequest(1, bookingRequest.date(), priceOfRoom.price());
        dataSource.payForBooking(roomPaymentRequest);

        assertEquals(1, dataSource.getBookedRooms().size());

        double price = dataSource.getBookedRooms().get(0).price();

        assertTrue(price > 0);

        double refundAmount = dataSource.cancelBooking(1, 1);

        assertEquals(0, dataSource.getBookedRooms().size());

        // 25% of the price refunded
        double expectedRefundAmount = Math.round( (0.25 * price) * 100.0) / 100.0;
        assertEquals(expectedRefundAmount, refundAmount);
    }


    @Test
    @DisplayName("Should be able to cancel a booking Eight days before and get a 50% refund")
    public void shouldBeAbleCancelBookingEightDaysBeforeAndGet50percentRefund(){

        // Booked for eight days after today
        String bookedForDate = LocalDate.now().plusDays(8).toString();

        BookingRequest bookingRequest = new BookingRequest(1, bookedForDate);

        RoomPrice priceOfRoom = dataSource.bookRoom(bookingRequest);
        RoomPaymentRequest roomPaymentRequest = new RoomPaymentRequest(1, bookingRequest.date(), priceOfRoom.price());
        dataSource.payForBooking(roomPaymentRequest);

        assertEquals(1, dataSource.getBookedRooms().size());

        double price = dataSource.getBookedRooms().get(0).price();

        assertTrue(price > 0);

        double refundAmount = dataSource.cancelBooking(1, 1);

        assertEquals(0, dataSource.getBookedRooms().size());

        // 50% of the price refunded
        double expectedRefundAmount = Math.round( (0.5 * price) * 100.0) / 100.0;
        assertEquals(expectedRefundAmount, refundAmount);
    }


    @Test
    @DisplayName("Should be able to cancel a booking Fifteen days before and get a 100% refund")
    public void testCancelBookingFifteenDaysBefore() {

        // Booked for fifteen days after today
        String bookedForDate = LocalDate.now().plusDays(15).toString();

        BookingRequest bookingRequest = new BookingRequest(1, bookedForDate);
        RoomPrice priceOfRoom = dataSource.bookRoom(bookingRequest);
        RoomPaymentRequest roomPaymentRequest = new RoomPaymentRequest(1, bookingRequest.date(), priceOfRoom.price());
        dataSource.payForBooking(roomPaymentRequest);

        assertEquals(1, dataSource.getBookedRooms().size());

        double price = dataSource.getBookedRooms().get(0).price();

        assertTrue(price > 0);

        double refundAmount = dataSource.cancelBooking(1, 1);

        assertEquals(0, dataSource.getBookedRooms().size());

        // 100% of the price refunded
        assertEquals(price, refundAmount);
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

        double rescheduleFee =  dataSource.getRescheduleFee(bookedRoom);

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
        double rescheduleFee =  dataSource.getRescheduleFee(bookedRoom);

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

        double rescheduleFee =  dataSource.getRescheduleFee(bookedRoom);

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
        double rescheduleFee =  dataSource.getRescheduleFee(bookedRoom);

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
        RoomPrice roomPrice = dataSource.bookRoom(bookingRequest);
        RoomPaymentRequest roomPaymentRequest = new RoomPaymentRequest(1, bookingRequest.date(), roomPrice.price());
        dataSource.payForBooking(roomPaymentRequest);

        assertEquals(1, dataSource.getBookedRooms().size());
        assertEquals(bookedForDate, dataSource.getBookedRooms().get(0).date());
        assertEquals(1, dataSource.getBookedRooms().get(0).bookingId());
        assertTrue(dataSource.getBookedRooms().get(0).price() > 0.0);

        // When:
        String rescheduleDate = LocalDate.now().plusDays(6).toString();
        BookingRequest rescheduleBookingRequest = new BookingRequest(1, rescheduleDate);

        RoomPrice reschedulePrice = dataSource.rescheduleBooking(1, rescheduleBookingRequest);
        dataSource.payForReschedule(1, reschedulePrice.price(), rescheduleBookingRequest);
        assertEquals(rescheduleDate, dataSource.getBookedRooms().get(0).date());


        // Then:

        // no fee for same day reschedule
        assertEquals(0.0, dataSource.getBookedRooms().get(0).price());
    }

    @Test
    @DisplayName("hasBookedFiveRoomsInWeek should return false if client has no booked rooms in a week")
    public void shouldBeAbleToCheckIfClientHasBooked5RoomsInAWeek1(){

        // When:
        boolean hasBooked5RoomsInAWeek = dataSource.hasBookedFiveRoomsInWeek(1);

        // Then:
        assertFalse(hasBooked5RoomsInAWeek);

    }

    @Test
    @DisplayName("Should throw RoomNotAvailableException if client has already booked 5 rooms in a week")
    public void shouldThrowRoomNotAvailableException(){

        // When/Then:

        RoomNotAvailableException exception = assertThrows(RoomNotAvailableException.class,  () -> {
            for (int i = 1; i <= 20; i++) {
                String bookedForDate = LocalDate.now().plusDays(i).toString();
                BookingRequest bookingRequest = new BookingRequest(1, bookedForDate);
                RoomPrice roomPrice = dataSource.bookRoom(bookingRequest);
                RoomPaymentRequest roomPaymentRequest = new RoomPaymentRequest(1, bookingRequest.date(), roomPrice.price());
                dataSource.payForBooking(roomPaymentRequest);
            }
        });

        assertEquals(
                "com.za.roomfinder.exceptions.RoomNotAvailableException: Client has already booked 5 rooms in a week",
                exception.getMessage());

    }
}
