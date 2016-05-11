package com.biomatters.exampleSequenceGraph2Plugin;

import com.biomatters.geneious.publicapi.documents.sequence.NucleotideGraph;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.GeneiousGraphics2D;
import com.biomatters.geneious.publicapi.plugin.SequenceGraph;

import java.awt.*;
import java.util.List;

/**
 * This is an example graph where the height of the graph for each column in the
 * alignment is proportional to the number of occurrences of the most common residue
 * in that column.
 * 
 * @author Matt Kearse
 * @version $Id$
 */
class ExampleAlignmentSequenceGraph extends SequenceGraph {
    private List<CharSequence> sequences;
    private final SequenceDocument.Alphabet alphabet;

    public ExampleAlignmentSequenceGraph(SequenceDocument.Alphabet alphabet) {
        this.alphabet = alphabet;
    }

    public String getName() {
        return "Example Alignment Sequence Graph";
    }

    @Override
    public void setResidues(List<CharSequence> sequences, List<NucleotideGraph> nucleotideGraphs, boolean ignoreEndGaps) {
        this.sequences = sequences;
    }

    public void draw(GeneiousGraphics2D graphics, int startResidue, int endResidue, int startX, int startY, int endX, int endY, double averageResidueWidth, int previousSectionWidth, int nextSectionWidth, int previousSectionResidueCount, int nextSectionResidueCount) {
        final int numberOfSequences = sequences.size();
        int totalValue = 0;
        for(int residueIndex = startResidue; residueIndex<=endResidue;residueIndex++) {
            totalValue+= getNumberOfOccurancesOfMostCommonCharacterInColumn(residueIndex);
        }
        int averageValue = totalValue/(endResidue-startResidue+1);
        double heightFraction = ((double)averageValue)/ numberOfSequences;
        int topY = (int) (endY - (endY-startY+1)*heightFraction);
        Color color;
        if (heightFraction>0.99) {
            color=Color.GREEN;
        }
        else if (heightFraction>=0.5) {
            color=Color.ORANGE;
        }
        else {
            color=Color.RED;
        }
        graphics.setColor(color);
        graphics.fillRect(startX,topY,endX,endY);
    }

    private int getNumberOfOccurancesOfMostCommonCharacterInColumn(int residueIndex) {
        final int maxCharacter = 128;
        int mostCommonCharacterCount = 0;
        int[] characterCounts = new int[maxCharacter];
        for (CharSequence sequence : sequences) {
            char c = Character.toUpperCase(sequence.charAt(residueIndex));
            if (alphabet==SequenceDocument.Alphabet.NUCLEOTIDE && c=='U')
                c='T';
            if (c>=0 && c<maxCharacter) {
                characterCounts[c]++;
                mostCommonCharacterCount = Math.max(mostCommonCharacterCount, characterCounts[c]);
            }
        }
        return mostCommonCharacterCount;
    }

    @Override
    public Location getDefaultLocation() {
        return Location.ABOVE_RESIDUES;
    }
}
