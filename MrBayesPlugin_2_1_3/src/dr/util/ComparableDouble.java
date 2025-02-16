/*
 * ComparableDouble.java
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
 * This class is unfortunate but necessary to conform to JDK 1.1
 *
 * @author Alexei Drummond
 * @version $Id: ComparableDouble.java 43609 2011-07-28 03:19:13Z matthew $
 */
public class ComparableDouble implements Comparable {

    private double value;

    public ComparableDouble(double d) {
        value = d;
    }

    public int compareTo(Object o) {

        ComparableDouble cd = (ComparableDouble) o;

        if (value < cd.value) {
            return -1;
        } else if (value > cd.value) {
            return 1;
        } else return 0;
    }

    public boolean equals(Object o) {
        return o instanceof ComparableDouble && ((ComparableDouble) o).value == value;
    }

    public double doubleValue() {
        return value;
    }

    public String toString() {
        return value + "";
    }
}
