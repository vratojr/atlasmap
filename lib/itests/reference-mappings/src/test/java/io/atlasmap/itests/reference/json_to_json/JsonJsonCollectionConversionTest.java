/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.itests.reference.json_to_json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.xml.xsom.impl.scd.Iterators;
import io.atlasmap.v2.*;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;

public class JsonJsonCollectionConversionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCollectionListSimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-list-simple.json").toURI());

        // contact<>.firstName -> contact<>.name

        String input = "{ \"contact\": [";
        for (int i = 0; i < 3; i++) {
            input += "{ \"firstName\": \"name" + i + "\"}";
            input += (i == 2) ? "" : ",";
        }
        input += "] }";

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[";
        for (int i = 0; i < 3; i++) {
            output += "{\"name\":\"name" + i + "\"}";
            output += (i == 2) ? "" : ",";
        }
        output += "]}";
        assertEquals(output, object);
    }

    @Test
    public void testProcessCollectionArraySimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-array-simple.json").toURI());

        // contact[].firstName -> contact[].name

        String input = "{ \"contact\": [";
        for (int i = 0; i < 3; i++) {
            input += "{ \"firstName\": \"name" + i + "\"}";
            input += (i == 2) ? "" : ",";
        }
        input += "] }";

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[";
        for (int i = 0; i < 3; i++) {
            output += "{\"name\":\"name" + i + "\"}";
            output += (i == 2) ? "" : ",";
        }
        output += "]}";
        assertEquals(output, object);
    }

    @Test
    public void testProcessCollectionToNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-to-noncollection.json").toURI());

        // contact<>.firstName -> contact.name

        String input = "{ \"contact\": [";
        for (int i = 0; i < 3; i++) {
            input += "{ \"firstName\": \"name" + i + "\"}";
            input += (i == 2) ? "" : ",";
        }
        input += "] }";

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":{\"name\":\"name2\"}}";
        assertEquals(output, object);
    }

    @Test
    public void testProcessCollectionFromNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-from-noncollection.json").toURI());

        // contact.firstName -> contact<>.name

        String input = "{ \"contact\": { \"firstName\": \"name9\" } }";
        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"name\":\"name9\"}]}";
        assertEquals(output, object);
    }

    @Test
    public void testProcessCollectionFromNonCollectionByIndex() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/atlasmapping-collection-from-noncollection.json").toURI());

        String input = "{ \"contact\": { \"firstName\": \"name9\" } }";
        AtlasSession session = context.createSession();

        ArrayList<Action> actions = new ArrayList<>();
        addMappings(session,new MapToIndex(1,"contact"),new MapToIndex(3,"contact"));
        // contact.firstName -> contact<1>.name
        // contact.firstName -> contact<3>.name

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{},{\"name\":\"name9\"},{},{\"name\":\"name9\"}]}";
        assertEquals(output, object);
    }

    @Test
    /// Test mapping a collection into a standard collection
    public void testProcessNestedCollectionFromNonCollectionByIndex() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/mapping/collections/atlasmapping-collection-from-noncollection-deep.json").toURI());

        String input = "{ \"contact\": { \"firstName\": \"name9\" } }";
        AtlasSession session = context.createSession();
        addMappings(session,new MapToIndex(1,"foreigner"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"foreigner\":[{},{\"name\":\"name9\"}]}]}";
        assertEquals(output, object);
    }

    @Test
    /// Test mapping a collection into a standard collection
    public void testProcessNestedCollectionFromNonCollectionByIndex_1() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/mapping/collections/atlasmapping-collection-from-noncollection-deep.json").toURI());

        String input = "{ \"contact\": { \"firstName\": \"name9\" } }";
        AtlasSession session = context.createSession();
        addMappings(session,new MapToIndex(1,"foreigner"),new MapToIndex(0,"contact"),new MapToIndex(0,"foreigner"));
        // contact.firstName -> contact<>/foreigner<1>.name
        // contact.firstName -> contact<0>/foreigner<0>.name

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"foreigner\":[{\"name\":\"name9\"},{\"name\":\"name9\"}]}]}";
        assertEquals(output, object);
    }

    @Test
    /// Test mapping a collection into a standard collection
    public void testProcessNestedCollectionFromNonCollectionByIndex_2() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/mapping/collections/atlasmapping-collection-from-noncollection-deep.json").toURI());

        String input = "{ \"contact\": { \"firstName\": \"name9\" } }";
        AtlasSession session = context.createSession();
        addMappings(session,new MapToIndex(1,"contact"),new MapToIndex(1,"foreigner"),new MapToIndex(1,"contact"),new MapToIndex(2,"foreigner"),new MapToIndex(2,"contact"));
        // contact.firstName -> contact<1>/foreigner<1>.name
        // contact.firstName -> contact<1>/foreigner<2>.name
        // contact.firstName -> contact<2>/foreigner<>.name

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{},{\"foreigner\":[{},{\"name\":\"name9\"},{\"name\":\"name9\"}]},{\"foreigner\":[{\"name\":\"name9\"}]}]}";
        assertEquals(output, object);
    }

    // This is a more complex case in which we have two different mappings
//    @Test
//    /// Test mapping a collection into a standard collection
//    public void testProcessNestedCollectionFromNonCollectionByIndex_3() throws Exception {
//        AtlasContext context = atlasContextFactory.createContext(
//            new File("src/test/resources/jsonToJson/mapping/collections/atlasmapping-collection-from-noncollection-deep.json").toURI());
//
//        String input = "{ \"contact\": { \"firstName\": \"name9\" } }";
//        AtlasSession session = context.createSession();
//        //contact<1>/foreigner<1>
//        //contact<0>
//        //contact<0>/foreigner<2>
//        addMappings(session,new MapToIndex(1,"contact"),new MapToIndex(1,"foreigner"),new MapToIndex(0,"contact"),new MapToIndex(0,"contact"),new MapToIndex(2,"foreigner"));
//
//        session.setDefaultSourceDocument(input);
//        context.process(session);
//
//        Object object = session.getDefaultTargetDocument();
//        assertNotNull(object);
//        assertTrue(object instanceof String);
//
//        String output = "{\"contact\":[{\"name\":\"name9\",\"foreigner\":[{},{},{\"name\":\"name9\"}]},{\"foreigner\":[{},{\"name\":\"name9\"}]}]}";
//        assertEquals(output, object);
//    }


    @Test
    public void testProcessNonCollectionFromCollectionByIndex() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/atlasmapping-noncollection-from-collection.json").toURI());

        String input = "{ \"contact\": [{ \"name\": \"name0\" },{ \"name\": \"name1\" },{ \"name\": \"name2\" }] }";
        AtlasSession session = context.createSession();

        ArrayList<Action> actions = new ArrayList<>();
        addMappings(session,new MapFromIndex(1,"contact"));
        // contact<1>/name -> contact<1>.name

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":{\"firstName\":\"name1\"}}]}";
        assertEquals(output, object);
    }

    private void addMappings(AtlasSession session,Action... mappings){
        ArrayList<Action> l = new ArrayList<>();
        for(Action m : mappings){
            l.add(m);
        }
        ((Mapping)((Collection)session.getMapping().getMappings().getMapping().get(0)).getMappings().getMapping().get(0)).getOutputField().get(0).setActions(l);
    }
}
