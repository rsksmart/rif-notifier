package org.rif.notifier.exception;

import org.rif.notifier.models.DTO.SubscriptionResponse;

public class SubscriptionException extends RuntimeException {

    private Object content;

    public SubscriptionException(String msg)  {
        super(msg);
    }

    public SubscriptionException(String msg, Exception e)  {
        super(msg, e);
    }
    public SubscriptionException(String msg, Object content, Exception e)  {
        super(msg, e);
        this.content = content;
    }

    public Object getContent()  {
        return content;
    }
}
