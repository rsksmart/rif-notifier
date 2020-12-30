package org.rif.notifier.exception;

public class RSKBlockChainException extends Exception    {

    public RSKBlockChainException(Exception e)   {
        super(e);
    }

    public RSKBlockChainException(String msg, Exception e)   {
        super(msg, e);
    }

}
