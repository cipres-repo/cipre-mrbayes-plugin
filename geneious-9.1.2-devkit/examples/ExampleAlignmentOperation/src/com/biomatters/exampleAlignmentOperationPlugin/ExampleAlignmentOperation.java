package com.biomatters.exampleAlignmentOperationPlugin;

import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;

import java.util.*;

import jebl.util.ProgressListener;

/**
 * This plugin shows how to create a simple AlignmentOperation by providing an implementation that just pads the alignment
 * to make all sequences the same length
 * 
 * @author Matt Kearse
 * @version $Id$
 */

public class ExampleAlignmentOperation extends AlignmentOperation {
    public String getHelp() {
        return "This plugin shows how to create a simple AlignmentOperation by providing a simple implementation that just pads the sequences with gaps to ensure they are all the same length.";
    }

    public String getName() {
        return "Padding";
    }

    public double getTabPosition() {
        return 0.5;
    }

    public boolean supportsAlphabet(SequenceDocument.Alphabet alphabet) {
        return true;
    }

    public boolean isProOnly() {
        return false;
    }

    public Options getOptions(SequenceDocument.Alphabet alphabet, boolean pairwise, boolean isSingleAlignment) throws DocumentOperationException {
        return new ExampleAlignmentOptions();
    }

    public List<CharSequence> align(SequenceDocument.Alphabet alphabet, List<CharSequence> sequences, Options options,
                                             ProgressListener progressListener) throws DocumentOperationException {
        int maxLength = getMaxLength(sequences);
        boolean padAtStart = ((ExampleAlignmentOptions)options).padAtStart();
        return getPaddedSequences(sequences, padAtStart, maxLength);
    }

    private static List<CharSequence> getPaddedSequences(List<CharSequence> sequences, boolean padAtStart, int maxLength) {
        List<CharSequence> results = new ArrayList<CharSequence>();
        for (CharSequence sequence: sequences) {
            results.add(getPaddedSequence(maxLength, padAtStart, sequence));
        }
        return results;
    }

    private static SequenceCharSequence getPaddedSequence(int maxLength, boolean padAtStart, CharSequence sequence) {
        int padding = maxLength - sequence.length();
        int gapsAtStart = padAtStart? padding: 0;
        int gapsAtEnd = padAtStart? 0: padding;
        return SequenceCharSequence.withTerminalGaps(gapsAtStart, sequence, gapsAtEnd);
    }

    private static int getMaxLength(List<CharSequence> sequences) {
        int maxLength = 0;
        for (CharSequence sequence: sequences) {
            maxLength = Math.max(sequence.length(), maxLength);
        }
        return maxLength;
    }

    public String getAlignmentOptionsDescription(List<CharSequence> alignedSequences, Options options) throws DocumentOperationException {
        return "Aligned using Padding Aligner";
    }

    public String getSupportedCharacters(SequenceDocument.Alphabet alphabet) {
        return null;
    }

    private static class ExampleAlignmentOptions extends Options {
        private final RadioOption<OptionValue> startOrEnd;
        private static final OptionValue[] startOrEndOptions = new OptionValue[] { new OptionValue("start", "Start"), new OptionValue("end", "End")};
        private ExampleAlignmentOptions() {
            startOrEnd = addRadioOption("startOrEnd", "Pad sequences at: ", startOrEndOptions, startOrEndOptions[0], Alignment.HORIZONTAL_ALIGN);
        }
        boolean padAtStart() {
            return startOrEnd.getValue() == startOrEndOptions[0];
        }
    }
}
