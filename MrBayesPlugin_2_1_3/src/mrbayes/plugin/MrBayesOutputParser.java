package mrbayes.plugin;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.DocumentUtilities;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.implementations.DefaultPhylogenyDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentImportException;
import com.biomatters.geneious.publicapi.plugin.PluginUtilities;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;
import com.biomatters.geneious.publicapi.utilities.GeneralUtilities;
import com.biomatters.geneious.publicapi.utilities.SystemUtilities;
import jebl.evolution.graphs.Node;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.trees.ConsensusTreeBuilder;
import jebl.evolution.trees.Tree;
import jebl.evolution.trees.TreeBuilderFactory;
import jebl.util.CompositeProgressListener;
import jebl.util.ProgressListener;
import org.jdom.Element;
import org.jdom.Verifier;
import org.suchard.gui.posterior.GeneiousTraces;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Richard
 * @version $Id: MrBayesOutputParser.java 69072 2015-08-10 20:01:57Z richard $
 */
public class MrBayesOutputParser {

    static final String EXT_MCMC = ".mcmc";
    static final String EXT_POSTERIORS = ".p";
    static final String EXT_RAW_TREES = ".t";
    static final String EXT_CONSENSUS_TREE = ".con(.tre)?";
    static final String EXT_PARTITIONS = ".parts";
    static final String EXT_TRPROBS = ".trprobs";
    private static final String TREE_PATTERN = "(\\.(tree\\d+))?";
    private static final String RUN_PATTERN = "(\\.(run\\d+))?";
    private static final DocumentField PARTITION_FIELD = DocumentField.createStringField("partitionId", "", "partitionId");
    public static final String SORTED_TOPOLOGIES_SUFFIX = " - Sorted Topologies";

    /**
     * cache
     */
    private List<AnnotatedPluginDocument> rawTreesCache;
    private List<AnnotatedPluginDocument> sortedTopologiesCache;

    private final AnnotatedPluginDocument alignment;

    private final List<String> failures = new ArrayList<String>();

    /**
     * contains stats from the mcmc run as a whole?
     * one per analysis
     * eg. geneious.nex.mcmc
     */
    private final File mcmcFile;

    /**
     * Nexus file with source alignment and MrBayes command block
     * one per analysis
     * eg. geneious.nex
     */
    private final File commandFile;

    /**
     * Contains posterior statistics for each "run"
     * one per "run" (ie. "nruns" option in mcmc command)
     * eg. geneious.nex.p (one run)
     * or geneious.nex.run1.p, geneious.nex.run2.p... (multiple runs)
     */
    private final File[] posteriorsFiles;

    /**
     * Nexus file containing two consensus trees for a partition (1st one has partition probabilities, 2nd doesn't)
     * one per partition
     * eg. geneious.nex.con (single partition)
     * or geneious.nex.tree1.con, geneious.nex.tree2.con... (multiple partitions)
     */
    private final File[] consensusFiles;

    /**
     * Nexus file containing all generated raw trees for one "run" on a partition
     * one per partition per "run" (ie. "nruns" option in mcmc command)
     * eg. geneious.nex.t (single partition, one run)
     * or geneious.nex.tree1.run1.t, geneious.nex.tree2.run1.t... (multiple runs and multiple partitions)
     */
    private final File[] rawTreesFiles;

    /**
     * Consensus partition information in arbitrary format
     * one per partition
     * eg. geneious.nex.parts (single partition)
     * or geneious.nex.tree1.parts.. (multiple partitions)
     */
    private final File[] partitionsFiles;

    /**
     * Nexus file containing topologies sorted by probability for a partition
     * one per partition
     * eg. geneious.nex.trprobs (single partition)
     * or geneious.nex.tree1.trprobs...  (multiple partitions)
     */
    private final File[] trprobsFiles;

