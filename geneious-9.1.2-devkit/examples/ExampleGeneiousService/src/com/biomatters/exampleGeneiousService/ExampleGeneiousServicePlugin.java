package com.biomatters.exampleGeneiousService;

import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.GeneiousService;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class ExampleGeneiousServicePlugin extends GeneiousPlugin {
    public String getName() {
        return "ExampleGeneiousServicePlugin";
    }

    public String getHelp() {
        return "ExampleGeneiousServicePlugin";
    }

    public String getDescription() {
        return "ExampleGeneiousServicePlugin";
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

    public GeneiousService[] getServices() {
        URL resource = ExampleGeneiousService.class.getResource("/sampledb.fasta");
        if(resource == null) {
            throw new IllegalStateException("Could not find sampledb.fasta");
        }

        File fastaFile;
        try {
            fastaFile = new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Could not load sampledb.fasta: " + e.getMessage(), e);
        }

        if(!fastaFile.exists()) {
            throw new IllegalStateException("sampledb.fasta is missing");
        }
        return new GeneiousService[]{
                new ExampleGeneiousService(fastaFile)
        };
    }
}
