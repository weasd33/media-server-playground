package com.github.weasd33.playground.common.exception;

public class MediaMtxApiException extends RuntimeException {

    public MediaMtxApiException(String message) {
        super(message);
    }

    public MediaMtxApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
