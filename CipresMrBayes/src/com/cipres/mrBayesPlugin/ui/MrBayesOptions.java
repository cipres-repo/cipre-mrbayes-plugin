package com.cipres.mrBayesPlugin.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdom.Element;
import org.json.simple.JSONObject;
import org.virion.jam.util.SimpleListener;

import com.biomatters.geneious.publicapi.components.Dialogs;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.documents.sequence.AminoAcidSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.NucleotideSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceAlignmentDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceCharSequence;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.DocumentOperationException;
import com.biomatters.geneious.publicapi.plugin.Options;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;
import com.biomatters.geneious.publicapi.utilities.SequenceUtilities;
import com.cipres.mrBayesPlugin.CipresMrBayesPlugin;
import com.cipres.mrBayesPlugin.utilities.MrBayesUtilities;

import jebl.evolution.sequences.GeneticCode;
/**
 * @author Matthew Cheung
 * @version $Id: MrBayesOptions.java 24127 2008-12-22 03:23:13Z steve $
 */
public class MrBayesOptions extends Options {

    private static final int MAX_SEED = 32000;
    private static final String CODON_MODEL_WARNING = "<html>Cannot use codon model. <br>The translation of one or more documents contains a stop codon.</html>";
    //models
    public static final String JC69 = "JC69";
    public static final String HKY85 = "HKY85";
    public static final String GTR = "GTR";
    public static final String CODON = "Codon (M1)";

    //amino acid rates
    private static final String[] AMINO_ACID_RATES = new String[]{
            "poisson"
            , "jones"
            , "dayhoff"
            , "mtrev"
            , "mtmam"
            , "wag"
            , "rtrev"
            , "cprev"
            , "vt"
            , "blosum"
            , "equalin"
//            ,"gtr"    todo, gtr causes Geneious to crash with invalid xml element name
    };

    //rate variations
    public static final String EQUAL = "equal";
    public static final String GAMMA = "gamma";
    public static final String PROPINV = "propinv";
    public static final String INVGAMMA = "invgamma";

    // Options name
    public static final String LENGTH = "chainLength";
    public static final String BURNIN = "burnin";
    public static final String SUB_FREQ = "subsampleFrequency";
    public static final String NUM_CHAINS = "numberOfChains";
    public static final String TEMP = "chainTemp";
    public static final String SEED = "randomSeed";
    public static final String RATE_VAR = "rateVariation";
    public static final String MODEL = "model";
    public static final String AMINO_ACID_RATE_MATRIX = "aminoAcidRateMatrix";
    public static final String GAMMA_CATS = "gammaCategories";
    public static final String PRIOR_TASKS = "priorTasks";
    public static final String BRANCH_LENGTH = "branchLength";
    public static final String TREE_AGE_ALPHA = "treeAgeAlpha";
    public static final String TREE_AGE_BETA = "treeAgeBeta";
    public static final String SHAPE_ALPHA = "shapeAlpha";
    public static final String OUTGROUP = "outgroup";
    static final String DUPLICATE_NAME_MESSAGE = "MrBayes cannot be performed on a document with more than one sequence with the same name.";

    // Options
    private static BooleanOption useCustomCommandBlock;
    private static MultipleLineStringOption commandBlockOption;
    private static IntegerOption length;
    private static IntegerOption burnin;
    private static IntegerOption subsampleFrequency;
    private static ComboBoxOption rateVariation;
    private static IntegerOption gammaCategories;
	private static RadioOption<OptionValue> priorTasks;
    private static DoubleOption branchLength;
    private static DoubleOption treeAgeAlpha;
    private static DoubleOption treeAgeBeta;
    private static DoubleOption shapeAlpha;
    private static AutoHideLabelOption codonModelWarning;
    private static Options clustalOptions;
    private static IntegerOption randomSeed;
    private static Options.Option[] affectedByRateVariation;
    private static HashMap<String, Options> serviceOptions;

    private ButtonOption restoreButton;
    ButtonOption restoreButton2;
    
    public static JSONObject jsonCommand = new JSONObject();
    public static JSONObject jsonInterface = new JSONObject();

