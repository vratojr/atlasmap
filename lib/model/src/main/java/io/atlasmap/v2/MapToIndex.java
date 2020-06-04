package io.atlasmap.v2;

import java.io.Serializable;

public class MapToIndex extends CollectionAction implements Serializable {

    private final static long serialVersionUID = 1L;

    public MapToIndex(Integer index, String collectionName) {
        super(index, collectionName);
    }

    public MapToIndex() {
    }

}
