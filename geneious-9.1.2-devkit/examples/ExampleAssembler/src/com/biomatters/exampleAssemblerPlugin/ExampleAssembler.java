package com.biomatters.exampleAssemblerPlugin;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.DefaultAlignmentDocument;
import com.biomatters.geneious.publicapi.implementations.PairedReadManager;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;

import java.util.*;

import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

/**
 * This plugin shows how to create a simple reference sequence Assembler by providing an implementation that just
 * maps reads to a random position.
 * 
 * @author Matt Kearse
 * @version $Id$
 */

public class ExampleAssembler extends Assembler {
    @Override
    public String getUniqueId() {
        return "ExampleAssembler";
    }

    @Override
    public String getName() {
        return "Example Read Mapper";
    }

    @Override
    public ReferenceSequenceSupport getReferenceSequenceSupport() {
        return ReferenceSequenceSupport.SingleReferenceSequence;
    }

    @Override
    public ContigOutputSupport getContigOutputSupport() {
        return ContigOutputSupport.ContigsOnly;
    }

    @Override
    public boolean providesUnusedReads() {
        return true;
    }

    @Override
    public Options getOptions(OperationLocationOptions locationOptions, AssemblerInput.Properties inputProperties) {
        return new ExampleAssemblerOptions();
    }

    @Override
    public void assemble(Options _options, AssemblerInput assemblyInput, ProgressListener progressListener, Callback callback) throws DocumentOperationException {
        ExampleAssemblerOptions options = (ExampleAssemblerOptions) _options;
        boolean onlyAssembleHalf = options.isOnlyAssembleHalfOfTheReads();

        CompositeProgressListener compositeProgressListener = new CompositeProgressListener(progressListener, 3);

        compositeProgressListener.beginSubtask();
        final SequenceDocument referenceSequence = assemblyInput.getReferenceSequence(0, compositeProgressListener);

        final AssemblerInput.Reads reads = assemblyInput.getReads();
        List<SequenceDocument> gappedResultSequences = new ArrayList<SequenceDocument>();
        gappedResultSequences.add(referenceSequence); // We are doing ungapped assembly of reads that always lie entirely in the reference sequence in this example, hence we don't need to add any gaps here.

        int referenceSequenceLength = referenceSequence.getSequenceLength();
        final long numberOfReads = assemblyInput.getNumberOfReadSequences();
        long numberOfReadsDone = 0;
        long numberOfReadPairsDone = 0;
        PairedReadManager pairedReadManager = new PairedReadManager(1);
        Random r = new Random(0);

        compositeProgressListener.beginSubtask();
        while(reads.hasNext()) {
            if (compositeProgressListener.setProgress(numberOfReadsDone, numberOfReads))
                throw new DocumentOperationException.Canceled();
            final AssemblerInput.Read readPair = reads.getNextReadPair();
            final NucleotideSequenceDocument sequence1 = readPair.getReadNormalized();
            final NucleotideSequenceDocument sequence2 = readPair.getMateNormalized();
            if (onlyAssembleHalf && numberOfReadPairsDone%2==0) {
                callback.addUnusedRead(readPair, ProgressListener.EMPTY);
            }
            else if (sequence2==null || (onlyAssembleHalf && numberOfReadPairsDone%4==1)) {
                // An unpaired read, or we decide to map just the first read of the pair:
                int endGaps = referenceSequenceLength - sequence1.getSequenceLength();
                if (endGaps<0) throw new DocumentOperationException("We don't support assembling reads to reference sequences that are shorter than the reference sequence length");
                int leadingGaps = r.nextInt(endGaps + 1);
                int trailingGaps = endGaps - leadingGaps;
                gappedResultSequences.add(createGappedSequence(sequence1, leadingGaps, trailingGaps));
                pairedReadManager.addSequence();
                if (sequence2!=null) {
                    callback.addUnusedRead(new AssemblerInput.Read(sequence2, readPair.getMateReferencedDocumentNormalized()), ProgressListener.EMPTY);
                }
            }
            else {
                // A paired read where we map both reads in the pair to the reference sequence:
                final int expectedDistance = readPair.getExpectedMateDistanceNormalized();
                if (sequence1.getSequenceLength()>expectedDistance || sequence2.getSequenceLength()>expectedDistance)
                    throw new DocumentOperationException("We don't support assembling reads which have lengths longer than their expected distance");
                int endGaps = referenceSequenceLength - expectedDistance;
                if (endGaps<0) throw new DocumentOperationException("We don't support assembling reads to reference sequences that are shorter than the reference sequence length");
                int leadingGaps = r.nextInt(endGaps + 1);
                int trailingGaps = endGaps - leadingGaps;
                pairedReadManager.addSequence();
                pairedReadManager.addSequence();
                pairedReadManager.setMates(gappedResultSequences.size(),gappedResultSequences.size()+1,expectedDistance);
                gappedResultSequences.add(createGappedSequence(sequence1, leadingGaps, referenceSequenceLength - sequence1.getSequenceLength() - leadingGaps));
                gappedResultSequences.add(createGappedSequence(sequence2, referenceSequenceLength - sequence2.getSequenceLength() - trailingGaps, trailingGaps));
            }
            numberOfReadPairsDone++;
            numberOfReadsDone++;
            if (sequence2!=null)
                numberOfReadsDone++;
        }

        DefaultAlignmentDocument contig = createContig(referenceSequence.getName()+" contig", gappedResultSequences, pairedReadManager);
        compositeProgressListener.beginSubtask();
        callback.addContigDocument(contig, null, false, compositeProgressListener);
    }

    private DefaultAlignmentDocument createContig(String contigName, List<SequenceDocument> gappedSequences, PairedReadManager pairedReadManager) {
        SequenceDocument[] resultSequencesArray = gappedSequences.toArray(new SequenceDocument[gappedSequences.size()]);
        DefaultAlignmentDocument result = new DefaultAlignmentDocument(resultSequencesArray,null,null,contigName);
        result.setContig(true);
        result.setContigReferenceSequenceIndex(0);
        for (int i = 0; i < resultSequencesArray.length; i++) {
            if (pairedReadManager.getMateIndex(i)>=0) {
                result.setMates(i, pairedReadManager.getMateIndex(i),pairedReadManager.getMateExpectedDistance(i));
            }
        }
        return result;
    }

    private static SequenceDocument createGappedSequence(SequenceDocument ungappedSequence, int leadingGaps, int trailingGaps) {
        SequenceCharSequence gappedSequence = SequenceCharSequence.withTerminalGaps(leadingGaps, ungappedSequence.getCharSequence(), trailingGaps);
        return SequenceUtilities.createSequenceCopyAdjustedForGapInsertion(ungappedSequence, gappedSequence);
    }
}