    public static JSONObject getJsonCommand() {
		return jsonCommand;
	}

	@SuppressWarnings("unchecked")
	public static void setJsonCommand(String key, Object value) {
		jsonCommand.put(key, value);
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject getJsonInterface(){
		jsonInterface.put("Gamma Categories", gammaCategories.getValue());
    	jsonInterface.put("Length", length.getValue());
    	jsonInterface.put("Burning", burnin.getValue());
    	jsonInterface.put("SubsampleFrequency", subsampleFrequency.getValue());
    	jsonInterface.put("Rate Variation", rateVariation.getValue());
    	jsonInterface.put("Prior Tasks", priorTasks.getValue());
    	jsonInterface.put("Branch Length", branchLength.getValue());
    	jsonInterface.put("Tree Age Alpha", treeAgeAlpha.getValue());
    	jsonInterface.put("Tree Age Beta", treeAgeBeta.getValue());
    	jsonInterface.put("Shape Parameter", shapeAlpha.getValue());
    	jsonInterface.put("Random Seed", randomSeed.getValue());
		return jsonInterface;
	}

	@SuppressWarnings("unchecked")
	public static void setJsonInterface(String key, Object value){
		jsonInterface.put(key, value);
	}
	private boolean panelCreated = false;
    


    public MrBayesOptions(Element e) throws XMLSerializationException {
        super(e);
        List<Element> serviceOptionList = e.getChildren("gridServiceOptions");
        serviceOptions = new HashMap<String, Options>();

//        for (Element xml : serviceOptionList) {
//            String id = xml.getAttributeValue("serviceId");
//            if (id != null) {
//                GeneiousService service = PluginUtilities.getGeneiousService(id);
//                if (service instanceof GeneiousGridService) {
//                    GeneiousGridService gridService = (GeneiousGridService) service;
//                    Options serviceOptions1 = gridService.getOptions("MrBayes", true);
//                    serviceOptions1.valuesFromXML(xml);
//                    serviceOptions.put(id, serviceOptions1);
//                }
//            }
//        }
    }

    private boolean duplicateNames = false;

    /**
     * WARNING you will need to add the field clustalOptions as childOptions somehow if you want this class to be properly XMLSerializable.
     *
     *
     * @param selectedDocuments
     * @throws DocumentOperationException
     */
    @SuppressWarnings("OverlyLongMethod")
    public MrBayesOptions(final AnnotatedPluginDocument[] selectedDocuments) throws DocumentOperationException {
        super(MrBayesOptions.class);
        final ArrayList<Option> allOptions = new ArrayList<Option>();
        Boolean isProtein = null;
        List<OptionValue> outgroupValues = new ArrayList<OptionValue>();
        
        //Check to see how many documents are selected *Error checking & error messages
        if (selectedDocuments.length > 1 && selectedDocuments.length < 10000) { //over 10000 becomes too much for a combobox so leave the outgroup list empty and it will use a string option instead
            isProtein = (AminoAcidSequenceDocument.class.isAssignableFrom(selectedDocuments[0].getDocumentClass()));
            int maxLength = 0;
            for (AnnotatedPluginDocument doc : selectedDocuments) {
                maxLength = Math.max(((SequenceDocument) doc.getDocument()).getSequenceLength(), maxLength);
            }
            for (AnnotatedPluginDocument selectedDocument : selectedDocuments) {
                OptionValue outgroupValue = new OptionValue(selectedDocument.getName(), selectedDocument.getName());
                if (outgroupValues.contains(outgroupValue)) {
                    addLabel(DUPLICATE_NAME_MESSAGE);
                    duplicateNames = true;
                    return;
                }
                outgroupValues.add(outgroupValue);
            }
        } else if (selectedDocuments.length == 1) {
            SequenceAlignmentDocument alignment = ((SequenceAlignmentDocument) selectedDocuments[0].getDocument());
            isProtein = alignment.getSequence(0) instanceof AminoAcidSequenceDocument;
            if (alignment.getNumberOfSequences() > 100 * 1000) {
                addLabel("Alignment too large to run MrBayes");
                return;
            }
            if (alignment.getNumberOfSequences() < 10000) { //over 10000 becomes too much for a combobox so leave the outgroup list empty and it will use a string option instead
                for (SequenceDocument sequenceDocument : alignment.getSequences()) {
                    OptionValue outgroupValue = new OptionValue(sequenceDocument.getName(), sequenceDocument.getName());
                    if (outgroupValues.contains(outgroupValue)) {
                        duplicateNames = true;
                        addLabel(DUPLICATE_NAME_MESSAGE);
                        return;
                    }
                    outgroupValues.add(outgroupValue);
                }
            }
        }
 
        DocumentOperation clustalOperation = PluginUtilities.getDocumentOperation("com.biomatters.plugins.clustal.ClustalOperation");
        if (clustalOperation == null && selectedDocuments.length > 1) {
            addLabel("Please enable the ClustalW Plugin in preferences to run MrBayes on unaligned sequences");
            return;
        }
        if (clustalOperation != null && selectedDocuments.length > 1) {
            clustalOptions = clustalOperation.getOptions(selectedDocuments);

            beginAlignHorizontally("", false);
            addCustomComponent(new JLabel("You need to align your sequences before you can run MrBayes", CipresMrBayesPlugin.warningIcon.getIcon16(), JLabel.LEFT));
            ButtonOption configureButton = addButtonOption("alignFirst", "", "Configure...");
            configureButton.setValue("Configure...");
            endAlignHorizontally();

            configureButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    Element oldValues = clustalOptions.valuesToXML("options");
                    if (!Dialogs.showOptionsDialog(clustalOptions, "Configure ClustalW", true)) {
                        clustalOptions.valuesFromXML(oldValues);
                    }
                }
            });
            addLabel(" ");
        }
        
