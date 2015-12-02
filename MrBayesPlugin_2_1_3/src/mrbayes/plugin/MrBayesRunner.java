package mrbayes.plugin;

import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.utilities.Execution;
import jebl.util.Cancelable;
import jebl.util.ProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MrBayesRunner {

    private final File MrBayesTmpDir;
    private final String mrbayesExeString;
    private final boolean isProtein;
    private MrBayesOutputListener outputListener = null;

    public MrBayesRunner(File tempFolder, String inMrBayesExeString, boolean isProtein) throws DocumentOperationException {
        this.isProtein = isProtein;
        mrbayesExeString = inMrBayesExeString;
        MrBayesTmpDir = tempFolder;
    }

    public boolean isFirstIterationComplete() {
        return outputListener != null && outputListener.isFirstIterationComplete;
    }

    public void cleanUpResultsDirectory() {
        File[] files = MrBayesTmpDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
        }
        System.out.println("Delete dir "+MrBayesTmpDir.getAbsolutePath()+": " + MrBayesTmpDir.delete());
    }

    private static class MrBayesOutputListener extends Execution.OutputListener {
        private double lastIteration = -1;
        private StringBuilder outLinesForError = new StringBuilder();

        private boolean resultsStarted = false;
        private String currentProgressMessage = "";
        private List<String> standardDeviations = new ArrayList<String>();
        private boolean isFirstIterationComplete = false;
        private final ProgressListener progressListener;
        private final int numIterations;
        private final boolean isProtein;
        private String errorMessage = null;

        MrBayesOutputListener(ProgressListener progressListener, int numIterations, boolean isProtein){
            this.progressListener = progressListener;
            this.numIterations = numIterations;
            this.isProtein = isProtein;
        }

        @Override
        public void stdoutWritten(String outLine) {
            if (outLine.contains("Stop codon"))
                errorMessage = "The codon model cannot be applied to this alignment because it contains stop codons.\n" +
                                "Please verify isProtein translation or use a nucleotide-based substitution model.\n\n" +
                                outLine;
            if (outLine.contains("requires triplets"))
                errorMessage = "The codon model cannot be applied to this alignment " +
                                "because its length is not divisible by 3.\n" +
                                "Please use a nucleotide-based substitution model.";
            if (outLine.contains("Chain results")) {
                resultsStarted = true;
            }
            if (resultsStarted) {
                outLine = outLine.trim();
                if (outLine.matches("Average standard deviation of split frequencies: \\d+\\.\\d+")) {
                    standardDeviations.add(outLine);
                    while (standardDeviations.size() > 3) {
                        standardDeviations.remove(0);
                    }
                    updateProgress(progressListener, currentProgressMessage, standardDeviations);
                }
                String field[] = outLine.split("\\s+");
                try {
                    Integer i = Integer.valueOf(field[0]);
                    if (i != 1) {
                        isFirstIterationComplete = true;
                        double iteration = i.doubleValue();
                        if (iteration > lastIteration) {
                            double completed = iteration / (double) numIterations;
                            //                        System.out.println("Completed: " + completed);

                            String timeEstimate = "";
                            if (field[field.length - 1].matches("\\d*:\\d*:\\d*")) {
                                timeEstimate = " (Estimated " + field[field.length - 1] + " remaining)";
                            }

                            //progress starts out as indeterminate because it can sit at 0% for a very long time waiting
                            //for the first iteration to complete. So don't update the progress unless we have a decent number
                            //to report.
                            //if the user has specified a custom block that we don't understand, we'll just report indeterminate
                            //progress for the entire run
                            if (!isProtein) {
                                if(numIterations < 0){
                                    currentProgressMessage = String.format("Completed iteration %,d" + timeEstimate, (int)iteration);
                                }
                                else if ((int)iteration == numIterations) {
                                    currentProgressMessage = "Run complete, analyzing results";
                                    progressListener.setProgress(completed);
                                }
                                else {
                                    currentProgressMessage = String.format("Completed iteration %,d of %,d" + timeEstimate, (int)iteration, numIterations);
                                    progressListener.setProgress(completed);
                                }
                                updateProgress(progressListener, currentProgressMessage, standardDeviations);
                            }
                            lastIteration = iteration;
                        }
                    }

                } catch (NumberFormatException e) {
                    outLinesForError.append(outLine).append('\n');
                    // Ignore
                }
            } else {
                outLinesForError.append(outLine).append("\n");
            }
        }

        @Override
        public void stderrWritten(String output) {
            // no stderr
        }

        private static void updateProgress(ProgressListener progress, String baseMessage, List<String> extraLines) {
            StringBuilder message = new StringBuilder("<html>").append(baseMessage).append("\n\n");
            for (int i = 0, extraLinesSize = extraLines.size(); i < extraLinesSize; i++) {
                String extraLine = extraLines.get(i);
                if (i == extraLines.size() - 1) {
                    message.append("<font size=-1>");
                } else if (i == 0 && extraLines.size() > 2) {
                    message.append("<font color=#666666 size=-1>");
                } else {
                    message.append("<font color=#444444 size=-1>");
                }
                message.append(extraLine).append("</font>\n");
            }
            progress.setMessage(message.append("</html>").toString());
        }
    }
    /**
     *
     * @return true if the operation was cancelled by the user
     * @throws DocumentOperationException
     */
    public void manageMrBayesExec(final ProgressListener progressListener, int numIterations, final AtomicBoolean stopped) throws DocumentOperationException {

        outputListener = new MrBayesOutputListener(progressListener, numIterations, isProtein);
        Cancelable cancelable = new Cancelable() {
            @Override
            public boolean isCanceled() {
                return progressListener.isCanceled() || stopped.get();
            }
        };
        Execution execution = new Execution(new String[] { mrbayesExeString, MrBayesDocumentOperation.GENEIOUS_NEX}, cancelable, outputListener, "y\n", false);
        execution.setWorkingDirectory(MrBayesTmpDir.getAbsolutePath());
        if(isProtein) {
            progressListener.setMessage("Running MrBayes (progress not available for protein analyses)");
        }
        try {
            final int code = execution.execute();
            if (progressListener.isCanceled() || stopped.get()) {
                return;
            }
            if (outputListener.errorMessage != null) {
                throw new DocumentOperationException(outputListener.errorMessage);
            }
            if( code != 0 ) {
               throw new DocumentOperationException("Failed to run MrBayes (" + code + ")\nOutput:\n" + outputListener.outLinesForError);
            }
        } catch (InterruptedException ex) {
            throw new DocumentOperationException("Failed to run MrBayes: " + ex.getMessage());
        } catch (IOException e) {
            throw new DocumentOperationException("Failed to run MrBayes: " + e.getMessage());
        }
    }
}
