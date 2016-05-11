package com.biomatters.exampleSequenceGraph2Plugin;

import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.SequenceGraphFactory;

public class ExampleSequenceGraph2Plugin extends GeneiousPlugin {

    public String getName() {
        return "ExampleSequenceGraph2Plugin";
    }

    public String getHelp() {
        return "ExampleSequenceGraph2Plugin";
    }

    public String getDescription() {
        return "ExampleSequenceGraph2Plugin";
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

    public SequenceGraphFactory[] getSequenceGraphFactories(){
        return new SequenceGraphFactory[]{
                new ExampleAlignmentSequenceGraphFactory()
        };
    }
}
