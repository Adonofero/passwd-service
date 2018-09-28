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
public class GroupsControllerTest {

    @Autowired
    private MockMvc mvc;

    private ObjectMapper mapper = new ObjectMapper();

    File testDataDir = new File("src/test/resources/groupfiles/");
    File defaultGroupFile = new File(testDataDir, "defaultGroupsFile.txt");
    File queryGroupFile = new File(testDataDir, "queryGroupsFile.txt");
    File malformedGroupFile = new File(testDataDir, "malformedGroupsFile.txt");
    File groupFile = new File("target/test/group");

    @Before
    public void setup() throws Exception {
        // Sets up default file for each test
        FileUtils.copyFile(defaultGroupFile, groupFile);
        groupFile.setReadable(true);
    }

    @Test
    public void groupsController_WithGetGroupsEndpoint_WithValidGroupFile_ShouldReturn_AllGroups() throws Exception {
        // Arrange
        List<Group> expectedGroups = new ArrayList<>();
        expectedGroups.add(createGroup("lpadmin", 118, new String[]{"adonofero"}));
        expectedGroups.add(createGroup("whoopsie", 119, new String[]{}));
        expectedGroups.add(createGroup("scanner", 120, new String[]{"saned"}));

        // Act
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/groups")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        Group[] foundGroups = mapper.readValue(responseContent, Group[].class);
        Assert.assertTrue(expectedGroups.size() == foundGroups.length);
        Assert.assertTrue(expectedGroups.containsAll(Arrays.asList(foundGroups)));
    }

    @Test
    public void groupsController_WithGetGroupsEndpoint_WithNonexistantGroupFile_ShouldReturn_InternalServerError() throws Exception {
        // Arrange
        FileUtils.forceDelete(groupFile);

        // Act and Assert
        mvc.perform(MockMvcRequestBuilders.get("/groups")).andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void groupsController_WithGetGroupsEndpoint_WithUnreadableGroupFile_ShouldReturn_InternalServerError() throws Exception {
        // Arrange
        groupFile.setReadable(false);

        // Act and Assert
        mvc.perform(MockMvcRequestBuilders.get("/groups")).andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void groupsController_WithGetGroupsEndpoint_WithMalformedGroupFile_ShouldReturn_InternalServerError() throws Exception {
        // Arrange
        FileUtils.copyFile(malformedGroupFile, groupFile);

        // Act and Assert
        mvc.perform(MockMvcRequestBuilders.get("/groups")).andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    public void groupsController_WithGetGroupByGidEndpoint_WithValidGid_ShouldReturn_AssociatedGroup() throws Exception {
        // Arrange
        Group expectedGroup = createGroup("lpadmin", 118, new String[]{"adonofero"});

        // Act
        MvcResult result = mvc.perform(MockMvcRequestBuilders.get("/groups/118")).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        Group foundGroup = mapper.readValue(responseContent, Group.class);
        Assert.assertEquals(expectedGroup, foundGroup);
    }

    @Test
    public void groupsController_WithGetGroupByGidEndpoint_WithInvalidGid_ShouldReturn_NotFound() throws Exception {
        // Arrange

        // Act and Assert
        mvc.perform(MockMvcRequestBuilders.get("/groups/9999")).andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void groupsController_WithQueryGroupsEndpoint_WithSingleAttribute_ShouldReturn_AllGroups_MatchingAttribute() throws Exception {
        // Arrange
        FileUtils.copyFile(queryGroupFile, groupFile);
        String baseURI = "/groups/query?";
        Map<String, List<Group>> testURIsWithExpectedGroups = new HashMap<>();
        /**
         * Splitting up each attribute into its own test would be tedious and would distract from the link between the query
         * and the use case we seek to test. Instead, I'll cover each attribute here.
         */
        // Name search test
        List<Group> testGroups = new ArrayList<>();
        testGroups.add(createGroup("sys", 1000, new String[]{"sys"}));
        testURIsWithExpectedGroups.put(baseURI + "name=sys", testGroups);
        // GID search test
        testGroups = new ArrayList<>();
        testGroups.add(createGroup("lpadmin", 118, new String[]{"sys"}));
        testURIsWithExpectedGroups.put(baseURI + "gid=118", testGroups);
        // Members search test
        testGroups = new ArrayList<>();
        testGroups.add(createGroup("multigroup", 1001, new String[]{"adonofero", "tester"}));
        testGroups.add(createGroup("othermulti", 1002, new String[]{"tester"}));
        testURIsWithExpectedGroups.put(baseURI + "member=tester", testGroups);
        testGroups = new ArrayList<>();
        testGroups.add(createGroup("multigroup", 1001, new String[]{"adonofero", "tester"}));
        testURIsWithExpectedGroups.put(baseURI + "member=tester&member=adonofero", testGroups);
        testGroups = new ArrayList<>();
        testGroups.add(createGroup("multigroup", 1001, new String[]{"adonofero", "tester"}));
        testGroups.add(createGroup("adonofero", 1003, new String[]{"adonofero"}));
        testURIsWithExpectedGroups.put(baseURI + "member=adonofero", testGroups);
        for (String testURI : testURIsWithExpectedGroups.keySet()) {
            List<Group> expectedGroups = testURIsWithExpectedGroups.get(testURI);

            // Act
            MvcResult result = mvc.perform(MockMvcRequestBuilders.get(testURI)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

            // Assert
            String responseContent = result.getResponse().getContentAsString();
            Group[] foundGroups = mapper.readValue(responseContent, Group[].class);
            Assert.assertTrue(expectedGroups.size() == foundGroups.length);
            Assert.assertTrue(expectedGroups.containsAll(Arrays.asList(foundGroups)));
        }
    }

    @Test
    public void groupsController_WithQueryGroupsEndpoint_WithMultipleAttribute_ShouldReturn_AllGroups_MatchingAllAttributes() throws Exception {
        // Arrange
        FileUtils.copyFile(queryGroupFile, groupFile);
        String baseURI = "/groups/query?";
        Map<String, List<Group>> testURIsWithExpectedGroups = new HashMap<>();
        /**
         * As each individual attribute was tested in 'groupsController_WithQueryGroupsEndpoint_WithSingleAttribute_ShouldReturn_AllGroups_MatchingAttribute',
         * I feel confident in testing a single query with multiple parameters to validate this case for now.
         */
        List<Group> testGroups = new ArrayList<>();
        testGroups.add(createGroup("adonofero", 1003, new String[]{"adonofero"}));
        testURIsWithExpectedGroups.put(baseURI + "member=adonofero&name=adonofero", testGroups);
        for (String testURI : testURIsWithExpectedGroups.keySet()) {
            List<Group> expectedGroups = testURIsWithExpectedGroups.get(testURI);

            // Act
            MvcResult result = mvc.perform(MockMvcRequestBuilders.get(testURI)).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

            // Assert
            String responseContent = result.getResponse().getContentAsString();
            Group[] foundGroups = mapper.readValue(responseContent, Group[].class);
            Assert.assertTrue(expectedGroups.size() == foundGroups.length);
            Assert.assertTrue(expectedGroups.containsAll(Arrays.asList(foundGroups)));
        }
    }

    private Group createGroup(String name, int gid, String[] members) {
        Group group = new Group();
        group.setName(name);
        group.setGid(gid);
        group.setMembers(members);
        return group;
    }

}
