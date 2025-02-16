package com.cipres.mrBayesPlugin.utilities;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.cipres.mrBayesPlugin.ui.MrBayesOptions;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Steven Stones-Havas
 * @version $Id$
 *          <p/>
 *          Created on 18/08/2008 10:38:02
 */
public class MrBayesUtilities {


	private static class NameAndIndexHolder implements Comparable {
        private String name;
        private int index;

        public NameAndIndexHolder(String name, int index) {
            this.name = name;
            this.index = index;
        }

        public String getName() {
            return name;
        }

        public int getIndex() {
            return index;
        }

        public int compareTo(Object o) {
            if (o instanceof NameAndIndexHolder) {
                return ((NameAndIndexHolder) o).getName().length() - name.length();
            }
            return Integer.MAX_VALUE;
        }
    }

    public static String getCustomOrGeneratedCommandBlockFromOptions(Options options, SequenceAlignmentDocument alignment) throws DocumentOperationException {
        String block;
        if ((Boolean) options.getValue("useCustomCommandBlock")) {
            block = options.getValue("customCommandBlock").toString();
        } else {
            block = getGeneratedCommandBlockFromOptions(options);
        }
        return renameSequencesInCommandBlock(block, alignment);
    }

    public static String renameSequencesInCommandBlock(String commandBlock, SequenceAlignmentDocument alignment) {
        NameAndIndexHolder[] names = new NameAndIndexHolder[alignment.getNumberOfSequences()];
        //sort the names in reverse order of length, so that if the names contain 'species' and 'species2', they are parsed properly
        for (int i = 0; i < alignment.getNumberOfSequences(); i++) {
            SequenceDocument sequence = alignment.getSequence(i);
            names[i] = new NameAndIndexHolder(sequence.getName(), i);
        }
        Arrays.sort(names);

        for (NameAndIndexHolder name : names) {
            commandBlock = commandBlock.replace(name.getName(), "" + (name.getIndex() + 1));
        }

        //commandBlock = commandBlock.replace(, ""+(i+1));
        return commandBlock;
    }

