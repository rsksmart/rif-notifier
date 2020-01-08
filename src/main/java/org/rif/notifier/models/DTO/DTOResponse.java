package org.rif.notifier.models.DTO;

import org.rif.notifier.constants.ResponseConstants;
import org.springframework.http.HttpStatus;

public class DTOResponse {

    private String message = ResponseConstants.OK;

    private Object data;

    //Default value to 200
    private HttpStatus status = HttpStatus.OK;

    public DTOResponse() {}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
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
                ", \"data\":\"" + data + "\"" +
                ", \"status\":\"" + status + "\"" +
                '}';
    }
}
