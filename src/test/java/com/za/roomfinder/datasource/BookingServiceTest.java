package com.za.roomfinder.datasource;

import com.za.roomfinder.dto.BookingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
        BookingRequest bookingRequest = new BookingRequest("client-1", "2023-02-20", "2023-02-25");
        bookingService.bookRoom(bookingRequest);
        bookingService.bookRoom(bookingRequest, latch::countDown);

        waitForOperationToFinish(1);
        assertEquals(1, bookingService.getBookedRooms().size());
    }

    @Test
    public void testBookRoomOverlappingDates() {
        BookingRequest bookingRequest1 = new BookingRequest("client-1", "2023-02-20", "2023-02-25");
        BookingRequest bookingRequest2 = new BookingRequest("client-2", "2023-02-23", "2023-02-27");
        bookingService.bookRoom(bookingRequest1, latch::countDown);
        waitForOperationToFinish(2);

        assertTrue(bookingService.overlaps(bookingRequest2));
    }


    @Test
    public void testCalculatePrice() {
        BookingRequest bookingRequest = new BookingRequest("client-1", "2023-02-20", "2023-02-25");
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
        BookingRequest bookingRequest = new BookingRequest("client1", "2022-10-01", "2022-10-01");
        BookingService bookingService = new BookingService();
        bookingService.bookRoom(bookingRequest,  latch::countDown);
        waitForOperationToFinish(1);
        assertEquals(1, bookingService.getBookedRooms().size());
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
