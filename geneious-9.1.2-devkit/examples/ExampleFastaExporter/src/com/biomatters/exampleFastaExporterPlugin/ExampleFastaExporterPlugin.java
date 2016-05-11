package com.biomatters.exampleFastaExporterPlugin;

import com.biomatters.geneious.publicapi.plugin.*;

public class ExampleFastaExporterPlugin extends GeneiousPlugin {
    public String getName() {
        return "ExampleFastaExporterPlugin";
    }

    public String getHelp() {
        return "ExampleFastaExporterPlugin";
    }

    public String getDescription() {
        return "ExampleFastaExporterPlugin";
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
    public DocumentFileExporter[] getDocumentFileExporters() {
        return new DocumentFileExporter[]{new ExampleFastaExporter()};
    }
}