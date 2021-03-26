package org.rif.notifier.exception;

import org.rif.notifier.models.DTO.DTOResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.persistence.PersistenceException;
import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Optional;

@RestControllerAdvice
public class GlobalControllerExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public DTOResponse internalServerError(Exception e)  {
        return newErrorResponse(e.getMessage(), null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public DTOResponse validationFailed(MethodArgumentNotValidException e)  {
        Optional<List<FieldError>> errors = Optional.ofNullable(e.getBindingResult().getFieldErrors());
        StringBuffer message = new StringBuffer();
        errors.ifPresent(fieldErrors->{
            fieldErrors.forEach(f->{
               message.append(f.getField()).append(" ").append(f.getDefaultMessage()).append(", ") ;
            });
        });
        return newErrorResponse(message.length() > 0 ? message.toString() : e.getMessage(), null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MissingRequestHeaderException.class,MissingServletRequestParameterException.class,HttpMessageNotReadableException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public DTOResponse httpInvalidRequest(Exception e)  {
        return newErrorResponse(e.getMessage(), null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ValidationException.class, SubscriptionException.class, PersistenceException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public DTOResponse badRequest(Exception e) {
        Object content = e instanceof SubscriptionException ? ((SubscriptionException)e).getContent() : null;
        return newErrorResponse(e.getMessage(), content, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = {LoginException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public DTOResponse unauthorizedRequest(Exception e) {
        return newErrorResponse(e.getMessage(), null, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {ResourceNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public DTOResponse resourceNotFound(ResourceNotFoundException e) {
        return newErrorResponse(e.getMessage(), null, HttpStatus.NOT_FOUND);
    }

    private DTOResponse newErrorResponse(String msg, Object content, HttpStatus status)  {
        DTOResponse resp = new DTOResponse();
        resp.setMessage(msg);
        resp.setContent(content);
        resp.setStatus(status);
        return resp;
    }
}
