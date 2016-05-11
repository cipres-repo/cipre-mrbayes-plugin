package com.biomatters.reversesequence;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.AminoAcidSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultAminoAcidSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.*;
import jebl.util.ProgressListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Joseph Heled
 * @version $Id: ReverseSeqPlugin.java 19210 2008-05-02 05:20:17Z amy $
 */
public class ReverseSeqPlugin extends GeneiousPlugin {
    
    public String getName() {
        return "Reverse Sequence";
    }

    public String getDescription() {
        return "Reverse direction of one sequence";
    }

    public String getHelp() {
        return null;
    }

    public String getAuthors() {
        return "Biomatters";
    }

    public String getVersion() {
        return "0.1";
    }

    public DocumentOperation[] getDocumentOperations() {
      return new DocumentOperation[]{reverseSequence};
    }

    private DocumentOperation reverseSequence = new DocumentOperation() {
        @Override
        public List<AnnotatedPluginDocument> performOperation(AnnotatedPluginDocument[] documents, ProgressListener progressListener, Options options) throws DocumentOperationException {
            final AnnotatedPluginDocument doc = documents[0];

            final SequenceDocument sequenceDoc = (SequenceDocument) doc.getDocument();
            String seq = sequenceDoc.getSequenceString();

            StringBuilder sb = new StringBuilder(seq);

            String reversed = sb.reverse().toString();

            final String name = doc.getName();
            String rname = name != null ? "(reversed) " + name : null;
            final String description = sequenceDoc.getDescription();

            String rdescription = description != null ? "(reversed) " + description : null;
            final Date date = new Date();

            PluginDocument reversedDoc;

            if( sequenceDoc instanceof NucleotideSequenceDocument) {
               reversedDoc = new DefaultNucleotideSequence(rname, rdescription, reversed, date);
            } else if( sequenceDoc instanceof AminoAcidSequenceDocument) {
                reversedDoc = new DefaultAminoAcidSequence(rname, rdescription, reversed, date);
            } else {
                throw new DocumentOperationException("unexpected sequence");
            }
            List<PluginDocument> results = new ArrayList<PluginDocument>();
            results.add(reversedDoc);
            return DocumentUtilities.createAnnotatedPluginDocuments(results);
        }

        public GeneiousActionOptions getActionOptions() {
            return new GeneiousActionOptions("Reverse Sequence", "Reverse the selected sequence",
                    null, GeneiousActionOptions.Category.None).setInMainToolbar(true);
        }

        public String getHelp() {
            return "Reverses one sequence.";
        }

        public DocumentSelectionSignature[] getSelectionSignatures() {
            DocumentSelectionSignature singleSequenceSignature =
                    new DocumentSelectionSignature(SequenceDocument.class, 1, 1);

            return new DocumentSelectionSignature[]{singleSequenceSignature};
        }

        @Override
        public Options getOptions(final AnnotatedPluginDocument... documents) {
            return null;
        }
    };

    public String getMinimumApiVersion() {
        return "4.0";
    }

    public int getMaximumApiVersion() {
        return 4;
    }
}
