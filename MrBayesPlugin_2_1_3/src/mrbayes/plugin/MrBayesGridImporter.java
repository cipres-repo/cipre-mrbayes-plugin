package mrbayes.plugin;

import com.biomatters.geneious.publicapi.plugin.DocumentFileImporter;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import com.biomatters.geneious.publicapi.plugin.GeneiousGridImporter;
import jebl.util.ProgressListener;

import java.io.File;
import java.io.IOException;

/**
 * @author Steven Stones-Havas
 * @version $Id$
 *          <p/>
 *          Created on 14/05/2008 14:55:57
 */
public class MrBayesGridImporter extends GeneiousGridImporter {
    protected void _importDocuments(File folder, DocumentFileImporter.ImportCallback callback, ProgressListener progressListener) throws IOException, DocumentImportException {
        MrBayesPosteriorImporter importer = new MrBayesPosteriorImporter();
        boolean imported = false;
        //noinspection ConstantConditions
        for(File f : folder.listFiles()) {
            if(f.getName().toLowerCase().endsWith(".mcmc")) {
                importer.importDocuments(f, callback, progressListener);
                imported = true;
            }
        }
        if(!imported) {
            throw new DocumentImportException("MrBayes did not produce an .mcmc file in its output!");
        }
    }
}
