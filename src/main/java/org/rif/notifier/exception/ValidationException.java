package org.rif.notifier.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ValidationException extends RuntimeException {

    public ValidationException(String msg)  {
        super(msg);
    }

    public ValidationException(String msg, Exception e)  {
        super(msg, e);
    }
}
