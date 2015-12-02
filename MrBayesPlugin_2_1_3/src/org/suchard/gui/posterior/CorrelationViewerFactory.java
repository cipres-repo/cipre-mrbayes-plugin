package org.suchard.gui.posterior;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;

import javax.swing.*;

/**
 * @author Richard
 * @version $Id: CorrelationViewerFactory.java 43609 2011-07-28 03:19:13Z matthew $
 */
public final class CorrelationViewerFactory extends DocumentViewerFactory {

	public String getName() {
		return "Correlation";
	}

	public String getDescription() {
		return "Correlation";
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
        return new CorrelationViewer(docList);
	}

	public ViewPrecedence getPrecedence() {
		return ViewPrecedence.HIGH;
	}
}

class CorrelationViewer extends TraceViewer {

	private GeneiousCorrelationPanel correlationPanel;

	public CorrelationViewer(AnnotatedPluginDocument[] docList) {
		super(docList);
		correlationPanel = new GeneiousCorrelationPanel();
		setTraceSelection(TraceSelectionManager.getSelection(documentUrns));
	}


	protected JComponent getTraceComponent() {
		return correlationPanel;
	}

	protected void setViewerTraceSelection(int[] selection) {
		int index1;
		int index2;
		if (selection.length != 2) {
			index1 = -1;
			index2 = -1;
		} else {
			index1 = selection[0];
			index2 = selection[1];
		}
		correlationPanel.setTraces(traces, index1, index2);
	}
}
