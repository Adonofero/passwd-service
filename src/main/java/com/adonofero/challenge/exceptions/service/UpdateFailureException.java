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
package com.adonofero.challenge.exceptions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exception to be thrown when a service cannot update its current list of entities.
 *
 * @author Alexander Donofero
 */
public class UpdateFailureException extends Exception {

    private static final Logger logger = LoggerFactory.getLogger(UpdateFailureException.class);

    public UpdateFailureException(String message) {
        super(message);
        logger.error(message);
    }

    public UpdateFailureException(Throwable ex) {
        super(ex);
        logger.error("User update failure: ", ex);
    }
}
