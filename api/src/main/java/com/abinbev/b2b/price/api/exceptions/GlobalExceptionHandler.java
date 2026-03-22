package com.abinbev.b2b.price.api.exceptions;

import static com.abinbev.b2b.price.api.exceptions.IssueEnum.BAD_REQUEST;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import com.abinbev.b2b.price.api.helpers.ApiConstants;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DataAccessResourceFailureException.class)
    @ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
    protected Issue processDataAccessResourceFailure(final DataAccessResourceFailureException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return new Issue(IssueEnum.DATABASE_ACCESS_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    protected Issue processExceptions(final Exception ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return new Issue(IssueEnum.UNEXPECTED_ERROR, List.of(ex.getLocalizedMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED, value = HttpStatus.METHOD_NOT_ALLOWED)
    protected Issue processHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException ex,
                                                                  final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return new Issue(IssueEnum.METHOD_NOT_ALLOWED, ex.getMethod(),
                ofNullable(ex.getSupportedHttpMethods()).orElse(emptySet()).stream()
                        .map(HttpMethod::toString).collect(joining(", ")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        final List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            errors.add(error.getObjectName() + ": " + error.getDefaultMessage());
        }

        return new Issue(BAD_REQUEST, errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue hadleHttpMessageNotReadableException(final HttpMessageNotReadableException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return new Issue(IssueEnum.JSON_DESERIALIZE_ERROR);
    }

    // Business Exceptions.

    @ExceptionHandler({BadRequestException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue processBadRequestException(final GlobalException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return ex.getIssue();
    }

    @ExceptionHandler({MissingHeaderException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue processMissingHeaderException(final MissingHeaderException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return ex.getIssue();
    }

    @ExceptionHandler({InvalidHeaderException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue processInvalidHeaderException(final InvalidHeaderException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return ex.getIssue();
    }

    @ExceptionHandler({InvalidParamException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue processInvalidParamException(final InvalidParamException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return ex.getIssue();
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue processConstraintViolationException(final ConstraintViolationException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);

        final List<String> details = new ArrayList<>();
        for (final ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            details.add(violation.getPropertyPath() + ": " + violation.getMessage());
        }

        return new Issue(BAD_REQUEST, details);
    }

    @ExceptionHandler({NotFoundException.class})
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    protected Issue processNotFoundException(final GlobalException ex, final WebRequest request) {

        LOGGER.warn("requestTraceId: {} - {}", request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex.getIssue().getMessage());
        return ex.getIssue();
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Issue handleMissingHeader(final MissingRequestHeaderException ex) {

        return new Issue(BAD_REQUEST, singletonList(IssueEnum.REQUIRED_HEADER_MISSING.getFormattedMessage(ex.getHeaderName())));
    }

    @ExceptionHandler({JWTException.class})
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    protected ResponseEntity<Void> processJwtException(final GlobalException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({MissingAccountHeaderException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue processMissingAccountHeaderException(final MissingAccountHeaderException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return ex.getIssue();
    }

    @ExceptionHandler({MismatchedAccountValuesException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue processMismatchedAccountValuesException(final MismatchedAccountValuesException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return ex.getIssue();
    }

    @ExceptionHandler({MismatchedSkusAndVendorItemsValuesException.class})
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue processMismatchedSkusAndVendorItemsValuesException(final MismatchedSkusAndVendorItemsValuesException ex,
                                                                       final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return ex.getIssue();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    protected Issue handleParameterException(final MissingServletRequestParameterException ex, final WebRequest request) {

        LOGGER.error(request.getHeader(ApiConstants.REQUEST_TRACE_ID_HEADER), ex);
        return new Issue(BAD_REQUEST, singletonList(ex.getMessage()));
    }
}
