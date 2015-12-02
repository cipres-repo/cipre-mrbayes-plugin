package org.suchard.gui.posterior;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;

import javax.swing.*;

/**
 * @author Richard
 * @version $Id: RawTraceViewerFactory.java 43609 2011-07-28 03:19:13Z matthew $
 */
public final class RawTraceViewerFactory extends DocumentViewerFactory {

	public String getName() {
		return "Trace";
	}

	public String getDescription() {
		return "Trace";
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
        return new RawTraceViewer(docList);
	}

	public ViewPrecedence getPrecedence() {
		return ViewPrecedence.HIGH;
	}
}

class RawTraceViewer extends TraceViewer {

	private GeneiousRawTracePanel tracePanel;

	public RawTraceViewer(AnnotatedPluginDocument[] docList) {
		super(docList);
		tracePanel = new GeneiousRawTracePanel();
		setTraceSelection(TraceSelectionManager.getSelection(documentUrns));
	}


	protected JComponent getTraceComponent() {
		return tracePanel;
	}

	protected void setViewerTraceSelection(int[] selection) {
		tracePanel.setTraces(traces, selection);
	}
}