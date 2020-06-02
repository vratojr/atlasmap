package io.atlasmap.itests.reference.json_to_json;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.v2.MapToIndex;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class JsonJsonMapToNestedCollectionIndex_2Test extends AtlasMappingBaseTest {

    private AtlasSession session;
    private AtlasContext context;
    private String input = "{ \"contact\": { \"firstName\": \"name9\" } }";

    @Before
    public void setup() throws Exception {
        context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/atlasmapping-empty-mapping.json").toURI());
        session = context.createSession();
    }

    // This is a more complex case in which we have two different mappings
    @Test
    @Ignore
    //contact<1>/foreigner<1>
    //contact<0>
    //contact<0>/foreigner<2>
    public void testProcessNestedCollectionFromNonCollectionByIndex_3() throws Exception {

        input = "{ \"contact\": { \"firstName\": \"name9\" } }";
        TestHelper.addMappings(session, new MapToIndex(1, "contact"), new MapToIndex(1, "foreigner"), new MapToIndex(0, "contact"), new MapToIndex(0, "contact"), new MapToIndex(2, "foreigner"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"name\":\"name9\",\"foreigner\":[{},{},{\"name\":\"name9\"}]},{\"foreigner\":[{},{\"name\":\"name9\"}]}]}";
        assertEquals(output, object);
    }

}
