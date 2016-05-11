package com.biomatters.exampleAlignmentOperationPlugin;

import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.SequenceAnnotationGenerator;
import com.biomatters.geneious.publicapi.plugin.AlignmentOperation;

/**
 * This plugin shows how to create a simple alignment operation plugin. This allows
 * the user to create an alignment document out of sequences.
 * <p/>
 * All the plugin has to do, is given a list of sequences return a list of aligned sequences to be
 * appear in the alignment and the Geneious framework handles the rest (such as:
 * <ul>;
 * <li>The type of documents that have been selected (sequence documents, sequence list documents, or alignment documents)</li>;
 * <li>Reordering and reversing of the sequences</li>;
 * <li>Whether the alignment is local or global</li>;
 * <li>Referenced documents</li>;
 * <li>Bases/residues unsupported by the underlying algorithm</li>;
 * </ul>;
 * <p/>
 * This class just provides the framework to hook the {@link ExampleAlignmentOperation}
 * into Geneious. All of the real work happens in {@link ExampleAlignmentOperation}.
 */
public class ExampleAlignmentOperationPlugin extends GeneiousPlugin {
    @Override
    public AlignmentOperation[] getAlignmentOperations() {
        return new AlignmentOperation[] {new ExampleAlignmentOperation() };
    }

    public String getName() {
        return "ExampleAlignmentOperationPlugin";
    }

    public String getHelp() {
        return "ExampleAlignmentOperationPlugin";
    }

    public String getDescription() {
        return "ExampleAlignmentOperationPlugin";
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
