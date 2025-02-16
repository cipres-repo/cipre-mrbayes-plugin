/*
 * Transform.java
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

package dr.util;

/**
 * interface for the transform of a continuous variable.
 * A static member Transform.LOG provides an instance of LogTransform
 *
 * @version $Id: Transform.java 43609 2011-07-28 03:19:13Z matthew $
 *
 * @author Andrew Rambaut
 */
public interface Transform
{
	/**
	 * @return the transformed value
	 */
	double transform(double value);

	/**
	 * @return the inverse transformed value
	 */
	double inverse(double value);

	/**
	 * @return the transform's name
	 */
	String getTransformName();
	

	public class LogTransform implements Transform {
	
		private LogTransform() { }
		
		public double transform(double value) {
			return Math.log(value);
		}

		public double inverse(double value) {
			return Math.exp(value);
		}
		
		public String getTransformName() { return "log"; }
	}
	
	public static final LogTransform LOG = new LogTransform();
}
