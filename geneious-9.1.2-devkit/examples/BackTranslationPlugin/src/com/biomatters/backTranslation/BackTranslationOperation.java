package com.biomatters.backTranslation;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.sequence.AminoAcidSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.*;
import jebl.util.ProgressListener;

import java.io.*;
import java.util.Date;
import java.util.List;


/**
 * This class allows use of the EMBOSS backtranambig tool with any amino acid
 * sequence document in Geneious.
 * <p>
 * Date: 2007-11-28
 * <p>
 * Copyright (C): Biomatters Ltd, New Zealand
 * 
 * @author Bruce Ashton
 */
class BackTranslationOperation extends DocumentOperation {

    /**
     * The genetic code table values and descriptions that bactranambig accepts.
     * Usually one would create an inner class that extends Options.OptionValue
     * for a complex option but, as it happens, Options.OptionValue has all the
     * functionality we need in this case.
     */
    private static final Options.OptionValue[] GENETIC_CODES = new Options.OptionValue[] {
            new Options.OptionValue("0", "Standard"),
            new Options.OptionValue("1", "Standard (with alternative initiation codons)"),
            new Options.OptionValue("2", "Vertebrate Mitochondrial"),
            new Options.OptionValue("3", "Yeast Mitochondrial"),
            new Options.OptionValue("4", "Mold, Protozoan, Coelenterate Mitochondrial and Mycoplasma/Spiroplasma"),
            new Options.OptionValue("5", "Invertebrate Mitochondrial"),
            new Options.OptionValue("6", "Ciliate Macronuclear and Dasycladacean"),
            new Options.OptionValue("9", "Echinoderm Mitochondrial"),
            new Options.OptionValue("10", "Euplotid Nuclear"),
            new Options.OptionValue("11", "Bacterial"),
            new Options.OptionValue("12", "Alternative Yeast Nuclear"),
            new Options.OptionValue("13", "Ascidian Mitochondrial"),
            new Options.OptionValue("14", "Flatworm Mitochondrial"),
            new Options.OptionValue("15", "Blepharisma Macronuclear"),
            new Options.OptionValue("16", "Chlorophycean Mitochondrial"),
            new Options.OptionValue("21", "Trematode Mitochondrial"),
            new Options.OptionValue("22", "Scenedesmus obliquus"),
            new Options.OptionValue("23", "Thraustochytrium Mitochondrial")
    };
    
    static final String HELP = "Back translates protein to DNA using EMBOSS's backtranambig";

    private static final String CODE_EXE = "EXE";
    private static final String CODE_GEN_CODE = "GEN_CODE";

    /**
     * The action options specify that this operation is available from the Sequence menu.
     */
    public GeneiousActionOptions getActionOptions() {
        return new GeneiousActionOptions("Back Translate...").
                setMainMenuLocation(GeneiousActionOptions.MainMenu.Sequence).
                setInMainToolbar(true);
    }

    public String getHelp() {
        return HELP;
    }

    /**
     * Options for backtranambig allow the user to select the executable with
     * file dialog and the genetic code to use with a combobox.
     * 
     * @param documents the document passed in is not considered for these options
     * @return an Options object
     */
    public Options getOptions(AnnotatedPluginDocument... documents) {
        Options options = new Options(getClass());
        options.addLabel("Note: Full EMBOSS environment must be installed on this computer", true, true);
        options.addLabel(" ");
        options.addFileSelectionOption(CODE_EXE, "The backtranambig executable", "");
        options.addComboBoxOption(CODE_GEN_CODE, "Genetic code to use", GENETIC_CODES, GENETIC_CODES[0]);
        return options;
    }

    /**
     * The document selection signature tells Geneious to enable backtranambig
     * only when a single amino acid document is selected
     */
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[] {
                new DocumentSelectionSignature(AminoAcidSequenceDocument.class, 1, 1)
        };
    }

    /**
     * This method does the actual work of running the backtranambig tool. It is
     * called in a background thread when a user selects Back Translation from
     * the Tools menu or the popup menu for amino acid sequence documents.
     * <p>
     * This method needs to be threadsafe as it is possible for time-consuming
     * operations to be running concurrently. It is invoked by user interaction.
     * 
     * @param documents always contains one AminoAcidSequenceDocument due to the
     *            selection signature
     * @param progressListener is ignored in this example
     * @param options as returned by getOptions()
     * @return a list containing one nucleotide sequence document
     * @throws DocumentOperationException if any error occurs
     */
    public List<AnnotatedPluginDocument> performOperation(AnnotatedPluginDocument[] documents,
            ProgressListener progressListener, Options options) throws DocumentOperationException {

        // Our selection signature guarantees we have just one AminoAcidSequenceDocument here.
        AminoAcidSequenceDocument aminoAcidSequence = (AminoAcidSequenceDocument) documents[0].getDocument();

        String executable = options.getValueAsString(CODE_EXE);
        Options.OptionValue geneticCode = (Options.OptionValue) options.getValue(CODE_GEN_CODE);

        try {
            String[] command = new String[] { executable, "-table", geneticCode.getName(), "-filter" };
            Process process = Runtime.getRuntime().exec(command);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(aminoAcidSequence.getSequenceString());
            writer.close();

            StringBuilder dnaSequence = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(">")) {
                    continue;
                }
                dnaSequence.append(line.trim());
            }
            reader.close();

            // Create our new back-translated DNA sequence document with IUPAC codes for ambiguities.
            NucleotideSequenceDocument dnaSequenceDocument = new DefaultNucleotideSequence(
                    aminoAcidSequence.getName() + " (translated)",
                    aminoAcidSequence.getDescription() + " back-translated with EMBOSS backtranambig",
                    dnaSequence.toString(), new Date());
            return DocumentUtilities.createAnnotatedPluginDocuments(dnaSequenceDocument);
        } catch (IOException e) {
            throw new DocumentOperationException("EMBOSS's backtranambig failed", e);
        }
    }
}
