package org.rif.notifier.exception;

import org.rif.notifier.models.entities.Notification;

public class NotificationException extends Exception    {

    public NotificationException(Exception e)   {
        super(e);
    }

    public NotificationException(String msg, Exception e)   {
        super(msg, e);
    }

}
