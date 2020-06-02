package io.atlasmap.itests.reference.json_to_json;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.MapToIndex;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class JsonJsonMapToNestedCollectionIndexTest extends AtlasMappingBaseTest {

    private AtlasSession session;
    private AtlasContext context;
    private String input = "{ \"contact\": { \"firstName\": \"name9\" } }";
    private JsonField inputField;
    private JsonField outField0;

    @Before
    public void setup() throws Exception {
        context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/atlasmapping-empty-mapping.json").toURI());
        session = context.createSession();

        inputField = TestHelper.addInputStringField(session, "/contact/firstName");
        outField0 = TestHelper.addOutputStringField(session, "/contact<>/foreigner<>/name");

    }

    @Test
    // contact.firstName -> contact<>/foreigner<1>.name
    public void testProcessNestedCollectionFromNonCollectionByIndex() throws Exception {

        TestHelper.addMappings(session, new MapToIndex(1, "foreigner"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"foreigner\":[{},{\"name\":\"name9\"}]}]}";
        assertEquals(output, object);
    }

    @Test
    // contact.firstName -> contact<>/foreigner<1>.name
    // contact.firstName -> contact<0>/foreigner<0>.name
    public void testProcessNestedCollectionFromNonCollectionByIndex_1() throws Exception {

        TestHelper.addMappings(session, new MapToIndex(1, "foreigner"), new MapToIndex(0, "contact"), new MapToIndex(0, "foreigner"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"foreigner\":[{\"name\":\"name9\"},{\"name\":\"name9\"}]}]}";
        assertEquals(output, object);
    }

    @Test
    // contact.firstName -> contact<1>/foreigner<1>.name
    // contact.firstName -> contact<1>/foreigner<2>.name
    // contact.firstName -> contact<2>/foreigner<>.name
    public void testProcessNestedCollectionFromNonCollectionByIndex_2() throws Exception {

        TestHelper.addMappings(session, new MapToIndex(1, "contact"), new MapToIndex(1, "foreigner"), new MapToIndex(1, "contact"), new MapToIndex(2, "foreigner"), new MapToIndex(2, "contact"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{},{\"foreigner\":[{},{\"name\":\"name9\"},{\"name\":\"name9\"}]},{\"foreigner\":[{\"name\":\"name9\"}]}]}";
        assertEquals(output, object);
    }

    @Test
    // contact.firstName -> contact<0>/foreigner<0>.name
    // contact.firstName -> contact<0>/other<1>.name
    public void testMappingToTwoSeparatedCollectionsByIndex() throws Exception {

        JsonField outField1 = TestHelper.addOutputStringField(session, "/contact<>/other<>/name");

        TestHelper.addMappings(outField0, new MapToIndex(0, "foreigner"));
        TestHelper.addMappings(outField1, new MapToIndex(1, "other"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"foreigner\":[{\"name\":\"name9\"}],\"other\":[{},{\"name\":\"name9\"}]}]}";
        assertEquals(output, object);

    }

    @Test
    // contact.firstName -> contact<1>/foreigner<0>.name
    // contact.firstName -> contact<0>/other<1>.name
    public void testMappingToTwoSeparatedCollectionsByIndex_1() throws Exception {

        JsonField outField1 = TestHelper.addOutputStringField(session, "/contact<>/other<>/name");

        TestHelper.addMappings(outField0, new MapToIndex(1, "contact"), new MapToIndex(0, "foreigner"));
        TestHelper.addMappings(outField1, new MapToIndex(0, "contact"), new MapToIndex(1, "other"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"other\":[{},{\"name\":\"name9\"}]},{\"foreigner\":[{\"name\":\"name9\"}]}]}";
        assertEquals(output, object);

    }

    @Test
    // contact.firstName -> contact<0>/foreigner<0>.name
    // contact.firstName -> contact<0>/foreigner<1>/other<0>.name
    public void testMappingToAsymmetricCollections() throws Exception {

        JsonField outField1 = TestHelper.addOutputStringField(session, "/contact<>/foreigner<>/other<>/name");

        TestHelper.addMappings(outField0, new MapToIndex(0, "contact"), new MapToIndex(0, "foreigner"));
        TestHelper.addMappings(outField1, new MapToIndex(0, "contact"), new MapToIndex(1, "foreigner"), new MapToIndex(0, "other"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"foreigner\":[{\"name\":\"name9\"},{\"other\":[{\"name\":\"name9\"}]}]}]}";
        assertEquals(output, object);

    }

}
