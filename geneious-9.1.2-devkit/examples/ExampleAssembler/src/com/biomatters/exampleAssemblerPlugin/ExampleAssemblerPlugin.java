package com.biomatters.exampleAssemblerPlugin;

import com.biomatters.geneious.publicapi.plugin.Assembler;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;

/**
 * This plugin shows how to create a simple assembler plugin.
 * <p/>
 * This class just provides the framework to hook the {@link ExampleAssembler}
 * into Geneious. All of the real work happens in {@link com.biomatters.exampleAssemblerPlugin.ExampleAssembler}
 */
public class ExampleAssemblerPlugin extends GeneiousPlugin {
    @Override
    public Assembler[] getAssemblers() {
        return new Assembler[]{
                new ExampleAssembler()
        };
    }

    public String getName() {
        return "ExampleAssemblerPlugin";
    }

    public String getHelp() {
        return "ExampleAssemblerPlugin";
    }

    public String getDescription() {
        return "ExampleAssemblerPlugin";
    }

    public String getAuthors() {
        return "Biomatters";
    }

    public String getVersion() {
        return "0.1";
    }

    public String getMinimumApiVersion() {
        return "4.11";
    }

    public int getMaximumApiVersion() {
        return 4;
    }
}
