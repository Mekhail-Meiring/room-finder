package com.za.roomfinder.datasource;

import com.google.common.annotations.VisibleForTesting;
import com.za.roomfinder.dto.BookedRoom;
import com.za.roomfinder.dto.BookingRequest;
import com.za.roomfinder.exceptions.RoomNotAvailableException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;


public class BookingService {

    private final ConcurrentMap<Integer, BookedRoom> bookedRooms = new ConcurrentHashMap<>();
    private int bookingId = 1;
    private ExecutorService executorService = Executors.newFixedThreadPool(1);


    public void bookRoom(BookingRequest bookingRequest) {
        executorService.submit(() -> {

            if (overlaps(bookingRequest)) {
                throw new RoomNotAvailableException("Room is already booked");
            }
            double price = calculatePrice(bookingRequest);
            bookedRooms.add(new BookedRoom(bookingRequest.clientId(), bookingRequest.startDate(), bookingRequest.endDate(), price));
        });
    }

    @VisibleForTesting
    public void bookRoom(BookingRequest bookingRequest, Runnable callback) {
        executorService.submit(() -> {
            if (overlaps(bookingRequest)) {
                throw new RoomNotAvailableException("Room is already booked");
            }

            double price = calculatePrice(bookingRequest);
            bookedRooms.add(new BookedRoom(bookingRequest.clientId(), bookingRequest.startDate(), bookingRequest.endDate(), price));
            callback.run();
        });
    }


    public boolean overlaps(BookingRequest bookingRequest) {

        for (BookedRoom bookedRoom: bookedRooms) {

            LocalDate startDate = LocalDate.parse(bookingRequest.startDate());
            LocalDate endDate = LocalDate.parse(bookingRequest.endDate());

            LocalDate bookedStartDate = LocalDate.parse(bookedRoom.startDate());
            LocalDate bookedEndDate = LocalDate.parse(bookedRoom.endDate());

            if (!startDate.isAfter(bookedEndDate) && !bookedStartDate.isAfter(endDate)) {
                return true;
            }
        }
        return false;
    }


    private double calculatePrice(BookingRequest bookingRequest) {

        LocalDate startDate = LocalDate.parse(bookingRequest.startDate());
        LocalDate endDate = LocalDate.parse(bookingRequest.endDate());

        long numDays = ChronoUnit.DAYS.between(startDate, endDate);
        double pricePerDay = (numDays + 100) / 12.0;
        return pricePerDay * numDays;
    }


    public void setExecutorServiceSize(int size) {
        this.executorService = Executors.newFixedThreadPool(size);
    }

    public List<BookedRoom> getBookedRooms() {
        return bookedRooms;
    }

    @VisibleForTesting
    public ExecutorService getExecutorService() {
        return executorService;
    }
}
