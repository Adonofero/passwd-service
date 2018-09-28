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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of {@link GroupsService} that retrieves {@link Group}s from a local file.
 * <p>
 * DESIGN NOTE:
 * Curent implementation refreshes the list of current users per request. Briefly considered setting up a watcher to
 * listen for changes to the file and then pumped it into an in-memory database to enhance performance.
 * Ultimately decided this was over-engineering the challenge and chose this simpler approach.
 *
 * @author Alexander Donofero
 */
public class LocalFileGroupsService implements GroupsService {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileGroupsService.class);


    private final File groupsFile;

    /**
     * Delimiter for fields in the 'user file'.
     * <p>
     * Currently I can expect ':' to be the delimiter for any 'user file'. If I am required to support additional
     * delimiters in the future, I can make this configurable.
     */
    private final String fieldDelimiter = ":";

    public LocalFileGroupsService(File groupsFile) {
        this.groupsFile = groupsFile;
    }

    @Override
    public List<Group> getAllGroups() throws UpdateFailureException {
        return getCurrentGroups();
    }

    @Override
    public List<Group> getGroupsOfUser(String user) throws UpdateFailureException {
        List<Group> groups = getCurrentGroups();
        List<Group> matchedGroups = new ArrayList<>();
        for (Group group : groups) {
            if (Arrays.stream(group.getMembers()).anyMatch(member -> member.equals(user))) {
                matchedGroups.add(group);
            }
        }
        return matchedGroups;
    }

    @Override
    public Group getGroupByID(int gid) throws UpdateFailureException, EntityNotFoundException {
        List<Group> groups = getCurrentGroups();
        for (Group group : groups) {
            if (group.getGid() == gid) {
                return group;
            }
        }
        String message = String.format("Could not find group with gid %d", gid);
        throw new EntityNotFoundException(message);
    }

    @Override
    public List<Group> queryGroups(String name, Integer gid, List<String> members) throws UpdateFailureException {
        List<Group> groups = getCurrentGroups();
        // For each search parameter specified, filter out the list of groups
        if (name != null) {
            List<Group> matchedGroups = new ArrayList<>();
            for (Group group : groups) {
                if (group.getName().equals(name)) {
                    matchedGroups.add(group);
                }
            }
            groups.retainAll(matchedGroups);
        }
        if (gid != null) {
            List<Group> matchedGroups = new ArrayList<>();
            for (Group group : groups) {
                if (group.getGid() == gid) {
                    matchedGroups.add(group);
                }
            }
            groups.retainAll(matchedGroups);
        }
        if (members != null) {
            List<Group> matchedGroups = new ArrayList<>();
            for (Group group : groups) {
                List<String> currentMembers = Arrays.asList(group.getMembers());
                if (currentMembers.containsAll(members)) {
                    matchedGroups.add(group);
                }
            }
            groups.retainAll(matchedGroups);
        }
        return groups;
    }

    /**
     * Private helper method to retrieve the current system {@link Group}s from the configured location
     *
     * @return All users currently on the system
     */
    private List<Group> getCurrentGroups() throws UpdateFailureException {
        // Initial sanity checks
        if (!groupsFile.exists()) {
            String message = String.format("Local groups file at %s does not exist", groupsFile.getAbsolutePath());
            throw new UpdateFailureException(message);
        }
        if (!groupsFile.canRead()) {
            String message = String.format("Cannot read local groups file at %s", groupsFile.getAbsolutePath());
            throw new UpdateFailureException(message);
        }
        // Attempt to perform update
        List<String> groupLines;
        try {
            groupLines = FileUtils.readLines(groupsFile, Charset.defaultCharset());
        } catch (IOException ex) {
            throw new UpdateFailureException(ex);
        }

        List<Group> groups = new ArrayList<>();
        for (String groupLine : groupLines) {
            String[] splitLine = groupLine.split(fieldDelimiter, -1);
            /**
             * Group file is expected to contain 4 fields in this order:
             * group name
             * password
             * group id (gid)
             * group list (list of user names who are members of group, comma delimited)
             */
            if (splitLine.length != 4) {
                String message = String.format("Groups file at %s is malformed. Expected group line to be have 4 fields delimited by %s. Found %d fields in line \"%s\"",
                        groupsFile.getAbsolutePath(), fieldDelimiter, splitLine.length, groupLine);
                throw new UpdateFailureException(message);
            }
            Group group = new Group();
            group.setName(splitLine[0]);
            group.setGid(Integer.parseInt(splitLine[2]));
            String members = splitLine[3];
            if (members.equals("")) {
                group.setMembers(new String[]{});
            } else {
                group.setMembers(members.split(","));
            }
            logger.trace("Retrieved group information {} from group file line {}", group, groupLine);
            groups.add(group);
        }
        logger.debug("Retrieval of current groups found the following groups: {}", groups);
        return groups;
    }
}
