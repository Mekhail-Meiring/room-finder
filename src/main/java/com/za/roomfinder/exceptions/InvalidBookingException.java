package com.za.roomfinder.exceptions;

public class InvalidBookingException extends RuntimeException{

        public InvalidBookingException(String message) {
            super(message);
        }
}
