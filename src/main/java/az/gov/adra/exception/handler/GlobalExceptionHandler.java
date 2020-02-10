package az.gov.adra.exception.handler;

import az.gov.adra.entity.response.Exception;
import az.gov.adra.entity.response.GenericResponse;
import az.gov.adra.entity.response.GenericResponseBuilder;
import az.gov.adra.exception.UserCredentialsException;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericResponse handleDataAccessException(DataAccessException e) {
        //Exception
        Exception exception = new Exception();
        exception.setCode("0x0010");
        exception.setMessage(e.getMessage());
        exception.setErrorStack("DataAccessException.");

        return new GenericResponseBuilder()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withDescription("DataAccessException")
                .withException(exception)
                .build();

    }

    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericResponse handleNumberFormatException(NumberFormatException e) {
        //Exception
        Exception exception = new Exception();
        exception.setCode("0x0020");
        exception.setMessage(e.getMessage());
        exception.setErrorStack("NumberFormatException.");

        return new GenericResponseBuilder()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withDescription("NumberFormatException")
                .withException(exception)
                .build();
    }

    @ExceptionHandler(UserCredentialsException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public GenericResponse handleUserCredentialsException(UserCredentialsException e) {
        //Exception
        Exception exception = new Exception();
        exception.setCode("0x0050");
        exception.setMessage(e.getMessage());
        exception.setErrorStack("UserCredentialsException.");

        return new GenericResponseBuilder()
                .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .withDescription("UserCredentialsException")
                .withException(exception)
                .build();
    }

}
