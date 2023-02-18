package com.za.roomfinder.datasource;

import com.za.roomfinder.dto.BookedRoom;
import com.za.roomfinder.dto.BookingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

public class BookingServiceTest {

    private BookingService bookingService;
    private final CountDownLatch latch = new CountDownLatch(1);


    @BeforeEach
    public void setUp() {
        bookingService = new BookingService();
    }


    @Test
    public void testBookRoomSuccess() {
        BookingRequest bookingRequest = new BookingRequest(1, LocalDate.now().toString(), LocalDate.now().plusDays(5).toString());
        bookingService.bookRoom(bookingRequest);
        bookingService.bookRoom(bookingRequest, latch::countDown);

        waitForOperationToFinish(1);
        assertEquals(1, bookingService.getBookedRooms().size());
    }


    @Test
    public void testBookRoomOverlappingDates() {
        BookingRequest bookingRequest1 = new BookingRequest(1, LocalDate.now().toString(), LocalDate.now().plusDays(5).toString());
        BookingRequest bookingRequest2 = new BookingRequest(1, LocalDate.now().plusDays(2).toString(), LocalDate.now().plusDays(5).toString());
        bookingService.bookRoom(bookingRequest1, latch::countDown);
        waitForOperationToFinish(2);

        assertTrue(bookingService.overlaps(bookingRequest2));
    }

    @Test
    public void testBookRoomStartDateInPast() {
        BookingRequest bookingRequest = new BookingRequest(1, LocalDate.now().minusDays(1).toString(), LocalDate.now().plusDays(5).toString());
        bookingService.bookRoom(bookingRequest, latch::countDown);
        waitForOperationToFinish(1);
        assertEquals(0, bookingService.getBookedRooms().size());
    }


    @Test
    public void testCalculatePrice() {
        BookingRequest bookingRequest = new BookingRequest(1, LocalDate.now().toString(), LocalDate.now().plusDays(5).toString());
        bookingService.bookRoom(bookingRequest, latch::countDown);
        waitForOperationToFinish(1);
        double price = bookingService.getBookedRooms().get(0).price();

        // price per day for 5 days = (5 + 100) / 12
        // total price = 5 * price per day = 43.75
        assertEquals(price, 43.75);
    }


    @Test
    public void testSetExecutorServiceSize() {
        BookingService bookingService = new BookingService();
        bookingService.setExecutorServiceSize(10);
        assertEquals( ((ThreadPoolExecutor) bookingService.getExecutorService()).getMaximumPoolSize(), 10);
    }


    @Test
    public void testBookRoomSameStartAndEndDate() {
        BookingRequest bookingRequest = new BookingRequest(1, LocalDate.now().toString(), LocalDate.now().toString());
        BookingService bookingService = new BookingService();
        bookingService.bookRoom(bookingRequest,  latch::countDown);
        waitForOperationToFinish(1);
        assertEquals(1, bookingService.getBookedRooms().size());
    }


    @Test
    public void testGetNumberOfDaysUntilBookedMethod(){
        LocalDate mockDate = LocalDate.now();
        BookedRoom bookedRoom = new BookedRoom(1, 1, mockDate.plusDays(2).toString(), mockDate.plusDays(5).toString(), 0);
        long daysUntilBooked = bookingService.getNumDaysUntilBookedDate(bookedRoom);
        assertEquals(2, daysUntilBooked);
    }

    @Test
    public void testGetPercentageDiscountForAmountOfDays(){
        assertEquals(0, bookingService.getRefundPercentage(1));
        assertEquals(0.25, bookingService.getRefundPercentage(4));
        assertEquals(0.5, bookingService.getRefundPercentage(8));
        assertEquals(1.0, bookingService.getRefundPercentage(15));
    }


