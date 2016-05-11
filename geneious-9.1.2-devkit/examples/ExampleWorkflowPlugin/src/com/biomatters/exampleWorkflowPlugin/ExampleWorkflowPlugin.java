package com.biomatters.exampleWorkflowPlugin;

import com.biomatters.geneious.publicapi.plugin.*;

public class ExampleWorkflowPlugin extends GeneiousPlugin {
    public String getName() {
        return "ExampleWorkflowPlugin";
    }

    public String getHelp() {
        return "ExampleWorkflowPlugin";
    }

    public String getDescription() {
        return "ExampleWorkflowPlugin";
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
    public DocumentOperation[] getDocumentOperations() {
        return new DocumentOperation[]{new ExampleWorkflow()};
    }
}