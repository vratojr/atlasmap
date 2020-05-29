/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.json.module;

import java.util.*;
import java.util.stream.IntStream;

import io.atlasmap.v2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasValidationException;
import io.atlasmap.core.AtlasPath;
import io.atlasmap.core.AtlasUtil;
import io.atlasmap.core.BaseAtlasModule;
import io.atlasmap.json.core.JsonFieldReader;
import io.atlasmap.json.core.JsonFieldWriter;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModuleDetail;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@AtlasModuleDetail(name = "JsonModule", uri = "atlas:json", modes = {"SOURCE", "TARGET"}, dataFormats = {
    "json"}, configPackages = {"io.atlasmap.json.v2"})
public class JsonModule extends BaseAtlasModule {
    private static final Logger LOG = LoggerFactory.getLogger(JsonModule.class);

    @Override
    public void processPreValidation(AtlasInternalSession atlasSession) throws AtlasException {
        if (atlasSession == null || atlasSession.getMapping() == null) {
            throw new AtlasValidationException("Invalid session: Session and AtlasMapping must be specified");
        }

        Validations validations = atlasSession.getValidations();
        JsonValidationService jsonValidationService = new JsonValidationService(getConversionService(), getFieldActionService());
        jsonValidationService.setMode(getMode());
        jsonValidationService.setDocId(getDocId());
        List<Validation> jsonValidations = jsonValidationService.validateMapping(atlasSession.getMapping());
        if (jsonValidations != null && !jsonValidations.isEmpty()) {
            validations.getValidation().addAll(jsonValidations);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Detected " + jsonValidations.size() + " json validation notices");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPreValidation completed", getDocId());
        }
    }

    @Override
    public void processPreSourceExecution(AtlasInternalSession session) throws AtlasException {
        Object sourceDocument = session.getSourceDocument(getDocId());
        String sourceDocumentString = null;
        if (sourceDocument == null || !(sourceDocument instanceof String)) {
            AtlasUtil.addAudit(session, getDocId(), String.format(
                "Null or non-String source document: docId='%s'", getDocId()),
                null, AuditStatus.WARN, null);
        } else {
            sourceDocumentString = String.class.cast(sourceDocument);
        }
        JsonFieldReader fieldReader = new JsonFieldReader(getConversionService());
        fieldReader.setDocument(sourceDocumentString);
        session.setFieldReader(getDocId(), fieldReader);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{} processPreSourceExcution completed", getDocId());
        }
    }

    @Override
    public void processPreTargetExecution(AtlasInternalSession session) throws AtlasException {
        JsonFieldWriter writer = new JsonFieldWriter();
        session.setFieldWriter(getDocId(), writer);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{} processPreTargetExcution completed", getDocId());
        }
    }

    @Override
    public void readSourceValue(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        JsonFieldReader reader = session.getFieldReader(getDocId(), JsonFieldReader.class);
        if (reader == null) {
            AtlasUtil.addAudit(session, sourceField.getDocId(), String.format(
                "Source document '%s' doesn't exist", getDocId()),
                sourceField.getPath(), AuditStatus.ERROR, null);
            return;
        }
        reader.read(session);

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processSourceFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}]",
                getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(),
                sourceField.getValue());
        }
    }

    @Override
    public void populateTargetField(AtlasInternalSession session) throws AtlasException {
        Field sourceField = session.head().getSourceField();
        Field targetField = session.head().getTargetField();
        AtlasPath path = new AtlasPath(targetField.getPath());
        FieldGroup targetFieldGroup = null;
        if (path.hasCollection() && !path.isIndexedCollection()) {
            targetFieldGroup = AtlasModelFactory.createFieldGroupFrom(targetField, true);
            session.head().setTargetField(targetFieldGroup);
        }

        // Attempt to Auto-detect field type based on input value
        if (targetField.getFieldType() == null && sourceField.getValue() != null) {
            targetField.setFieldType(getConversionService().fieldTypeFromClass(sourceField.getValue().getClass()));
        }

        if (targetFieldGroup == null) {
            if (sourceField instanceof FieldGroup) {
                List<Field> subFields = ((FieldGroup) sourceField).getField();
                if (subFields != null && subFields.size() > 0) {
                    Integer index = targetField.getIndex();
                    if (index != null) {
                        if (subFields.size() > index) {
                            sourceField = subFields.get(index);
                        } else {
                            AtlasUtil.addAudit(session, getDocId(), String.format(
                                "The number of source fields (%s) is smaller than target index (%s) - ignoring",
                                subFields.size(), index),
                                null, AuditStatus.WARN, null);
                            return;
                        }
                    } else {
                        // The last one wins for compatibility
                        sourceField = subFields.get(subFields.size() - 1);
                    }
                    session.head().setSourceField(sourceField);
                }
            }
            super.populateTargetField(session);
        } else if (sourceField instanceof FieldGroup) {
            Field previousTargetSubField = null;
            for (int i = 0; i < ((FieldGroup) sourceField).getField().size(); i++) {
                Field sourceSubField = ((FieldGroup) sourceField).getField().get(i);
                JsonField targetSubField = new JsonField();
                AtlasJsonModelFactory.copyField(targetField, targetSubField, false);
                getCollectionHelper().copyCollectionIndexes(sourceField, sourceSubField, targetSubField, previousTargetSubField);
                previousTargetSubField = targetSubField;
                targetFieldGroup.getField().add(targetSubField);
                session.head().setSourceField(sourceSubField);
                session.head().setTargetField(targetSubField);
                super.populateTargetField(session);
            }
            session.head().setSourceField(sourceField);
            session.head().setTargetField(targetFieldGroup);
        } else {
            addTargetCollectionElements(targetField, path, targetFieldGroup, session);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(
                "{}: processTargetFieldMapping completed: SourceField:[docId={}, path={}, type={}, value={}], TargetField:[docId={}, path={}, type={}, value={}]",
                getDocId(), sourceField.getDocId(), sourceField.getPath(), sourceField.getFieldType(),
                sourceField.getValue(), targetField.getDocId(), targetField.getPath(), targetField.getFieldType(),
                targetField.getValue());
        }
    }

    class ActionGroup {
        private List<MapToIndex> collectionActions = new ArrayList<>();
        private List<Action> actions = new ArrayList<>();
        private AtlasPath pathTemplate;

        public ActionGroup(AtlasPath pathTemplate){
            this.pathTemplate = new AtlasPath(pathTemplate.toString());
        }

        public List<MapToIndex> getCollectionActions() {
            return collectionActions;
        }

        public List<Action> getActions() {
            return actions;
        }

        // For a given segment, return the collection index to which the group maps to. Eg if the group maps to /contacts<0> it will return 0 for argument "contacts"
        public Integer getTargetCollectionIndexBySegment(String name){
            List<AtlasPath.SegmentContext> segments =  pathTemplate.getSegments(true);
            for(AtlasPath.SegmentContext segment : segments){
                MapToIndex indexAction = collectionActions.stream().filter(n->n.getCollectionName().equalsIgnoreCase(segment.getName())).findFirst().orElse(null);
                if(indexAction == null){
                    return 0;
                }
                else{
                    return indexAction.getIndex();
                }
            }
            return -1;
        }

        // Return the final path to which this group refers to
        public String toPath() {
            List<AtlasPath.SegmentContext> segments =  pathTemplate.getSegments(true);
            for(AtlasPath.SegmentContext segment : segments){
                int segmentIndex = segments.indexOf(segment);
                MapToIndex indexAction = collectionActions.stream().filter(n->n.getCollectionName().equalsIgnoreCase(segment.getName())).findFirst().orElse(null);
                if(indexAction == null){
                    pathTemplate.setCollectionIndex(segmentIndex,0);
                }
                else{
                    pathTemplate.setCollectionIndex(segmentIndex,indexAction.getIndex());
                }
            }
            return pathTemplate.toString();
        }

    }

    private List<ActionGroup> extractActionGroups(AtlasPath path, List<Action> actions) {
        List<ActionGroup> res = new ArrayList<>();

        ActionGroup group = new ActionGroup(path);
        res.add(group);
        if (actions == null || actions.size() == 0) {
            return res;
        }

        //First step
        Action lastAction = actions.get(0);
        if (lastAction instanceof MapToIndex) {
            group.getCollectionActions().add((MapToIndex) lastAction);
        } else {
            group.getActions().add(lastAction);
        }

        List<AtlasPath.SegmentContext> contexts = path.getCollectionSegments(true);

        // Process the rest
        for (int i = 1; i < actions.size(); i++) {
            Action action = actions.get(i);
            /*
             * Check if we are starting a new group.
             * This can either be: a MapToIndex while the previous action is a normal one
             * or a MapToIndex to the same (or a previos) segment as the one targetted by the previous MapToIndex.
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
                group.getCollectionActions().add(mapAction);
            } else {
                group.getActions().add(action);
            }
            lastAction = action;
        }

        return res;
    }

    private void addTargetCollectionElements(Field targetField, AtlasPath path, FieldGroup targetFieldGroup, AtlasInternalSession session) throws AtlasException {
        List<ActionGroup> groups = extractActionGroups(path,session.head().getTargetField().getActions() );

        /* Iterate on the path.
           For each segment check if there is an action group defined.
           If yes, iterate up to max index and add padding elements where the action group is not defined.
           If not do nothing (should not happen)
         */
        List<AtlasPath.SegmentContext> segments = path.getCollectionSegments(true);
        for (int s = 0; s < segments.size(); s++) {
            AtlasPath.SegmentContext segment = segments.get(s);
            // Sort by segment
            TreeMap<Integer, List<ActionGroup>> orderedGroups = groups.stream()
                .collect(groupingBy(a -> a.getTargetCollectionIndexBySegment(segment.getName()), TreeMap::new, toList()));
            int max = orderedGroups.lastKey();

            for (int i = 0; i <= max; i++) {
                List<ActionGroup> groupsByIndex = orderedGroups.get(i);
                if (groupsByIndex == null) { // Add padding
                    JsonField targetSubField = new JsonField();
                    AtlasJsonModelFactory.copyField(targetField, targetSubField, false);
                    AtlasPath tempPath = new AtlasPath(segments.subList(0, segments.indexOf(segment) + 1));
                   // tempPath.setCollectionIndex(tempPath.getSegments(true).size()-1,0);
                    targetSubField.setPath(tempPath.toString());
                    targetFieldGroup.getField().add(targetSubField);
                    session.head().setTargetField(targetSubField);
                    LOG.info("Added padding :{}", tempPath.toString());
                } else if (s == segments.size() - 1) { // If we are at the last segment, map the fields that need to
                    for (ActionGroup g : groupsByIndex) {
                        LOG.info("Handling actiongroup:{}", g.toPath());

                        JsonField targetSubField = new JsonField();
                        AtlasJsonModelFactory.copyField(targetField, targetSubField, false);
                        targetSubField.setPath(g.toPath());
                        targetFieldGroup.getField().add(targetSubField);
                        session.head().setTargetField(targetSubField);
                        super.populateTargetField(session);// It should be done only for groups and not for paddings
                    }
                }
            }
        }

        session.head().setTargetField(targetFieldGroup);
    }

    public void writeTargetValue(AtlasInternalSession session) throws AtlasException {
        JsonFieldWriter writer = session.getFieldWriter(getDocId(), JsonFieldWriter.class);
        if (session.head().getTargetField() instanceof FieldGroup) {
            FieldGroup targetFieldGroup = (FieldGroup) session.head().getTargetField();
            if (targetFieldGroup.getField().size() > 0) {
                for (Field f : targetFieldGroup.getField()) {
                    session.head().setTargetField(f);
                    writer.write(session);
                }
                return;
            }
        }
        writer.write(session);
    }

    @Override
    public void processPostSourceExecution(AtlasInternalSession session) throws AtlasException {
        session.removeFieldReader(getDocId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPostSourceExecution completed", getDocId());
        }
    }

    @Override
    public void processPostTargetExecution(AtlasInternalSession session) throws AtlasException {
        JsonFieldWriter writer = session.getFieldWriter(getDocId(), JsonFieldWriter.class);
        if (writer != null && writer.getRootNode() != null) {
            String outputBody = writer.getRootNode().toString();
            session.setTargetDocument(getDocId(), outputBody);
            if (LOG.isDebugEnabled()) {
                LOG.debug(String.format("processPostTargetExecution converting JsonNode to string size=%s",
                    outputBody.length()));
            }
        } else {
            AtlasUtil.addAudit(session, getDocId(), String
                    .format("No target document created for DataSource:[id=%s, uri=%s]", getDocId(), this.getUri()),
                null, AuditStatus.WARN, null);
        }
        session.removeFieldWriter(getDocId());

        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: processPostTargetExecution completed", getDocId());
        }
    }

    @Override
    public Boolean isSupportedField(Field field) {
        if (super.isSupportedField(field)) {
            return true;
        }
        return field instanceof JsonField;
    }

    @Override
    public Field cloneField(Field field) throws AtlasException {
        return AtlasJsonModelFactory.cloneField((JsonField) field, true);
    }
}
