/*
 * Axis.java
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

import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import dr.app.tracer.TraceRuntimeException;

import java.text.DecimalFormat;

/** 
 * Axis.java
 *
 * Description:	Provides an interface for axis
 * @author Andrew Rambaut	
 * @version	$Id: Axis.java 63305 2014-04-30 04:54:26Z jonas $
 */

public interface Axis {

	// These constants are used for automatic scaling to select exactly
	// where the axis starts and stops. 
	static public final int AT_MAJOR_TICK=0;
	static public final int AT_MAJOR_TICK_PLUS=1;
	static public final int AT_MINOR_TICK=2;
	static public final int AT_MINOR_TICK_PLUS=3;
	static public final int AT_DATA=4;
	static public final int AT_ZERO=5;
	
	/**	
	*	Set axis flags
	*/
	void setAxisFlags(int minAxisFlag, int maxAxisFlag);

	/**	
	*	Set preferred number of ticks
	*/
	void setPrefNumTicks(int prefNumMajorTicks, int prefNumMinorTicks);
	
	/**	
	*	Set integer scale
	*/
	void setIsDiscrete(boolean isDiscrete);

	/**	
	*	Set show label for first tick 
	*/
	void setLabelFirst(boolean labelFirst);

	/**	
	 *	Set the formatter for the tick labels
	 */
	void setFormatter(DecimalFormat formatter);
	
	/**	
	*	Set show label for last tick
	*/
	void setLabelLast(boolean labelLast);

	/**	
	*@return show label for first tick 
	*/
	boolean getLabelFirst();

	/**	
	*@return show label for last tick
	*/
	boolean getLabelLast();
	
	/**	
	*	Manually set the axis range and ticks
	*/
	void setManualAxis(double minTick, double maxTick, double majorTick, double minorTick);
		
	/**
	*	Set the axis to automatic calibration
	*/
	void setAutomatic();
		
	/**
	*	Set the axis to automatic calibration
	*/
	public void setAutomatic(int minAxisFlag, int maxAxisFlag);
		
	/**
	*	Set the range of the data
	*/
	public void setRange(double minValue, double maxValue);

	/**
	*	Adds the range of the data
	*/
	public void addRange(double minValue, double maxValue);

	/**
	*	Transform a value
	*/
	public double transform(double value);
	
	/**
	*	Untransform a value
	*/
	public double untransform(double value);
	
	/**
	*	@return a DecimalFormat for formating the axis labels
	*/
	public DecimalFormat getFormatter();
		
	/**
	*	@return minimum range of the axis
	*/
	public double getMinAxis();
	
	/**
	*	@return maximum range of the axis
	*/
	public double getMaxAxis();

	/**
	*	@return minimum range of the data
	*/
	public double getMinData();
	
	/**
	*	@return maximum range of the data
	*/
	public double getMaxData();

	/**
	*	@return the number of major tick marks along the axis
	*/
	public int getMajorTickCount();
	
	/**
	*	@return the number of minor tick marks within each major one
	*	By default all major ticks have the same number of minor ticks
	*	except the last which has none.
	*/
	public int getMinorTickCount(int majorTickIndex);
	
	/**
	*	@return the value of the majorTickIndex'th major tick
	*/
	public double getMajorTickValue(int majorTickIndex);
	
	/**
	*	@return the value of the minorTickIndex'th minor tick
	*/
	public double getMinorTickValue(int minorTickIndex, int majorTickIndex);
	
	/**
	*	@return the spacing between major ticks
	*/
	public double getMajorTickSpacing();
	
	/**
	*	@return the spacing between minor ticks
	*/
	public double getMinorTickSpacing();
	
	/**	class AbstractAxis
	*	This class provides a base class for all axis.
	*/

	public abstract class AbstractAxis implements Axis {

		// The minimum and maximum values of the data
		protected double minData=Double.POSITIVE_INFINITY, maxData=Double.NEGATIVE_INFINITY;

		// The number of major ticks and minor ticks within them
		protected int majorTickCount, minorTickCount; // calculated automatically
		
		// The prefered minimum number of ticks
		protected int prefMajorTickCount = 5, prefMinorTickCount = 2; // set manually
		
		// Flags using the above constants
		protected int minAxisFlag, maxAxisFlag;
		
		// The distance between major ticks and minor Ticks
		protected double majorTick, minorTick; // calculated automatically or set by user
		
		// The value of the first and last major tick
		protected double minTick, maxTick; // calculated automatically or set by user
		
