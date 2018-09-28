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

import java.util.List;

/**
 * Interface for the Service level for {@link User}s.
 *
 * @author Alexander Donofero
 */
public interface UsersService {

    /**
     * Retrieves the current list of {@link User}s.
     *
     * @return current list of {@link User}s
     * @throws UpdateFailureException if the current list of users cannot be retrieved
     */
    List<User> getAllUsers() throws UpdateFailureException;

    /**
     * Query for users matching the input criteria. Pass 'null' for parameters to exclude them from the query.
     * Excluding all parameters results in all current users being returned. Passing multiple parameters is the equivalent
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
    List<User> queryUsers(String name, Integer uid, Integer gid, String comment, String home, String shell) throws UpdateFailureException;

    /**
     * Retrieve {@link User} based on uid.
     *
     * @param uid id of {@link User}
     * @return {@link User} matching provided uid
     * @throws UpdateFailureException  if the current list of users cannot be retrieved
     * @throws EntityNotFoundException if no user matches the provided uid
     */
    User getUserById(int uid) throws UpdateFailureException, EntityNotFoundException;

    /**
     * Retrieves all groups associated with the given uid.
     *
     * @param uid id of user to find groups associated with
     * @return list of {@link Group}s associated with provided uid
     * @throws UpdateFailureException  if the current list of {@link Group}s cannot be retrieved
     * @throws EntityNotFoundException if the uid doesn't match any current user
     */
    List<Group> getGroupsOfUser(int uid) throws UpdateFailureException, EntityNotFoundException;

}
