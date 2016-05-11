package com.biomatters.exampleFastaExporterPlugin;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceListDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentFileExporter;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentType;
import com.biomatters.geneious.publicapi.plugin.Options;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple example of a fasta exporter that exports its sequences into a single fasta file.
 */
public class ExampleFastaExporter extends DocumentFileExporter {
    public String getFileTypeDescription() {
        return "Example Fasta Exporter";
    }

    public String getDefaultExtension() {
        return ".fasta";
    }

    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[]{DocumentSelectionSignature.forNucleotideSequences(1, Integer.MAX_VALUE)};
    }

    @Override
    public void export(File file, AnnotatedPluginDocument[] documents, ProgressListener progressListener, Options options) throws IOException {
        CompositeProgressListener compositeProgressListener = new CompositeProgressListener(progressListener, documents.length);
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        try {
            for (AnnotatedPluginDocument document : documents) {
                compositeProgressListener.beginSubtask();
                final List<SequenceDocument> sequences = getSequences(document);
                CompositeProgressListener compositeProgressListener2 = new CompositeProgressListener(compositeProgressListener, sequences.size());
                for (SequenceDocument sequence : sequences) {
                    if (progressListener.isCanceled()) {
                        return;
                    }
                    compositeProgressListener2.beginSubtask();
                    exportSequence(out, sequence);
                }
            }
        } finally {
            out.close();
            if (progressListener.isCanceled()) {
                file.delete();
            }
        }
    }

    private void exportSequence(BufferedWriter out, SequenceDocument sequence) throws IOException {
        out.write(">");
        out.write(sequence.getName());
        out.write("\n");
        out.write(sequence.getSequenceString());
        out.write("\n");
    }

    private List<SequenceDocument> getSequences(AnnotatedPluginDocument annotatedPluginDocument) throws IOException {
        List<SequenceDocument> sequences = new ArrayList<SequenceDocument>();
        final PluginDocument pluginDocument = annotatedPluginDocument.getDocumentOrThrow(true, ProgressListener.EMPTY, IOException.class);
        if (DocumentType.isSequence(pluginDocument)) {
            SequenceDocument sequenceDocument = (SequenceDocument) pluginDocument;
            sequences.add(sequenceDocument);
        }
        else if (DocumentType.isSequenceList(pluginDocument)) {
            SequenceListDocument sequenceListDocument = (SequenceListDocument) pluginDocument;
            sequences.addAll(sequenceListDocument.getNucleotideSequences());
            sequences.addAll(sequenceListDocument.getAminoAcidSequences());
        }
        return sequences;
    }
}