		// The value of the beginning and end of the axis
		protected double minAxis, maxAxis;
		
		// Flags to give automatic scaling and integer division
		protected boolean isAutomatic=true, isDiscrete=false;
		
		// Flags to specify that the first tick and last tick should have labels.
		// It is up to the AxisPanel to do something about this.
		protected boolean labelFirst=false, labelLast=false;
		
		protected boolean isCalibrated = false;
		
		protected DecimalFormat formatter = new DecimalFormat("0.0#######");
		
		// Used internally
		private double epsilon;
		private int fraction;
		
		/**
		*	The constructor for automatic calibration
		*/
		public AbstractAxis() {
			this(AT_MAJOR_TICK, AT_MAJOR_TICK, false);
		}

		/**
		*	The constructor for automatic calibration
		*/
		public AbstractAxis(int minAxisFlag, int maxAxisFlag) {
			this(minAxisFlag, maxAxisFlag, false);
		}

		/**
		*	The constructor for automatic calibration
		*/
		public AbstractAxis(int minAxisFlag, int maxAxisFlag, boolean isDiscrete) {
			this.minAxisFlag = minAxisFlag;
			this.maxAxisFlag = maxAxisFlag;
			this.isDiscrete = isDiscrete;
			isAutomatic = true;
			isCalibrated = false;
		}

		/**	
		*	The constructor for manual calibration
		*/
		public AbstractAxis(double minTick, double maxTick,
							double majorTick, double minorTick) {
			setManualAxis(minTick, maxTick, majorTick, minorTick);
		}

		/**	
		*	Set axis flags
		*/
		public void setAxisFlags(int minAxisFlag, int maxAxisFlag) {
			this.minAxisFlag = minAxisFlag;
			this.maxAxisFlag = maxAxisFlag;
			isCalibrated = false;
		}

		/**	
		*	Set preferred number of ticks
		*/
		public void setPrefNumTicks(int prefMajorTickCount, int prefMinorTickCount) {
			this.prefMajorTickCount = prefMajorTickCount;
			this.prefMinorTickCount = prefMinorTickCount;
			isCalibrated = false;
		}

		/**	
		*	Set integer scale
		*/
		public void setIsDiscrete(boolean isDiscrete) {
			this.isDiscrete = isDiscrete;
			isCalibrated = false;
		}

		/**	
		*	Set show label for first tick flag
		*/
		public void setLabelFirst(boolean labelFirst) {
			this.labelFirst = labelFirst;
		}

		/**	
		*	Set show label for last tick flag
		*/
		public void setLabelLast(boolean labelLast) {
			this.labelLast = labelLast;
		}

		/**	
		*	Set the formatter for the tick labels
		*/
		public void setFormatter(DecimalFormat formatter) {
			this.formatter = formatter;
		}

		/**	
		*	return show label for first tick flag
		*/
		public boolean getLabelFirst() {
			if (getMinorTickCount(-1)==0) // The first tick is a label anyway
				return false;
			else
				return labelFirst;
		}

		/**	
		*	return show label for last tick flag
		*/
		public boolean getLabelLast() {
			if (getMinorTickCount(majorTickCount-1)==0) // The last tick is a label anyway
				return false;
			else
				return labelLast;
		}

		/**	
		*	Manually set the axis range and ticks
		*/
		public void setManualAxis(double minTick, double maxTick, 
									double majorTick, double minorTick) {
			this.minTick = minTick;
			this.maxTick = maxTick;
			
			this.majorTick = majorTick;
			this.minorTick = minorTick;

			majorTickCount = (int)((maxTick-minTick)/majorTick)+1; // Add 1 to include the last tick
			minorTickCount = (int)(majorTick/minorTick)-1;	// Sub 1 to exclude the major tick
			isAutomatic=false;
			isCalibrated = false;
		}
			
		/**	
		*	Set the axis to automatic calibration
		*/
		public void setAutomatic() {
			setAutomatic(AT_MAJOR_TICK, AT_MAJOR_TICK);
		}
			
		/**	
		*	Set the axis to automatic calibration
		*/
		public void setAutomatic(int minAxisFlag, int maxAxisFlag) {
			setAxisFlags(minAxisFlag, maxAxisFlag);
			isAutomatic = true;
			isCalibrated = false;
		}
			
		/**	
		*	Set the range of the data
		*/
		public void setRange(double minValue, double maxValue) {
			if (!Double.isNaN(minValue)) {
				this.minData = minValue;
			}
			if (!Double.isNaN(maxValue)) {
				this.maxData = maxValue;
			}
			
			isCalibrated = false;
		}
		
