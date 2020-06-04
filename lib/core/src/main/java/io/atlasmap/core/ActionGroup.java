package io.atlasmap.core;

import io.atlasmap.v2.Action;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.MapToIndex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Contains the set of actions that should be applied on a given target path.
 *
 * A given field can map to multiple target elements in case its mapping actions contain some CollectionActions.
 * For each target element (identified by a unique path) a specific set of mappings can be defined.
 */
public class ActionGroup {

    private List<MapToIndex> collectionActions = new ArrayList<>();
    private List<Action> actions = new ArrayList<>();
    private AtlasPath pathTemplate;

    public static List<ActionGroup> identifyActionGroups(Field field) {
        AtlasPath path = new AtlasPath(field.getPath());
        List<Action> actions = field.getActions();

        List<ActionGroup> res = new ArrayList<>();

        ActionGroup group = new ActionGroup(path);
        res.add(group);
        if (actions == null || actions.size() == 0) {
            return res;
        }

        //First step
        Action lastAction = actions.get(0);
        if (lastAction instanceof MapToIndex) {
            group.addCollectionAction((MapToIndex) lastAction);
        } else {
            group.addAction(lastAction);
        }

        List<AtlasPath.SegmentContext> contexts = path.getCollectionSegments(true);

        // Process the rest
        for (int i = 1; i < actions.size(); i++) {
            Action action = actions.get(i);
            /*
             * Check if we are starting a new group.
             * This can either be: a MapToIndex while the previous action is a normal one
             * or a MapToIndex to the same (or a previous) segment as the one targetted by the previous MapToIndex.
             */
            if (action instanceof MapToIndex) {
                MapToIndex mapAction = (MapToIndex) action;
                boolean isNewGroup = !(lastAction instanceof MapToIndex);
                if (lastAction instanceof MapToIndex) {
                    MapToIndex lastMapAction = (MapToIndex) lastAction;
                    int lastSegmentIndex = IntStream.range(0, contexts.size()).filter(j -> contexts.get(j).getName().equalsIgnoreCase(lastMapAction.getCollectionName())).findFirst().orElse(-1);
                    isNewGroup |= IntStream.range(0, contexts.size()).filter(j -> contexts.get(j).getName().equalsIgnoreCase(mapAction.getCollectionName())).findFirst().orElse(-1) <= lastSegmentIndex;
                }
                if (isNewGroup) {
                    group = new ActionGroup(path);
                    res.add(group);
                }
                group.addCollectionAction(mapAction);
            } else {
                group.addAction(action);
            }
            lastAction = action;
        }

        return res;
    }

    /***
     * @return the final path to which this group refers to
     */
    public String toPath() {
        List<AtlasPath.SegmentContext> segments = pathTemplate.getSegments(true);
        for (AtlasPath.SegmentContext segment : segments) {
            int segmentIndex = segments.indexOf(segment);
            MapToIndex indexAction = collectionActions.stream().filter(n -> n.getCollectionName().equalsIgnoreCase(segment.getName())).findFirst().orElse(null);
            if (indexAction == null) {
                pathTemplate.setCollectionIndex(segmentIndex, 0);
            } else {
                pathTemplate.setCollectionIndex(segmentIndex, indexAction.getIndex());
            }
        }
        return pathTemplate.toString();
    }

    public List<Action> getActions() {
        return actions;
    }

    private ActionGroup(AtlasPath pathTemplate) {
        this.pathTemplate = pathTemplate;
    }

    private void addCollectionAction(MapToIndex a) {
        this.collectionActions.add(a);
    }

    private void addAction(Action a) {
        this.actions.add(a);
    }


}
