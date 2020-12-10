package org.rif.notifier.exception;

import org.rif.notifier.boot.configuration.NotifierConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ErrorHandler;

public class ScheduledErrorHandler implements ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledErrorHandler.class);

    public void handleError(Throwable t)    {
       if (t instanceof Exception)  {
           logger.error("Runtime exception while running scheduled task  - ", t);
       }
       else if(t instanceof Error)  {
            logger.error("FATAL ERROR IN SCHEDULED TASK -", t);
       }
    }
}
