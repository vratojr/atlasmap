package io.atlasmap.core;

import io.atlasmap.v2.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ActionGroupTest {

    @Test
    public void thatIfThereAreNoCollectionActionsThenASingleGroupIsExtracted() {
        Field f = new MockField();
        f.setActions(createActions(new Add()));
        f.setPath("/contact<>/name");

        List<ActionGroup> group = ActionGroup.identifyTargetActionGroups(f);
        assertEquals(1, group.size());
        assertEquals("By default, for an array, we map to element 0", "/contact<0>/name", group.get(0).toPath());
    }

    @Test
    public void thatCollectionActionIsUsedToFormThePath() {
        Field f = new MockField();
        f.setActions(createActions(new MapToIndex(1, "contact")));
        f.setPath("/contact<>/name");

        List<ActionGroup> group = ActionGroup.identifyTargetActionGroups(f);
        assertEquals(1, group.size());
        assertEquals("/contact<1>/name", group.get(0).toPath());
    }

    @Test
    public void thatPathForNestedCollectionsIsCalculated() {
        Field f = new MockField();
        f.setActions(createActions(new MapToIndex(1, "contact"), new MapToIndex(1, "foreigner")));
        f.setPath("/contact<>/foreigner<>/name");

        List<ActionGroup> group = ActionGroup.identifyTargetActionGroups(f);
        assertEquals(1, group.size());
        assertEquals("/contact<1>/foreigner<1>/name", group.get(0).toPath());
    }

    @Test
    public void thatGroupsAreSplitWhenTwoCollectionMappingsAreUnordered() {
        Field f = new MockField();
        f.setActions(createActions(
            new MapToIndex(0, "foreigner"),
            new MapToIndex(1, "contact"))); // This is considered to be a mapping to a new element
        f.setPath("/contact<>/foreigner<>/name");

        List<ActionGroup> group = ActionGroup.identifyTargetActionGroups(f);
        assertEquals(2, group.size());
        assertEquals("/contact<0>/foreigner<0>/name", group.get(0).toPath());
        assertEquals("/contact<1>/foreigner<0>/name", group.get(1).toPath());
    }

    @Test
    public void thatGroupsAreSplitByActions() {
        Field f = new MockField();
        f.setActions(createActions(new MapToIndex(0, "contact"), new Add(), new MapToIndex(1, "contact"), new Append()));
        f.setPath("/contact<>/name");

        List<ActionGroup> group = ActionGroup.identifyTargetActionGroups(f);
        assertEquals(2, group.size());
        assertEquals("/contact<0>/name", group.get(0).toPath());
        assertEquals(1, group.get(0).getActions().size());
        assertTrue(group.get(0).getActions().get(0) instanceof Add);
        assertEquals("/contact<1>/name", group.get(1).toPath());
        assertEquals(1, group.get(1).getActions().size());
        assertTrue(group.get(1).getActions().get(0) instanceof Append);
    }

    @Test
    public void thatGroupsAreSplitWhenTwoCollectionMappingsReferToTheSamePath() {
        Field f = new MockField();
        f.setActions(createActions(new MapToIndex(0, "contact"), new MapToIndex(1, "contact"), new Append()));
        f.setPath("/contact<>/name");

        List<ActionGroup> group = ActionGroup.identifyTargetActionGroups(f);
        assertEquals(2, group.size());
        assertEquals("/contact<0>/name", group.get(0).toPath());
        assertEquals(0, group.get(0).getActions().size());
        assertEquals("/contact<1>/name", group.get(1).toPath());
        assertEquals(1, group.get(1).getActions().size());
        assertTrue(group.get(1).getActions().get(0) instanceof Append);
    }

    private ArrayList<Action> createActions(Action... a) {
        ArrayList<Action> actions = new ArrayList<Action>();
        for (Action aa : a) {
            actions.add(aa);
        }
        return actions;
    }
}
