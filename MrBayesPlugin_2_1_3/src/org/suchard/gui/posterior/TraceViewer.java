package org.suchard.gui.posterior;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.NoLongerViewedListener;
import dr.app.tracer.TraceRuntimeException;
import org.virion.jam.framework.Exportable;
import org.virion.jam.util.PrintUtilities;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Richard
 * @version $Id: TraceViewer.java 63305 2014-04-30 04:54:26Z jonas $
 */
abstract class TraceViewer extends DocumentViewer {

	protected final GeneiousTraces traces;
	protected final List<URN> documentUrns;

	private final TraceSelectionPanel traceSelectionPanel;
	private JPanel panel;
	private JProgressBar progressBar = new JProgressBar();
	private JSlider burninSlider = new JSlider(0, 50, TraceSelectionManager.DEFAULT_BURNIN);
    private JLabel burninLabel = new JLabel("Burn-in:");

    /**
	 * add selection listener to selection manager
	 */
	protected TraceViewer(AnnotatedPluginDocument[] docList) {
		progressBar.setValue(100);
		PosteriorDocument doc = (PosteriorDocument) docList[0].getDocumentOrNull();
		this.traces = doc.getTraces();
		this.documentUrns = Arrays.asList(docList[0].getURN());
		traceSelectionPanel = new TraceSelectionPanel(traces, new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
                int[] traceSelection = traceSelectionPanel.getSelection();
                try {
    				setViewerTraceSelection(traceSelection);
                } catch (TraceRuntimeException exception) {
                    Dialogs.showMessageDialog("The trace " + traces.getTraceName(traceSelection[0]) + " could not be loaded. Probably the file was created with MrBayes ≤ 3.2.1. Try recreating the file.", "Trace not loaded", null, Dialogs.DialogIcon.WARNING);
                    System.out.println("Couldn't initialize trace \"" + traces.getTraceName(traceSelection[0]) + "\"; deleting...");
                    traces.removeTrace(traceSelection[0]);
                }
			}
		});
		TraceSelectionManager.addChangeListener(selectionListener);
        burninSlider.setMaximum(traces.getMaxState()/2);
        burninSlider.setMajorTickSpacing(traces.getMaxState()/5);
        burninSlider.setMinorTickSpacing(traces.getMaxState()/20);
        burninSlider.setSnapToTicks(true);
        burninSlider.addChangeListener(new ChangeListener() {
			private int oldValue = burninSlider.getValue();

			public void stateChanged(ChangeEvent e) {
				int newValue = burninSlider.getValue();
				newValue = (int) Math.round(newValue / ((double)traces.getMaxState()/20)) * (traces.getMaxState()/20);
				if (newValue != oldValue) {
                    if(burninLabel != null)
                        burninLabel.setText("Burn in ("+newValue+"):");
                    oldValue = newValue;
					//int numBurnSteps = (int) ((newValue * 0.01 * traces.getMaxState()) / traces.getStepSize());
					//int newBurnin = traces.getStepSize() * numBurnSteps;
					traces.setBurnIn(newValue);
					analyseTraceList();
				}
			}
		});
        burninSlider.setValue((traces.getMaxState()* TraceSelectionManager.DEFAULT_BURNIN)/100);    
    }

	private int currentAnalysisId = 0;

	public void analyseTraceList() {
//        System.out.println("Analyse trace list");

		new Thread(new Runnable() {
			public void run() {
				currentAnalysisId++;
				int analysisId = currentAnalysisId;
				final int length = traces.getTraceCount();
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						progressBar.setMaximum(length);
					}
				});
				for (int i = 0; i < length; i++) {
					if (currentAnalysisId > analysisId) {
						return;
					}
					final int k = i;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							progressBar.setValue(k);
						}
					});
					//progressBar.setValue(i);
					traces.analyseTrace(i);
//    	    		System.out.println(i);
				}
