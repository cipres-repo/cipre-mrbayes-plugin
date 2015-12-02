/*
 * GeneiousTraces.java
 *
 */

package org.suchard.gui.posterior;

import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.plugin.Geneious;
import dr.app.tracer.Trace;
import dr.app.tracer.TraceCorrelation;
import dr.app.tracer.TraceDistribution;
import dr.app.tracer.TraceList;
import jebl.util.ProgressListener;
import org.jdom.Element;

import java.util.*;


/**
 * A class that stores a set of traces from a single chain
 *
 * @author Andrew Rambaut
 * @author Alexei Drummond
 * @version $Id: GeneiousTraces.java 63305 2014-04-30 04:54:26Z jonas $
 */

public class GeneiousTraces implements TraceList, XMLSerializable, XMLSerializable.OldVersionCompatible {
    
    public static final String GEN = "Gen";

    public GeneiousTraces(String name) {
        this.name = name;
    }

    /**
     * @return the number of traces in this traceset
     */
    public int getTraceCount() {
        return traces.size();
    }

    /**
     * @return the index of the trace with the given name
     */
    public int getTraceIndex(String name) {
        for (int i = 0; i < traces.size(); i++) {
            Trace trace = getTrace(i);
            if (name.equals(trace.getName())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @return the name of the trace with the given index
     */
    public String getTraceName(int index) {
        return getTrace(index).getName();
    }

    /**
     * @return the name of this traceset
     */
    public String getName() {
        return name != null ? name : "Untitled";
    }

    /**
     * @return the last state in the chain
     */
    public int getMaxState() {
        return lastState;
    }

    /**
     * @return the number of states excluding the burnin
     */
    public int getStateCount() {
//        System.err.println(lastState + " " + getBurnIn() + " " + stepSize);
        //System.exit(-1);
//        return ((lastState - getBurnIn()) / stepSize) + 1;
        return stateCount - (getBurnIn() / stepSize) + 1;
    }

    /**
     * @return the size of the step between states
     */
    public int getStepSize() {
        return stepSize;
    }

    public int getBurnIn() {
        if (burnIn < 0) {
            int n = lastState / stepSize;

            setBurnIn((n / 10) * stepSize);
        }
        return burnIn;
    }

    private boolean recalculateStatistics = true;

    public void setBurnIn(int burnIn) {
        this.burnIn = burnIn;
        recalculateStatistics = true;
    }

    public void getValues(int index, double[] destination) {
        //System.err.println("Debug: "+burnIn+" "+stepSize+" "+destination.length);
        //System.exit(-1);
        getTrace(index).getValues((burnIn / stepSize), destination, 0);
        //getTrace(index).getValues(0, destination, 0);

    }

    public void getValues(int index, double[] destination, int offset) {
        getTrace(index).getValues((burnIn / stepSize), destination, offset);
    }

    public TraceDistribution getDistributionStatistics(int index) {
        return getCorrelationStatistics(index);
    }

    public TraceCorrelation getCorrelationStatistics(int index) {
//		System.out.println("This is null?");
        if (traceStatistics == null) {
            return null;
        }
//		System.out.println("getting closer");

        return traceStatistics[index];
    }

    public void analyseTrace(int index) {
        //System.out.println("RUN!");
        double[] values = new double[getStateCount()];
        int offset = (getBurnIn() / stepSize);

        Trace trace = getTrace(index);
        trace.getValues(offset, values);
        TraceCorrelation traceCorrelation = new TraceCorrelation(values, stepSize);

        if (recalculateStatistics) {
            traceStatistics = new TraceCorrelation[getTraceCount()];
            recalculateStatistics = false;
        }
        //bug 3396, traceStatistics was being set to null in setBurnin from another
        //thread in between here.
        traceStatistics[index] = traceCorrelation;
    }

    public String toHTML() {
        StringBuffer sb = new StringBuffer();
        sb.append(GEN);
        for (Trace trace : traces) {
            sb.append("\t").append(trace.getName());
        }
        sb.append("\n");
        int length = traces.get(0).getCount();
        for (int i = 0; i < length; i++) {
            if (i == 0)
                sb.append("1");
            else
                sb.append(Integer.toString(i * stepSize));
            for (Trace trace : traces)
                sb.append("\t").append(String.format("%6.3f", trace.getValue(i)));
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public Geneious.MajorVersion getVersionSupport(VersionSupportType versionType) {
        return Geneious.MajorVersion.Version6_0; // update this (taking versionType into account) if toXML ever changes.
    }

    @Override
    public Element toXML() {
        return toXML(Geneious.getMajorVersion(), ProgressListener.EMPTY);
    }

    @Override
    public Element toXML(Geneious.MajorVersion majorVersion, ProgressListener progressListener) {
        // Update getVersionSupport if this method ever changes.
        Element parameters = new Element("parameters");
        // Add Gen first
        Element generation = new Element(GEN);
        generation.addContent("1.0");
        int state = stepSize;
        for (; state <= lastState; ) {
            generation.addContent(" " + (double) state);
            state += stepSize;
        }
        if (state != lastState + stepSize) {
            generation.addContent(" " + (double)lastState);
        }
        parameters.addContent(generation);
        for (Trace trace : traces) {
            Element traceElement = new Element(trace.getName());
            for (int i = 0, n = trace.getCount(); i < n; i++)
                traceElement.addContent(trace.getValue(i) + " ");
            parameters.addContent(traceElement);
        }
        return parameters;
    }


    public void fromXML(Element root) throws XMLSerializationException {
        Element parameters = root.getChild("parameters");
        List parameterList = parameters.getChildren();
        try {
            for (Object paraObject : parameterList) {
                Element paraElement = (Element) paraObject;
                if (paraElement.getName().equals(GEN)) {
                    String text = paraElement.getText();
                    StringTokenizer st = new StringTokenizer(text);
                    st.nextToken(); // Skip first, added already as 0
                    stateCount ++;
                    stepSize = (int) (new Double(st.nextToken())).doubleValue();
                    int state;
                    int prevState = 0;
                    while (st.hasMoreTokens()) {
                        state = (int)(new Double(st.nextToken())).doubleValue();
                        if (state == prevState) { //this is because of a bug i introduced where it would serialise the last state twice
                            continue;
                        }
                        prevState = state;
                        stateCount ++;
                    }
                    this.lastState = prevState;

                } else {

                    Trace trace = new Trace(paraElement.getName());
                    StringTokenizer st = new StringTokenizer(paraElement.getText());
                    while (st.hasMoreTokens()) {
                        trace.add(new Double(st.nextToken()));
                    }
                    traces.add(trace);
                }
            }
        } catch (NumberFormatException e) {
            throw new XMLSerializationException(e.getMessage(), e); // NumberFormatException's message already quotes the illegal String that was encountered
        }
        catch(NoSuchElementException ex){
            throw new XMLSerializationException("Number formatting error: could not read the step size from the file");
        }
    }

    /**
     * @return the trace for a given index
     */
    public Trace getTrace(int index) {
        return traces.get(index);
    }

    public void sortTracesByName() {
        Collections.sort(traces, new Comparator<Trace>() {
            public int compare(Trace o1, Trace o2) {
                if (o1.getName().startsWith("LnL") && !o2.getName().startsWith("LnL")) {
                    return -1;
                } else if (!o1.getName().startsWith("LnL") && o2.getName().startsWith("LnL")) {
                    return 1;
                }
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    public void removeTrace(int index) {
        traces.remove(index);
    }

    private ArrayList<Trace> traces = new ArrayList<Trace>();
    private TraceCorrelation[] traceStatistics = null;

    private String name;

    private int burnIn = -1;
    private int lastState;
    private int stepSize;
    private int stateCount = 0;
}

