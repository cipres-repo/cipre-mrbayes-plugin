package com.biomatters.exampleFastaImporterPlugin;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.DocumentFileImporter;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import com.biomatters.geneious.publicapi.utilities.ProgressInputStream;
import jebl.util.ProgressListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A simple example of a fasta importer that always imports its sequences as unaligned nucleotide sequences and
 * doesn't elegantly handle files containing invalid data. The actual
 * fasta importer used by Geneious is more robust and also guesses the sequence type and whether the data is an alignment
 * or stand-alone sequences.
 */
public class ExampleFastaImporter extends DocumentFileImporter {
    public String[] getPermissibleExtensions() {
        return new String[]{".fasta",".fa"};
    }

    public String getFileTypeDescription() {
        return "Example Fasta Importer";
    }

    public AutoDetectStatus tentativeAutoDetect(File file, String fileContentsStart) {
        if (fileContentsStart.startsWith(">")) {
            return AutoDetectStatus.MAYBE;
        }
        else {
            return AutoDetectStatus.REJECT_FILE;
        }
    }

    public void importDocuments(File file, ImportCallback callback, ProgressListener progressListener) throws IOException, DocumentImportException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ProgressInputStream(progressListener, file)));
        try {
            String line;
            StringBuilder currentSequence = new StringBuilder();
            String currentName ="";
            int numberImportedSoFar=0;
            while ((line = reader.readLine()) != null) {
                if (line.length() == 0) {
                    continue;
                }
                if (line.startsWith(">")) {
                    if (currentSequence.length() > 0) {
                        final SequenceDocument seq = new DefaultNucleotideSequence(currentName, currentSequence.toString());
                        callback.addDocument(seq);
                        numberImportedSoFar++;
                        if (numberImportedSoFar%1000==0) {
                            progressListener.setMessage(String.format("Imported %,d",numberImportedSoFar));
                        }
                        currentSequence = new StringBuilder();
                    }
                    currentName = line.substring(1);
                } else {
                    currentSequence.append(line);
                }
            }
            if (currentSequence.length() > 0) {
                final SequenceDocument seq = new DefaultNucleotideSequence(currentName, currentSequence.toString());
                callback.addDocument(seq);
            }
        } finally {
            reader.close();
        }
    }
}