//    	    	System.out.println("Finished");
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						progressBar.setValue(length);
                        try {
                            setViewerTraceSelection(traceSelectionPanel.getSelection());
                        } catch (TraceRuntimeException exception) {
                            // normally this trace is already deleted in another thread, so don't worry about it here...
                            System.out.println("Couldn't analyze trace \"" + traces.getTraceName(traceSelectionPanel.getSelection()[0]) + "\"; should be deleted when TraceViewer gets instantiated!");
                        }
                    }
				});

			}
		}).start();
	}

	private SelectionListener selectionListener = new SelectionListener() {

		/**
		 * Set selection in trace selection panel and updates the trace viewer to
		 * display the new selection
		 */
		void selectionChanged() {
			setTraceSelection(TraceSelectionManager.getSelection(documentUrns));
		}

		void burninChanged() {
//        	analyseTraceList();
			burninSlider.setValue(TraceSelectionManager.getBurnin(documentUrns));
		}
	};

	private NoLongerViewedListener noLongerViewedListener = new NoLongerViewedListener() {
		/**
		 * if temporary then set selection in selection manager (another viewer is about to be viewed).
		 * if not then remove listener from selection manager (viewer is being disposed).
		 */
		public void noLongerViewed(boolean isTemporary) {
			if (isTemporary) {
				TraceSelectionManager.setSelection(documentUrns, traceSelectionPanel.getSelection());
				TraceSelectionManager.setBurnin(documentUrns, burninSlider.getValue());
			} else {
				TraceSelectionManager.removeChangeListener(selectionListener);
			}
		}
	};

	public NoLongerViewedListener getNoLongerViewedListener() {
		return noLongerViewedListener;
	}

	/**
	 * @return the trace selection panel and the specific trace viewer.
	 * @see #getTraceComponent()
	 */
	public JComponent getComponent() {
		if (panel == null) {
			progressBar.setOpaque(false);
			burninSlider.setOpaque(false);
			//burninSlider.setMajorTickSpacing(10);
			//burninSlider.setMinorTickSpacing(5);
			burninSlider.setPaintTicks(true);
			burninSlider.setPaintLabels(true);
			//burninSlider.setSnapToTicks(false);
            //burninLabel = new JLabel("Burn-in:");

            panel = new JPanel(new BorderLayout(5, 5));
			JComponent traceComponent = getTraceComponent();
			panel.setOpaque(traceComponent.isOpaque());

			JPanel sidePanel = new JPanel(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(0, 3, 5, 3);
			c.weighty = 1.0;
			c.weightx = 1.0;
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.fill = GridBagConstraints.BOTH;
			sidePanel.setOpaque(false);

			sidePanel.add(traceSelectionPanel, c);
			c.insets = new Insets(0, 3, 0, 3);
			c.weighty = 0.0;
			sidePanel.add(burninLabel, c);
			c.insets = new Insets(0, 3, 5, 3);
			sidePanel.add(burninSlider, c);
			sidePanel.add(progressBar, c);
			c.insets = new Insets(15, 3, 5, 3);
//            addCitationText(sidePanel, c);

			panel.add(sidePanel, BorderLayout.EAST);
			panel.add(traceComponent, BorderLayout.CENTER);
		}
		return panel;
	}

	/**
	 * @return the graph component for the specific trace viewer. DOESN'T include
	 *         tabs, trace selection panel etc.
	 */
	protected abstract JComponent getTraceComponent();

	void setTraceSelection(int[] selection) {
		traceSelectionPanel.setSelection(selection);
	}

	/**
	 * Called when the selection traces are changed in the trace selection panel.
	 * Trace viewer/graph should update immediately to reflect this.
	 */
	protected abstract void setViewerTraceSelection(int[] selection);


	public Printable getPrintable() {
		return new Printable() {
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
				JComponent traceComponent = getTraceComponent();
				if (traceComponent instanceof Printable) {
					return ((Printable) traceComponent).print(graphics, pageFormat, pageIndex);
				} else {
					JComponent comp = ((Exportable) traceComponent).getExportableComponent();
					return PrintUtilities.printScaled(comp, graphics, pageFormat, pageIndex);
				}
			}
		};
	}
}
