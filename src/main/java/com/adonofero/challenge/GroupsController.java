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
import com.adonofero.challenge.services.groups.GroupsService;
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
public class GroupsController {

    private static final Logger logger = LoggerFactory.getLogger(GroupsController.class);


    /**
     * Service layer used to perform the logic needed to fulfill client requests.
     */
    private final GroupsService groupsService;

    /**
     * Default Constructor.
     *
     * @param groupsService Service layer used to perform the logic needed to fulfill client requests.
     */
    public GroupsController(GroupsService groupsService) {
        this.groupsService = groupsService;
    }

    /**
     * Allow clients to retrieve a current list of all groups
     *
     * @return list of all groups
     * @throws UpdateFailureException if current list of all groups cannot be retrieved
     */
    @RequestMapping(path = "/groups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Group> getGroups() throws UpdateFailureException {
        logger.info("BEGIN: Received GET request for all groups");
        List<Group> retrievedGroups = groupsService.getAllGroups();
        logger.debug("GET request for all groups found the following groups: {}", retrievedGroups);
        logger.info("END: GET request for all groups resolved successfully");
        return retrievedGroups;
    }

    /**
     * Allow clients to retrieve {@link Group} based on gid.
     *
     * @param gid id of {@link Group}
     * @return {@link Group} matching provided gid
     * @throws UpdateFailureException  if the current list of groups cannot be retrieved
     * @throws EntityNotFoundException if no group matches the provided uid
     */
    @RequestMapping(path = "/groups/{gid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Group getUserById(@PathVariable("gid") int gid) throws UpdateFailureException, EntityNotFoundException {
        logger.info("BEGIN: Received GET request for group with GID {}", gid);
        Group retrievedGroup = groupsService.getGroupByID(gid);
        logger.debug("Retrieved the following group when searching with GID {}: {}", gid, retrievedGroup);
        logger.info("END: Successfully retrieved group with GID {}", gid);
        return retrievedGroup;
    }

    /**
     * Allow clients to query for groups matching the input criteria.
     * Excluding all parameters results in a 400 Bad Request. Passing multiple parameters is the equivalent
     * of an "AND" query in that a {@link Group} must match ALL criteria in order to be returned.
     *
     * @param name    name of group
     * @param gid     id of group
     * @param members list of members that a group must contain
     * @return list of {@link Group}s matching specified criteria
     * @throws UpdateFailureException if the current list of groups cannot be retrieved
     */
    @RequestMapping(path = "/groups/query", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Group> queryGroups(@RequestParam(name = "name", required = false) String name, @RequestParam(name = "gid", required = false) Integer gid,
                                   @RequestParam(name = "member", required = false) List<String> members) throws UpdateFailureException {
        logger.info("BEGIN: Received GET request for querying groups based on parameters");
        /**
         * DESIGN NOTE: The challenge requirements didn't specify an explicit behavior for calling the query endpoint with no paramters.
         *
         * Given that an endpoint exists to retrieve all groups, I determined that calling the 'query' endpoint with no parameters
         * should be considered a 'bad request' rather than returning all groups.
         */
        if (name == null && gid == null && members == null) {
            throw new MissingParametersException("Minimum of 1 query parameter required on queryGroups endpoint");
        }
        List<Group> retrievedGroups = groupsService.queryGroups(name, gid, members);
        logger.debug("Found the following groups given query criteria name='{}', gid='{}', member='{}': {}",
                name, gid, members, retrievedGroups);
        logger.info("END: GET request for groups query resolved successfully");
        return retrievedGroups;
    }
}