		/**	
		*	Adds the range to the existing range, widening if neccessary
		*/
		public void addRange(double minValue, double maxValue) {
			if (!Double.isNaN(maxValue) && maxValue > maxData) {
				maxData = maxValue;
			}
			if (!Double.isNaN(minValue) && minValue < minData) {
				minData = minValue;
			}
			
			//System.err.println("addRange("+minValue +", "+maxValue+")");
			//System.err.println("maxValue = "+maxData);
			//System.err.println("maxData = "+maxData);
			
			isCalibrated = false;
		}
		
		/**	
		*	A static method that uses the natural log to obtain log to base10.
		*	This is required for the linear autoCalibrate but will also be
		*	used by a derived class giving a log transformed axis.
		*/
		static public double log10(double inValue) {
			return Math.log(inValue)/Math.log(10.0);
		}	
				
		/**	autoCalibrate
		*	Attempt to find the optimum axis range and ticks.
		*	This will attempt to use atleast numMajorTick ticks on the axis.
		*/
			static private final int UNIT=0;
			static private final int HALFS=1;
			static private final int QUARTERS=2;
			static private final int FIFTHS=3;
			
		public void calibrate() {

			double minValue = minData;
			double maxValue = maxData;

            if( Double.isInfinite(minValue) || Double.isNaN(minValue) ||
                    Double.isInfinite(maxValue) || Double.isNaN(maxValue)) {
                throw new TraceRuntimeException("Bad values in trace file, can't calibrate: Min/Max : " + minValue + "/" + maxValue);
            }

			if (minAxisFlag==AT_ZERO ) {
				minValue = 0;
			}

			if (maxAxisFlag==AT_ZERO) {
				maxValue = 0;
			}
				
			double range = maxValue - minValue;
			if (range < 0.0) {
				range = 0.0;
			}
			
			epsilon = range * 1.0E-10;

			if (isAutomatic) {
				// We must find the optimum minMajorTick and maxMajorTick so 
				// that they contain the data range (minData to maxData) and
				// are in the right order of magnitude
				
				if (range < 1.0E-30) {
					if (minData < 0.0) {
						majorTick = Math.pow(10.0, Math.floor(log10(Math.abs(minData))));
						minTick = Math.floor(minData / majorTick) * majorTick;
						maxTick = 0.0;
					} else if (minData > 0.0) {
						majorTick = Math.pow(10.0, Math.floor(log10(Math.abs(minData))));
						minTick = 0.0;
						maxTick = Math.ceil(maxData / majorTick) * majorTick;
					} else {
						minTick = -1.0;
						maxTick = 1.0;
						majorTick = 1.0;
					}
					
					minorTick = majorTick;
					majorTickCount = 1;
					minorTickCount = 0;
					
				} else {
					// First find order of magnitude below the data range...
					majorTick = Math.pow(10.0, Math.floor(log10(range)));

					calcMinTick();
					calcMaxTick();
			
					calcMajorTick();
					calcMinorTick();
				}
			}
									
			minAxis = minTick;
			maxAxis = maxTick;
			
			handleAxisFlags();

			isCalibrated=true;
		}
		
		/**
		* Calculate the optimum minimum tick. Override to change default behaviour
		*/
		public void calcMinTick() {
			// Find the nearest multiple of majorTick below minData
			if (minData == 0.0)
				minTick = 0;
			else
				minTick = Math.floor(minData /  majorTick) * majorTick;
		}
			
		/**
		* Calculate the optimum maximum tick. Override to change default behaviour
		*/
		public void calcMaxTick() {
			// Find the nearest multiple of majorTick above maxData
			if (maxData == 0)
				maxTick = 0;
			//else if (maxData < 0.0) {
				// Added so that negative values are handled correctly -- AJD
				//maxTick = -Math.floor(-maxData / majorTick) * majorTick;
			//} 
			else {
				maxTick = Math.ceil(maxData / majorTick) * majorTick;
			}
		}
			
