package com.biomatters.exampleSequenceViewerStatisticsPlugin;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.DefaultSequenceGraphFactories;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.SequenceGraphFactory;
import com.biomatters.geneious.publicapi.plugin.SequenceSelection;
import com.biomatters.geneious.publicapi.plugin.SequenceViewerExtension;
import com.biomatters.geneious.publicapi.utilities.Interval;
import com.biomatters.geneious.publicapi.utilities.StringUtilities;
import jebl.util.ProgressListener;

import java.awt.*;
import java.util.*;
import java.util.List;

public class ExampleSequenceViewerStatisticsPlugin extends GeneiousPlugin {

    public String getName() {
        return "ExampleSequenceViewerStatisticsPlugin";
    }

    public String getHelp() {
        return "ExampleSequenceViewerStatisticsPlugin";
    }

    public String getDescription() {
        return "ExampleSequenceViewerStatisticsPlugin";
    }

    public String getAuthors() {
        return "Biomatters";
    }

    public String getVersion() {
        return "0.1";
    }

    public String getMinimumApiVersion() {
        return "4.700";
    }

    public int getMaximumApiVersion() {
        return 4;
    }

    @Override
    public SequenceViewerExtension.Factory[] getSequenceViewerExtensionFactories() {
        return new SequenceViewerExtension.Factory[] {
                new SequenceViewerExtension.StatisticsFactory() {
                    @Override
                    public List<SequenceViewerExtension.StatisticsSection> createStatistics(SequenceViewerExtension.PropertyRetrieverAndEditor propertyRetriever, ProgressListener progressListener) {
                        // This counts the number of 'A's in the selected region(s)
                        int frequencyOfA = 0;
                        for (SequenceSelection.SelectionInterval selectionInterval : propertyRetriever.getSelectionForStatistics().getIntervals(false)) {
                            for (Integer sequenceIndex : selectionInterval.getSequencesRange()) {
                                if (progressListener.isCanceled())
                                    return Collections.emptyList();
                                SequenceCharSequence charSequence = propertyRetriever.getSequenceCharSequence(sequenceIndex);

                                Interval clippedRange = selectionInterval.getResidueInterval().clipToRange(charSequence.getLeadingGapsLength(), charSequence.getTrailingGapsStartIndex());
                                // We clip the residue range to the the leading/trailing gaps length to improve performance. Otherwise on large contigs where only a tiny fraction of the length is non-gaps
                                // we would spend most of the time iterating over gaps.

                                for (Integer residueIndex : clippedRange) {
                                    char c = charSequence.charAt(residueIndex);
                                    if (c=='A' || c=='a')
                                        frequencyOfA++;
                                }
                            }
                        }
                        String text = "#A: "+ StringUtilities.commaFormat(frequencyOfA);
                        double verticalPosition = SequenceViewerExtension.StatisticsSection.POSITION_SEQUENCE_LENGTH + SequenceViewerExtension.StatisticsSection.RELATIVE_POSITION_DIRECTLY_BELOW; // Make it appear just below the sequence length statistic
                        SequenceViewerExtension.StatisticsSection section = new SequenceViewerExtension.StatisticsSection("frequencyOfA", frequencyOfA, text, verticalPosition);
                        return Collections.singletonList(section);
                    }
                }
        };
    }
}