    /**
     *
     * @param mcmcFile
     * @param alignment source alignment document or null to use the one in the nexus command file
     * @throws IOException
     */
    public MrBayesOutputParser(File mcmcFile, AnnotatedPluginDocument alignment) throws IOException, DocumentImportException {
        this.mcmcFile = mcmcFile;
        checkFile(mcmcFile, "mcmc");
        File folder = mcmcFile.getParentFile();
        final String baseName = mcmcFile.getName().substring(0, mcmcFile.getName().lastIndexOf("."));

        commandFile = new File(folder, baseName);
        checkFile(commandFile, "command");

        if (alignment == null) {
            alignment = PluginUtilities.importDocuments(commandFile, ProgressListener.EMPTY).get(0);
        }
        this.alignment = alignment;

        posteriorsFiles = folder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().matches(baseName + RUN_PATTERN + EXT_POSTERIORS);
            }
        });
        consensusFiles = folder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().matches(baseName + TREE_PATTERN + EXT_CONSENSUS_TREE);
            }
        });
        rawTreesFiles = folder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().matches(baseName + TREE_PATTERN + RUN_PATTERN + EXT_RAW_TREES);
            }
        });
        partitionsFiles = folder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().matches(baseName + TREE_PATTERN + EXT_PARTITIONS);
            }
        });
        trprobsFiles = folder.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return !file.isDirectory() && file.getName().matches(baseName + TREE_PATTERN + EXT_TRPROBS);
            }
        });
    }

    private static void checkFile(File file, String typeName) throws IOException {
        if (!file.exists()) {
            throw new IOException("MrBayes " + typeName + " file not found: " + file.getAbsolutePath());
        }
        if (file.isDirectory()) {
            throw new IOException("MrBayes " + typeName + " file is a directory: " + file.getAbsolutePath());
        }
        if (!file.canRead()) {
            throw new IOException("Permission denied for MrBayes " + typeName + " file: " + file.getAbsolutePath());
        }
    }

    /**
     *
     * @return elements for constructing posterior output documents from the output
     * @throws IOException
     */
    public AnnotatedPluginDocument importPosteriorsDocument() throws XMLSerializationException, IOException {
        //todo progress

        String commandFileText = FileUtilities.getTextFromFile(commandFile);
        String commandLowerCase = commandFileText.toLowerCase();
        if (!commandLowerCase.contains("begin mrbayes")) {
            throw new IOException("MrBayes block not found in command file \"" + commandFile.getName() + "\". This does not appear to be a MrBayes result file.");
        }

        String sampleFreqPattern = "mcmc[.&&[^;]]*samplefreq\\s*=\\s*(\\d+)";
        Pattern pattern = Pattern.compile(sampleFreqPattern);
        Matcher matcher = pattern.matcher(commandLowerCase);
        int subsampleFrequency = 100; //default value
        if (matcher.find()) {
            try {
                subsampleFrequency = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                assert false : "failed to parse samplefreq \"" + commandLowerCase + "\"";
                //no biggie
            }
        }

        String burninPattern = "sumt[.&&[^;]]*burnin\\s*=\\s*(\\d+)";
        int burnin = -1;
        boolean wantConsensus = false;
        if (commandLowerCase.contains("sumt")) { //command block specifies consensus tree should be built
            wantConsensus = true;
            pattern = Pattern.compile(burninPattern);
            matcher = pattern.matcher(commandLowerCase);
            if (matcher.find()) {
                try {
                    burnin = Integer.parseInt(matcher.group(1));
                } catch (NumberFormatException e) {
                    assert false : "failed to parse burnin \"" + commandLowerCase + "\"";
                    //no biggie
                }
            }
        }

        Element root = new Element("posteriorResults");
        root.setAttribute(MrBayesPosteriorDocument.XML_PROGRAM, "MrBayes");
        root.addContent(new Element("commandFile").addContent(commandFileText));

        Pattern posteriorPattern = Pattern.compile(RUN_PATTERN + EXT_POSTERIORS);

        try {
            String mcmcFileText = readEntireTextFile(mcmcFile);
            root.addContent(new Element(MrBayesPosteriorDocument.XML_MCMC).addContent(mcmcFileText));
        } catch (IOException e) {
            failures.add("Failed to import mcmc diagnostics file: " + e.getMessage());
        }

        List<Tree> consensusTrees = Collections.emptyList();
        boolean renameSequences = true;

        if (consensusFiles.length >= 1) {
            try {
                consensusTrees = importConsensusTrees();
                wantConsensus = false;
            } catch (IOException e) {
                failures.add("Failed to import consensus trees, generating consensus instead: " + e.getMessage());
            }
        }

        if (wantConsensus) {
            consensusTrees = new ArrayList<Tree>();
            List<AnnotatedPluginDocument> rawTreeDocuments;
            try {
                rawTreeDocuments = importRawTrees(ProgressListener.EMPTY);
            } catch (IOException e) {
                failures.add("Failed to import raw trees: " + e.getMessage());
                rawTreeDocuments = Collections.emptyList();
            }
            for (AnnotatedPluginDocument rawTreeDocument : rawTreeDocuments) {
                List<? extends Tree> trees = ((DefaultPhylogenyDocument) rawTreeDocument.getDocumentOrCrash()).getTrees();
                Tree consensusTree = generateConsensusFromRawTrees(trees, burnin, ProgressListener.EMPTY);
                Object name = rawTreeDocument.getFieldValue(PARTITION_FIELD);
                if (name == null) {
                    name = "consensus";
                } else {
                    name = name + "_consensus";
                }
                consensusTree.setAttribute("name", name);
                consensusTrees.add(consensusTree);
            }
            renameSequences = false;
        }

        if(partitionsFiles.length == 1) {
            //todo multiple partition files?
            try {
                checkFile(partitionsFiles[0], "partition");
                String text = readEntireTextFile(partitionsFiles[0]);
                root.addContent(new Element(MrBayesPosteriorDocument.XML_PARTITION).addContent(text));
            } catch (IOException e) {
                failures.add("Failed to import partitions file: " + e.getMessage());
            }
        }

        Element parameters = new Element("parameters");
        for (File postFile : posteriorsFiles) {
            Matcher m = posteriorPattern.matcher(postFile.getName());
            m.find();
            String runId = m.group(2);

            //throw any exception from here because it is a bit tricky to recover
            parsePosteriorParameters(postFile, runId, subsampleFrequency, parameters);
        }
        root.addContent(parameters);

        String name = alignment.getName();
        if (consensusTrees.isEmpty()) {
            return DocumentUtilities.createAnnotatedPluginDocument(new MrBayesTreelessPosteriorDocument(name, root));
        } else {
            return DocumentUtilities.createAnnotatedPluginDocument(new MrBayesPosteriorDocument(name, root, consensusTrees, alignment, renameSequences));
        }
    }

    private void parsePosteriorParameters(File posteriorsFile, String runId, int subsampleFrequency, Element parameters) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(posteriorsFile));
            String suffix = "";
            if (runId != null && runId.length() > 0) {
                suffix = "_" + runId;
            }
            //todo refactor xml crap in to document classes
            //noinspection UnusedAssignment
            String inLine = reader.readLine();
            inLine = reader.readLine();
            inLine = inLine.trim();
            String[] name = inLine.split("\\s+");
            int numFields = name.length;
            List[] data = new List[numFields];
            for (int i = 0; i < numFields; i++)
                data[i] = new ArrayList<Double>();
            inLine = reader.readLine();
            while (inLine != null) {
                inLine = inLine.trim();
                String[] field = inLine.split("\\s+");
                for (int i = 0; i < numFields; i++) {
                    //noinspection unchecked
                    data[i].add(new Double(field[i]));
                }
                inLine = reader.readLine();
            }
            for (int i = 0; i < numFields; i++) {
                String elementName = stripCharacters(name[i]);
                if (elementName.equals(GeneiousTraces.GEN)) {
                    if (parameters.getChild(GeneiousTraces.GEN) != null) {
                        //only add generations once :)
                        continue;
                    }
                } else {
                    elementName = elementName + suffix;
                }
                Element element = new Element(elementName);
                for (Object d : data[i]) {
                    String stringContent = d.toString() + " ";
                    if (i == 0 /* Gen */ && subsampleFrequency == 1)
                        element.addContent(Double.toString((Double) d - 1.0) + " ");
                    else
                        element.addContent(stringContent);
                }
                // Check to see if first state should be 0 (subsample > 1), or 1 (subsample == 1)
                parameters.addContent(element);

            }
        } catch (IOException e) {
            throw new IOException("Error importing posterior parameter distribution file:\n" + e.toString(), e);
        } finally {
            GeneralUtilities.attemptClose(reader);
        }
    }

    /**
     *
     * @param progress
     * @return trees one list of trees per partition per run or empty list if canceled
     * @throws IOException
     */
    public List<AnnotatedPluginDocument> importRawTrees(ProgressListener progress) throws IOException {
        if (rawTreesCache == null) {

            long memoryRequired = 0;
            for (File file : rawTreesFiles) {
                memoryRequired += (file.length() * 6 * 3) / 1024 / 1024;
            }
            if (SystemUtilities.isAvailableMemoryLessThan(100 + memoryRequired)) {
                throw new IOException("Not enough free memory (estimated " + memoryRequired + "MB required)");
            }

            rawTreesCache = new ArrayList<AnnotatedPluginDocument>();
            CompositeProgressListener progressListener = new CompositeProgressListener(progress, rawTreesFiles.length);

            Pattern treePattern = Pattern.compile(TREE_PATTERN + RUN_PATTERN + EXT_RAW_TREES);
            Map<String, List<File>> treesByPartition = new HashMap<String, List<File>>();

            for (File rawTrees : rawTreesFiles) {
                Matcher m = treePattern.matcher(rawTrees.getName());
                m.find();
                String treeId = m.group(2);
                List<File> files = treesByPartition.get(treeId);
                if (files == null) {
                    files = new ArrayList<File>();
                    treesByPartition.put(treeId, files);
                }
                files.add(rawTrees);
            }

            for (Map.Entry<String, List<File>> partitionEntry : treesByPartition.entrySet()) {
                List<Tree> trees = new ArrayList<Tree>();
                for (File file : partitionEntry.getValue()) {

                    Matcher m = treePattern.matcher(file.getName());
                    m.find();
                    String runId = m.group(4);

                    progressListener.beginSubtask();
                    if (progressListener.isCanceled()) {
                        return Collections.emptyList();
                    }

                    List<Tree> theseTrees;
                    try {
                        theseTrees = importTreesFromNexus(file);
                    } catch (IOException e) {
                        rawTreesCache = Collections.emptyList();
                        throw e;
                    }
                    if (runId != null) {
                        for (Tree t : theseTrees) {
                            t.setAttribute("name", t.getAttribute("name") + "_" + runId);
                        }
                    }
                    trees.addAll(theseTrees);
                }
                String name = partitionEntry.getKey();
                if (name == null) {
                    name = alignment.getName();
                } else {
                    name = alignment.getName() + " - " + name;
                }
                name = name + " - Raw Trees";
                AnnotatedPluginDocument annotatedDocument = DocumentUtilities.createAnnotatedPluginDocument(new DefaultPhylogenyDocument(trees, null, name, "Raw trees from MrBayes run", alignment, true));
                annotatedDocument.setHiddenFieldValue(PARTITION_FIELD, partitionEntry.getKey());
                rawTreesCache.add(annotatedDocument);
            }
        }
        return rawTreesCache;
    }

    public Tree generateConsensusFromRawTrees(List<? extends Tree> rawTrees, int burnin, ProgressListener progress) {
        if(burnin < 0 || burnin >= rawTrees.size())
            burnin = rawTrees.size() / 10;
        List<? extends Tree> trees = rawTrees.subList(burnin, rawTrees.size()-1);
        progress.setMessage("Building Consensus tree");
        ConsensusTreeBuilder builder = TreeBuilderFactory.buildRooted(trees.toArray(new Tree[trees.size()]), 0.5, TreeBuilderFactory.ConsensusMethod.GREEDY);
        return builder.build();
    }

    /**
     *
     * @return one list of trees per partition or empty list if canceled
     */
    public List<AnnotatedPluginDocument> importSortedTopologies(ProgressListener progress) throws IOException {
        if (sortedTopologiesCache == null) {
            CompositeProgressListener progressListener = new CompositeProgressListener(progress, trprobsFiles.length);
            sortedTopologiesCache = new ArrayList<AnnotatedPluginDocument>();
            Pattern trprobsPattern = Pattern.compile(TREE_PATTERN + EXT_TRPROBS);
            for (File trprobs : trprobsFiles) {
                checkFile(trprobs, "trprobs");
                progressListener.beginSubtask();
                if (progressListener.isCanceled()) {
                    return Collections.emptyList();
                }
                List<Tree> trees = importTreesFromNexus(trprobs);

                Matcher m = trprobsPattern.matcher(trprobs.getName());
                m.find();
                String treeId = m.group(2);

                String name;
                if (treeId == null) {
                    name = alignment.getName();
                } else {
                    name = alignment.getName() + " - " + treeId;
                }
                name = name + SORTED_TOPOLOGIES_SUFFIX;
                sortedTopologiesCache.add(DocumentUtilities.createAnnotatedPluginDocument(new DefaultPhylogenyDocument(trees, null, name, "Sorted topologies from MrBayes run", alignment, true)));
            }
        }
        return sortedTopologiesCache;
    }

    private List<Tree> importConsensusTrees() throws IOException {
        Pattern consensusPattern = Pattern.compile(TREE_PATTERN + EXT_CONSENSUS_TREE);
        List<Tree> trees = new ArrayList<Tree>();
        for (File conFile : consensusFiles) {
            checkFile(conFile, "consensus");
            Tree tree = importTreesFromNexus(conFile).get(0);

            for (Node n : tree.getNodes()) {
                for (Map.Entry<String, Object> entry: new HashSet<Map.Entry<String, Object>>(n.getAttributeMap().entrySet())) {
                    String key = entry.getKey();
                    Object objValue = entry.getValue();
                    if (objValue instanceof Object[]) {
                        Object[] value = (Object[])objValue;
                        if (value.length == 2 && value[0] instanceof Double && value[1] instanceof Double) {
                            n.setAttribute(key, String.format("%.6f - %.6f", (Double)value[0], (Double)value[1]));
                        }
                    }
                    if ("label".equals(key)) {
                        n.setAttribute("Posterior Probability", objValue);
                        n.removeAttribute("label");
                    }
                    if ("prob".equals(key)) {
                        n.setAttribute("Posterior Probability", objValue);
                        n.removeAttribute("prob");
                    }
                }
            }
            // GEN-20355 attributes on the tips are pointless because their values are constant (eg. 1 or 100%)
            for (Node externalNode : tree.getExternalNodes()) {
                for (String attributeName : new ArrayList<String>(externalNode.getAttributeNames())) {
                    externalNode.removeAttribute(attributeName);
                }
            }

            Matcher m = consensusPattern.matcher(conFile.getName());
            m.find();
            String treeId = m.group(2);
            String name = "consensus";

            if (treeId != null) {
                name = treeId + "_" + name;
            }
            tree.setAttribute("name", name);
            trees.add(tree);
        }
        return trees;
    }

    public List<String> getFailures() {
        return failures;
    }

    private static List<Tree> importTreesFromNexus(File nexusFile) throws IOException {
        try {
            NexusImporter importer = new NexusImporter(new FileReader(nexusFile), false, nexusFile.length());
            List<Tree> trees = importer.importTrees();
            if (trees == null || trees.size() == 0)
                throw new IOException("No trees in file");
            return trees;
        } catch (ImportException e) {
            throw new IOException(nexusFile.getName() + ": " + e.getMessage());
        } catch (IOException e) {
            throw new IOException(nexusFile.getName() + ": " + e.getMessage());
        }
    }

    private static String stripCharacters(String text) {
        if ("XML names cannot be null or empty".equals(Verifier.checkXMLName(text))){
             text="Untitled";
         }
         while(Verifier.checkXMLName(text)!=null){
             String[] splitResult = Verifier.checkXMLName(text).split("\"");
             text = text.replaceAll("\\" + splitResult[1], "");
         }
        return text;
    }

    static String readEntireTextFile(File inFile) throws IOException {
        StringBuffer sb = new StringBuffer();
        String inLine = "";
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new FileReader(inFile));
            while (inLine != null) {
                sb.append(inLine);
                sb.append("\n");
                inLine = reader.readLine();
            }
        } finally {
            GeneralUtilities.attemptClose(reader);
        }
        return sb.toString();
    }
}
