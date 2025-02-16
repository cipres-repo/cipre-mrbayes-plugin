/*
 * RiemannApproximation.java
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

package dr.math;

/**
 * Approximates the integral of a given function using
 * Riemann integration
 *
 * @author Alexei Drummond
 *
 * @version $Id: RiemannApproximation.java 43609 2011-07-28 03:19:13Z matthew $
 */
public class RiemannApproximation implements Integral {

	public RiemannApproximation(int sampleSize) {
		this.sampleSize = sampleSize;
	}

	/**
	 * @return the approximate integral of the given function
	 * within the given range.
	 * @param f the function whose integral is of interest
	 * @param min the minimum value of the function
	 * @param max the  upper limit of the function
	 */
	public double integrate(UnivariateFunction f, double min, double max) {
	
		double integral=0.0;
		
		double gridpoint = min;
		double step = (max - min) / sampleSize;
		for (int i =1; i <= sampleSize; i++) {
			integral += f.evaluate(gridpoint);
			gridpoint += step;
		}
		integral *= (max-min)/(double)sampleSize;
		return integral;
	}
	
	public static final void main(String[] args) {
	
		UnivariateFunction normalPDF = new NormalDistribution(0.0, 1.0).getProbabilityDensityFunction();
		UnivariateFunction normalPDF2 = new NormalDistribution(0.0, 1.0).getProbabilityDensityFunction();
		UnivariateFunction normalPDF3 = new NormalDistribution(0.0, 1.0).getProbabilityDensityFunction();
		
		double Z = 1.0;
		//double Z = Math.sqrt(2*Math.PI);
		CompoundFunction threeNormals = new CompoundFunction(new UnivariateFunction[] {normalPDF, normalPDF2, normalPDF3}, Z);
		
		System.out.println("Riemann approximation to the integral of a three normal distribution:");
		RiemannApproximation integrator = new RiemannApproximation(100000);
		System.out.println("integral(N(0.0, 1.0))=" + integrator.integrate(normalPDF, -4.0, 4.0));	
		System.out.println("integral(N(1.0, 2.0))=" + integrator.integrate(normalPDF2, -8.0, 8.0));	
		System.out.println("integral(N(2.0, 3.0))=" + integrator.integrate(normalPDF3, -16.0, 16.0));	
		
		
		double integral = integrator.integrate(threeNormals, -16.0, 16.0);
		System.out.println("Riemann approximation to the integral of the compound of three normal distribution:");
		System.out.println("integral(N(0.0, 1.0)*N(1.0, 2.0)*N(2.0, 3.0))=" + integral);	
		System.out.println("Estimate normalizing constant is " + (1.0/integral));
		
		
		
		/*System.out.println("Ten monte carlo approximations to the integral of a normal distribution:");
		MonteCarloIntegral integrator2 = new MonteCarloIntegral(10000);
		for (int i = 0; i < 10; i++) {
			System.out.println(integrator2.integrate(normalPDF, -4.0, 4.0));	
		}*/
	}
	
	
	
	
	private int sampleSize;
}
