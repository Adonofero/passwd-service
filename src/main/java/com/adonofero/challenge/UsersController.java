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

import com.adonofero.challenge.entity.Group;
import com.adonofero.challenge.entity.User;
import com.adonofero.challenge.exceptions.rest.MissingParametersException;
import com.adonofero.challenge.exceptions.service.EntityNotFoundException;
import com.adonofero.challenge.exceptions.service.UpdateFailureException;
import com.adonofero.challenge.services.users.UsersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for endpoints related to the retrieval of {@link User}s.
 *
 * @author Alexander Donofero
 */
@RestController
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);


    /**
     * Service layer used to perform the logic needed to fulfill client requests.
     */
    private final UsersService usersService;

    /**
     * Default Constructor.
     *
     * @param usersService Service layer used to perform the logic needed to fulfill client requests.
     */
    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * Allow clients to retrieve a current list of all users
     *
     * @return list of all users
     * @throws UpdateFailureException if current list of all users cannot be retrieved
     */
    @RequestMapping(path = "/users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> getUsers() throws UpdateFailureException {
        logger.info("BEGIN: Received GET request for all users");
        List<User> retrievedUsers = usersService.getAllUsers();
        logger.debug("GET request for all users found the following users: {}", retrievedUsers);
        logger.info("END: GET request for all users resolved successfully");
        return retrievedUsers;
    }

    /**
     * Allow clients to query for users matching the input criteria.
     * Excluding all parameters results in a 400 Bad Request. Passing multiple parameters is the equivalent
     * of an "AND" query in that a {@link User} must match ALL criteria in order to be returned.
     *
     * @param name    name of {@link User}
     * @param uid     id of {@link User}
     * @param gid     group id of {@link User}
     * @param comment comment field for {@link User}
     * @param home    home directory of {@link User}
     * @param shell   shell for {@link User}
     * @return List of {@link User}s matching all specified criteria
     * @throws UpdateFailureException if the current list of users cannot be retrieved
     */
    @RequestMapping(path = "/users/query", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<User> queryUsers(@RequestParam(name = "name", required = false) String name, @RequestParam(name = "uid", required = false) Integer uid,
                                 @RequestParam(name = "gid", required = false) Integer gid, @RequestParam(name = "comment", required = false) String comment,
                                 @RequestParam(name = "home", required = false) String home, @RequestParam(name = "shell", required = false) String shell) throws UpdateFailureException {
        logger.info("BEGIN: Received GET request for querying user based on parameters");
        /**
         * DESIGN NOTE: The challenge requirements didn't specify an explicit behavior for calling the query endpoint with no paramters.
         *
         * Given that an endpoint exists to retrieve all users, I determined that calling the 'query' endpoint with no parameters
         * should be considered a 'bad request' rather than returning all users.
         */
        if (name == null && uid == null && gid == null && comment == null && home == null && shell == null) {
            throw new MissingParametersException("Minimum of 1 query parameter required on queryUsers endpoint");
        }
        List<User> retrievedUsers = usersService.queryUsers(name, uid, gid, comment, home, shell);
        logger.debug("Found the following users given query criteria name='{}', uid='{}', gid='{}', comment='{}', home='{}', shell='{}': {}",
                name, uid, gid, comment, home, shell, retrievedUsers);
        logger.info("END: GET request for user query resolved successfully");
        return retrievedUsers;
    }

    /**
     * Allow clients to retrieve {@link User} based on uid.
     *
     * @param uid id of {@link User}
     * @return {@link User} matching provided uid
     * @throws UpdateFailureException  if the current list of users cannot be retrieved
     * @throws EntityNotFoundException if no user matches the provided uid
     */
    @RequestMapping(path = "/users/{uid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public User getUserById(@PathVariable("uid") int uid) throws UpdateFailureException, EntityNotFoundException {
        logger.info("BEGIN: Received GET request for user with UID {}", uid);
        User retrievedUser = usersService.getUserById(uid);
        logger.debug("Retrieved the following user when seaching with UID {}: {}", uid, retrievedUser);
        logger.info("END: Successfully retrieved user with UID {}", uid);
        return retrievedUser;
    }

    /**
     * Allow clients to retrieve {@link Group}s containing a user with specified uid
     *
     * @param uid id of user to find group membership of
     * @return {@link Group}s that have user with specified uid as a member
     * @throws UpdateFailureException  if the current list of groups cannot be retrieved
     * @throws EntityNotFoundException if there is no user matching specified uid
     */
    @RequestMapping(path = "/users/{uid}/groups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Group> getGroupsOfUser(@PathVariable("uid") int uid) throws UpdateFailureException, EntityNotFoundException {
        logger.info("BEGIN: Received GET request to retrieve groups containing user with uid of {}", uid);
        List<Group> retrievedGroups = usersService.getGroupsOfUser(uid);
        logger.debug("Retrieved the following groups with searching for groups of user with uid of {}: {}", uid, retrievedGroups);
        logger.info("END: Successfully retrieved groups for user with uid of {}", uid);
        return retrievedGroups;
    }
}
