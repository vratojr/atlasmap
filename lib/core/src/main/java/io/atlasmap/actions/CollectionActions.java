package io.atlasmap.actions;

import io.atlasmap.spi.AtlasActionProcessor;
import io.atlasmap.spi.AtlasFieldAction;
import io.atlasmap.v2.MapFromIndex;
import io.atlasmap.v2.MapToIndex;

public class CollectionActions implements AtlasFieldAction {

    @AtlasActionProcessor
    public static String mapToIndex(MapToIndex action, String input) {
        return input;
    }

    @AtlasActionProcessor
    public static String mapFromIndex(MapFromIndex action, String input) { return input; }

}
