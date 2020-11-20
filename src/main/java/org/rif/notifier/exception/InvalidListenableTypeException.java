package org.rif.notifier.exception;

public class InvalidListenableTypeException extends RuntimeException    {
    public InvalidListenableTypeException(String msg)   {
        super(msg);
    }
    public InvalidListenableTypeException(String msg, Exception e)  {
        super(msg, e);
    }
}
