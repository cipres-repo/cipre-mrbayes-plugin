package mrbayes.plugin;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.*;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Richard Moir
 * @version $Id: MrBayesPosteriorImporter.java 68327 2015-06-16 20:40:05Z richard $
 *          <p/>
 *          Created on Sep 19, 2007 1:32:34 PM
 */
public class MrBayesPosteriorImporter extends DocumentFileImporter {

    public String[] getPermissibleExtensions() {
        //other types of files in the results can be imported individually as nexus format :)
        return new String[]{MrBayesOutputParser.EXT_MCMC, MrBayesOutputParser.EXT_POSTERIORS};
    }

    public String getFileTypeDescription() {
        return "MrBayes results";
    }

    public AutoDetectStatus tentativeAutoDetect(File file, String fileContentsStart) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(MrBayesOutputParser.EXT_MCMC) ? AutoDetectStatus.MAYBE : AutoDetectStatus.REJECT_FILE;
    }

    public void importDocuments(File file, ImportCallback callback, ProgressListener progress) throws IOException, DocumentImportException {
        doImport(file, null, callback, progress);
    }

    public void doImport(File mcmcFile, AnnotatedPluginDocument alignment, ImportCallback callback, ProgressListener progress) throws DocumentImportException, IOException {
        if (mcmcFile.isDirectory()) {
            throw new DocumentImportException("Cannot import folder of MrBayes results. Select the .mcmc file instead.");
        }
        if (mcmcFile.getName().endsWith(MrBayesOutputParser.EXT_POSTERIORS)) {
            throw new DocumentImportException("Cannot import MrBayes .p file. Select the .mcmc file instead.");
        }
        if (!mcmcFile.getName().endsWith(MrBayesOutputParser.EXT_MCMC)) {
            throw new DocumentImportException("MCMC file must have .mcmc extension");
        }
        File folder = mcmcFile.getParentFile();

        CompositeProgressListener progressListener = new CompositeProgressListener(progress, 0.1, 1, 1, 1);

        progressListener.beginSubtask("Importing alignment");

        File originalAlignmentFile = new File(folder, "originalAlignment.geneious.zip");
        //import the original alignment
        if (alignment == null && originalAlignmentFile.exists()) {
            List<AnnotatedPluginDocument> documents = PluginUtilities.importDocuments(originalAlignmentFile, progressListener);
            if(documents.size() == 1){
                alignment = documents.get(0);

                //try to get the burnin from options
                File optionsFile = new File(folder, "geneiousOptions.xml");
                if(optionsFile.exists()) {
                    try {
                        SAXBuilder builder = new SAXBuilder();
                        Element optionsValues = builder.build(optionsFile).detachRootElement();
                        Options options = new MrBayesOptions(new AnnotatedPluginDocument[] {alignment});
                        options.valuesFromXML(optionsValues);
                    }
                    catch(IOException e){
                        assert false : "The original options file exists, but we couldn't read from it: "+e.getMessage();
                    } catch (JDOMException e) {
                        assert false : "The original options file exists, but seems to be corrupted: "+e.getMessage();
                    } catch (DocumentOperationException e) {
                        assert false : "Unspecified exception importing the options: "+e.getClass().getName()+", "+e.getMessage();
                    }
                }
            }
        }


        if (progressListener.isCanceled()) return;
        MrBayesOutputParser outputParser = new MrBayesOutputParser(mcmcFile, alignment);
        List<String> failures = new ArrayList<String>();

        progressListener.beginSubtask("Importing posterior results");
        try {
            AnnotatedPluginDocument posteriorDocument = outputParser.importPosteriorsDocument();
            callback.addDocument(posteriorDocument);
        } catch (XMLSerializationException e) {
            throw new DocumentImportException(e.getMessage(), e);
        }

        if (progressListener.isCanceled()) return;
        progressListener.beginSubtask("Importing sorted topologies");
        List<AnnotatedPluginDocument> sortedTopologyDocuments;
        try {
            sortedTopologyDocuments = outputParser.importSortedTopologies(progressListener);
        } catch (IOException e) {
            sortedTopologyDocuments = Collections.emptyList();
            failures.add("Failed to import sorted topologies: " + e.getMessage());
        }
        for (AnnotatedPluginDocument sortedTopologiesDocument : sortedTopologyDocuments) {
            if (progressListener.isCanceled()) return;
            callback.addDocument(sortedTopologiesDocument);
        }

        if (progressListener.isCanceled()) return;
        progressListener.beginSubtask("Importing raw trees");
        List<AnnotatedPluginDocument> rawTreeDocuments;
        try {
            rawTreeDocuments = outputParser.importRawTrees(progressListener);
        } catch (IOException e) {
            rawTreeDocuments = Collections.emptyList();
            failures.add("Failed to import raw trees: " + e.getMessage());
        }
        for (AnnotatedPluginDocument rawTreesDocument : rawTreeDocuments) {
            if (progressListener.isCanceled()) return;
            callback.addDocument(rawTreesDocument);
        }

        failures.addAll(outputParser.getFailures());
        if (!failures.isEmpty()) {
            StringBuilder message = new StringBuilder("<html><b>Could not import some of the MrBayes results for the following reason(s):</b>\n\n");
            for (String failure : failures) {
                message.append(failure).append("\n\n");
            }
            message.append("To try importing the results again, import the following file into ").append(Geneious.getName()).append(":\n");
            message.append(mcmcFile);
            throw new DocumentImportException(message.append("</html>").toString());
        }
    }
}
