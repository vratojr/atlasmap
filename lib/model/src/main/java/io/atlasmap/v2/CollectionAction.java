package io.atlasmap.v2;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Action that modifies the object that contains the field target of the action.
 * */
public abstract class CollectionAction extends Action {
    protected Integer index;

    protected String collectionName;

    protected CollectionAction(Integer index, String collectionName) {
        this.index = index;
        this.collectionName = collectionName;
    }

    protected CollectionAction() {
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
    @JsonPropertyDescription("The index of the item in the target collection.")
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