		/**
		* Calculate the optimum major tick distance. Override to change default behaviour
		*/
		public void calcMajorTick() {
			fraction=UNIT;
			
			// make sure that there are at least prefNumMajorTicks major ticks
			// by dividing up into halves, quarters, fifths or tenths 
			double u=majorTick;
			double r=maxTick-minTick;
			majorTickCount=(int)(r/u);
			
			while (majorTickCount < prefMajorTickCount) {
				u=majorTick/2;	// Try using halves
				if (!isDiscrete || u==Math.floor(u)) { // u is an integer
					majorTickCount=(int)(r/u);
					fraction=HALFS;
					if (majorTickCount >= prefMajorTickCount)
						break;
				}
								
				u=majorTick/4;	// Try using quarters
				if (!isDiscrete || u==Math.floor(u)) { // u is an integer
					majorTickCount=(int)(r/u);
					fraction=QUARTERS;
					if (majorTickCount >= prefMajorTickCount)
						break;
				}
								
				u=majorTick/5;	// Try using fifths
				if (!isDiscrete || u==Math.floor(u)) { // u is an integer
					majorTickCount=(int)(r/u);
					fraction=FIFTHS;
					if (majorTickCount >= prefMajorTickCount)
						break;
				}
											
				if (isDiscrete && (majorTick/10)!=Math.floor(majorTick/10)) {
					// majorTick/10 is not an integer so no point in further subdivision
					u=majorTick;
					majorTickCount=(int)(r/u);
					break;
				}
				
				majorTick/=10;	// finally just divide by ten
				u=majorTick;	// and go back to whole units
				majorTickCount=(int)(r/u);
				fraction=UNIT;
				
			}
			majorTick=u;
			
			if (isDiscrete && majorTick<1.0) {
				majorTick=1.0;
				majorTickCount=(int)(r/majorTick);
				fraction=UNIT;
			}
			
			majorTickCount++;	// Add 1 to give the final tick
			
			// Trim down any excess major ticks either side of the data range
			// Epsilon allows for any inprecision in the calculation
			while ((minTick + majorTick - epsilon)<minData) {
				minTick+=majorTick;
				majorTickCount--;
			}
			while ((maxTick - majorTick + epsilon)>maxData) {
				maxTick-=majorTick;
				majorTickCount--;
			}
		}
		
		/**
		* Calculate the optimum minor tick distance. Override to change default behaviour
		*/
		public void calcMinorTick() {
			minorTick=majorTick; // start with minorTick the same as majorTick
			double u=minorTick;
			double r=majorTick;
			minorTickCount=(int)(r/u);
			
			while (minorTickCount < prefMinorTickCount) {
				// if the majorTick was divided as quarters, then we can't
				// divide the minor ticks into halves or quarters.
				if (fraction!=QUARTERS) { 
					u=minorTick/2;	// Try using halves
					if (!isDiscrete || u==Math.floor(u)) { // u is an integer
						minorTickCount=(int)(r/u);
						if (minorTickCount>=prefMinorTickCount)
							break;
					}
										
					u=minorTick/4;	// Try using quarters
					if (!isDiscrete || u==Math.floor(u)) { // u is an integer
						minorTickCount=(int)(r/u);
						if (minorTickCount>=prefMinorTickCount)
							break;
					}
				}
						
				u=minorTick/5;	// Try using fifths
				if (!isDiscrete || u==Math.floor(u)) { // u is an integer
					minorTickCount=(int)(r/u);
					if (minorTickCount>=prefMinorTickCount)
						break;
				}
								
				if (isDiscrete && (minorTick/10)!=Math.floor(minorTick/10)) {
					// minorTick/10 is not an integer so no point in further subdivision
					u=minorTick;
					minorTickCount=(int)(r/u);
					break;
				}
				
				minorTick/=10;	// finally just divide by ten
				u=minorTick;	// and go back to whole units
				minorTickCount=(int)(r/u);
			}
			minorTick=u;
					
			minorTickCount--;
		}