//////////Command block option code block begins
        beginAlignHorizontally(null, false);
        useCustomCommandBlock = addBooleanOption("useCustomCommandBlock", "Use custom command block", false);
        final ButtonOption editCommandBlock = addButtonOption("editCommandBlock", "", "Edit command block");
        final JComponent customBinaryHelp = new JButton(new AbstractAction("", IconUtilities.getIcons("help16.png").getIcon16()) {
            public void actionPerformed(ActionEvent e) {
                Dialogs.showMessageDialog("If you are using a custom MrBayes 3.2.1 executable with CUDA/Beagle support, you can add\n\nset usebeagle=yes beagledevice=gpu;\n\n" +
                        " in the custom command block to enable it.  Refer to the MrBayes documentation for more help.");
            }
        });

        final StringOption customCommandBlockStorage = addStringOption("customCommandBlock", "", "");
        customCommandBlockStorage.setVisible(false);
        addCustomComponent(customBinaryHelp);
        endAlignHorizontally();
        useCustomCommandBlock.setValue(false);
        editCommandBlock.setEnabled(false);
        useCustomCommandBlock.addChangeListener(new SimpleListener() {
            public void objectChanged() {
                //set the edit button enabled state
                Boolean useCustomBlock = useCustomCommandBlock.getValue();
                editCommandBlock.setEnabled(useCustomBlock);

                //store the custom command block
                if (useCustomBlock) {
                    try {
                        customCommandBlockStorage.setValue(MrBayesUtilities.getGeneratedCommandBlockFromOptions(MrBayesOptions.this));
                    } catch (DocumentOperationException e1) {
                        Dialogs.showMessageDialog(e1.getMessage(), "Command block error", null, Dialogs.DialogIcon.ERROR);
                    }
                }

                //set the enabled state of everything else
                for (Option o : allOptions) {
                    o.setEnabled(!useCustomCommandBlock.getValue());
                }
            }
        });
        editCommandBlock.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String commandBlock = "";
                try {
                    commandBlock = MrBayesUtilities.getGeneratedCommandBlockFromOptions(MrBayesOptions.this);
                } catch (DocumentOperationException e1) {
                    Dialogs.showMessageDialog(e1.getMessage(), "Command block error", null, Dialogs.DialogIcon.ERROR);
                }
                final AutoHideLabelOption warning = new AutoHideLabelOption("warning", "If you change the command block in a way that affects the output files, Geneious may have trouble importing your results.");
                Options commandBlockOptions = new Options(MrBayesOptions.class) {
                    protected JPanel createPanel() {
                        JPanel panel = new JPanel(new BorderLayout());
                        panel.add(warning.getComponent(), BorderLayout.NORTH);
                        panel.add(commandBlockOption.getComponent(), BorderLayout.CENTER);
                        JPanel buttonHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                        buttonHolder.add(restoreButton2.getComponent());
                        buttonHolder.add(restoreButton.getComponent());
                        panel.add(buttonHolder, BorderLayout.SOUTH);
                        return panel;
                    }
                };
                warning.setSpanningComponent(true);
                commandBlockOptions.addCustomOption(warning);
                commandBlockOption = commandBlockOptions.addMultipleLineStringOption("commandBlock", "", commandBlock, 20, false);
                //commandBlockOption.setSpanningComponent(true);
                commandBlockOption.setValue(customCommandBlockStorage.getValue());
                final String previousBlock = Preferences.userNodeForPackage(MrBayesOptions.class).get("mrbayesPreviousRunCommandBlock", "");
                commandBlockOptions.beginAlignHorizontally("", false);
                restoreButton = commandBlockOptions.addButtonOption("restoreButton", "", "Restore from previous run");
                restoreButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        commandBlockOption.setValue(previousBlock);
                    }
                });

                restoreButton2 = commandBlockOptions.addButtonOption("restoreButton2", "", "Restore from basic options");
                restoreButton2.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        commandBlockOption.setValue(commandBlockOption.getDefaultValue());
                    }
                });
                if (previousBlock.equals(""))
                    restoreButton.setEnabled(false);
                commandBlockOptions.endAlignHorizontally();
                Dialogs.DialogOptions dopt = new Dialogs.DialogOptions(Dialogs.OK_CANCEL, "Custom command block");
                dopt.setMaxWidth(999);
                if (Dialogs.showDialog(dopt, commandBlockOptions.getPanel()).equals(Dialogs.OK)) {
                    customCommandBlockStorage.setValue(commandBlockOption.getValue());
                }
            }
        });
