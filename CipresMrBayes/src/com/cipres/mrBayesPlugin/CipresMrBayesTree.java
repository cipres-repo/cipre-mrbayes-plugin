package com.cipres.mrBayesPlugin;

import java.util.List;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.GeneiousActionOptions;
import com.biomatters.geneious.publicapi.plugin.Icons;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.cipres.mrBayesPlugin.ui.MrBayesOptions;
import com.cipres.mrBayesPlugin.utilities.MrBayesUtilities;

import jebl.util.ProgressListener;

public class CipresMrBayesTree extends DocumentOperation{

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
        options = new MrBayesOptions(docs);
        return options;
    }
	
	public List performOperation(AnnotatedPluginDocument[] docs, ProgressListener progress, Options options) throws DocumentOperationException{
		String sb = MrBayesUtilities.getGeneratedCommandBlockFromOptions(options);
		System.out.println(sb);
		
		
		return null;
		
	}

}
