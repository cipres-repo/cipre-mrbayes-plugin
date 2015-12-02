package mrbayes.plugin;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.AminoAcidSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;
import com.biomatters.geneious.publicapi.utilities.SystemUtilities;
import jebl.evolution.sequences.Sequence;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.suchard.gui.SafePrintWriter;
import org.virion.jam.util.SimpleListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class MrBayesDocumentOperation extends GeneiousGridDocumentOperation {

    private final boolean isInTreeCategory;

    static final String GENEIOUS_NEX = "geneious.nex";
    private static final String FINISH_NOW = "Finish Now";

    public MrBayesDocumentOperation(boolean isInTreeCategory) {
        this.isInTreeCategory = isInTreeCategory;
    }

    @Override
    public String getUniqueId() {
        return super.getUniqueId() + (isInTreeCategory ? "" : "2");
    }

    /**
     * Returns a {@link com.biomatters.geneious.publicapi.plugin.GeneiousGridDocumentOperation.GridInput} object representing
     * all the files and command-line inputs needed to run a job on the grid
     *
     * @param documentList   the user-selected documents
     * @param programOptions the options returned by {@link #getProgramOptions(com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument[])}
     * @param fullOptions    the options returned by {@link #getOptions(com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument[])}
     * @return A {@link com.biomatters.geneious.publicapi.plugin.GeneiousGridDocumentOperation.GridInput} object representing
     *         all the files and command-line inputs needed to run a job on the grid
     * @throws com.biomatters.geneious.publicapi.plugin.DocumentOperationException
     *          if something goes wrong
     */
    @Override
    public GeneiousGridDocumentOperation.GridInput getGridInput(AnnotatedPluginDocument[] documentList, Options programOptions, Options fullOptions) throws DocumentOperationException {
        checkForDuplicateNamesPromptToRename(Arrays.asList(documentList));
        List<File> files = new ArrayList<File>();
        final AnnotatedPluginDocument doc = documentList[0];
        final SequenceAlignmentDocument alignmentDoc = (SequenceAlignmentDocument) doc.getDocument();

        if (alignmentDoc.getNumberOfSequences() < 4) {
            throw new DocumentOperationException("MrBayes requires at least 4 sequences.\nPlease select an alignment" +
                    " containing 4 or more sequences.");
        }

//        checkForInvalidNameCharacters(alignmentDoc);

        StringWriter scriptString = new StringWriter();
        MrBayesExporter nexusExporter = new MrBayesExporter(scriptString);
        //true if there is a sequence in the alignment who's name contains characters
        //that are not accepted by MrBayes.
        boolean renameSequences = true;
        try {
            List<Sequence> sequences = new ArrayList<Sequence>();
            for (int j = 0; j < alignmentDoc.getNumberOfSequences(); j++) {
                AnnotatedPluginDocument refDoc = alignmentDoc.getReferencedDocument(j);
                SequenceDocument seq = alignmentDoc.getSequence(j);
                //optimised for memory efficiency rather than speed.
                //neither should really be an issue anyway.
                if (seq instanceof AminoAcidSequenceDocument) {
                    CharSequence sequence = seq.getCharSequence();
                    for (int i = 0; i < sequence.length(); i++) {
                        if (sequence.charAt(i) == '*')
                            throw new DocumentOperationException("The sequence \"" + seq.getName() + "\" in your alignment contains a stop codon at position " + (i + 1) + ".  MrBayes cannot analyze sequences that contain a stop codon.");
                    }
                }
                sequences.add(SequenceUtilities.asJeblSequence(refDoc, seq));
            }
            nexusExporter.setRenameSequences(renameSequences);
            nexusExporter.exportSequences(sequences);
        } catch (IOException io) {
            throw new DocumentOperationException(io.getMessage());
        }


        //int numIterations;


        try {
            String block = MrBayesUtilities.getCustomOrGeneratedCommandBlockFromOptions(programOptions, alignmentDoc);
            //numIterations = MrBayesUtilities.getNumOfIterationsFromOptions(options);


            File tempFolder = FileUtilities.createTempFile("Geneious", "MrBayes", false);
            //noinspection ResultOfMethodCallIgnored
            tempFolder.delete();
            //noinspection ResultOfMethodCallIgnored
            tempFolder.mkdirs();

            File outputFile = new File(tempFolder, GENEIOUS_NEX);
            files.add(outputFile);
            SafePrintWriter out = new SafePrintWriter(new FileWriter(outputFile));
            out.println(scriptString);
            out.println(block);
            out.close();

            File originalAlignmentFile = new File(tempFolder, "originalAlignment.geneious.zip");
            PluginUtilities.exportDocumentsInGeneiousFormat(originalAlignmentFile, true, doc);
            files.add(originalAlignmentFile);

            Element optionValues = programOptions.valuesToXML("MrBayesOptions");
            File optionsFile = new File(tempFolder, "geneiousOptions.xml");
            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
            FileWriter optionsWriter = new FileWriter(optionsFile);
            outputter.output(optionValues, optionsWriter);
            optionsWriter.close();
            files.add(optionsFile);
        } catch (IOException ex) {
            throw new DocumentOperationException("Could not write temp files to disk", ex);
        }

        return new GeneiousGridDocumentOperation.GridInput(files, Arrays.asList(GENEIOUS_NEX), "");
    }

    /**
     * Returns the name of (or path to) the executable used for local runs
     *
     * @return the name of (or path to) the executable used for local runs
     */
    @Override
    public String getExecutableName() {
        String exeName = "mb";
        String version = "3_2_2_";
        String osSpecific = null;

        if (SystemUtilities.isMac()) {
            // This is a universal binary
            osSpecific = "MacOSX";
        } else if (SystemUtilities.isWindows()) {
            osSpecific = "win32.exe";
        } else if (SystemUtilities.isLinux()) {
            if (SystemUtilities.is64BitOS()) {
                osSpecific = "linux64";
            } else {
                osSpecific = "linux";
            }
        }

        if (osSpecific != null) {
            // give the executable name only so that if it's on the path it will still work
            exeName += version + osSpecific;
        }

        File binary = FileUtilities.getResourceForClass(MrBayesDocumentOperation.class, "/" + exeName);
        return binary.getParentFile().getAbsolutePath() + File.separator + exeName;
    }

    /**
     * Returns the name of the program as given on the grid.  Cannot return null
     *
     * @return Returns the name of the program as given on the grid.  Cannot return null.
     */
    @Override
    public String getProgramName() {
        return "MrBayes";
    }

    @Override
    public String getProgramVersion() {
        return "3.2.2";
    }

    /**
     * This method returns options for running the program
     *
     * @param documents user-selected documents
     * @return options for running the program
     * @throws com.biomatters.geneious.publicapi.plugin.DocumentOperationException
     *          if something goes wrong
     */
    @Override
    public Options getProgramOptions(AnnotatedPluginDocument... documents) throws DocumentOperationException {
        return new MrBayesOptions(documents);
    }

    private static boolean isProtein(AnnotatedPluginDocument[] documents) throws DocumentOperationException {
        if (documents.length == 0) {
            return false;
        }
        if (documents.length == 1) { //alignment
            SequenceAlignmentDocument alignment = (SequenceAlignmentDocument) documents[0].getDocument();
            return alignment.getSequence(0) instanceof AminoAcidSequenceDocument;
        }
        return AminoAcidSequenceDocument.class.isAssignableFrom(documents[0].getDocumentClass());
    }

    public GeneiousGridImporter getImporter() {
        return new MrBayesGridImporter();
    }

    /**
     * GeneiousGridDocumentOperations that support local runs should implement this method.
     * Performs a local run of the program, and returns any documents that are generated.
     *
     * @param documents           The user-selected input documents
     * @param progressListener    a progress listener
     * @param programOptions      the options returned by {@link #getProgramOptions(com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument[])}
     * @param fullOptions         the options returned by {@link #getOptions(com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument[])}
     * @param _executableLocation a string representing the location of the program executable on disk
     * @return A list of documents generated in the run, or null if operation failed or canceled
     * @throws com.biomatters.geneious.publicapi.plugin.DocumentOperationException
     *          if something goes wrong
     */
    @Override
    public void runLocally(AnnotatedPluginDocument[] documents, final ProgressListener progressListener, Options programOptions, Options fullOptions, String _executableLocation, final OperationCallback operationCallback) throws DocumentOperationException {

        int numIterations = MrBayesUtilities.getNumOfIterationsFromOptions(programOptions);//(Integer)options.getValue(MrBayesOptions.LENGTH) * 1000;
        AtomicReference<String> executableLocation = new AtomicReference<String>(_executableLocation);

        GeneiousGridDocumentOperation.GridInput gridInput = getGridInput(documents, programOptions, fullOptions);
        if (gridInput == null) {
            return; //canceled
        }

        final AtomicBoolean stopped = new AtomicBoolean(false);
        progressListener.addFeedbackAction(FINISH_NOW, new SimpleListener() {
            public void objectChanged() {
                stopped.set(true);
                progressListener.setMessage("Stopping...");
            }
        });

        final CompositeProgressListener progress = new CompositeProgressListener(progressListener, 0.95, 0.05);
        progress.setIndeterminateProgress();
        progress.setMessage("Waiting for first iteration to complete...");

        File nexFile = gridInput.getFiles().get(0);
        File tempFolder = nexFile.getParentFile();
        MrBayesRunner runner = new MrBayesRunner(tempFolder, executableLocation.get(), isProtein(documents));

        try {
            runner.manageMrBayesExec(progress, numIterations, stopped);
        } catch (DocumentOperationException e) {
            if (runner.isFirstIterationComplete()) {
                String doImport = "Import Partial Results";
                Dialogs.DialogOptions dialogOptions = new Dialogs.DialogOptions(new String[]{doImport, "Cancel"}, "MrBayes Failed", null, Dialogs.DialogIcon.INFORMATION);
                Object choice = Dialogs.showDialog(dialogOptions, e.getMessage());
                if (!doImport.equals(choice)) {
                    throw e;
                }
            } else {
                throw e;
            }
        }

        if (!runner.isFirstIterationComplete()) {
            throw new DocumentOperationException("No results were saved because MrBayes did not complete the first iteration.");
        }

        progress.beginNextSubtask("");

        if (!progressListener.isCanceled()) {
            final AtomicReference<DocumentOperationException> exception = new AtomicReference<DocumentOperationException>();
            DocumentFileImporter.ImportCallback callback = new DocumentFileImporter.ImportCallback() {
                public AnnotatedPluginDocument addDocument(PluginDocument doc) {
                    AnnotatedPluginDocument adoc = DocumentUtilities.createAnnotatedPluginDocument(doc);
                    return addDocument(adoc);
                }

                public AnnotatedPluginDocument addDocument(AnnotatedPluginDocument adoc) {
                    //noinspection ThrowableResultOfMethodCallIgnored
                    if (exception.get() == null) {  // If exception is non-null just ignore so we can exit quickly and throw the exception
                        try {
                            return operationCallback.addDocument(adoc, false, ProgressListener.EMPTY);
                        } catch (DocumentOperationException e) {
                            exception.set(e);
                        }
                    }
                    return null;
                }
            };

            try {
                progressListener.removeFeedbackAction(FINISH_NOW);
                new MrBayesPosteriorImporter().doImport(new File(tempFolder, GENEIOUS_NEX + MrBayesOutputParser.EXT_MCMC), documents[0], callback, progress);
                DocumentOperationException e = exception.get();
                if (e != null) {
                    throw e;
                }
            } catch (IOException ex) {
                throw new DocumentOperationException(ex.getMessage(), ex);
            } catch (DocumentImportException ex) {
                throw new DocumentOperationException(ex.getMessage(), ex);
            }
        }

        progress.setComplete();
        //clean up results directory after importing because import may fail due to out of memory etc.
        runner.cleanUpResultsDirectory();
//        System.out.println("Runing MrBayes took "+(System.currentTimeMillis()-time)+" Milliseconds.");
    }

    public static boolean testing = false;

    public GeneiousActionOptions getActionOptions() {
        GeneiousActionOptions.Category category = isInTreeCategory ? GeneiousActionOptions.Category.TreeBuilding : GeneiousActionOptions.Category.None;
        return new GeneiousActionOptions(isInTreeCategory?"MrBayes...":"Tree Building - MrBayes",
                "Perform posterior tree simulation from an alignment", MrBayesPlugin.toolbarIcons, category);
    }

    public String getHelp() {
        return "To perform MrBayes, Bayesian inference of phylogeny, select an alignment";
    }

    public DocumentSelectionSignature[] getSelectionSignatures() {
        DocumentSelectionSignature singleAlignmentSignature = new DocumentSelectionSignature(
                SequenceAlignmentDocument.class, 1, 1);
        return new DocumentSelectionSignature[]{singleAlignmentSignature/*, nucleotideSequenceSelectionSignature, proteinSequenceSelectionSignature*/};
    }


    /**
     * todo this code is duplicated in common SequenceAlignmentUtilities
     * <p/>
     * Checks for sequences with duplicate names in or between the alignments. If no duplicate encountered
     * then returns without doing anything. If a duplicate is encountered and the alignment
     * is editable, the user will be prompted for renaming the dup's (if not scripting) and
     * if they click yes (or scripting) then the duplicate sequences will be renamed with unique numbers
     * appended. If no or not editable then DocumentOperationException will be thrown with
     * name of duplicate in message.
     *
     * @param annotatedAlignments the alignments to check
     * @throws DocumentOperationException .
     */
    public static void checkForDuplicateNamesPromptToRename(List<AnnotatedPluginDocument> annotatedAlignments) throws DocumentOperationException {
        //todo duplicate in phyml
        List<SequenceAlignmentDocument> alignments = new ArrayList<SequenceAlignmentDocument>();
        List<SequenceDocument> seqs = new ArrayList<SequenceDocument>();
        boolean allEditable = true;
        for (AnnotatedPluginDocument annotatedAlignment : annotatedAlignments) {
            SequenceAlignmentDocument alignment = (SequenceAlignmentDocument) annotatedAlignment.getDocument();
            alignments.add(alignment);
            if (!alignment.isEditable()) {
                allEditable = false;
            }
            for (SequenceDocument seq : alignment.getSequences()) {
                seqs.add(seq);
            }
        }

        //we need to tell the user which name is duplicate (we'll just tell them the first duplicate name
        //if there are more than one set
        Set<String> names = new HashSet<String>();
        Set<String> duplicateNames = new HashSet<String>();
        for (SequenceDocument seq : seqs) {
            if (!names.add(seq.getName())) {
                duplicateNames.add(seq.getName());
            }
        }

        if (duplicateNames.isEmpty()) {
            return;
        }
        DocumentOperationException duplicatesException = new DocumentOperationException(
                "Operation cannot be performed because selected alignment" + (annotatedAlignments.size() > 1 ? "s" : "") +
                        " contain" + (annotatedAlignments.size() > 1 ? "" : "s") +
                        " sequences with identical names (\"" + duplicateNames.toArray()[0] + "\")." +
                        " Please rename the sequence(s) and try again."
        );
        if (allEditable) {
// todo scripting check in publicAPI...
//            if (!PrivateApiUtilities.isRunningFromScript()) {
            final String message = (annotatedAlignments.size() > 1 ? "These alignments contain" : "This alignment contains") +
                    " sequences with duplicate names.\nIn order to perform this" +
                    " operation, there must be no duplicate names.\nDo you wish rename the original sequences to" +
                    " contain no duplicates?";
            if (!Dialogs.showYesNoDialog(message, "Rename Duplicates", null, Dialogs.DialogIcon.QUESTION)) {
                throw duplicatesException;
            }
//            }
            for (String duplicateName : duplicateNames) {
                int duplicateCounter = 1;
                for (SequenceAlignmentDocument alignment : alignments) {
                    seqs = alignment.getSequences();
                    for (int i = 0; i < seqs.size(); i++) {
                        SequenceDocument seq = seqs.get(i);
                        if (seq.getName().equals(duplicateName)) {
                            String newName;
                            do {
                                newName = seq.getName() + " " + (duplicateCounter++);
                            } while (names.contains(newName));
                            alignment.setSequenceName(i, newName, true);
                        }
                    }
                }
            }
            for (AnnotatedPluginDocument annotatedAlignment : annotatedAlignments) {
                annotatedAlignment.saveDocument();
            }
        } else {
            throw duplicatesException;
        }
    }

    @Override
    public boolean canRunOnGeneiousServer() {
        return true;
    }
}
