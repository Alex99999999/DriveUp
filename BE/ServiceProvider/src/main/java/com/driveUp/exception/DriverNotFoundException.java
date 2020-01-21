package com.driveUp.exception;

import java.util.UUID;

public class DriverNotFoundException extends RuntimeException {

    public DriverNotFoundException(UUID id) {
        super("Driver id not found : " + id);
    }
}
