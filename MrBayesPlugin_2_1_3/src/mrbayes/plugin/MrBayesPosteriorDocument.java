package mrbayes.plugin;

import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import com.biomatters.geneious.publicapi.implementations.DefaultPhylogenyDocument;
import com.biomatters.geneious.publicapi.plugin.Geneious;
import jebl.evolution.trees.Tree;
import jebl.util.ProgressListener;
import org.jdom.Element;
import org.suchard.gui.posterior.GeneiousTraces;
import org.suchard.gui.posterior.PosteriorDocument;

import java.util.List;


public class MrBayesPosteriorDocument extends DefaultPhylogenyDocument implements PosteriorDocument, XMLSerializable {

	private String dataName;
    private String name;

    private GeneiousTraces traces;

    private String commandFile;
	private String partitionFile;
	private String mcmcFile;

	public MrBayesPosteriorDocument(String inDataName, Element inResults, List<Tree> trees,
                                    AnnotatedPluginDocument alignmentDoc, boolean renameSequences) throws XMLSerializationException {
		super(trees, null, "MrBayes Posterior", "Posterior document generated by MrBayes", alignmentDoc, renameSequences);
		dataName = inDataName;
		this.parseFromResults(inResults);
	}

	public MrBayesPosteriorDocument() {
	}

    public GeneiousTraces getTraces() {
        return traces;
	}

	public String getName() {
        if (name != null) {
            return name;
        }
        if (dataName != null && dataName.compareTo("") != 0) {
            name = dataName + " - Posterior output";
        } else {
            name = "Posterior output";
        }
        return name;
	}

	public String getDescription() {
		return dataName != null && dataName.compareTo("") != 0 ? "Posterior output from MrBayes using " + dataName : "Posterior output from MrBayes";
	}

	public String getString() {
		StringBuilder sb = new StringBuilder();
		//sb.append("<pre>");
		sb.append("COMMAND FILE:\n");
		sb.append(commandFile);
		sb.append("\n\nMCMC FILE:\n");
		sb.append(mcmcFile);
		sb.append("\n\nPARTITIONS FILE:\n");
		sb.append(partitionFile);
		sb.append("\n\nPARAMTER VALUES:\n");
		sb.append(traces.toHTML());
		//sb.append("</pre>");
		return sb.toString();
	}

	private String programName;

	public final static String XML_PROGRAM = "program";
	public final static String XML_MCMC = "mcmcFile";
	public final static String XML_PARTITION = "partitionFile";

	private void parseFromResults(Element inRoot) throws XMLSerializationException {
		programName = inRoot.getAttributeValue(XML_PROGRAM);
		commandFile = inRoot.getChildText("commandFile");
		mcmcFile = inRoot.getChildText(XML_MCMC);
        partitionFile = inRoot.getChildText(XML_PARTITION);
        traces = new GeneiousTraces(programName);
        traces.fromXML(inRoot);
        traces.sortTracesByName();
	}

	public void fromXML(Element inRoot) throws XMLSerializationException {
		super.fromXML(inRoot);
		Element rootElement = inRoot.getChild("posteriorOutput");
		programName = rootElement.getAttributeValue(XML_PROGRAM);
		commandFile = rootElement.getChildText("commandFile");
		mcmcFile = rootElement.getChildText(XML_MCMC);
		partitionFile = rootElement.getChildText(XML_PARTITION);
        traces = new GeneiousTraces(programName);
        try {
            traces.fromXML(rootElement);
        } catch (XMLSerializationException e) {
            throw new RuntimeException(e);
        }
	}

    @Override
    public Geneious.MajorVersion getVersionSupport(VersionSupportType versionType) {
        Geneious.MajorVersion result = Geneious.MajorVersion.Version6_0; // update this (taking versionType into account) if toXML ever changes.
        result = Geneious.MajorVersion.max(result,super.getVersionSupport(versionType));
        result = Geneious.MajorVersion.max(result,traces.getVersionSupport(versionType));
        return result;
    }

    @Override
    public Element toXML(Geneious.MajorVersion version, ProgressListener progressListener) {
        // Update getVersionSupport if this method ever changes.
		Element superElement = super.toXML(version, progressListener);
		Element rootElement = new Element("posteriorOutput");
		rootElement.setAttribute("program", programName != null ? programName : "");
		rootElement.addContent((new Element("commandFile")
				.addContent(commandFile))
		);
		rootElement.addContent(new Element(XML_MCMC).addContent(mcmcFile));
		rootElement.addContent(new Element(XML_PARTITION).addContent(partitionFile));
		rootElement.addContent(traces.toXML());
		superElement.addContent(rootElement);
		return superElement;
	}
}
