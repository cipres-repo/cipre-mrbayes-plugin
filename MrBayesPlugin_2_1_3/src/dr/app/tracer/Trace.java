/*
 * Trace.java
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

package dr.app.tracer;

import jebl.evolution.sequences.NucleotideState;
import jebl.evolution.sequences.Nucleotides;

import java.util.Map;
import java.util.HashMap;
import java.util.List;


/**
 * A simple class that stores a trace for a single statistic
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: Trace.java 43609 2011-07-28 03:19:13Z matthew $
 */
public class Trace {

	public static final int INITIAL_SIZE = 1000;
	public static final int INCREMENT_SIZE = 1000;
	
	public Trace(String name) {
		this.name = name;
    }
	
	public Trace(String name, double[] values) {

		this.values = new double[values.length];
		valueCount = values.length;
		System.arraycopy(values, 0, this.values, 0, values.length);
	}
	
	/**
	 * add a value
	 */
	public void add(double value) {
	
		if (valueCount == values.length) {
			double[] newValues = new double[valueCount + INCREMENT_SIZE];
			System.arraycopy(values, 0, newValues, 0, values.length);
			values = newValues;
		}
		
		values[valueCount] = value;
		valueCount++;
	}
	
	/**
	 * add all the values in an array of doubles
	 */
	public void add(double[] values) {
		for (int i = 0; i < values.length; i++) {
			add(values[i]);
		}
		valueCount += values.length;
	}
	
	public int getCount() { return valueCount; }
	public double getValue(int index) { return values[index]; }
	public void getValues(int start, double[] destination) {

        try {
            //ssh: I got an unreproduceable crash here at one point, so I've surrounded it in a try/catch
            System.arraycopy(values, start, destination, 0, valueCount - start);
        } catch (Exception e) {
            assert false : values.length+", "+start+", "+destination.length+", "+valueCount;
            e.printStackTrace();
        }
    }
	public void getValues(int start, double[] destination, int offset) { 
		System.arraycopy(values, start, destination, offset, valueCount - start);
	}

	public String getName() { return name; }
	public String getDescription() { return nameToDescription.get(name);}
	
	//************************************************************************
	// private methods
	//************************************************************************
	
	private double[] values = new double[INITIAL_SIZE];
	private int valueCount = 0;
	private String name;
    private static Map<String, String> nameToDescription = new HashMap<String, String>() {
        {
            // got these from the MrBayes manual http://mrbayes.csit.fsu.edu/mb3.1_manual.pdf
            put("LnL", "Log likelihood of cold chain");
            put("TL", "Total tree length");
            List<NucleotideState> canonicalStates = Nucleotides.getCanonicalStates();
            for(int i = 0; i < canonicalStates.size(); ++i) {
                String name = canonicalStates.get(i).getName();
                put("pi" + name , "Frequency of " + name + " nucleotides");
                for (int j = i + 1; j < canonicalStates.size(); ++j) {
                    String name2 = canonicalStates.get(j).getName();
                    put("r" + name + "-" + name2, "GTR rate " + name + "<->" + name2);
                }
            }
            put("alpha", "Shape parameter of gamma distribution");
            put("kappa", "Transition/transversion rate ratio");
            put("pinvar", "Proportion of invariable sites");
        }
    };
}