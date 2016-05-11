package com.biomatters.exampleAssemblerPlugin;

import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * @author Matt Kearse
 * @version $Id$
 */
class ExampleAssemblerOptions extends Options {
    private final BooleanOption onlyAssembleHalf;

    public ExampleAssemblerOptions() {
        onlyAssembleHalf = addBooleanOption("onlyAssembleHalf", "Only assemble half of the reads", false);
    }

    public boolean isOnlyAssembleHalfOfTheReads() {
        return onlyAssembleHalf.getValue();
    }
}
