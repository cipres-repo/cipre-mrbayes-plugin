/*
 * FrequencyPlot.java
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

import dr.util.FrequencyDistribution;
import dr.util.Variate;

import java.awt.*;

public class FrequencyPlot extends Plot.AbstractPlot {

	FrequencyDistribution frequency = null;
	Variate raw = null;
	
	Paint barPaint = new Color(64,96,255);
	Paint quantilePaint = Color.red;
	
	boolean hasQuantiles = false;
	double quantiles = 0;
	
	boolean hasIntervals = false;
	double upperInterval = 0.0;
	double lowerInterval = 0.0;
	
	public FrequencyPlot(Variate data, int minimumBinCount) {
		super();
		setData(data, minimumBinCount);
	}
		
	public FrequencyPlot(double[] data, int minimumBinCount) {
		super();
		setData(data, minimumBinCount);
	}
		
	/**	
	*	Set data
	*/
	public void setData(double[] data, int minimumBinCount) {
		Variate.Double d = new Variate.Double(data);
		setData(d, minimumBinCount);
	}

	/**	
	*	Set data
	*/
	public void setData(Variate data, int minimumBinCount) {
	
		this.raw = data;
		frequency = getFrequencyDistribution(data, minimumBinCount);
		
		Variate.Double xData = new Variate.Double();
		Variate.Double yData = new Variate.Double();

		double x = frequency.getLowerBound();
		
		for (int i = 0; i < frequency.getBinCount(); i++) {
		
			xData.add(x);
			yData.add(0.0);

			x += frequency.getBinSize();
			
			xData.add(x);
			yData.add(frequency.getFrequency(i));
			
		}
		setData(xData, yData);
	}	
	
	/**	
	*	Get the FrequencyDistribution object
	*/
	protected FrequencyDistribution getFrequencyDistribution(Variate data, int minimumBinCount) {
		double min = data.getMin();
		double max = data.getMax();
		
		if (min == max) {
			if (min == 0) {
				min = -1.0;
			} else {
				min -= Math.abs(min / 10.0);
			}
			if (max == 0) {
				max = 1.0;
			} else {
				max += Math.abs(max / 10.0);
			}
		}
		
		Axis axis = new LinearAxis(Axis.AT_MAJOR_TICK, Axis.AT_MAJOR_TICK);
		axis.setRange(min, max);
		
		int majorTickCount = axis.getMajorTickCount();
		axis.setPrefNumTicks(majorTickCount, 4);

		double binSize = axis.getMinorTickSpacing();
		int binCount = (int)((axis.getMaxAxis() - axis.getMinAxis()) / binSize) + 2;
		
		while (binCount < minimumBinCount) {
			majorTickCount++;
			axis.setPrefNumTicks(majorTickCount, 4);

			binSize = axis.getMinorTickSpacing();
			binCount = (int)((axis.getMaxAxis() - axis.getMinAxis()) / binSize);
		}	
		
		frequency = new FrequencyDistribution(axis.getMinAxis(), binCount, binSize);

		for (int i = 0; i < raw.getCount(); i++) {
			frequency.addValue(raw.get(i));		
		}
		
		return frequency;
	}
	
	/**	
	*	Set quantiles to use (0 for none).
	*/
	public void setQuantiles(double quantiles) {
		this.quantiles = quantiles;
		hasQuantiles = (quantiles > 0.0);
		hasIntervals = false;
	}

	/**	
	*	Set arbitrary intervals to use (0 for none).
	*/
	public void setIntervals(double upper, double lower) {
		hasQuantiles = false;
		hasIntervals = (upper > 0.0 || lower > 0.0);
		upperInterval = upper;
		lowerInterval = lower;
	}


	/**	
	 *	Set bar fill style. Use a barPaint of null to not fill bar. 
	 *  Bar outline style is set using setLineStyle
	 */
	public void setBarFillStyle(Paint barPaint) {
		this.barPaint = barPaint;
	}

	/**	
	 *	Paint data series
	 */
	protected void paintData(Graphics2D g2, Variate xData, Variate yData) {

		double x1, y1, x2, y2;
		double lower = 0.0, upper = 0.0;
		int n = xData.getCount();
		
		if (hasQuantiles) {
			lower = raw.getQuantile(quantiles);
			upper = raw.getQuantile(1.0 - quantiles);
		} else if (hasIntervals) {
			lower = lowerInterval;
			upper = upperInterval;
		}
			
		g2.setStroke(lineStroke);
		for (int i = 0; i < n; i+=2) {
		
			x1 = xData.get(i);
			y1 = yData.get(i);
			x2 = xData.get(i+1);
			y2 = yData.get(i+1);
			
			if (y1 != y2) {
				if (barPaint != null) {
					if (hasQuantiles || hasIntervals) {
						if (x1 < lower) {
							if (x2 <= lower) {
								g2.setPaint(quantilePaint);
								fillRect(g2, x1, y1, x2, y2);
							} else {
								g2.setPaint(quantilePaint);
								fillRect(g2, x1, y1, lower, y2);
								g2.setPaint(barPaint);
								fillRect(g2, lower, y1, x2, y2);
							}
						} else if (x2 > upper) {
							if (x1 >= upper) {
								g2.setPaint(quantilePaint);
								fillRect(g2, x1, y1, x2, y2);
							} else {
								g2.setPaint(barPaint);
								fillRect(g2, x1, y1, upper, y2);
								g2.setPaint(quantilePaint);
								fillRect(g2, upper, y1, x2, y2);
							}
						} else {
					
							g2.setPaint(barPaint);
							fillRect(g2, x1, y1, x2, y2);
						}
					} else {
				
						g2.setPaint(barPaint);
						fillRect(g2, x1, y1, x2, y2);
					}
				}
				
				if (lineStroke != null && linePaint != null) {
					g2.setStroke(lineStroke);
					g2.setPaint(linePaint);
					drawRect(g2, x1, y1, x2, y2);
				}
			}
		}
	}
}
