package com.biomatters.textfiles;

import com.biomatters.geneious.publicapi.plugin.DocumentFileImporter;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;

import java.io.*;
import java.util.Date;

import jebl.util.ProgressListener;

class TextDocumentImporter extends DocumentFileImporter {

    public String[] getPermissibleExtensions() {
        return new String[]{".txt"};
    }

    public String getFileTypeDescription() {
        return "Text"; 
    }

    public final AutoDetectStatus tentativeAutoDetect(File file, String fileContentsStart) {
        return AutoDetectStatus.MAYBE;
    }

    public void importDocuments(File file, ImportCallback importCallback, ProgressListener progressListener)
            throws IOException, DocumentImportException
    {
        try {
            String name = file.getName();
            Date createDate = new Date(file.lastModified());
            final Reader reader = new FileReader(file);

            final StringBuilder sb = new StringBuilder();
            while( true ) {
                int c = reader.read();
                if( c < 0 ) break;

                sb.append((char)c);
            }
            importCallback.addDocument(new TextDocument(name, createDate, sb.toString()));
        } catch (FileNotFoundException e) {
            throw new IOException(e.getMessage());
        }
     }
}
