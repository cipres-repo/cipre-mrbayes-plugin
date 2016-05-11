package com.biomatters.exampleWorkflowPlugin;

import com.biomatters.geneious.publicapi.databaseservice.DatabaseService;
import com.biomatters.geneious.publicapi.databaseservice.DatabaseServiceException;
import com.biomatters.geneious.publicapi.databaseservice.Query;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.*;
import java.util.List;

import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;

/**
 * A simple example of a workflow operation that takes some search text provided by the user,
 * searches NCBI for all matching nucleotide sequences, builds an alignment from the
 * results, and then builds a tree from that alignment.
 */
public class ExampleWorkflow extends DocumentOperation {
    private static final String DESCRIPTION = "An example workflow which searches NCBI, builds an alignment from the results, then builds a tree from the alignment";

    @Override
    public GeneiousActionOptions getActionOptions() {
        // Specifies how this plugin is accessed from the user interface.
        return new GeneiousActionOptions("Example Workflow",DESCRIPTION).
                setMainMenuLocation(GeneiousActionOptions.MainMenu.Tools);
    }

    @Override
    public String getHelp() {
        return DESCRIPTION;
    }

    @Override
    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[0]; // Takes no documents as input
    }

    @Override
    public Options getOptions(AnnotatedPluginDocument... documents) throws DocumentOperationException {
        return new ExampleWorkflowOptions();
    }

    @Override
    public List<AnnotatedPluginDocument> performOperation(AnnotatedPluginDocument[] annotatedDocuments, ProgressListener progressListener, Options options) throws DocumentOperationException {
        String queryText = ((ExampleWorkflowOptions)options).getSearchQuery();
        // First create a composite progress to represent the 3 components of this example
        // 1) Getting the results from NCBI (80% of total time)
        // 2) Building the alignment (15% of total time)
        // 3) Building a tree (5% of total time)
        CompositeProgressListener compositeProgress=new CompositeProgressListener(progressListener,
                0.8, 0.15, 0.05);

        // First get the ncbi database and search it:
        compositeProgress.beginSubtask("Searching NCBI");
        DatabaseService ncbiNucleotideService = (DatabaseService) PluginUtilities.getGeneiousService("NCBI_nucleotide_gbc");
        // The various services available are found by running this commented out code:
        //for (GeneiousService service : PluginUtilities.getGeneiousServices()) {
        //    System.out.println(service.getUniqueID());
        //}
        Query query=Query.Factory.createQuery(queryText);
        List<AnnotatedPluginDocument> sequences;
        try {
            sequences = ncbiNucleotideService.retrieve(query, compositeProgress);
        } catch (DatabaseServiceException e) {
            throw new DocumentOperationException("Failed to search NCBI database",e);
        }
        if (compositeProgress.isCanceled())throw new DocumentOperationException.Canceled();
        if (sequences.size()<3)
            throw new DocumentOperationException("NCBI returned "+sequences.size()+" results, but we require at least 3 to build a tree");


        // Now get the alignment operation and perform the alignment:
        compositeProgress.beginSubtask("Build alignment");
        final DocumentOperation alignmentOperation = PluginUtilities.getCategoryOperation(GeneiousActionOptions.Category.Alignment);
        Options alignmentOptions = alignmentOperation.getOptions(sequences); // Get the options provided by the alignment operation.
        alignmentOptions.setValue("operation","MUSCLE_NUCLEOTIDE_"); // Tell it to use muscle for alignment.
        // To find out what options are available on the alignment do the following commented out line:
        //System.out.println(alignmentOptions.getDescriptionAndState());

        List<AnnotatedPluginDocument> alignment = alignmentOperation.performOperation(sequences, compositeProgress, alignmentOptions);
        if (compositeProgress.isCanceled())throw new DocumentOperationException.Canceled();

        // Now get the tree operation and build a tree:
        compositeProgress.beginSubtask("Build tree");
        final DocumentOperation treeOperation = PluginUtilities.getCategoryOperation(GeneiousActionOptions.Category.TreeBuilding);
        Options treeOptions = treeOperation.getOptions(alignment);
        treeOptions.setValue("treeBuilding.buildMethod", "UPGMA");
        // To find out what options are available on the tree builder, do the following commented out line:
        //System.out.println(treeOptions.getDescriptionAndState());

        final List<AnnotatedPluginDocument> treeResults = treeOperation.performOperation(alignment, compositeProgress, treeOptions);
        if (treeResults!=null && treeResults.size()==1) {
            treeResults.get(0).setName("Tree of "+queryText);
        }
        return treeResults;
    }
}
