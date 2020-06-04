package io.atlasmap.v2;

import java.io.Serializable;

public class MapFromIndex extends CollectionAction implements Serializable {

    private final static long serialVersionUID = 1L;

    public MapFromIndex(Integer index, String collectionName) {
        super(index, collectionName);
    }

    public MapFromIndex() {
    }
}
