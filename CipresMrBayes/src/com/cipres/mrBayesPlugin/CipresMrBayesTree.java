package com.cipres.mrBayesPlugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;
import org.ngbw.directclient.CiCipresException;
import org.ngbw.restdatatypes.ErrorData;
import org.ngbw.restdatatypes.LimitStatus;
import org.ngbw.restdatatypes.ParamError;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.components.Dialogs.DialogOptions;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.GeneiousActionOptions;
import com.biomatters.geneious.publicapi.plugin.Icons;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.cipres.mrBayesPlugin.ui.LoginOptions;
import com.cipres.mrBayesPlugin.ui.MrBayesOptions;
import com.cipres.mrBayesPlugin.utilities.CipresUtilities;
import com.cipres.mrBayesPlugin.utilities.MrBayesUtilities;

import jebl.util.ProgressListener;
import jebl.util.SafePrintWriter;


public class CipresMrBayesTree extends DocumentOperation{
	static final String GENEIOUS_NEX = "geneious.nex";

	@Override
	public GeneiousActionOptions getActionOptions() {
		//saved under cipres-mrbayes-plugin/GeneiousFiles/resources/images/TreeIcon.png for now
		Icons icon= IconUtilities.getIcons("TreeIcon.png");
        return new GeneiousActionOptions("MrBayes_Cipres",
                "Perform posterior tree simulation from an alignment", icon, GeneiousActionOptions.Category.TreeBuilding);
	}

	@Override
	public String getHelp() {
		return "Getting Help";
	}

	@Override
	public DocumentSelectionSignature[] getSelectionSignatures() {
		DocumentSelectionSignature singleAlignmentSignature = new DocumentSelectionSignature(
                SequenceAlignmentDocument.class, 1, 1);
        return new DocumentSelectionSignature[]{singleAlignmentSignature/*, nucleotideSequenceSelectionSignature, proteinSequenceSelectionSignature*/};
	}
	
	public Options getOptions(final AnnotatedPluginDocument[] docs) throws DocumentOperationException{
        Options options = null;
        if(CipresMrBayesToolbar.getNewUser() == true){
        	
        } else{
        	options = new MrBayesOptions(docs);
        }
        
        return options;
    }
	
	public List performOperation(AnnotatedPluginDocument[] docs, ProgressListener progress, Options options) throws DocumentOperationException{
		String sb = MrBayesUtilities.getGeneratedCommandBlockFromOptions(options);
		
		Map<String, Collection<String>> vParams = new HashMap<String, Collection<String>>();
		Map<String, String> inputParams = new HashMap<String, String>();
		Map<String, String> metadata = new HashMap<String, String>();
		
		
		List<File> files = new ArrayList<File>();
        final AnnotatedPluginDocument doc = docs[0];
        final SequenceAlignmentDocument alignmentDoc = (SequenceAlignmentDocument) doc.getDocument();

        if (alignmentDoc.getNumberOfSequences() < 4) {
            throw new DocumentOperationException("MrBayes requires at least 4 sequences.\nPlease select an alignment" +
                    " containing 4 or more sequences.");
        }
		
        StringWriter scriptString = new StringWriter();
        try {
            String block = MrBayesUtilities.getCustomOrGeneratedCommandBlockFromOptions(options, alignmentDoc);
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

            Element optionValues = options.valuesToXML("MrBayesOptions");
            File optionsFile = new File(tempFolder, "geneiousOptions.xml");
//            XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
//            FileWriter optionsWriter = new FileWriter(optionsFile);
//            outputter.output(optionValues, optionsWriter);
//            optionsWriter.close();
            files.add(optionsFile);
        } catch (IOException ex) {
            throw new DocumentOperationException("Could not write temp files to disk", ex);
        }
        
        metadata = MrBayesUtilities.getMetadata(docs[0].getName());
        vParams = MrBayesUtilities.getVParams(MrBayesOptions.getJsonCommand());
		inputParams.put("infile_", files.get(0).toString());
		System.err.println(vParams);
        try {
			CipresUtilities.submitJob(vParams, inputParams, metadata);
		} catch (CiCipresException ce) {
			System.err.println("Caught in SubmitJob");
			
            ErrorData ed = ce.getErrorData();
            System.out.println("Cipres error code=" + ed.code + ", message=" + ed.displayMessage);
            if (ed.code == ErrorData.FORM_VALIDATION)
            {
                for (ParamError pe : ed.paramError)
                {
                    System.out.println(pe.param + ": " + pe.error);
                }
            } else if (ed.code == ErrorData.USAGE_LIMIT)
            {
                LimitStatus ls = ed.limitStatus;
                System.out.println("Usage Limit Error, type=" + ls.type + ", ceiling=" + ls.ceiling);
            }
		}
		return null;
		
	}

}