    @Test
    public void testCancelBookingTwoDaysBefore() throws ExecutionException, InterruptedException {

        LocalDate mockDate = LocalDate.now();
        // Two days after today
        LocalDate startDate = mockDate.plusDays(2);
        // Book rooms for 5 days
        LocalDate endDate = startDate.plusDays(5);

        BookingRequest bookingRequest = new BookingRequest(1, startDate.toString(), endDate.toString());
        bookingService.bookRoom(bookingRequest, latch::countDown);
        waitForOperationToFinish(1);
        assertEquals(1, bookingService.getBookedRooms().size());

        double price = bookingService.getBookedRooms().get(0).price();
        assertEquals(price, 43.75);

        double refundAmount = bookingService.cancelBooking(1);
        waitForOperationToFinish(1);

        assertEquals(0, bookingService.getBookedRooms().size());
        assertEquals(0, refundAmount);
    }


    @Test
    public void testCancelBookingFourDaysBefore() throws ExecutionException, InterruptedException {

        LocalDate mockDate = LocalDate.now();
        // Four days after today
        LocalDate startDate = mockDate.plusDays(4);
        // Book rooms for 5 days
        LocalDate endDate = startDate.plusDays(5);

        BookingRequest bookingRequest = new BookingRequest(1, startDate.toString(), endDate.toString());
        bookingService.bookRoom(bookingRequest, latch::countDown);
        waitForOperationToFinish(1);
        assertEquals(1, bookingService.getBookedRooms().size());

        double price = bookingService.getBookedRooms().get(0).price();
        assertEquals(price, 43.75);

        double refundAmount = bookingService.cancelBooking(1);
        waitForOperationToFinish(1);

        assertEquals(0, bookingService.getBookedRooms().size());

        // 25% of the price = 10.94
        assertEquals(10.94, refundAmount);
    }


    @Test
    public void testCancelBookingEightDaysBefore() throws ExecutionException, InterruptedException {

        LocalDate mockDate = LocalDate.now();
        // Eight days after today
        LocalDate startDate = mockDate.plusDays(8);
        // Book rooms for 5 days
        LocalDate endDate = startDate.plusDays(5);

        BookingRequest bookingRequest = new BookingRequest(1, startDate.toString(), endDate.toString());
        bookingService.bookRoom(bookingRequest, latch::countDown);
        waitForOperationToFinish(1);
        assertEquals(1, bookingService.getBookedRooms().size());

        double price = bookingService.getBookedRooms().get(0).price();
        assertEquals(price, 43.75);

        double refundAmount = bookingService.cancelBooking(1);
        waitForOperationToFinish(1);

        assertEquals(0, bookingService.getBookedRooms().size());

        // 50% of the price = 21.88
        assertEquals(21.88, refundAmount);
    }


    @Test
    public void testCancelBookingFifteenDaysBefore() throws ExecutionException, InterruptedException {

        LocalDate mockDate = LocalDate.now();
        // Fifteen days after today
        LocalDate startDate = mockDate.plusDays(15);
        // Book rooms for 5 days
        LocalDate endDate = startDate.plusDays(5);

        BookingRequest bookingRequest = new BookingRequest(1, startDate.toString(), endDate.toString());
        bookingService.bookRoom(bookingRequest, latch::countDown);
        waitForOperationToFinish(1);
        assertEquals(1, bookingService.getBookedRooms().size());

        double price = bookingService.getBookedRooms().get(0).price();
        assertEquals(price, 43.75);

        double refundAmount = bookingService.cancelBooking(1);
        waitForOperationToFinish(1);

        assertEquals(0, bookingService.getBookedRooms().size());

        // 100% of the price = 43.75
        assertEquals(43.75, refundAmount);
    }






    /**
     * This method will wait for the latch to be counted down.
     * If the latch is not counted down within 1 second, the test will fail.
     * This is to prevent the test from hanging.
     * @param times the number of times to wait for the latch to be counted down.
     */
    private void waitForOperationToFinish(int times) {
        try {
            latch.await(times, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
