package com.biomatters.exampleSequenceGraph2Plugin;

import com.biomatters.geneious.publicapi.plugin.SequenceGraphFactory;
import com.biomatters.geneious.publicapi.plugin.SequenceGraph;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;

/**
 * @author Matt Kearse
 * @version $Id$
 */
class ExampleAlignmentSequenceGraphFactory extends SequenceGraphFactory {
    public SequenceGraph createResidueBasedGraph(SequenceDocument.Alphabet alphabet, boolean isAlignment, boolean isChromatogram, boolean isContig) {
        if (!isAlignment || isContig) {
            return null; // This graph only appears on alignments (not unaligned sequences and not contigs)
        }
        return new ExampleAlignmentSequenceGraph(alphabet);
    }
}
