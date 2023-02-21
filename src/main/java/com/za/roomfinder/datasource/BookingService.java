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
        Future<BookedRoom> bookedRoomFuture =  executorService.submit(() -> {
            checkRequestDate(bookingRequest);

            double price = calculatePrice(bookingRequest);
            String today = LocalDate.now().toString();

            return new BookedRoom(bookingRequest.clientId(), bookingId, bookingRequest.date(), today, price);
        });

        try {
            bookedRooms.put(bookingId, bookedRoomFuture.get());
            bookingId++;
        } catch (InterruptedException | ExecutionException e) {
            throw new RoomNotAvailableException(e.getMessage());
        }
    }


    private void checkRequestDate(BookingRequest bookingRequest) throws RoomNotAvailableException{

        if (LocalDate.parse(bookingRequest.date()).isBefore(LocalDate.now())) {
            throw new RoomNotAvailableException("Date cannot be in the past");
        }

        if (checkIfBookingRequestDateIsTaken(bookingRequest)) {
            throw new RoomNotAvailableException("There is already a booking for this date " + bookingRequest.date());
        }
    }


    public boolean checkIfBookingRequestDateIsTaken(BookingRequest bookingRequest) {

        for (BookedRoom bookedRoom: bookedRooms.values()) {

            LocalDate requestDate = LocalDate.parse(bookingRequest.date());
            LocalDate bookedDate = LocalDate.parse(bookedRoom.date());

            if (requestDate.isEqual(bookedDate)) {
                return true;
            }
        }
        return false;
    }


    public double calculatePrice(BookingRequest bookingRequest) {

        LocalDate requestBookingDate = LocalDate.parse(bookingRequest.date());
        LocalDate startOfYearDate = LocalDate.MIN.withYear(requestBookingDate.getYear());

        long numDays = ChronoUnit.DAYS.between(startOfYearDate, requestBookingDate.plusDays(1));

        return Math.round ( ( (numDays + 100) / 12.0 ) * 100.0 ) / 100.0;
    }


    public double cancelBooking(int bookingId, int clientId) throws ExecutionException, InterruptedException {

        Future<Double> refundAmountFuture = executorService.submit(() -> {

            if (clientNotOwner(clientId, bookingId)) {
                throw new InvalidBookingException("Client does not own this booking");
            }

            BookedRoom bookedRoom = bookedRooms.remove(bookingId);
            checkIfBookingExists(bookedRoom);
            return getRefundAmount(bookedRoom);
        });

        return refundAmountFuture.get();
    }

    public boolean clientNotOwner(int clientId, int bookingId){
        BookedRoom bookedRoom = bookedRooms.get(bookingId);
        return bookedRoom.fromClientId() != clientId;
    }


    private void checkIfBookingExists(BookedRoom bookedRoom) throws RoomNotAvailableException{
        if (bookedRoom == null) {
            throw new RoomNotAvailableException("Booking does not exist");
        }
    }


    public double getRefundAmount(BookedRoom bookedRoom) {
        long numDays = getAmountOfDaysBeforeBookedDate(bookedRoom);
        double refundPercentage = getPercentageFee(numDays);
        return Math.round( (refundPercentage * bookedRoom.price()) * 100.0) / 100.0;
    }


    public long getAmountOfDaysBeforeBookedDate(BookedRoom bookedRoom){
        LocalDate bookedForDate = LocalDate.parse(bookedRoom.date());
        LocalDate currentDate = LocalDate.now();

        return ChronoUnit.DAYS.between(currentDate, bookedForDate);
    }

    public long getAmountOfDaysAfterRoomWasBooked (BookedRoom bookedRoom){

        LocalDate bookedOnDate = LocalDate.parse(bookedRoom.bookedOnDate());
        LocalDate currentDate = LocalDate.now();

        return Math.abs(ChronoUnit.DAYS.between(currentDate, bookedOnDate));
    }


    public double getPercentageFee(long numDays){

        double percentageFee = 0.0;
        if (numDays >= 14) {
            percentageFee = 1.0;
        } else if (numDays >= 7) {
            percentageFee = 0.5;
        } else if (numDays >= 3) {
            percentageFee = 0.25;
        }

        return percentageFee;
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
