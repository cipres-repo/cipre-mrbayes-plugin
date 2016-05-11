package com.biomatters.exampleFastaImporterPlugin;

import com.biomatters.geneious.publicapi.plugin.*;

public class ExampleFastaImporterPlugin extends GeneiousPlugin {
    public String getName() {
        return "ExampleFastaImporterPlugin";
    }

    public String getHelp() {
        return "ExampleFastaImporterPlugin";
    }

    public String getDescription() {
        return "ExampleFastaImporterPlugin";
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

    @Override
    public DocumentFileImporter[] getDocumentFileImporters() {
        return new DocumentFileImporter[]{new ExampleFastaImporter()};
    }
}