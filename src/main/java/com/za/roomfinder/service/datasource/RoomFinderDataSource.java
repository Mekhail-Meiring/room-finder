package com.za.roomfinder.service.datasource;

import com.za.roomfinder.service.datasource.dto.*;
import com.za.roomfinder.exceptions.*;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.concurrent.*;


@Repository
public class RoomFinderDataSource {

    private final ConcurrentMap<Integer, BookedRoom> bookedRooms = new ConcurrentHashMap<>();
    private final ConcurrentMap<Integer, ClientPP> clientPPs = new ConcurrentHashMap<>();
    private int bookingId = 1;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private final ConcurrentMap<Integer, Client> clients = new ConcurrentHashMap<>();


    public synchronized void registerClient(Client client) {

        if (!clients.containsKey(client.idNumber())){
            clients.put(client.idNumber(), client);
        }

        else {
            throw new ClientRegistrationException(
                    "Client already with id number" + client.idNumber()+ " already exists");
        }
    }

    public synchronized List<Client> getClients() {
        return clients.values().stream().toList();
    }


    public synchronized Client clientLogin(int clientId) {

        Client client = clients.get(clientId);

        if (client == null){
            throw new ClientNotFoundException("Client does not exists");
        }

        return client;

    }


    public boolean hasBookedFiveRoomsInWeek(int clientId) {
        List<BookedRoom> bookings = getBookingsForClient(clientId);
        bookings.sort(Comparator.comparing(BookedRoom::date));

        Map<Integer, Integer> roomsInAWeek = new HashMap<>();
        final int maxRooms = 5;

        for (BookedRoom booking : bookings) {
            int weekNr = getWeekOfDate(booking.date());
            int nrOfRoomsInWeek = roomsInAWeek.getOrDefault(weekNr, 0);

            if (nrOfRoomsInWeek > maxRooms) {
                return true;
            }
            roomsInAWeek.put(weekNr, nrOfRoomsInWeek + 1);
        }

        return false;
    }

    public int getWeekOfDate(String date) {
        LocalDate bookingDate = LocalDate.parse(date);
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        return bookingDate.get(weekFields.weekOfWeekBasedYear());
    }


    public List<BookedRoom> getBookingsForClient(int clientId) {
        List<BookedRoom> bookings = new ArrayList<>();
        for (BookedRoom bookedRoom: bookedRooms.values()) {
            if (bookedRoom.fromClientId() == clientId) {
                bookings.add(bookedRoom);
            }
        }
        return bookings;
    }

    public RoomPrice bookRoom(BookingRequest bookingRequest) {
        try {
            return new RoomPrice(bookedRoomPriceFuture(bookingRequest).get());
        } catch (InterruptedException | ExecutionException e) {
            throw new RoomNotAvailableException(e.getMessage());
        }
    }

    public void payForBooking(RoomPaymentRequest roomPaymentRequest) {

        String today = LocalDate.now().toString();

        BookedRoom bookedRoom = new BookedRoom(
                roomPaymentRequest.clientId(),
                bookingId,
                roomPaymentRequest.date(),
                today,
                roomPaymentRequest.price()
        );

        bookedRooms.put(bookingId, bookedRoom);
        bookingId++;
    }


    private Future<Double> bookedRoomPriceFuture(BookingRequest bookingRequest) {
        return executorService.submit(() -> {

            checkRequestDate(bookingRequest);

            if (hasBookedFiveRoomsInWeek(bookingRequest.clientId())){
                throw new RoomNotAvailableException("Client has already booked 5 rooms in a week");
            }
            return calculatePrice(bookingRequest);

        });
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

        int dayOfYear = LocalDate.parse(bookingRequest.date()).getDayOfYear();
        return Math.round ( ( (dayOfYear + 100) / 12.0 ) * 100.0 ) / 100.0;
    }


    public double cancelBooking(int bookingId, int clientId){

        try {
            return refundAmountFuture(bookingId, clientId).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new InvalidBookingException(e.getMessage());
        }

    }


    private Future<Double> refundAmountFuture(int bookingId, int clientId) {
        return executorService.submit(() -> {
            if (clientNotOwner(clientId, bookingId)) {
                throw new InvalidBookingException("Client does not own this booking");
            }

            BookedRoom bookedRoom = bookedRooms.remove(bookingId);
            checkIfBookingExists(bookedRoom);
            return getRefundAmount(bookedRoom);
        });
    }

    public boolean clientNotOwner(int clientId, int bookedRoomId){
        BookedRoom bookedRoom = bookedRooms.get(bookedRoomId);
        return bookedRoom.fromClientId() != clientId;
    }


    private void checkIfBookingExists(BookedRoom bookedRoom) throws RoomNotAvailableException{
        if (bookedRoom == null) {
            throw new InvalidBookingException("Booking does not exist");
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


    public RoomPrice rescheduleBooking(int bookedRoomId, BookingRequest newBookingRequest) {

        try {
            return rescheduledBookedRoomPriceFuture(bookedRoomId, newBookingRequest).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new InvalidBookingException(e.getMessage());
        }
    }

    public void payForReschedule(int bookingId, double reschedulePrice, BookingRequest newBookingRequest){

        String today = LocalDate.now().toString();
        bookedRooms.remove(bookingId);
        BookedRoom bookedRoom = new BookedRoom(
                newBookingRequest.clientId(),
                bookingId,
                newBookingRequest.date(),
                today,
                reschedulePrice
        );

        bookedRooms.put(bookingId, bookedRoom);

    }


    private Future<RoomPrice> rescheduledBookedRoomPriceFuture(int bookingId, BookingRequest newBookingRequest) {
        return executorService.submit(() -> {

                if (clientNotOwner(newBookingRequest.clientId(), bookingId)) {
                    throw new InvalidBookingException("Client does not own this booking");
                }

                BookedRoom bookedRoom = bookedRooms.get(bookingId);

                checkIfBookingExists(bookedRoom);
                checkRequestDate(newBookingRequest);

                return new RoomPrice(getRescheduleFee(bookedRoom));
            }
        );
    }


    public double getRescheduleFee(BookedRoom bookedRoom){

        long numDays = getAmountOfDaysAfterRoomWasBooked(bookedRoom);
        double rescheduleFeePercentage = getPercentageFee(numDays);
        return Math.round( (rescheduleFeePercentage * bookedRoom.price()) * 100.0) / 100.0;
    }


    public List<BookedRoom> getBookedRooms() {
        return new ArrayList<>(bookedRooms.values());
    }

    public void uploadProfilePic(ClientPP clientPP) {
        clientPPs.put(clientPP.clientId(), clientPP);
    }

    public ClientPP getProfilePic(int clientId) {
        return clientPPs.get(clientId);
    }

    public List<ClientPP> getClientsPP(int clientId) {
        return clientPPs.values().stream().toList();
    }
}
