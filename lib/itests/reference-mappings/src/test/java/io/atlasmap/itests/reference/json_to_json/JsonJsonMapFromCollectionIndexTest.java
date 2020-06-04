package io.atlasmap.itests.reference.json_to_json;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.v2.*;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class JsonJsonMapFromCollectionIndexTest extends AtlasMappingBaseTest {

    @Test
    // contact<1>.name -> contact.firstName
    public void testProcessNonCollectionFromCollectionByIndex() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/atlasmapping-empty-mapping.json").toURI());

        String input = "{ \"contact\": [{ \"name\": \"name0\" },{ \"name\": \"name1\" }] }";
        AtlasSession session = context.createSession();

        // Output field
        TestHelper.addInputStringField(session, "/contact<>/name");
        TestHelper.addOutputStringField(session, "/contact/firstName");

        TestHelper.addInputMappings(session,new MapFromIndex(1,"contact"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":{\"firstName\":\"name1#name2\"}}]}";
        assertEquals(output, object);
    }

    @Test
    // contact<1>.name + contact<2>.name -> contact.firstName
    public void testConcatenateByIndex() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/atlasmapping-empty-mapping.json").toURI());

        String input = "{ \"contact\": [{ \"name\": \"name0\" },{ \"name\": \"name1\" },{ \"name\": \"name2\" }] }";
        AtlasSession session = context.createSession();

        //Input field
        FieldGroup group = new FieldGroup();
        ArrayList<Action> groupActions = new ArrayList<>();
        Concatenate concatenate = new Concatenate();
        concatenate.setDelimiter("#");
        groupActions.add(concatenate);
        group.setActions(groupActions);
        group.getField().add(TestHelper.createJsonStringField("/contact<>/name"));
        if (session.getMapping().getMappings().getMapping().isEmpty()) {
            session.getMapping().getMappings().getMapping().add(new Mapping());
        }
        ((Mapping)session.getMapping().getMappings().getMapping().get(0)).setInputFieldGroup(group);

        // Output field
        TestHelper.addOutputStringField(session, "/contact.firstName");

        TestHelper.addInputMappings(session,new MapFromIndex(1,"contact"),new MapFromIndex(2,"contact"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":{\"firstName\":\"name1#name2\"}}]}";
        assertEquals(output, object);
    }


}
