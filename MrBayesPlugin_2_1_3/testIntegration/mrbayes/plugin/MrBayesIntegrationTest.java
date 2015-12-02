package mrbayes.plugin;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.DefaultAlignmentDocument;
import com.biomatters.geneious.publicapi.implementations.DefaultPhylogenyDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultAminoAcidSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.plugin.TestGeneious;
import jebl.evolution.io.NewickExporter;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.Utils;
import jebl.util.Attributable;
import jebl.util.ProgressListener;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * @author amy
 * Created on 5/06/13 10:30 AM
 */
public class MrBayesIntegrationTest extends TestCase {
    public void testPerformOperation_nucleotideAlignment_unconstrained() throws DocumentOperationException {
        runMrBayes(true, true, false);
    }

    public void testPerformOperation_nucleotideAlignment_molecularClock() throws DocumentOperationException {
        runMrBayes(true, false, false);
    }

    public void testPerformOperation_proteinAlignment_unconstrained() throws DocumentOperationException {
        runMrBayes(false, true, false);
    }

    public void testPerformOperation_proteinAlignment_molecularClock() throws DocumentOperationException {
        runMrBayes(false, false, false);
    }

    public void testPerformOperation_nucleotideAlignment_unconstrainedCustom() throws DocumentOperationException {
        runMrBayes(true, true, true);
    }

    public void testPerformOperation_nucleotideAlignment_molecularClockCustom() throws DocumentOperationException {
        runMrBayes(true, false, true);
    }

    public void testPerformOperation_proteinAlignment_unconstrainedCustom() throws DocumentOperationException {
        runMrBayes(false, true, true);
    }

    public void testPerformOperation_proteinAlignment_molecularClockCustom() throws DocumentOperationException {
        runMrBayes(false, false, true);
    }

    private static void runMrBayes(boolean nucleotide, boolean unconstrained, boolean useCustomBlock) throws DocumentOperationException {
        SequenceAlignmentDocument alignment = nucleotide? nucleotideAlignment(): proteinAlignment();
        TestGeneious.initialize();
        MrBayesDocumentOperation op = new MrBayesDocumentOperation(false);
        Options options = op.getOptions(DocumentUtilities.createAnnotatedPluginDocuments(alignment));
        adjustOptions(options, unconstrained, useCustomBlock);
        List<AnnotatedPluginDocument> results = op.performOperation(DocumentUtilities.createAnnotatedPluginDocuments(alignment), ProgressListener.EMPTY, options);
        assertEquals(3, results.size());  // posterior probability, raw trees and sorted topology
        for (AnnotatedPluginDocument result: results) {
            DefaultPhylogenyDocument treeDocument = (DefaultPhylogenyDocument) result.getDocument();
            boolean isPosteriorOutput = treeDocument instanceof MrBayesPosteriorDocument;
            for (Tree tree: treeDocument.getTrees()) {
                assertEquals(!unconstrained || treeDocument.getName().contains(MrBayesOutputParser.SORTED_TOPOLOGIES_SUFFIX), Utils.isRooted(tree));
                if (isPosteriorOutput) {
                    checkForPosteriorProbability(tree.getInternalNodes(), true);
                    checkForPosteriorProbability(tree.getExternalNodes(), false);
                    StringWriter writer = new StringWriter();
                    NewickExporter exp = new NewickExporter(writer);
                    try {
                        exp.exportTree(tree);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void checkForPosteriorProbability(Set<? extends Attributable> attributables, boolean shouldBeThere) {
        boolean hasPosterior = false;
        for (Attributable attributable: attributables) {
            if (attributable.getAttributeMap().containsKey("Posterior Probability")) {
                hasPosterior = true;
                break;
            }
        }
        assertEquals("If this is failing unexpectedly it might be because it produced a tree that had only the root and tips, no other internal nodes. " +
                        "Posterior values are only shown on internal nodes other than root.", shouldBeThere, hasPosterior);
    }

    private static void adjustOptions(Options _options, boolean unconstrained, boolean useCustomBlock) {
        MrBayesOptions options = (MrBayesOptions)_options.getChildOptions().get("programOptions");
        options.setRandomSeed(1);
        options.setChainlength(1000);
        options.setBurnIn(100);
        options.setSubsamplingFrequency(10);
        options.setUnconstrained(unconstrained);
        options.setUseCustomCommandBlock(useCustomBlock);
    }

    private static DefaultAlignmentDocument nucleotideAlignment() {
        List<CharSequence> sequences = new ArrayList<CharSequence>();
        sequences.add("TTCTTTCCTAGG---GAAGCAGA-TTTGGGTACCTTGACTCA--");
        sequences.add("TTCTTTCATGGG---GAAGCAGA--TTGGGTACC---ACCCAAG");
        sequences.add("TTCTTTCATGGGCACGAAGCAGA-TTTGGGTACC---ACCCAAG");
        sequences.add("TTCTTTCATGGG------GAAGA-TTTGGGTACC---ACCCAAG");
        sequences.add("TTCTTTCATGGGGAACAGGCAGATTTTGGGTACC---ACCCAAG");
        sequences.add("TTCTTTCATGGGGAACAGGCAGCTTTTGGGTACC---ACCCAAG");
        sequences.add("TTCTTTCATGGGGAACAGGCACCTTTTGGGTACC---ACCCAAG");
        sequences.add("TTCTTTCATGGGGAACAGGCACTTTTTGGGTACC---ACCCAAG");
        return getAlignment(sequences, SequenceDocument.Alphabet.NUCLEOTIDE);
    }

    private static DefaultAlignmentDocument proteinAlignment() {
        List<CharSequence> sequences = new ArrayList<CharSequence>();
        sequences.add("FFPR-EADLGTLT");
        sequences.add("FFHG-EADWVPPK");
        sequences.add("FFHGHEADLGTTQ");
        sequences.add("FFHGE--DLGTTQ");
        sequences.add("FFHGEQADFGYHP");
        sequences.add("FFHGEQADFGTHP");
        sequences.add("FFHGEQADLGTHP");
        return getAlignment(sequences, SequenceDocument.Alphabet.PROTEIN);
    }

    private static DefaultAlignmentDocument getAlignment(List<CharSequence> sequences, SequenceDocument.Alphabet alphabet) {
        SequenceDocument[] sequenceDocuments = new SequenceDocument[sequences.size()];
        for (int i = 0; i < sequences.size(); ++i) {
            String name = "seq" + i;
            CharSequence sequence = sequences.get(i);
            sequenceDocuments[i] = alphabet == SequenceDocument.Alphabet.NUCLEOTIDE? new DefaultNucleotideSequence(name, sequence): new DefaultAminoAcidSequence(name, sequence);
        }
        return new DefaultAlignmentDocument("test", sequenceDocuments);
    }

}
