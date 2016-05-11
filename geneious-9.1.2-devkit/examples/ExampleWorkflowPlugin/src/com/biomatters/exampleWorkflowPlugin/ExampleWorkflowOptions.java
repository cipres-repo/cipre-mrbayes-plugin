package com.biomatters.exampleWorkflowPlugin;

import com.biomatters.geneious.publicapi.plugin.Options;

public class ExampleWorkflowOptions extends Options {
    private StringOption searchQuery;

    public ExampleWorkflowOptions() {
        searchQuery = addStringOption("searchQuery","Search For","small and piglet and not bacterium");
    }

    public String getSearchQuery() {
        return searchQuery.getValue();
    }
}
