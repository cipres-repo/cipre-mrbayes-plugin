/*
 * ExponentialDistribution.java
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
 * exponential distribution.
 *
 * (Parameter: lambda; mean: 1/lambda; variance: 1/lambda^2)
 *
 * The exponential distribution is a special case of the Gamma distribution
 * (shape parameter = 1.0, scale = 1/lambda).
 *
 * @version $Id: ExponentialDistribution.java 43609 2011-07-28 03:19:13Z matthew $
 *
 * @author Alexei Drummond
 * @author Korbinian Strimmer
 */
public class ExponentialDistribution implements Distribution
{
	//
	// Public stuff
	//

	/**
	 * Constructor
     *
     * @param lambda the rate of the exponential distribution (1/mean)
	 */
	public ExponentialDistribution(double lambda) {
		this.lambda = lambda;
	}

	public double pdf(double x) { return pdf(x, lambda); }
	public double logPdf(double x) { return logPdf(x, lambda); }
	public double cdf(double x) { return cdf(x, lambda); }
	public double quantile(double y) { return quantile(y, lambda); }
	public double mean() { return mean(lambda); }
	public double variance() { return variance(lambda); }
	
	public final UnivariateFunction getProbabilityDensityFunction() { return pdfFunction; }

	private UnivariateFunction pdfFunction = new UnivariateFunction() {
		public final double evaluate(double x) { return pdf(x); }
		public final double getLowerBound() { return 0.0; }
		public final double getUpperBound() { return Double.POSITIVE_INFINITY; }
	};


	/**
	 * probability density function of the exponential distribution
	 * (mean = 1/lambda)
	 *
	 * @param x argument
	 * @param lambda parameter of exponential distribution
	 *
	 * @return pdf value
	 */
	public static double pdf(double x, double lambda)
	{
		return lambda*Math.exp(-lambda*x);
	}

	/**
	 * the natural log of the probability density function of the distribution
	 * (mean = 1/lambda)
	 *
	 * @param x argument
	 * @param lambda parameter of exponential distribution
	 *
	 * @return log pdf value
	 */
	public static double logPdf(double x, double lambda)
	{
		return Math.log(lambda) - (lambda*x);
	}

	/**
	 * cumulative density function of the exponential distribution
	 *
	 * @param x argument
	 * @param lambda parameter of exponential distribution
	 *
	 * @return cdf value
	 */
	public static double cdf(double x, double lambda)
	{
		return 1.0-Math.exp(-lambda*x);
	}


	/**
	 * quantile (inverse cumulative density function) of the exponential distribution
	 *
	 * @param y argument
	 * @param lambda parameter of exponential distribution
	 *
	 * @return icdf value
	 */
	public static double quantile(double y, double lambda)
	{
		return -(1.0/lambda)*Math.log(1.0-y);
	}

	/**
	 * mean of the exponential distribution
	 *
	 * @param lambda parameter of exponential distribution
	 *
	 * @return mean
	 */
	public static double mean(double lambda)
	{
		return 1.0/(lambda);
	}

	/**
	 * variance of the exponential distribution
	 *
	 * @param lambda parameter of exponential distribution
	 *
	 * @return variance
	 */
	public static double variance(double lambda)
	{
		return 1.0/(lambda*lambda);
	}

	// Private

	protected double lambda;

}
