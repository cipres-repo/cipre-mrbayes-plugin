/*
 * Attributable.java
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

import java.util.HashMap;
import java.util.Iterator;

/**
 * Interface for associating attributes with an object.
 *
 * @version $Id: Attributable.java 43609 2011-07-28 03:19:13Z matthew $
 *
 * @author Andrew Rambaut
 */
public interface Attributable {
	
	public final static String ATTRIBUTE = "att";
	public final static String NAME = "name";
	public final static String VALUE = "value";

	/**
	 * Sets an named attribute for this object.
	 * @param name the name of the attribute.
	 * @param value the new value of the attribute.
	 */
	public void setAttribute(String name, Object value);

	/**
	 * @return an object representing the named attributed for this object.
	 * @param name the name of the attribute of interest.
	 */
	public Object getAttribute(String name);

	/**
	 * @return an iterator of the attributes that this object has.
	 */
	Iterator getAttributeNames();
	
	public static final class AttributeHelper implements Attributable {
		/**
		 * Sets an named attribute for this object.
		 * @param name the name of the attribute.
		 * @param value the new value of the attribute.
		 */
		public void setAttribute(String name, Object value) {
			attributes.put(name, value);
		}

		/**
		 * @return an object representing the named attributed for this object.
		 * @param name the name of the attribute of interest.
		 */
		public Object getAttribute(String name) {
			return attributes.get(name);
		}

		/**
		 * @return an iterator of the attributes that this object has.
		 */
		public Iterator getAttributeNames() {
			return attributes.keySet().iterator();
		}
	
		// **************************************************************
		// INSTANCE VARIABLE
		// **************************************************************
		
		private HashMap attributes = new HashMap();
	};
	
	
}


