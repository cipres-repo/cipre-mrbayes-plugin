package org.suchard.gui.posterior;

import dr.app.tracer.TraceCorrelation;
import dr.app.tracer.TraceDistribution;
import dr.app.tracer.TraceException;
import dr.app.tracer.TraceList;



/**
 * An class for analysing multiple tracesets
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: GeneiousCombinedTraces.java 43609 2011-07-28 03:19:13Z matthew $
 */
 
public class GeneiousCombinedTraces implements TraceList {

	public GeneiousCombinedTraces(String name, TraceList[] traceLists) throws TraceException {

		if (traceLists == null || traceLists.length < 1) {
			throw new TraceException("Must have at least 1 Traces object in a CombinedTraces");
		}
		
		this.name = name;
		this.traceLists = new TraceList[traceLists.length];
		this.traceLists[0] = traceLists[0];
		
		for (int i = 1; i < traceLists.length; i++) {
			if (traceLists[i].getTraceCount() != traceLists[0].getTraceCount()) {
				throw new TraceException("Cannot add to a CombinedTraces: the count of new traces do not match existing traces");
			}
			
			if (traceLists[i].getStepSize() != traceLists[0].getStepSize()) {
				throw new TraceException("Cannot add to a CombinedTraces: the step sizes of the new traces do not match existing traces");
			}
			
			for (int j = 0; j < traceLists[0].getTraceCount(); j++) {
				if (!traceLists[i].getTraceName(j).equals(traceLists[0].getTraceName(j))) {
					throw new TraceException("Cannot add to a CombinedTraces: new traces do not match existing traces");
				}
			}				
			this.traceLists[i] = traceLists[i];
		}
		
		
	}
	
	/** @return the name of this trace list */
	public String getName() { return name; }

	/** @return the number of traces in this trace list */
	public int getTraceCount() { return traceLists[0].getTraceCount(); }
	     
	/** @return the index of the trace with the given name */
	public int getTraceIndex(String name) {
		return traceLists[0].getTraceIndex(name);
	}
	     
	/** @return the name of the trace with the given index */
	public String getTraceName(int index) {
		return traceLists[0].getTraceName(index);
	}
	
	/** @return the number of states excluding the burnin */
	public int getStateCount() {
		int sum = 0;
		for (int i = 0; i < traceLists.length; i++) {
			sum += traceLists[i].getStateCount();
		}
		return sum;
	}
	
	public int getBurnIn() { return 0; }
	
	/** @return the size of the step between states */
	public int getStepSize() {
		return traceLists[0].getStepSize();
	}
	
	public void getValues(int index, double[] destination) {
		int offset = 0;
		for (int j = 0; j < traceLists.length; j++) {
			((GeneiousTraces)traceLists[j]).getValues(index, destination, offset);
			offset += traceLists[j].getStateCount();
		}
	}
		
	/** @return the trace distribution statistic object for the given index */
	public TraceDistribution getDistributionStatistics(int index) {
		if (traceStatistics == null) {
			return null;
		}
		
		return traceStatistics[index];
	}

	/** @return the trace correlation statistic object for the given index */
	public TraceCorrelation getCorrelationStatistics(int index) {
		// Combined traceset cannot do correlation statistics...
		return null;
	}

	public void analyseTrace(int index) {
		double[] values = new double[getStateCount()];

		if (traceStatistics == null) {
			traceStatistics = new TraceDistribution[getTraceCount()];
		}
		
		int offset = 0;
		double ESS = 0;
		for (int j = 0; j < traceLists.length; j++) {
			((GeneiousTraces)traceLists[j]).getValues(index, values, offset);
			offset += traceLists[j].getStateCount();
			TraceDistribution td = traceLists[j].getDistributionStatistics(index);
			ESS += td.getESS();
		}
		traceStatistics[index] = new TraceDistribution(values, traceLists[0].getStepSize(), ESS);
	}
	
	/** @return the number of trace lists that make up this combined */
	public int getTraceListCount() { return traceLists.length; }
	     
	/** @return the ith TraceList */
	public TraceList getTraceList(int index) {
		return traceLists[index];
	}
	     

	//************************************************************************
	// private methods
	//************************************************************************
	
	private TraceList[] traceLists = null;
	private TraceDistribution[] traceStatistics = null;

	private String name;
	
}