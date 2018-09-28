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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.util.*;

/**
 * @author Alexander Donofero
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ContextConfiguration(classes = {PasswdApplication.class})
public class UsersControllerTest {

    @Autowired
    private MockMvc mvc;

    private ObjectMapper mapper = new ObjectMapper();

    File testDataDir = new File("src/test/resources/usersfiles/");
    File defaultUserFile = new File(testDataDir, "defaultUsersFile.txt");
    File updatedUserFile = new File(testDataDir, "updatedUsersFile.txt");
    File queryUserFile = new File(testDataDir, "queryUsersFile.txt");
    File malformedUserFile = new File(testDataDir, "malformedUsersFile.txt");
    File passwdFile = new File("target/test/passwd.txt");
    File queryGroupFile = new File("src/test/resources/groupfiles/queryGroupsFile.txt");
    File groupFile = new File("target/test/group");

    @Before
    public void setup() throws Exception {
        // Sets up default file for each test
        FileUtils.copyFile(defaultUserFile, passwdFile);
        passwdFile.setReadable(true);
    }

    @Test
    public void usersController_WithGetUsersEndpoint_WithValidUsersFile_ShouldReturnAllUsers() throws Exception {
        // Arrange
        List<User> expectedUsers = new ArrayList<>();
        expectedUsers.add(createUser("root", 0, 0, "root", "/root", "/bin/bash"));
        expectedUsers.add(createUser("daemon", 1, 1, "daemon", "/usr/sbin", "/usr/sbin/nologin"));
        expectedUsers.add(createUser("bin", 2, 2, "bin", "/bin", "/usr/sbin/nologin"));
        expectedUsers.add(createUser("sys", 3, 3, "sys", "/dev", "/usr/sbin/nologin"));

        // Act
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/users")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        User[] foundUsers = mapper.readValue(responseContent, User[].class);
        Assert.assertTrue(expectedUsers.size() == foundUsers.length);
        Assert.assertTrue(expectedUsers.containsAll(Arrays.asList(foundUsers)));
    }

    /**
     * Given that all users endpoints currently share the same method for refreshing the current list of users,
     * this test is sufficient to cover the 'nonexistant' file case for the other users endpoints as well.
     */
    @Test
    public void usersController_WithGetUsersEndpoint_WithNonExistantUsersFile_ShouldReturn_InternalServerError() throws Exception {
        // Arrange
        FileUtils.forceDelete(passwdFile);

        // Act and Assert
        mvc.perform(MockMvcRequestBuilders.get("/users")).andExpect(MockMvcResultMatchers.status().isInternalServerError());

    }

    /**
     * Given that all users endpoints currently share the same method for refreshing the current list of users,
     * this test is sufficient to cover the 'unreadable' file case for the other users endpoints as well.
     */
    @Test
    public void usersController_WithGetUsersEndpoint_WithUnreadableUsersFile_ShouldReturn_InternalServerError() throws Exception {
        // Arrange
        passwdFile.setReadable(false);

        // Act and Assert
        mvc.perform(MockMvcRequestBuilders.get("/users")).andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    /**
     * Given that all users endpoints currently share the same method for refreshing the current list of users,
     * this test is sufficient to cover the 'malformed' file case for the other users endpoints as well.
     */
    @Test
    public void usersController_WithGetUsersEndpoint_WithMalformedUsersFile_ShouldReturn_InternalServerError() throws Exception {
        // Arrange
        FileUtils.copyFile(malformedUserFile, passwdFile);

        // Act and Assert
        mvc.perform(MockMvcRequestBuilders.get("/users")).andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    /**
     * Given that all users endpoints currently share the same method for refreshing the current list of users,
     * this test is sufficient to cover the 'updated' file case for the other users endpoints as well.
     */
    @Test
    public void usersController_WithGetUsersEndpoint_WithChangesToUsersFile_ShouldReturn_UpdatedListOfUsers() throws Exception {
        // Arrange
        List<User> expectedUsers = new ArrayList<>();
        expectedUsers.add(createUser("root", 0, 0, "root", "/root", "/bin/bash"));
        expectedUsers.add(createUser("daemon", 1, 1, "daemon", "/usr/sbin", "/usr/sbin/nologin"));
        expectedUsers.add(createUser("bin", 2, 2, "bin", "/bin", "/usr/sbin/nologin"));
        expectedUsers.add(createUser("sys", 3, 3, "sys", "/dev", "/usr/sbin/nologin"));

        // Verify that users exist in default state.
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/users")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        String responseContent = result.getResponse().getContentAsString();
        User[] foundUsers = mapper.readValue(responseContent, User[].class);
        Assert.assertTrue(expectedUsers.size() == foundUsers.length);
        Assert.assertTrue(expectedUsers.containsAll(Arrays.asList(foundUsers)));

        // Updated user list and set up updated expectations
        FileUtils.copyFile(updatedUserFile, passwdFile);
        expectedUsers = new ArrayList<>();
        expectedUsers.add(createUser("colord", 118, 125, "colord colour management daemon,,,", "/var/lib/colord", "/usr/sbin/nologin"));
        expectedUsers.add(createUser("hplip", 119, 7, "HPLIP system user,,,", "/var/run/hplip", "/bin/false"));
        expectedUsers.add(createUser("adonofero", 1000, 1000, "Alex Donofero,,,", "/home/adonofero", "/bin/bash"));

        // Act
        result = mvc.perform(MockMvcRequestBuilders.get("/users")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        // Assert
        responseContent = result.getResponse().getContentAsString();
        foundUsers = mapper.readValue(responseContent, User[].class);
        Assert.assertTrue(expectedUsers.size() == foundUsers.length);
        Assert.assertTrue(expectedUsers.containsAll(Arrays.asList(foundUsers)));
    }

    @Test
    public void usersController_WithGetUserByUIDEndpoint_WithValidUID_ShouldReturn_SpecifiedUser() throws Exception {
        // Arrange
        User expectedUser = createUser("sys", 3, 3, "sys", "/dev", "/usr/sbin/nologin");

        // Act
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/users/3")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        User foundUser = mapper.readValue(responseContent, User.class);
        Assert.assertEquals(expectedUser, foundUser);
    }

    @Test
    public void usersController_WithGetUserByUIDEndpoint_WithUIDThatDoesNotExist_ShouldReturn_404NotFound() throws Exception {
        // Arrange
        // N/A

        // Act and Assert
        mvc.perform(MockMvcRequestBuilders.get("/users/9999")).andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    public void usersController_WithGetUserByUIDEndpoint_WithNonNumericUID_ShouldReturn_400BadRequest() throws Exception {
        // Arrange
        // N/A

        // Act
        mvc.perform(MockMvcRequestBuilders.get("/users/DONTDOTHIS")).andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

    @Test
    public void queryUsersEndpoint_WithSingleAttribute_ShouldReturn_AllUsers_MatchingAttribute() throws Exception {
        // Arrange
        FileUtils.copyFile(queryUserFile, passwdFile);
        String baseURI = "/users/query?";
        Map<String, List<User>> testURIsWithExpectedUsers = new HashMap<>();
        /**
         * Splitting up each attribute into its own test would be tedious and would distract from the link between the query
         * and the use case we seek to test. Instead, I'll cover each attribute here.
         */
        // Name search test
        List<User> testUsers = new ArrayList<>();
        testUsers.add(createUser("sys", 3, 3, "sys", "/dev", "/usr/sbin/nologin"));
        testURIsWithExpectedUsers.put(baseURI + "name=sys", testUsers);
        // UID search test
        testUsers = new ArrayList<>();
        testUsers.add(createUser("daemon", 1, 1, "daemon", "/usr/sbin", "/usr/sbin/nologin"));
        testURIsWithExpectedUsers.put(baseURI + "uid=1", testUsers);
        // GID search test
        testUsers = new ArrayList<>();
        testUsers.add(createUser("daemon", 1, 1, "daemon", "/usr/sbin", "/usr/sbin/nologin"));
        testUsers.add(createUser("sameGroup", 4, 1, "sameGroup", "/usr/sbin", "/bin/bash"));
        testURIsWithExpectedUsers.put(baseURI + "gid=1", testUsers);
        // Comment search test
        testUsers = new ArrayList<>();
        testUsers.add(createUser("sys", 3, 3, "sys", "/dev", "/usr/sbin/nologin"));
        testUsers.add(createUser("sys2", 5, 5, "sys", "/dev", "/bin/tcsh"));
        testURIsWithExpectedUsers.put(baseURI + "comment=sys", testUsers);
        // Home search test
        testUsers = new ArrayList<>();
        testUsers.add(createUser("root", 0, 0, "root", "/root", "/bin/bash"));
        testURIsWithExpectedUsers.put(baseURI + "home=/root", testUsers);
        // Shell search test
        testUsers = new ArrayList<>();
        testUsers.add(createUser("root", 0, 0, "root", "/root", "/bin/bash"));
        testUsers.add(createUser("sameGroup", 4, 1, "sameGroup", "/usr/sbin", "/bin/bash"));
        testURIsWithExpectedUsers.put(baseURI + "shell=/bin/bash", testUsers);

        for (String testURI : testURIsWithExpectedUsers.keySet()) {
            List<User> expectedUsers = testURIsWithExpectedUsers.get(testURI);

            // Act
            MvcResult result = mvc.perform(MockMvcRequestBuilders.get(testURI)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

            // Assert
            String responseContent = result.getResponse().getContentAsString();
            User[] foundUsers = mapper.readValue(responseContent, User[].class);
            Assert.assertTrue(expectedUsers.size() == foundUsers.length);
            Assert.assertTrue(expectedUsers.containsAll(Arrays.asList(foundUsers)));
        }
    }

    @Test
    public void queryUsersEndpoint_WithMultipleAttributes_ShouldReturn_AllUsers_MatchingEveryAttribute() throws Exception {
        // Arrange
        FileUtils.copyFile(queryUserFile, passwdFile);
        String baseURI = "/users/query?";
        Map<String, List<User>> testURIsWithExpectedUsers = new HashMap<>();
        /**
         * As each individual attribute was tested in 'queryUsersEndpoint_WithSingleAttribute_ShouldReturn_AllUsers_MatchingAttribute',
         * I feel confident in testing a single query with multiple parameters to validate this case for now.
         */
        List<User> testUsers = new ArrayList<>();
        testUsers.add(createUser("sameGroup", 4, 1, "sameGroup", "/usr/sbin", "/bin/bash"));
        testURIsWithExpectedUsers.put(baseURI + "uid=4&gid=1", testUsers);

        for (String testURI : testURIsWithExpectedUsers.keySet()) {
            List<User> expectedUsers = testURIsWithExpectedUsers.get(testURI);

            // Act
            MvcResult result = mvc.perform(MockMvcRequestBuilders.get(testURI)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

            // Assert
            String responseContent = result.getResponse().getContentAsString();
            User[] foundUsers = mapper.readValue(responseContent, User[].class);
            Assert.assertTrue(expectedUsers.size() == foundUsers.length);
            Assert.assertTrue(expectedUsers.containsAll(Arrays.asList(foundUsers)));
        }
    }

    @Test
    public void usersController_WithGetGroupsForUserEndpoint_WithValidUID_ShouldReturn_AllGroupsForUser() throws Exception {
        // Arrange
        FileUtils.copyFile(queryGroupFile, groupFile);
        List<Group> expectedGroups = new ArrayList<>();
        expectedGroups.add(createGroup("lpadmin", 118, new String[]{"sys"}));
        expectedGroups.add(createGroup("sys", 1000, new String[]{"sys"}));

        // Act
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/users/3/groups")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        Group[] foundGroups = mapper.readValue(responseContent, Group[].class);
        Assert.assertTrue(expectedGroups.size() == foundGroups.length);
        Assert.assertTrue(expectedGroups.containsAll(Arrays.asList(foundGroups)));
    }

    @Test
    public void usersController_WithGetGroupsForUserEndpoint_WithInvalidUID_ShouldReturn_NotFound() throws Exception {
        // Arrange

        // Act and Assert
        mvc.perform(MockMvcRequestBuilders.get("/users/9999/groups")).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    private User createUser(String name, int uid, int gid, String comment, String home, String shell) {
        User user = new User();
        user.setName(name);
        user.setUid(uid);
        user.setGid(gid);
        user.setComment(comment);
        user.setHome(home);
        user.setShell(shell);
        return user;
    }

    private Group createGroup(String name, int gid, String[] members) {
        Group group = new Group();
        group.setName(name);
        group.setGid(gid);
        group.setMembers(members);
        return group;
    }
}
