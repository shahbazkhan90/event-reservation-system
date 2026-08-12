package com.project.eventreservation.exception;


public class BadRequestException extends RuntimeException {
    public BadRequestException(String message){
        super(message);
    }


}
