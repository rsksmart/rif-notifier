package org.rif.notifier.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String msg)  {
        super(msg);
    }

    public ResourceNotFoundException(String msg, Exception e)  {
        super(msg, e);
    }
}
