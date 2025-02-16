/*
 * LinePlot.java
 *
 * Copyright (C) 2002-2006 Alexei Drummond and Andrew Rambaut
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package dr.gui.chart;

import dr.util.Variate;

import java.awt.*;
import java.awt.geom.GeneralPath;

/** 
 * Description:	A line plot.
 *
 * @author Andrew Rambaut	
 * @author Alexei Drummond	
 * @version	$Id: LinePlot.java 43609 2011-07-28 03:19:13Z matthew $
 */

public class LinePlot extends Plot.AbstractPlot {

		
	/**	
	* Constructor
	*/
	public LinePlot(Variate xData, Variate yData) {
		super(xData, yData);
	}
	
	/**	
	* Constructor
	*/
	public LinePlot(double[] xData, double[] yData) {
		super(xData, yData);
	}

	/**	
	*	Paint data series
	*/
	protected void paintData(Graphics2D g2, Variate xData, Variate yData) {
	
		double x = transformX(xData.get(0));
		double y = transformY(yData.get(0));
		
		GeneralPath path = new GeneralPath();
		path.moveTo((float)x, (float)y);
		
		int n = xData.getCount();			
		boolean failed = false;
		for (int i = 1; i < n; i++) {
			x = transformX(xData.get(i));
			y = transformY(yData.get(i));
			if (x == Double.NEGATIVE_INFINITY || y == Double.NEGATIVE_INFINITY) {
				failed = true;
			} else if (failed) {
				failed = false;
				path.moveTo((float)x, (float)y);
			} else {
				path.lineTo((float)x, (float)y);
			}
		}
		
		g2.setPaint(linePaint);
		g2.setStroke(lineStroke);
			
		g2.draw(path);
	}
	
}

