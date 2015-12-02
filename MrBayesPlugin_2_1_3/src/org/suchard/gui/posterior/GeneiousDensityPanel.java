package org.suchard.gui.posterior;


import dr.app.tracer.DensityPanel;
import org.virion.jam.util.PrintUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/**
 * A panel that displays density plots of traces
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: GeneiousDensityPanel.java 43609 2011-07-28 03:19:13Z matthew $
 */
public class GeneiousDensityPanel extends DensityPanel implements Printable {

	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		PageFormat format1 = (PageFormat) pageFormat.clone();
		Paper paper1 = format1.getPaper();
		double citationProportion = 0.1; //vertical
		paper1.setImageableArea(
				paper1.getImageableX(),
				paper1.getImageableY(),
				paper1.getImageableWidth(),
				paper1.getImageableHeight() * (1 - citationProportion));
		paper1.setSize(paper1.getWidth(), paper1.getHeight());
		format1.setPaper(paper1);
		PrintUtilities.printScaled(getExportableComponent(), graphics.create(), format1, pageIndex);

		PageFormat format4 = (PageFormat) pageFormat.clone();
		Paper paper4 = format4.getPaper();
		Component citationText = PosteriorDocument.Utilities.getViewerCitationText();
		JDialog d = new JDialog();
		d.getContentPane().add(citationText, BorderLayout.CENTER);
		d.pack();
		double ratio = d.getPreferredSize().getWidth() / d.getPreferredSize().getHeight();
		double width = paper4.getImageableHeight() * citationProportion * ratio;
		width *= 0.5;
		paper4.setImageableArea(
				paper4.getImageableX() + paper4.getImageableWidth() / 2 - width,
				paper4.getImageableY() + paper4.getImageableHeight() * (1 - citationProportion),
				paper4.getImageableWidth(),
				paper4.getImageableHeight() * citationProportion);
		paper4.setSize(paper4.getWidth(), paper4.getHeight());
		format4.setPaper(paper4);
		return PrintUtilities.printScaled(citationText, graphics, format4, pageIndex);
	}
}
