package org.suchard.gui.posterior;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;

import javax.swing.*;

/**
 * @author Richard
 * @version $Id: SummaryStatisticsViewerFactory.java 43609 2011-07-28 03:19:13Z matthew $
 */
public final class SummaryStatisticsViewerFactory extends DocumentViewerFactory {

	public String getName() {
		return "Parameter Estimates";
	}

	public String getDescription() {
		return "Posterior Parameter Estiamtes";
	}

	public String getHelp() {
		return null;
	}

	public DocumentSelectionSignature[] getSelectionSignatures() {
		DocumentSelectionSignature sig = new DocumentSelectionSignature(PosteriorDocument.class, 1, 1);
		return new DocumentSelectionSignature[]{sig};
	}

	public DocumentViewer createViewer(AnnotatedPluginDocument[] docList) {
        if(((PosteriorDocument) docList[0].getDocumentOrNull()).getTraces() == null)
            return null;
        return new SummaryStatisticsViewer(docList);
	}

	public ViewPrecedence getPrecedence() {
		return ViewPrecedence.HIGHEST;
	}
}

class SummaryStatisticsViewer extends TraceViewer {

	private GeneiousSummaryStatisticsPanel summaryPanel;

	public SummaryStatisticsViewer(AnnotatedPluginDocument[] docList) {
		super(docList);
		// TODO show some sort of panel while loading the first time
//        System.out.println("Does this come up?");
		summaryPanel = new GeneiousSummaryStatisticsPanel();
		if (System.getProperty("os.name").startsWith("Mac OS")) {
			summaryPanel.setOpaque(true);
		}
		setTraceSelection(TraceSelectionManager.getSelection(documentUrns));
		analyseTraceList();
	}


	protected JComponent getTraceComponent() {
		return summaryPanel;
	}

	protected void setViewerTraceSelection(int[] selection) {
		//analyseTraceList();
		summaryPanel.setTraces(traces, selection);
	}

	/*
		 * Not yet properly implemented.  Need to combine with a burnin slider.
		 */

//	
//    public void updateCombinedTraces() {
//        TraceList[] traces = new TraceList[traceLists.size()];
//        traceLists.toArray(traces);
//        try {
//            combinedTraces = new CombinedTraces("Combined", traces);
//
//            analyseTraceList(combinedTraces);
//        } catch (TraceException te) {
//            // do nothing
//        }
//    }


}