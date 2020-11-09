package org.rif.notifier.models.DTO;

import org.rif.notifier.constants.ResponseConstants;
import org.springframework.http.HttpStatus;

public class DTOResponse {

    private String message = ResponseConstants.OK;

    private Object content;

    //Default value to 200
    private HttpStatus status = HttpStatus.OK;

    public DTOResponse() {}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content= content;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "{" +
                "\"message\":\"" + message + "\"" +
                ", \"content\":\"" + content+ "\"" +
                ", \"status\":\"" + status + "\"" +
                '}';
    }
}
