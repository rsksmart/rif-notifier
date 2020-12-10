package org.rif.notifier.models.entities;

public enum NotificationServiceType {
    SMS("SMSService"),
    EMAIL("EmailService"),
    API("APIService");

    private String className;
    NotificationServiceType(String className)    {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

}
