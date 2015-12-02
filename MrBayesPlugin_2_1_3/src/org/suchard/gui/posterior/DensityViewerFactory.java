package org.suchard.gui.posterior;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;

import javax.swing.*;

/**
 * @author Richard
 * @version $Id: DensityViewerFactory.java 43609 2011-07-28 03:19:13Z matthew $
 */
public final class DensityViewerFactory extends DocumentViewerFactory {

	public String getName() {
		return "Density";
	}

	public String getDescription() {
		return "Density";
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
        return new DensityViewer(docList);
	}

	public ViewPrecedence getPrecedence() {
		return ViewPrecedence.HIGH;
	}
}

class DensityViewer extends TraceViewer {

	private GeneiousDensityPanel densityPanel;

	public DensityViewer(AnnotatedPluginDocument[] docList) {
		super(docList);
		densityPanel = new GeneiousDensityPanel();
		setTraceSelection(TraceSelectionManager.getSelection(documentUrns));
	}


	protected JComponent getTraceComponent() {
		return densityPanel;
	}

	protected void setViewerTraceSelection(int[] selection) {
		densityPanel.setTraces(traces, selection);
	}
}
