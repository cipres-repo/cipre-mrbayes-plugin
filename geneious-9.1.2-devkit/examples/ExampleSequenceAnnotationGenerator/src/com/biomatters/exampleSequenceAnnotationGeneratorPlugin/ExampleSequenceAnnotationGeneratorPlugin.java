package com.biomatters.exampleSequenceAnnotationGeneratorPlugin;

import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.SequenceAnnotationGenerator;

/**
 * This plugin shows how to create a simple annotation generator plugin. This allows
 * the user to create annotations that appear in the sequence viewer.
 * <p/>
 * A few lines of code cause a menu entry to appear in the tools drop-down menu in the sequence viewer.
 * Upon selecting it a dialog populated with various options appears. Finally all the
 * code in this plugin has to do, is given a sequence return a list of annotations to be
 * added to the sequence and the Geneious framework handles the rest (such as saving
 * to disk, providing undo functionality, laying out the options GUI, remembering user
 * selected option values between invocations).
 * <p/>
 * This class just provides the framework to hook the {@link com.biomatters.exampleSequenceAnnotationGeneratorPlugin.ExampleSequenceAnnotationGenerator}
 * into Geneious. All of the real work happens in {@link com.biomatters.exampleSequenceAnnotationGeneratorPlugin.ExampleSequenceAnnotationGenerator}.
 */
public class ExampleSequenceAnnotationGeneratorPlugin extends GeneiousPlugin {
    public SequenceAnnotationGenerator[] getSequenceAnnotationGenerators() {
        return new SequenceAnnotationGenerator[]{
                new ExampleSequenceAnnotationGenerator()
        };
    }

    public String getName() {
        return "ExampleSequenceAnnotationGeneratorPlugin";
    }

    public String getHelp() {
        return "ExampleSequenceAnnotationGeneratorPlugin";
    }

    public String getDescription() {
        return "ExampleSequenceAnnotationGeneratorPlugin";
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
}
