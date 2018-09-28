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
package com.adonofero.challenge.services.groups;

import com.adonofero.challenge.entity.Group;
import com.adonofero.challenge.exceptions.service.EntityNotFoundException;
import com.adonofero.challenge.exceptions.service.UpdateFailureException;

import java.util.List;

/**
 * Interface for the Service level for {@link Group}s
 *
 * @author Alexander Donofero
 */
public interface GroupsService {

    /**
     * Retrieves the current list of {@link Group}s.
     *
     * @return current list of {@link Group}s
     * @throws UpdateFailureException if the current list of {@link Group}s cannot be retrieved
     */
    List<Group> getAllGroups() throws UpdateFailureException;

    /**
     * Retrieves all groups associated with the given uid.
     *
     * @param user name of user to find groups associated with
     * @return list of {@link Group}s associated with provided uid
     * @throws UpdateFailureException if the current list of {@link Group}s cannot be retrieved
     */
    List<Group> getGroupsOfUser(String user) throws UpdateFailureException;

    /**
     * Retrieve {@link Group} based on gid.
     *
     * @param gid id of group to retrieve
     * @return {@link Group} associated with provided gid
     * @throws UpdateFailureException  if the current list of {@link Group}s cannot be retrieved
     * @throws EntityNotFoundException if no {@link Group} matches the provided uid
     */
    Group getGroupByID(int gid) throws UpdateFailureException, EntityNotFoundException;

    /**
     * Query for {@link Group}s matching the input criteria. Pass 'null' for parameters to exclude them from the query.
     * Excluding all parameters results in all current {@link Group}s being returned. Passing multiple parameters is the equivalent
     * of an "AND" query in that a {@link Group} must match ALL criteria in order to be returned.
     *
     * @param name    name of group
     * @param gid     id of group
     * @param members subset of group members that must be present
     * @return
     * @throws UpdateFailureException
     */
    List<Group> queryGroups(String name, Integer gid, List<String> members) throws UpdateFailureException;
}
