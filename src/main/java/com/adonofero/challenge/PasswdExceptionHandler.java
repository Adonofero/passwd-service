/*
 * Copyright 2018 Alexander Donofero
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.adonofero.challenge;

import com.adonofero.challenge.exceptions.service.EntityNotFoundException;
import com.adonofero.challenge.exceptions.service.UpdateFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * REST Exception handler for all exceptions thrown by the Passwd as a Service application.
 *
 * @author Alexander Donofero
 */
@ControllerAdvice
public class PasswdExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = EntityNotFoundException.class)
    public ResponseEntity<Object> resolveEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
        return handleExceptionInternal(ex, "Requested user was not found.", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(value = UpdateFailureException.class)
    public ResponseEntity<Object> resolveUpdateFailureException(UpdateFailureException ex, WebRequest request) {
        return handleExceptionInternal(ex, "Internal error retrieving system data. Please contact system administrator.", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Object> resolveException(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "Unknown error has occurred. Please contact system administrator.", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
