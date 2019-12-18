import { useAtlasmap, IAtlasmapFieldWithField } from '@atlasmap/provider';
import { AtlasmapUI, GroupId, IAtlasmapField } from "@atlasmap/ui";
import { Page, PageHeader, PageSection } from "@patternfly/react-core";
import React, { useCallback, useRef, useState } from 'react';
import "./App.css";
import { useConfirmationDialog } from './useConfirmationDialog';
import { useSingleInputDialog } from './useSingleInputDialog';

const App: React.FC = () => {
  const [sourceFilter, setSourceFilter] = useState<string | undefined>();
  const [targetFilter, setTargetFilter] = useState<string | undefined>();
  const {
    sources,
    targets,
    mappings,
    pending,
    error,
    importAtlasFile,
    resetAtlasmap,
    exportAtlasFile,
    deleteAtlasFile,
    changeActiveMapping,
    documentExists,
    enableMappingPreview,
    onFieldPreviewChange,
    addToMapping
  } = useAtlasmap({
    sourceFilter,
    targetFilter
  });

  const documentToDelete = useRef<GroupId | undefined>();
  let documentIsSource = useRef<boolean>();
  let importDocument = useRef<File | undefined>();

  const [importDialog, openImportDialog] = useConfirmationDialog({
    title: 'Overwrite selected document?',
    content: 'Are you sure you want to overwrite the selected document and remove any associated mappings?',
    onConfirm: (closeDialog) => {
      closeDialog();
      importAtlasFile(importDocument.current!, documentIsSource.current!);
    },
    onCancel: (closeDialog) => {
      closeDialog();
    }
  });

  const handleAddToMapping = useCallback(
    (fieldId: string, mappingId: string) => {
      addToMapping(fieldId, mappingId);
    },
    [addToMapping]
  );

  const handleImportAtlasFile = useCallback(
    (selectedFile: File) => importAtlasFile(selectedFile, false),
    [importAtlasFile]
  );

  const handleImportSourceDocument = useCallback(
    (selectedFile: File) => {
      importDocument.current = selectedFile;
      documentIsSource.current = true;
      if (documentExists(selectedFile, true)) {
        openImportDialog();
      } else {
        importAtlasFile(importDocument.current!, documentIsSource.current!);
      }
    },
    [importAtlasFile, openImportDialog, documentExists]
  );

  const handleImportTargetDocument = useCallback(
    (selectedFile: File) => {
      importDocument.current = selectedFile;
      documentIsSource.current = false;
      if (documentExists(selectedFile, false)) {
        openImportDialog();
      } else {
        importAtlasFile(importDocument.current!, documentIsSource.current!);
      }
    },
    [importAtlasFile, openImportDialog, documentExists]
  );

  const defaultCatalogName = 'atlasmap-mapping.adm';
  const [exportDialog, openExportDialog] = useSingleInputDialog({
    title: 'Export Mappings and Documents.',
    content: 'Please enter a name for your exported catalog file',
    placeholder: defaultCatalogName,
    onConfirm: (closeDialog, value) => {
      closeDialog();
      if (value.length === 0) {
        value = defaultCatalogName;
      }
      exportAtlasFile(value);
    },
    onCancel: (closeDialog) => {
      closeDialog();
    },
  });

  const [resetDialog, openResetDialog] = useConfirmationDialog({
    title: 'Reset All Mappings and Imports?',
    content: 'Are you sure you want to reset all mappings and clear all imported documents?',
    onConfirm: (closeDialog) => {
      closeDialog();
      resetAtlasmap();
    },
    onCancel: (closeDialog) => {
      closeDialog();
    }
  });

  const [deleteDocumentDialog, openDeleteDocumentDialog] = useConfirmationDialog({
    title: 'Remove selected document?',
    content: 'Are you sure you want to remove the selected document and any associated mappings?',
    onConfirm: (closeDialog) => {
      if (documentToDelete.current === undefined || documentIsSource.current === undefined) {
        throw new Error(
          `Fatal internal error: Could not remove the specified file.`
        );
      }
      closeDialog();
      deleteAtlasFile(documentToDelete.current!, documentIsSource.current!);
    },
    onCancel: (closeDialog) => {
      closeDialog();
    }
  });

  const handleDeleteSourceDocumentDialog = useCallback((id: GroupId) => {
    documentToDelete.current = id;
    documentIsSource.current = true;
    openDeleteDocumentDialog();
  }, [openDeleteDocumentDialog]);

  const handleDeleteTargetDocumentDialog = useCallback((id: GroupId) => {
    documentToDelete.current = id;
    documentIsSource.current = false;
    openDeleteDocumentDialog();
  }, [openDeleteDocumentDialog]);

  const handleFieldPreviewChange = useCallback((field: IAtlasmapField, value: string) => {
    onFieldPreviewChange(field as IAtlasmapFieldWithField, value);
  }, [onFieldPreviewChange]);

  return (
    <Page
      header={
        <PageHeader
          logo={<><strong>Atlasmap</strong>&nbsp;Data Mapper UI</>}
          style={{ minHeight: 40 }}
        />
      }
    >
      <PageSection variant={'light'} noPadding={true}>
        <AtlasmapUI
          sources={sources}
          targets={targets}
          mappings={mappings}
          pending={pending}
          error={error}
          onImportAtlasFile={handleImportAtlasFile}
          onImportSourceDocument={handleImportSourceDocument}
          onImportTargetDocument={handleImportTargetDocument}
          onDeleteSourceDocument={handleDeleteSourceDocumentDialog}
          onDeleteTargetDocument={handleDeleteTargetDocumentDialog}
          onResetAtlasmap={openResetDialog}
          onSourceSearch={setSourceFilter}
          onTargetSearch={setTargetFilter}
          onExportAtlasFile={openExportDialog}
          onActiveMappingChange={changeActiveMapping}
          onShowMappingPreview={enableMappingPreview}
          onFieldPreviewChange={handleFieldPreviewChange}
          onAddToMapping={handleAddToMapping}
          onCreateMapping={() => void 0}
        />
        {exportDialog}
        {importDialog}
        {deleteDocumentDialog}
        {resetDialog}
      </PageSection>
    </Page>
  );
};

export default App;
