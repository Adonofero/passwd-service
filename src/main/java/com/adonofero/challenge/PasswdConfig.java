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

import com.adonofero.challenge.services.groups.GroupsService;
import com.adonofero.challenge.services.groups.LocalFileGroupsService;
import com.adonofero.challenge.services.users.LocalFileUsersService;
import com.adonofero.challenge.services.users.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.File;

/**
 * Configuration class for the Passwd as a service application.
 *
 * @author Alexander Donofero
 */
@Configuration
public class PasswdConfig {

    @Autowired
    Environment env;

    private static final String PROP_KEY_USER_FILE_PATH = "passwd.users.filepath";
    private static final String PROP_KEY_GROUPS_FILE_PATH = "passwd.groups.filepath";

    @Bean
    public UsersService usersService() {
        /**
         * DESIGN NOTE:
         * If for whatever reason I needed to extend this service to have multiple implementations of the UsersService,
         * this is where I would put whatever logic is needed to select an implementation based on the deployed environment.
         */
        return new LocalFileUsersService(env.getRequiredProperty(PROP_KEY_USER_FILE_PATH, File.class), groupsService());
    }

    @Bean
    public GroupsService groupsService() {
        /**
         * DESIGN NOTE:
         * If for whatever reason I needed to extend this service to have multiple implementations of the UsersService,
         * this is where I would put whatever logic is needed to select an implementation based on the deployed environment.
         */
        return new LocalFileGroupsService(env.getRequiredProperty(PROP_KEY_GROUPS_FILE_PATH, File.class));
    }
}
