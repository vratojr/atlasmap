package io.atlasmap.v2;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.io.Serializable;

public class MapToIndex extends FieldContainerAction implements Serializable {

    private final static long serialVersionUID = 1L;

    protected Integer index;

    protected String collectionName;

    public MapToIndex(Integer index, String collectionName) {
        this.index = index;
        this.collectionName = collectionName;
    }

    public MapToIndex() {
    }

    /**
     * Gets the value of the index property.
     *
     * @return possible object is
     * {@link Integer }
     */
    public Integer getIndex() {
        return index;
    }

    /**
     * Sets the value of the index property.
     *
     * @param index allowed object is
     *              {@link Integer }
     */
    @JsonPropertyDescription("The index of the item to target within the parent collection.")
    @AtlasActionProperty(title = "Index", type = FieldType.INTEGER)
    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * Gets the value of the collectionName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getCollectionName() { return collectionName; }

    /**
     * Sets the value of the collectionName property.
     *
     * @param collectionName allowed object is
     *              {@link String }
     */
    @JsonPropertyDescription("The name of the parent collection to which the index refers to.")
    @AtlasActionProperty(title = "CollectionName", type = FieldType.STRING)
    public void setCollectionName(String collectionName) {   this.collectionName = collectionName; }
}
