package com.za.roomfinder.service;

import com.za.roomfinder.service.datasource.RoomFinderDataSource;

import com.za.roomfinder.service.datasource.dto.BookingRequest;
import com.za.roomfinder.service.datasource.dto.RoomPaymentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RoomFinderServiceTest {

    RoomFinderDataSource dataSource = Mockito.mock(RoomFinderDataSource.class);

    private final RoomFinderService service = new RoomFinderService(dataSource);


    @Test
    @DisplayName("Should call its datasource to register a client")
    public void test1(){

        // When:
        service.registerClient(null);

        // Then:
        Mockito.verify(dataSource, Mockito.times(1)).registerClient(null);
    }


    @Test
    @DisplayName("Should call its datasource to get all bookings made")
    public void test2(){

        // When:
        service.getBookedRooms();

        // Then:
        Mockito.verify(dataSource, Mockito.times(1)).getBookedRooms();
    }


    @Test
    @DisplayName("Should call its datasource to reschedule a booking")
    public void test3(){

        // When:
        service.rescheduleBooking(1, null);

        // Then:
        Mockito.verify(dataSource, Mockito.times(1)).rescheduleBooking(1, null);
    }


    @Test
    @DisplayName("Should call its datasource to cancel a booking")
    public void test4(){

        // When:
        service.cancelBooking(1,1);

        // Then:
        Mockito.verify(dataSource, Mockito.times(1)).cancelBooking(1,1);
    }


    @Test
    @DisplayName("Should call its datasource to book a room")
    public void test5(){

        // When:
        service.bookRoom(null);

        // Then:
        Mockito.verify(dataSource, Mockito.times(1)).bookRoom(null);
    }


    @Test
    @DisplayName("Should call its datasource when a client logs in")
    public void test6(){

        // When:
        service.clientLogin(1);

        // Then:
        Mockito.verify(dataSource, Mockito.times(1)).clientLogin(1);
    }

    @Test
    @DisplayName("Should call its datasource when a client confirms a booking")
    public void test7(){

        // Given
        RoomPaymentRequest roomPaymentRequest = new RoomPaymentRequest(1, "2021-01-01", 0.0);

        // When:
        service.payForBooking(roomPaymentRequest);

        // Then:
        Mockito.verify(dataSource, Mockito.times(1)).payForBooking(roomPaymentRequest);

    }
}