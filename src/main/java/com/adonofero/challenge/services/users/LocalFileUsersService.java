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
package com.adonofero.challenge.services.users;

import com.adonofero.challenge.entity.Group;
import com.adonofero.challenge.entity.User;
import com.adonofero.challenge.exceptions.service.EntityNotFoundException;
import com.adonofero.challenge.exceptions.service.UpdateFailureException;
import com.adonofero.challenge.services.groups.GroupsService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link UsersService} that retrieves {@link User}s from a local file.
 * <p>
 * DESIGN NOTE:
 * Curent implementation refreshes the list of current users per request. Briefly considered setting up a watcher to
 * listen for changes to the file and then pumped it into an in-memory database to enhance performance.
 * Ultimately decided this was over-engineering the challenge and chose this simpler approach.
 *
 * @author Alexander Donofero
 */
public class LocalFileUsersService implements UsersService {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileUsersService.class);

    private final File usersFile;
    private final GroupsService groupsService;

    /**
     * Delimiter for fields in the 'user file'.
     * <p>
     * Currently I can expect ':' to be the delimiter for any 'user file'. If I am required to support additional
     * delimiters in the future, I can make this configurable.
     */
    private final String fieldDelimiter = ":";

    public LocalFileUsersService(File usersFile, GroupsService groupsService) {
        this.usersFile = usersFile;
        this.groupsService = groupsService;
    }

    @Override
    public List<User> getAllUsers() throws UpdateFailureException {
        return getCurrentUsers();
    }

    @Override
    public List<User> queryUsers(String name, Integer uid, Integer gid, String comment, String home, String shell) throws UpdateFailureException {
        List<User> users = getCurrentUsers();
        // For each search parameter specified, filter out the list of users
        if (name != null) {
            List<User> matchedUsers = new ArrayList<>();
            for (User user : users) {
                if (user.getName().equals(name)) {
                    matchedUsers.add(user);
                }
            }
            users.retainAll(matchedUsers);
        }
        if (uid != null) {
            List<User> matchedUsers = new ArrayList<User>();
            for (User user : users) {
                if (user.getUid() == uid) {
                    matchedUsers.add(user);
                }
            }
            users.retainAll(matchedUsers);
        }
        if (gid != null) {
            List<User> matchedUsers = new ArrayList<User>();
            for (User user : users) {
                if (user.getGid() == gid) {
                    matchedUsers.add(user);
                }
            }
            users.retainAll(matchedUsers);
        }
        if (comment != null) {
            List<User> matchedUsers = new ArrayList<User>();
            for (User user : users) {
                if (user.getComment().equals(comment)) {
                    matchedUsers.add(user);
                }
            }
            users.retainAll(matchedUsers);
        }
        if (home != null) {
            List<User> matchedUsers = new ArrayList<User>();
            for (User user : users) {
                if (user.getHome().equals(home)) {
                    matchedUsers.add(user);
                }
            }
            users.retainAll(matchedUsers);
        }
        if (shell != null) {
            List<User> matchedUsers = new ArrayList<User>();
            for (User user : users) {
                if (user.getShell().equals(shell)) {
                    matchedUsers.add(user);
                }
            }
            users.retainAll(matchedUsers);
        }
        return users;
    }

    @Override
    public User getUserById(int uid) throws UpdateFailureException, EntityNotFoundException {
        List<User> currentUsers = getCurrentUsers();
        // As uid should be unique, I can assume that I only need to find the first user matching the uid
        for (User user : currentUsers) {
            if (user.getUid() == uid) {
                return user;
            }
        }
        String message = String.format("Could not find user with uid %d", uid);
        throw new EntityNotFoundException(message);
    }

    @Override
    public List<Group> getGroupsOfUser(int uid) throws UpdateFailureException, EntityNotFoundException {
        User targetUser = this.getUserById(uid);
        logger.debug("Matched user {} to uid {}", targetUser, uid);
        return groupsService.getGroupsOfUser(targetUser.getName());
    }

    /**
     * Private helper method to retrieve the current system users from the configured location
     *
     * @return All users currently on the system
     */
    private List<User> getCurrentUsers() throws UpdateFailureException {
        // Initial sanity checks
        if (!usersFile.exists()) {
            String message = String.format("Local user file at %s does not exist", usersFile.getAbsolutePath());
            throw new UpdateFailureException(message);
        }
        if (!usersFile.canRead()) {
            String message = String.format("Cannot read local user file at %s", usersFile.getAbsolutePath());
            throw new UpdateFailureException(message);
        }
        // Attempt to perform update
        List<String> userLines;
        try {
            userLines = FileUtils.readLines(usersFile, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new UpdateFailureException(ex);
        }
        List<User> users = new ArrayList<User>();
        for (String userLine : userLines) {
            String[] splitLine = userLine.split(fieldDelimiter);
            /**
             * Users file is expected to contain 7 fields in this order:
             * user name
             * encrypted password
             * uid
             * gid
             * comment (sometimes referred to as GECOS in UNIX documentation)
             * user home directory
             * login shell
             */
            if (splitLine.length != 7) {
                String message = String.format("User file at %s is malformed. Expected user line to be have 7 fields delimited by %s. Found %d fields in line \"%s\"",
                        usersFile.getAbsolutePath(), fieldDelimiter, splitLine.length, userLine);
                throw new UpdateFailureException(message);
            }
            User user = new User();
            user.setName(splitLine[0]);
            user.setUid(Integer.parseInt(splitLine[2]));
            user.setGid(Integer.parseInt(splitLine[3]));
            user.setComment(splitLine[4]);
            user.setHome(splitLine[5]);
            user.setShell(splitLine[6]);
            logger.trace("Retrieved user information {} from user file line {}", user, userLine);
            users.add(user);
        }
        logger.debug("Retrieval of current users found the following users: {}", users);
        return users;
    }
}
