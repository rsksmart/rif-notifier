package org.rif.notifier.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    public static String writeValueAsString(Object value)   {
        if (value == null)  {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(value);
        } catch(IOException e)  {
            logger.warn(e.getMessage(), e);
            return "";
        }
    }
}
