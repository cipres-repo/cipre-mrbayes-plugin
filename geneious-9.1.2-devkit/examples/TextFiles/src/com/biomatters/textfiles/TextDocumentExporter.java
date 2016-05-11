package com.biomatters.textfiles;

import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentFileExporter;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import jebl.util.SafePrintWriter;
import jebl.util.ProgressListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class TextDocumentExporter extends DocumentFileExporter {
    public String getFileTypeDescription() {
         return "Text";
    }

    public String getDefaultExtension() {
        return ".txt";
    }

    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[] {
                new DocumentSelectionSignature(TextDocument.class, 1,1)};
    }

    public void export(File file, PluginDocument[] documents, ProgressListener progressListener)
            throws IOException
    {
        SafePrintWriter writer = new SafePrintWriter(new FileWriter(file));
        writer.write(((TextDocument)documents[0]).getText());
        writer.flush();
        writer.close();
    }

    public boolean mayDiscardInformation() {
        return false;
    }
}
