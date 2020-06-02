package io.atlasmap.itests.reference.json_to_json;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class JsonJsonMapToCollectionIndexTest extends AtlasMappingBaseTest {

    @Test
    // contact.firstName -> contact<1>.name
    // contact.firstName -> contact<3>.name
    public void testProcessCollectionFromNonCollectionByIndex() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/atlasmapping-empty-mapping.json").toURI());

        AtlasSession session = context.createSession();
        TestHelper.addInputStringField(session,"/contact/firstName");
        TestHelper.addOutputStringField(session,"/contact<>/name");
        TestHelper.addMappings(session,new MapToIndex(1,"contact"),new MapToIndex(3,"contact"));
        String input = "{ \"contact\": { \"firstName\": \"name9\" } }";

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{},{\"name\":\"name9\"},{},{\"name\":\"name9\"}]}";
        assertEquals(output, object);
    }


    @Test
    @Ignore//TODO
    public void testProcessNonCollectionFromCollectionByIndex() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/atlasmapping-noncollection-from-collection.json").toURI());

        String input = "{ \"contact\": [{ \"name\": \"name0\" },{ \"name\": \"name1\" },{ \"name\": \"name2\" }] }";
        AtlasSession session = context.createSession();

        ArrayList<Action> actions = new ArrayList<>();
        TestHelper.addMappings(session,new MapFromIndex(1,"contact"));
        // contact<1>/name -> contact<1>.name

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":{\"firstName\":\"name1\"}}]}";
        assertEquals(output, object);
    }


}
