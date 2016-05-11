package com.biomatters.exampleSequenceAnnotationGeneratorPlugin;

import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotation;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAnnotationInterval;
import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;

import jebl.util.ProgressListener;

import java.util.*;

/**
 * This plugin shows how to create a simple SequenceAnnotationGenerator by providing a simple implementation of a Motif finder.
 * 
 * @author Matt Kearse
 * @version $Id$
 */

public class ExampleSequenceAnnotationGenerator extends SequenceAnnotationGenerator {
    public GeneiousActionOptions getActionOptions() {
        return new GeneiousActionOptions("Example Find Motif",
                "Finds a non-ambiguous motif and creates annotations covering each occurrence").
                setMainMenuLocation(GeneiousActionOptions.MainMenu.AnnotateAndPredict);
    }

    public String getHelp() {
        return "This plugin shows how to create a simple SequenceAnnotationGenerator by providing a simple implementation of a Motif finder.";
    }

    public Options getOptions(AnnotatedPluginDocument[] documents, SelectionRange selectionRange) throws DocumentOperationException {
        return new ExampleSequenceAnnotationGeneratorOptions(); // Provides all the options we display to the user. See ExampleSequenceAnnotationGeneratorOptions below for details.
    }

    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[] {
                new DocumentSelectionSignature(NucleotideSequenceDocument.class,1,1)
                // This indicates this annotation generator will accept a single nucleotide sequence as input  
        };
    }

    public List<List<SequenceAnnotation>> generateAnnotations(AnnotatedPluginDocument[] annotatedPluginDocuments, SelectionRange selectionRange, ProgressListener progressListener, Options _options) throws DocumentOperationException {
        NucleotideSequenceDocument sequence= (NucleotideSequenceDocument) annotatedPluginDocuments[0].getDocument();
        // We can safely access the first element from the array since our selection signature specifies we accept 1 and only 1 document.
        // And we can safely cast it to a NucleotideSequenceDocument as the selection signature specified that too.

        ExampleSequenceAnnotationGeneratorOptions options = (ExampleSequenceAnnotationGeneratorOptions) _options;
        // We can safely cast this to ExampleSequenceAnnotationGeneratorOptions because that is all we ever return from getOptions()

        String sequenceString=sequence.getSequenceString().toUpperCase();

        List<SequenceAnnotation> results = new ArrayList<SequenceAnnotation>();
        String basesToFind=options.getBasesToFind();
        if (basesToFind.length()==0) {
            throw new DocumentOperationException("Must specify at least 1 base to find");
        }

        int indexToSearchFrom=0;
        int foundIndex;
        while((foundIndex=sequenceString.indexOf(basesToFind, indexToSearchFrom))>=0) {
            final SequenceAnnotationInterval interval = new SequenceAnnotationInterval(foundIndex + 1, foundIndex + basesToFind.length());
            SequenceAnnotation annotation=new SequenceAnnotation(options.getAnnotationName(),options.getAnnotationType(),interval);
            results.add(annotation);
            indexToSearchFrom=foundIndex+1;
        }

        if (options.isAlsoFindReverse()) {
            String reversedSequenceString = SequenceUtilities.reverseComplement(sequenceString).toString();
            indexToSearchFrom = 0;
            while((foundIndex=reversedSequenceString.indexOf(basesToFind, indexToSearchFrom))>=0) {
                final SequenceAnnotationInterval interval = new SequenceAnnotationInterval(sequenceString.length()-foundIndex, sequenceString.length()-foundIndex - basesToFind.length()+1);
                SequenceAnnotation annotation=new SequenceAnnotation(options.getAnnotationName(),options.getAnnotationType(),interval);
                results.add(annotation);
                indexToSearchFrom=foundIndex+1;
            }
        }

        return Arrays.asList(results); // Put the results in a single element array since we only operate on a single sequence hence there is only 1 set of results.
    }

    private static class ExampleSequenceAnnotationGeneratorOptions extends Options {
        private final BooleanOption alsoFindReverse;
        private final StringOption basesToFind;
        private final StringOption annotationType;
        private final StringOption annotationName;
        private ExampleSequenceAnnotationGeneratorOptions() {
            basesToFind = addStringOption("basesToFind","Bases to find","");
            alsoFindReverse = addBooleanOption("alsoFindReverse", "Also find on reverse complement", true);

            annotationType = addStringOption("annotationType","Annotation type", SequenceAnnotation.TYPE_MOTIF);
            annotationType.setAdvanced(true);
            annotationType.setDescription("The type of annotations that will be created on each matching occurrence");

            annotationName = addStringOption("annotationName","Annotation name", "");
            annotationName.setAdvanced(true);
            annotationName.setDescription("The name of the annotations that will be created on each matching occurrence");
        }

        public boolean isAlsoFindReverse() {
            return alsoFindReverse.getValue();
        }

        public String getBasesToFind() {
            return basesToFind.getValue().toUpperCase();
        }

        public String getAnnotationType() {
            return annotationType.getValue();
        }

        public String getAnnotationName() {
            return annotationName.getValue();
        }

    }
}