		/**
		* Handles axis flags. Override to change default behaviour
		*/
		public void handleAxisFlags() {
			// Now we must honor the min/maxAxisFlag settings
			if (minAxisFlag==AT_MAJOR_TICK_PLUS || minAxisFlag==AT_MINOR_TICK_PLUS) {
				if (minAxis==minData) {
					majorTickCount++;
					minTick-=majorTick;
					minAxis=minTick;
				}
			}
			
			if (minAxisFlag==AT_MINOR_TICK_PLUS) {
				if ((minAxis+minorTick)<minData) {
					majorTickCount--;
					minTick+=majorTick;
					while ((minAxis+minorTick)<minData) {
						minAxis+=minorTick;
					}
				}
			} else if (minAxisFlag==AT_MINOR_TICK) {
				if ((minAxis+minorTick)<=minData) {
					majorTickCount--;
					minTick+=majorTick;
					while ((minAxis+minorTick)<=minData) {
						minAxis+=minorTick;
					}
				}
			} else if (minAxisFlag==AT_DATA) {
				if (minTick<minData) { // in case minTick==minData
					majorTickCount--;
					minTick+=majorTick;
					minAxis=minData;
				}
			} else if (minAxisFlag==AT_ZERO) {
				majorTickCount+=(int)(minTick/majorTick);
				minTick=0;
				minAxis=0;
			}

			if (maxAxisFlag==AT_MAJOR_TICK_PLUS || maxAxisFlag==AT_MINOR_TICK_PLUS) {
				if (maxAxis==maxData) {
					majorTickCount++;
					maxTick+=majorTick;
					maxAxis=maxTick;
				}
			}
			
			if (maxAxisFlag==AT_MINOR_TICK_PLUS) {
				if ((maxAxis-minorTick)>maxData) {
					majorTickCount--;
					maxTick-=majorTick;
					while ((maxAxis-minorTick)>maxData) {
						maxAxis-=minorTick;
					}
				}
			} else if (maxAxisFlag==AT_MINOR_TICK) {
				if ((maxAxis-minorTick)>=maxData) {
					majorTickCount--;
					maxTick-=majorTick;
					while ((maxAxis-minorTick)>=maxData) {
						maxAxis-=minorTick;
					}
				}
			} else if (maxAxisFlag==AT_DATA) {
				if (maxTick>maxData) { // in case maxTick==maxData
					majorTickCount--;
					maxTick-=majorTick;
					maxAxis=maxData;
				}
			} else if (maxAxisFlag==AT_ZERO) {
				majorTickCount+=(int)(-maxTick/majorTick);
				maxTick=0;
				maxTick=0;
			}
		}

		/**
		*	Scale a value to between 0 and 1.
		*/
		public double scaleValue(double value) {
			if (!isCalibrated)
				calibrate();
		
			double f=(transform(value)-transform(minAxis))/(transform(maxAxis)-transform(minAxis));
			return f;
		}

		/**
		*	@return a DecimalFormat for formating the axis labels
		*/
		public DecimalFormat getFormatter() { 
			return formatter; 
		}
		
		/**
		*	@return minimum range of the axis
		*/
		public double getMinAxis() { 
			if (!isCalibrated)
				calibrate();
		
			return minAxis; 
		}
		
		/**
		*	@return maximum range of the axis
		*/
		public double getMaxAxis() { 
			if (!isCalibrated)
				calibrate();
		
			return maxAxis;
		}
		
		/**
		*	@return minimum range of the data
		*/
		public double getMinData() { return minData; }
		
		/**
		*	@return maximum range of the data
		*/
		public double getMaxData() { return maxData; }
		
		/**	
		*	Returns the number of major tick marks along the axis
		*/
		public int getMajorTickCount() {
			if (!isCalibrated)
				calibrate();
			return majorTickCount;
		}
		
		/**	
		*	Returns the number of minor tick marks within each major one
		*	By default all major ticks have the same number of minor ticks
		*	except the last which has none.
		*/
		public int getMinorTickCount(int majorTickIndex) {
			if (!isCalibrated)
				calibrate();
				
			if (majorTickIndex == majorTickCount-1)
				return (int)((maxAxis-maxTick)/minorTick);
			else if (majorTickIndex==-1)
				return (int)((minTick-minAxis)/minorTick);
			else
				return minorTickCount; 
		}
		
		/**	getMajorTick
		*	Returns the value of the majorTickIndex'th major tick
		*/
		public double getMajorTickValue(int majorTickIndex) {
			if (!isCalibrated)
				calibrate();
			return (majorTickIndex*majorTick)+minTick;
		}
		
		/**	getMinorTick
		*	Returns the value of the minorTickIndex'th minor tick
		*/
		public double getMinorTickValue(int minorTickIndex, int majorTickIndex) {
			if (!isCalibrated)
				calibrate();
			// get minorTickIndex+1 to skip the major tick
			if (majorTickIndex==-1)
				return minTick-((minorTickIndex+1)*minorTick);
			else
				return ((minorTickIndex+1)*minorTick)+getMajorTickValue(majorTickIndex);
		}
		
		/**
		*	@return the spacing between major ticks
		*/
		public double getMajorTickSpacing() {
			if (!isCalibrated)
				calibrate();
			return majorTick;
		}
		
		/**
		*	@return the spacing between minor ticks
		*/
		public double getMinorTickSpacing() {
			if (!isCalibrated)
				calibrate();
			return minorTick;
		}
	
	}
	
}