    public static String getGeneratedCommandBlockFromOptions(Options options) throws DocumentOperationException {
    	StringBuilder sb = new StringBuilder();
        String modelString = options.getValueAsString(MrBayesOptions.MODEL);
        String rateString = options.getValueAsString(MrBayesOptions.RATE_VAR);
        sb.append("Begin MrBayes;\n");
        String outgroup = options.getValueAsString(MrBayesOptions.OUTGROUP);
        if (outgroup.length() > 0) {
            sb.append("outgroup ").append(outgroup).append(";\n");
        }
        sb.append("lset ");
        if (modelString.equals(MrBayesOptions.JC69)){
            sb.append(" Nst=1");
        	MrBayesOptions.setJsonCommand("nstopts_", 1);
        }
        else if (modelString.equals(MrBayesOptions.HKY85)){
            sb.append(" Nst=2");
        	MrBayesOptions.setJsonCommand("nstopts_", 2);
        }
        else if (modelString.equals(MrBayesOptions.GTR)){
            sb.append(" Nst=6");
            MrBayesOptions.setJsonCommand("nstopts_", 6);
        }
        else if (modelString.equals(MrBayesOptions.CODON)){
            sb.append(" Nucmodel=Codon");
            MrBayesOptions.setJsonCommand("nucmodelopts_", "Codon");
        //else do nothing, we are probably dealing with protein sequences
        }
        if (rateString.equals(MrBayesOptions.EQUAL)){
            sb.append(" rates=equal");
            MrBayesOptions.setJsonCommand("rateopts_", "equal");
        }
        else if (rateString.equals(MrBayesOptions.PROPINV)){
            sb.append(" rates=propinv");
            MrBayesOptions.setJsonCommand("rateopts_", "propinv");
        }
        else if (rateString.equals(MrBayesOptions.GAMMA)) {
            sb.append(" rates=gamma");
            MrBayesOptions.setJsonCommand("rateopts_", "gamma");
            sb.append(" ngammacat=").append(options.getValueAsString(MrBayesOptions.GAMMA_CATS));
//            MrBayesOptions.setJsonCommand("ngammacat", options.getValueAsString(MrBayesOptions.GAMMA_CATS));
        } else if (rateString.equals(MrBayesOptions.INVGAMMA)) {
            sb.append(" rates=invgamma");
            MrBayesOptions.setJsonCommand("rateopts_", "invgamma");
            sb.append(" ngammacat=").append(options.getValueAsString(MrBayesOptions.GAMMA_CATS));
//            MrBayesOptions.setJsonCommand("ngammacat", options.getValueAsString(MrBayesOptions.GAMMA_CATS));
        } else throw new DocumentOperationException("Rate model '" + rateString + "' is not implemented.");
        sb.append(";\n");

        double branchPrior = (Double) options.getValue(MrBayesOptions.BRANCH_LENGTH);
        double alphaPrior = (Double) options.getValue(MrBayesOptions.SHAPE_ALPHA);
//            double omegaPrior = mrbayesOptions.codonOmega.getValue();
        double agePriorAlpha = (Double) options.getValue(MrBayesOptions.TREE_AGE_ALPHA);
        double agePriorBeta = (Double) options.getValue(MrBayesOptions.TREE_AGE_BETA);
        if (((MrBayesOptions) options).getPriorType().equals(MrBayesOptions.PriorTask.UNCONSTRAINED)) {
            sb.append("prset brlenspr=unconstrained:exponential(").append(branchPrior).append(")");
        } else {
            sb.append("prset brlenspr=clock:uniform treeagepr=gamma(").append(agePriorAlpha).append(",").append(agePriorBeta).append(")");
        }
        String aminoAcidMatrix = ((MrBayesOptions) options).getAminoAcidRateMatrix();
        if (aminoAcidMatrix != null) {
            sb.append(" aamodelpr=fixed(").append(aminoAcidMatrix).append(")");
            MrBayesOptions.setJsonCommand("aamodelpropts_", "fixed(".concat(aminoAcidMatrix).concat(")"));
        }
        if (rateString.equals(MrBayesOptions.GAMMA) ||
                rateString.equals(MrBayesOptions.INVGAMMA))
            sb.append(" shapepr=exponential(").append(alphaPrior).append(")");
        	MrBayesOptions.setJsonCommand("shapepropts_", "exponential");
        	MrBayesOptions.setJsonCommand("shapeprexp1_", String.valueOf(alphaPrior));
        if (modelString.equals(MrBayesOptions.JC69))
            sb.append(" statefreqpr=fixed(equal)");
        sb.append(";\n");

        int numIterations = (Integer) options.getValue(MrBayesOptions.LENGTH);

        sb.append("mcmc ngen=")
                .append(numIterations)
                .append(" samplefreq=")
                .append(options.getValueAsString(MrBayesOptions.SUB_FREQ))
                .append(" printfreq=1000" + " nchains=")
                .append(options.getValueAsString(MrBayesOptions.NUM_CHAINS))
                .append(" temp=")
                .append(new DecimalFormat("0.0").format(options.getValue(MrBayesOptions.TEMP)))
                        // 3.2.1 version
                .append(" savebrlens=yes starttree=random;\nset seed=")
                .append(options.getValue(MrBayesOptions.SEED))
                .append(";\n");
        MrBayesOptions.setJsonCommand("ngenval_", numIterations);
        MrBayesOptions.setJsonCommand("samplefreqval_", options.getValueAsString(MrBayesOptions.SUB_FREQ));
//        MrBayesOptions.setJsonCommand("printfreq", options.getValueAsString(MrBayesOptions.NUM_CHAINS));
        MrBayesOptions.setJsonCommand("tempval_", new DecimalFormat("0.0").format(options.getValue(MrBayesOptions.TEMP)));
        MrBayesOptions.setJsonCommand("sbrlensval_", "Savebrlens=Yes");
//        MrBayesOptions.setJsonCommand("starttree", "random");
        
        
        int burninvalue = (Integer) options.getValue(MrBayesOptions.BURNIN);
        if (burninvalue >= numIterations) {
            // smaller burnin - no summary
            burninvalue = numIterations / 2;
        }
        sb.append("sumt burnin=")
                .append(burninvalue / (Integer) options.getValue(MrBayesOptions.SUB_FREQ))
                .append(";\n");
        MrBayesOptions.setJsonCommand("sumtburnin_", burninvalue / (Integer) options.getValue(MrBayesOptions.SUB_FREQ));
        sb.append("End;\n");
        
        return sb.toString();
    
    }

    public static int getNumOfIterationsFromOptions(Options options) {
        if ((Boolean) options.getValue("useCustomCommandBlock")) {
            String commandBlock = options.getValue("customCommandBlock").toString();
            int beginIndex = commandBlock.indexOf("ngen=");
            if (beginIndex >= 0) {
                int endIndex = commandBlock.indexOf(" ", beginIndex);
                if (endIndex >= 0) {
                    String number = commandBlock.substring(beginIndex + 5, endIndex);
                    try {
                        return Integer.parseInt(number);
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                }
            }
            return -1;
        }
        return (Integer) options.getValue(MrBayesOptions.LENGTH);
    }

    public static HashMap<String, String> getMetadata(String jobName){
    	HashMap<String, String> metadata = new HashMap<String,String>();
    	metadata.put("statusEmail", "true");
    	metadata.put("clientJobName", jobName);
    	return metadata;
    }

    
    public static Map<String, Collection<String>> getVParams(JSONObject json){
    	ObjectMapper mapper = new ObjectMapper();
    	Map<String, Collection<String>> vParams = 
    			new HashMap<String, Collection<String>>();
    	mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    	try {
			vParams = mapper.readValue(json.toString(), new TypeReference<Map<String, Collection<String>>>(){});
		} catch (IOException e) {
			
			System.err.println("getVParam exception caught in Utilities");
			e.printStackTrace();
		}
    
    	return vParams;
    }
}
