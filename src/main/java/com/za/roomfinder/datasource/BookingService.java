package com.za.roomfinder.datasource;

import com.google.common.annotations.VisibleForTesting;
import com.za.roomfinder.dto.BookedRoom;
import com.za.roomfinder.dto.BookingRequest;
import com.za.roomfinder.dto.Client;
import com.za.roomfinder.exceptions.ClientNotFoundException;
import com.za.roomfinder.exceptions.ClientRegistrationException;
import com.za.roomfinder.exceptions.InvalidBookingException;
import com.za.roomfinder.exceptions.RoomNotAvailableException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

@Repository
public class BookingService {

    private final ConcurrentMap<Integer, BookedRoom> bookedRooms = new ConcurrentHashMap<>();
    private int bookingId = 1;
    private int nrOfThreads = 1;
    private ExecutorService executorService = Executors.newFixedThreadPool(nrOfThreads);
    private final List<Client> clients = new ArrayList<>();


    public void registerClient(Client client) {

        if (!checkIfClientExists(client)){
            clients.add(client);
            nrOfThreads++;
        }

        else {
            throw new ClientRegistrationException(
                    "Client already with id number" + client.idNumber()+ " already exists");
        }

    }

    public List<Client> getClients() {
        return clients;
    }

    public Client clientLogin(Client client) {

        if (checkIfClientExists(client)){
            return findClientById(client.idNumber());
        }

        throw new ClientNotFoundException("Client does not exists");
    }


    public boolean checkIfClientExists(Client client) {

        for (Client otherClient: clients) {
            if (otherClient.idNumber() == client.idNumber()) {
                return true;
            }
        }
        return false;
    }


    public Client findClientById(int clientId) {

        for (Client client: clients) {
            if (client.idNumber() == clientId) {
                return client;
            }
        }

        throw new ClientNotFoundException("Client does not exist");
    }


    public void bookRoom(BookingRequest bookingRequest) {
        executorService.submit(() -> {

            checkDates(bookingRequest);

            double price = calculatePrice(bookingRequest);
            bookedRooms.put(bookingId, new BookedRoom(bookingRequest.clientId(), bookingId, bookingRequest.startDate(), bookingRequest.endDate(), price));
            bookingId++;
        });

    }

    @VisibleForTesting
    public void bookRoom(BookingRequest bookingRequest, Runnable callback) {
        executorService.submit(() -> {

            checkDates(bookingRequest);

            double price = calculatePrice(bookingRequest);
            bookedRooms.put(bookingId, new BookedRoom(bookingRequest.clientId(), bookingId, bookingRequest.startDate(), bookingRequest.endDate(), price));
            bookingId++;
            callback.run();
        });
    }

    private void checkDates (BookingRequest bookingRequest) throws RoomNotAvailableException{

        if (LocalDate.parse(bookingRequest.startDate()).isBefore(LocalDate.now())) {
            throw new RoomNotAvailableException("Start date cannot be in the past");
        }

        if (overlaps(bookingRequest)) {
            throw new RoomNotAvailableException("Room is already booked");
        }
    }


    public boolean overlaps(BookingRequest bookingRequest) {

        for (BookedRoom bookedRoom: bookedRooms.values()) {

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


    public double cancelBooking(int bookingId) throws ExecutionException, InterruptedException {

        Future<Double> refundAmountFuture = executorService.submit(() -> {
            checkIfBookingExists(bookingId);
            BookedRoom bookedRoom = bookedRooms.remove(bookingId);
            return getRefundAmount(bookedRoom);
        });

        return refundAmountFuture.get();
    }

    private void checkIfBookingExists(int bookingId) throws RoomNotAvailableException{
        if (!bookedRooms.containsKey(bookingId)) {
            throw new RoomNotAvailableException("Booking does not exist");
        }
    }

    private double getRefundAmount(BookedRoom bookedRoom) {
        long numDays = getNumDaysUntilBookedDate(bookedRoom);
        double refundPercentage = getRefundPercentage(numDays);
        return Math.round( (refundPercentage * bookedRoom.price()) * 100.0) / 100.0;
    }


    public long getNumDaysUntilBookedDate(BookedRoom bookedRoom){
        LocalDate startDate = LocalDate.parse(bookedRoom.startDate());
        LocalDate currentDate = LocalDate.now();

        return ChronoUnit.DAYS.between(currentDate, startDate);
    }


    public double getRefundPercentage(long numDays){

        double refundPercentage = 0.0;
        if (numDays >= 14) {
            refundPercentage = 1.0;
        } else if (numDays >= 7) {
            refundPercentage = 0.5;
        } else if (numDays >= 3) {
            refundPercentage = 0.25;
        }

        return refundPercentage;
    }




    public void setExecutorServiceSize(int size) {
        this.executorService = Executors.newFixedThreadPool(size);
    }

    public List<BookedRoom> getBookedRooms() {
        return new ArrayList<>(bookedRooms.values());
    }

    @VisibleForTesting
    public ExecutorService getExecutorService() {
        return executorService;
    }
}
