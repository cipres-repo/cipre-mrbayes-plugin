package com.biomatters.exampleSequenceGraphPlugin;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.DefaultSequenceGraphFactories;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.SequenceGraphFactory;

import java.awt.*;

public class ExampleSequenceGraphPlugin extends GeneiousPlugin {

    public String getName() {
        return "ExampleSequenceGraphPlugin";
    }

    public String getHelp() {
        return "ExampleSequenceGraphPlugin";
    }

    public String getDescription() {
        return "ExampleSequenceGraphPlugin";
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

    public SequenceGraphFactory[] getSequenceGraphFactories(){
        // This implementation just provides some very simple implementations. By not using DefaultSequenceGraphFactories
        // and instead implementing SequenceGraphFactory yourself you can create more advanced types of graphs.
        
        //the scorer allows you to assign a score for each residue, and also assign a color to each score.
        DefaultSequenceGraphFactories.SingleSequenceScorer scorer = new DefaultSequenceGraphFactories.SingleSequenceScorer(){
            public Double getScore(char residue) {
                return Math.random();
            }

            public Color getColor(double score) {
                return Color.black;
            }
        };

        return new SequenceGraphFactory[]{
            DefaultSequenceGraphFactories.createSingleSequenceGraphFactory("Nucleotide Ludicrousity", "What a ludicrous graph!", true, 25, SequenceDocument.Alphabet.NUCLEOTIDE, scorer, false), //a bar graph
            DefaultSequenceGraphFactories.createSingleSequenceGraphFactory("Protein Ludicrousity", "What a ludicrous graph!", true, 25, SequenceDocument.Alphabet.PROTEIN, scorer, true) //a line graph
        };
    }

}
