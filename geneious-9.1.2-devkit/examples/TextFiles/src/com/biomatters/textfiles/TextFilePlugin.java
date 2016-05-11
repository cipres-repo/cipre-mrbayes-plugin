package com.biomatters.textfiles;

import com.biomatters.geneious.publicapi.plugin.DocumentFileImporter;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.DocumentType;
import com.biomatters.geneious.publicapi.plugin.DocumentFileExporter;

public class TextFilePlugin extends GeneiousPlugin {
    public String getName() {
        return "Text Files";
    }

    public String getDescription() {
        return "Handle simple text files inside Geneious";
    }

    public String getHelp() {
        return null;
    }

    public String getAuthors() {
        return "Biomatters";
    }

    public String getVersion() {
        return "0.1";
    }

    public String getMinimumApiVersion() {
        return "4.0";
    }

    public int getMaximumApiVersion() {
        return 4;
    }

    public DocumentType[] getDocumentTypes() {
        return new DocumentType[]{new DocumentType("Text Files", TextDocument.class, null)};
    }

    public DocumentFileImporter[] getDocumentFileImporters() {
        return new DocumentFileImporter[]{new TextDocumentImporter()};
    }

    public DocumentFileExporter[] getDocumentFileExporters() {
        return new DocumentFileExporter[] {new TextDocumentExporter()};
    }
}
