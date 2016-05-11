package com.biomatters.exampleDocumentViewerPlugin;

import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;

public class ExampleDocumentViewerPlugin extends GeneiousPlugin {
    public String getName() {
        return "ExampleDocumentViewerPlugin";
    }

    public String getHelp() {
        return "ExampleDocumentViewerPlugin";
    }

    public String getDescription() {
        return "ExampleDocumentViewerPlugin";
    }

    public String getAuthors() {
        return "Biomatters";
    }

    public String getVersion() {
        return "0.1";
    }

    public String getMinimumApiVersion() {
        return "4.1";
    }

    public int getMaximumApiVersion() {
        return 4;
    }

    public DocumentViewerFactory[] getDocumentViewerFactories() {
        return new DocumentViewerFactory[]{
                new DocumentViewerFactory() {
                    public DocumentSelectionSignature[] getSelectionSignatures(){
                        return new DocumentSelectionSignature[]{new DocumentSelectionSignature(SequenceDocument.class,1,1)};
                    }

                    public String getHelp(){
                        return "A Viewer";
                    }

                    public String getDescription(){
                        return "Ludicrous, isn't it?";
                    }

                    public String getName(){
                        return "Ludicrously Giant Sequence View";
                    }

                    public DocumentViewer createViewer(AnnotatedPluginDocument[] annotatedDocuments) {
                        return new ExampleDocumentViewer((SequenceDocument) annotatedDocuments[0].getDocumentOrCrash()); // We use getDocumentOrCrash since it is guaranteed by the DocumentViewer API that the document is already loaded and will not fail.
                    }
                }
        };
    }
}