//////////Command block option code block ends

        if (isProtein==null || isProtein) {
            List<OptionValue> aminoAcidRateValues = new ArrayList<OptionValue>();
            for (String aminoAcidRate : AMINO_ACID_RATES) {
                aminoAcidRateValues.add(new OptionValue(aminoAcidRate, aminoAcidRate));
            }
            beginAlignHorizontally(null, true);
            ComboBoxOption aminoAcidRateMatrix = addComboBoxOption(AMINO_ACID_RATE_MATRIX, "Rate Matrix (fixed):", aminoAcidRateValues, aminoAcidRateValues.get(0));
            aminoAcidRateMatrix.setDescription("Specifies the fixed model type for amino acid data");
            allOptions.add(aminoAcidRateMatrix);
        }
        if (isProtein==null || !isProtein) {
            final OptionValue[] models = new OptionValue[4];
            models[0] = new OptionValue(JC69, JC69);
            models[1] = new OptionValue(HKY85, HKY85);
            models[2] = new OptionValue(GTR, GTR);
            models[3] = new OptionValue(CODON, CODON);
            if (isProtein==null)
                endAlignHorizontally();
            codonModelWarning = new AutoHideLabelOption("codonModelWarning", CODON_MODEL_WARNING, JLabel.CENTER);
            codonModelWarning.setSpanningComponent(true);
            addCustomOption(codonModelWarning);
            codonModelWarning.setValue(CODON_MODEL_WARNING);
            beginAlignHorizontally(null, true);
            final ComboBoxOption model = addComboBoxOption(MODEL, "Substitution Model:", models, models[1]);

            allOptions.add(model);
            allOptions.add(codonModelWarning);

            SimpleListener codonListener = new SimpleListener() {
                public void objectChanged() {
                    if (!useCustomCommandBlock.getValue() && model.getValue() == models[3]) {
                        boolean enabled;
                        try {
                            enabled = !willCodonModelWork(selectedDocuments);
                        } catch (DocumentOperationException e) {
                            throw new RuntimeException(e.getMessage(), e);
                        }
                        codonModelWarning.setEnabled(enabled);
                    } else {
                        codonModelWarning.setEnabled(false);
                    }
                }
            };
            model.addChangeListener(codonListener);
            useCustomCommandBlock.addChangeListener(codonListener);
            codonListener.objectChanged();
            setJsonInterface("Substitution Model", model.getValue());
        }

        OptionValue[] rateVariations = new OptionValue[4];
        rateVariations[0] = new OptionValue(EQUAL, EQUAL, "No variation across sites");
        rateVariations[1] = new OptionValue(GAMMA, GAMMA, "Gamma distributed variation");
        rateVariations[2] = new OptionValue(PROPINV, PROPINV, "Proportion of sites invariable");
        rateVariations[3] = new OptionValue(INVGAMMA, INVGAMMA, "Proportion invariable, remaining gamma");
        rateVariation = addComboBoxOption(RATE_VAR, "Rate Variation:", rateVariations, rateVariations[1]);
        rateVariation.setDescription("Model for among-site rate variation");
        allOptions.add(rateVariation);
        endAlignHorizontally();

        beginAlignHorizontally(null, true);
        Option outgroupOption;
        if (outgroupValues.isEmpty()) {
            outgroupOption = addStringOption(OUTGROUP, "Outgroup:", "");
        }
        else {
            outgroupOption = addComboBoxOption(OUTGROUP, "Outgroup:", outgroupValues, outgroupValues.get(0));
        }
        outgroupOption.setDescription("Taxon name to use as outgroup. Note that the tree will only be rooted if branch lengths are clock-constrained.");
        allOptions.add(outgroupOption);
        gammaCategories = addIntegerOption(GAMMA_CATS, "Gamma Categories:", 4, 1, 10);
        gammaCategories.setDescription("Number of rate categories for the gamma distribution");
        allOptions.add(gammaCategories);
        endAlignHorizontally();

        addDivider("MCMC Settings");

        beginAlignHorizontally(null, true);
        length = addIntegerOption(LENGTH, "Chain Length:", 1100000, 1, Integer.MAX_VALUE);
        length.setDescription("Number of cycles for the MCMC algorithm");
        subsampleFrequency = addIntegerOption(SUB_FREQ, "Subsampling Freq:", 200, 1, Integer.MAX_VALUE);
        subsampleFrequency.setDescription("How often the Markov chain is sampled");

        endAlignHorizontally();
        beginAlignHorizontally(null, true);
        IntegerOption numberOfChains = addIntegerOption(NUM_CHAINS, "Heated Chains:", 4, 1, Integer.MAX_VALUE);
        numberOfChains.setDescription("<html>How many chains are run for each analysis for the MCMC variant<br>" +
                "Set to 1 to use regular MCMC sampling, without heating.</html>");
        burnin = addIntegerOption(BURNIN, "Burn-in Length:", 100000, 0, Integer.MAX_VALUE);
        burnin.setDescription("Number of samples that will be discarded before calculating summary statistics");
        endAlignHorizontally();

        beginAlignHorizontally(null, true);
        DoubleOption chainTemp = addDoubleOption(TEMP, "Heated Chain Temp:", 0.2, 0.0, Double.MAX_VALUE);
        chainTemp.setDescription("Temperature of heated chains");
        randomSeed = addIntegerOption(SEED, "Random Seed:", (int) Math.ceil(MAX_SEED * Math.random()), 1, Integer.MAX_VALUE);
        randomSeed.setDescription("Seed number for the random number generator");
        randomSeed.setValue(randomSeed.getDefaultValue());
        endAlignHorizontally();
        allOptions.add(length);
        allOptions.add(numberOfChains);
        allOptions.add(subsampleFrequency);
        allOptions.add(burnin);
        allOptions.add(randomSeed);
        allOptions.add(chainTemp);


        addDivider("Priors");

        OptionValue[] priors = new OptionValue[2];
        priors[0] = new OptionValue(PriorTask.UNCONSTRAINED.toString(), PriorTask.UNCONSTRAINED.toString());
        priors[1] = new OptionValue(PriorTask.MOLECULAR_CLOCK.toString(), PriorTask.MOLECULAR_CLOCK.toString());
        priorTasks = addRadioOption(PRIOR_TASKS, "", priors, priors[0], Alignment.VERTICAL_ALIGN);

        branchLength = addDoubleOption(BRANCH_LENGTH, "Exponential (", 10.0, 0.0, Double.MAX_VALUE);
        branchLength.setUnits(")");
        beginAlignHorizontally(null, false);
        treeAgeAlpha = addDoubleOption(TREE_AGE_ALPHA, "Gamma (", 1.0, 0.0, Double.MAX_VALUE);
        treeAgeAlpha.setUnits(" , ");
        treeAgeBeta = addDoubleOption(TREE_AGE_BETA, "", 1.0, 0.0, Double.MAX_VALUE);
        treeAgeBeta.setUnits(")");
        endAlignHorizontally();
        shapeAlpha = addDoubleOption(SHAPE_ALPHA, "Shape Parameter: Exponential (", 10.0, 0.0, Double.MAX_VALUE);
        shapeAlpha.setUnits(")");
        shapeAlpha.setDescription("Prior for the gamma shape parameter for among-site rate variation");

        affectedByRateVariation = new Options.Option[]{gammaCategories, shapeAlpha};

        priorTasks.addDependent(priors[0], branchLength, true);
        priorTasks.addDependent(priors[1], treeAgeAlpha, true);
        priorTasks.addDependent(priors[1], treeAgeBeta, true);
        priorTasks.setDependentPosition(RadioOption.DependentPosition.RIGHT);

        allOptions.add(priorTasks);
        allOptions.add(branchLength);
        allOptions.add(treeAgeAlpha);
        allOptions.add(treeAgeBeta);
        allOptions.add(shapeAlpha);

        SimpleListener rateVariationListener = new SimpleListener() {
            public void objectChanged() {
                boolean enabled = rateVariation.getValue().toString().equals(GAMMA) ||
                        rateVariation.getValue().toString().equals(INVGAMMA);
                for (Options.Option o : affectedByRateVariation)
                    o.setEnabled(enabled);
            }
        };
        rateVariation.addChangeListener(rateVariationListener);
        rateVariationListener.objectChanged();

        addLabel(" "); //spacer
        for (String s : textLines) {
            addLabel(s, true, true);
        }
        
        setJsonInterface("Outgroup", outgroupOption.getValue());
        setJsonInterface("Heated Chains", numberOfChains.getValue());
        setJsonInterface("Heated Chain Temp", chainTemp.getValue());
        
        
    }

    void setRandomSeed(int seed) {
        randomSeed.setValue(seed);
    }

    void setUseCustomCommandBlock(boolean useCustom) {
        useCustomCommandBlock.setValue(useCustom);
    }

    public Element toXML() {
        Element xml = super.toXML();
        if (serviceOptions != null) {
            Set<Map.Entry<String, Options>> entries = serviceOptions.entrySet();
            for (Map.Entry<String, Options> entry : entries) {
                Element values = entry.getValue().valuesToXML("gridServiceOptions");
                values.setAttribute("serviceId", entry.getKey());
                System.out.println(values);
                xml.addContent(values);
            }
        }
//        if(clustalOptions != null) { //todo: make this work in the fromXML() to make this class properly serializable
//            Element clustalValues = clustalOptions.valuesToXML("clustalOptions");
//            xml.addContent(clustalValues);
//        }
        return xml;
    }

    Boolean codonModelWorks = null;

    public boolean willCodonModelWork(AnnotatedPluginDocument[] docs) throws DocumentOperationException {
        if (codonModelWorks != null) {
            return codonModelWorks;
        }

        codonModelWorks = true;

        List<SequenceDocument> sequences;
        if (docs.length == 1) {
            SequenceAlignmentDocument alignment = (SequenceAlignmentDocument) docs[0].getDocument();
            SequenceDocument firstSequence = alignment.getSequence(0);
            if (firstSequence instanceof NucleotideSequenceDocument && firstSequence.getSequenceLength() % 3 != 0) {
                codonModelWorks = false;
                return codonModelWorks;
            }
            sequences = alignment.getSequences();
        } else {
            sequences = new ArrayList<SequenceDocument>();
            for (AnnotatedPluginDocument doc : docs) {
                sequences.add((SequenceDocument) doc.getDocument());
            }
        }

        //check for stop codons
        for (SequenceDocument doc : sequences) {
            CharSequence sequence;
            if (doc instanceof AminoAcidSequenceDocument) {
                sequence = doc.getCharSequence();
            } else {
                SequenceCharSequence nucleotideSequence = doc.getCharSequence();
                sequence = SequenceUtilities.asTranslation(SequenceUtilities.removeGaps(nucleotideSequence), GeneticCode.UNIVERSAL, true);
            }

            for (int i = 0; i < sequence.length(); i++) {
                if (sequence.charAt(i) == '*') {
                    codonModelWorks = false;
                    return codonModelWorks;
                }
            }
        }

        return codonModelWorks;
    }

    public String getSelectedGridServiceId() {
        if (getOption("local") == null || !((OptionValue) getOption("local").getValue()).getName().equals("remote"))
            return null;
        return getValueAsString("selectedService");
    }

    public Options getSelectedServiceOptions() {
        if (getOption("selectedService") == null)
            return null;
        return serviceOptions.get(getSelectedGridServiceId());
    }

    public String verifyOptionsAreValid() {
        if (duplicateNames) {
            return DUPLICATE_NAME_MESSAGE;
        }
        if (useCustomCommandBlock.getValue()) {
            Preferences.userNodeForPackage(MrBayesOptions.class).put("mrbayesPreviousRunCommandBlock", getValueAsString("customCommandBlock"));
        }
        if (length.getValue() <= subsampleFrequency.getValue())
            return "Chain length must be greater than subsampling frequency";

        if (burnin.getValue() >= length.getValue())
            return "Burn-in must be less than chain length.";

        //removing this check since it wasn't working for ages and now that i have fixed it again somebody is complaining they get this every time.
        //Ideally we would warn them and allow them to continue but since there is no nice API for doing that i'm just taking it out.
//        if (length.getValue() / subsampleFrequency.getValue() < 1000)
//             return "This subsampling frequency will return less than 1000 samples";

        if (gammaCategories.getValue() < 2 || gammaCategories.getValue() > 20)
            return "Gamma Categories must be between 2 and 20";

        if (branchLength.getValue() == 0)
            return "Branch length hyperprior must be positive";

        if (treeAgeAlpha.getValue() == 0 || treeAgeBeta.getValue() == 0)
            return "Tree age hyperprior must be positive";

        if (shapeAlpha.getValue() == 0)
            return "Shape parameter hyperprior must be positive";

        //the user has clicked OK, so save the serviceOptions preferences...
        if (getSelectedServiceOptions() != null) {
            getSelectedServiceOptions().savePreferences();
            return getSelectedServiceOptions().verifyOptionsAreValid();
        }

        return null;
    }

    @Override
    public boolean areValuesGoodEnoughToContinue() {
        if (duplicateNames) {
            return true;
        }
        if (getNumberOfSamples() > 6000) { //6000 because the default settings produce 5500 samples :P
            final String message = "These settings will produce a large number of samples which " +
                    "may not fit in to your computer's memory. Analysis results may be lost if you continue.\n\n" +
                    "Consider increasing the Subsampling Frequency.";
            String aContinue = "Continue";
            Dialogs.DialogOptions dialogOptions = new Dialogs.DialogOptions(new String[]{aContinue, "Cancel"}, "Excessive Sample Count", panelCreated ? getPanel() : null, Dialogs.DialogIcon.WARNING);
            Object choice = Dialogs.showDialog(dialogOptions, message);
            if (!aContinue.equals(choice)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return number of samples = chain length * 1000 / subsample frequency
     */
    public int getNumberOfSamples() {
        return (length.getValue()) / subsampleFrequency.getValue();
    }

    public void setChainlength(int chainLength) {
        length.setValue(chainLength);
    }

    public void setSubsamplingFrequency(int freq) {
        subsampleFrequency.setValue(freq);
    }

    /**
     * Returns null if no matrix should be specified
     */
    public String getAminoAcidRateMatrix() {
        String matrix = getValueAsString(AMINO_ACID_RATE_MATRIX);
        return matrix.length() == 0 ? null : matrix;
    }

    static final String[] textLines = {
            "Cipres MrBayes",
    };

    public PriorTask getPriorType() {
        return PriorTask.fromName(priorTasks.getValue().toString());
    }

    public void setUnconstrained(boolean unconstrained) {
        PriorTask priorTask = unconstrained? PriorTask.UNCONSTRAINED: PriorTask.MOLECULAR_CLOCK;
        priorTasks.setValue(new OptionValue(priorTask.toString(), priorTask.toString()));
    }

    public void setBurnIn(int i) {
        burnin.setValue(i);
    }

    public static enum PriorTask {
        UNCONSTRAINED("Unconstrained Branch Lengths:"),
        MOLECULAR_CLOCK("Molecular Clock with Uniform Branch Lengths:");

        private String name;

        PriorTask(String name) {

            this.name = name;
        }

        public String toString() {
            return name;
        }

        public static PriorTask fromName(String name) {
            for (PriorTask task : values()) {
                if (task.name.equals(name)) {
                    return task;
                }
            }
            return null;
        }
    }

    @Override
    protected JPanel createPanel() {
        panelCreated = true;
        for (Option option : getOptions()) {
            if (option instanceof ComboBoxOption) {
                ((JComboBox) option.getComponent()).setPrototypeDisplayValue("sequence name");
            }
        }
        return super.createPanel();
    }
}

class AutoHideLabelOption extends Options.Option<String, JLabel> {

    private boolean visible = true;
    private JLabel label;

    private String text;
    private int alignment;

    public AutoHideLabelOption(Element e) throws XMLSerializationException {
        super(e);
    }

    public AutoHideLabelOption(String name, String text) {
        this(name, text, JLabel.LEFT);
    }

    public AutoHideLabelOption(String name, String text, int alignment) {
        super(name, "", text);
        this.text = text;
        this.alignment = alignment;
    }

    public String getValueFromString(String value) {
        return value;
    }

    protected void setValueOnComponent(JLabel component, String value) {
        component.setText(value);
    }

    protected JLabel createComponent() {
        if (label != null)
            return label;
        //noinspection MagicConstant
        label = new JLabel(text, alignment) {
            public void paintComponent(Graphics g) {
                if (visible && isEnabled()) {
                    super.paintComponent(g);
                }
            }
        };
        Icon warningIcon = CipresMrBayesPlugin.warningIcon.getIcon16();
        if (warningIcon instanceof ImageIcon) {
            Image icon16 = ((ImageIcon) warningIcon).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            warningIcon = new ImageIcon(icon16);
        }
        label.setOpaque(false);
        label.setIcon(warningIcon);
        return label;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (label != null)
            label.repaint();
    }


    protected void handleSetEnabled(JLabel component, boolean enabled) {
        super.handleSetEnabled(component, enabled);
        component.setForeground(enabled ? SystemColor.textText : SystemColor.textInactiveText);
    }
}


class AdvisorButton extends Options.Option<String, JPanel> {

    private List<ActionListener> actionListeners;

    public AdvisorButton() {
        super("advisor", "", "Suggest ideal settings...");
        actionListeners = new ArrayList<ActionListener>();
    }

    public AdvisorButton(Element e) throws XMLSerializationException {
        super(e);
        actionListeners = new ArrayList<ActionListener>();
    }

    public String getValueFromString(String value) {
        return value;
    }

    protected void setValueOnComponent(JPanel component, String value) {
    }

    protected JPanel createComponent() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton button = new JButton(getDefaultValue(), CipresMrBayesPlugin.lightbulbIcon.getIcon16());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (ActionListener al : actionListeners)
                    al.actionPerformed(e);
            }
        });
        panel.add(button);
        return panel;
    }

    public void addActionListener(ActionListener al) {
        actionListeners.add(al);
    }
    
   


}
