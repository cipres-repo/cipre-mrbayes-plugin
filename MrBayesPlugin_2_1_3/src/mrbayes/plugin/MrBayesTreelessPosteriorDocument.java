package mrbayes.plugin;

import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.geneious.publicapi.plugin.Geneious;
import jebl.util.ProgressListener;
import org.jdom.Element;
import org.suchard.gui.posterior.GeneiousTraces;
import org.suchard.gui.posterior.PosteriorDocument;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Steven Stones-Havas
 * @version $Id$
 *          <p/>
 *          Created on 18/08/2008 16:47:47
 */
public class MrBayesTreelessPosteriorDocument implements PluginDocument, PosteriorDocument, XMLSerializable, XMLSerializable.OldVersionCompatible {
    private String documentName;
    private Date created;
    private String programName;
    private String commandFile;
    private String mcmcFile;
    private String partitionFile;
    private GeneiousTraces traces;

    public MrBayesTreelessPosteriorDocument(String documentName, Element element) throws XMLSerializationException {
        this.documentName = documentName;
        created = new Date();
        fromXML(element);
        traces.sortTracesByName();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public MrBayesTreelessPosteriorDocument(){} //for xml serialization

    public List<DocumentField> getDisplayableFields() {
        return Collections.emptyList();
    }

    public Object getFieldValue(String fieldCodeName) {
        return null;
    }

    public String getName() {
        String name;
        if (documentName != null && !documentName.equals("")) {
            name = documentName + " - Posterior output";
        }
        else{
            name = "Posterior output";
        }
        return name;
    }

    public URN getURN() {
        return null;
    }

    public Date getCreationDate() {
        return created;
    }

    public String getDescription() {
        return "Posterior output from MrBayes";
    }

    public String toHTML() {
        return null;
    }

	public String getString() {
		StringBuilder sb = new StringBuilder();
		//sb.append("<pre>");
		sb.append("COMMAND FILE:\n");
		sb.append(commandFile != null ? commandFile : "");
		sb.append("\n\nMCMC FILE:\n");
		sb.append(mcmcFile != null ? mcmcFile : "");
		sb.append("\n\nPARTITIONS FILE:\n");
		sb.append(partitionFile != null ? partitionFile : "");
		sb.append("\n\nPARAMTER VALUES:\n");
		sb.append(traces != null ? traces.toHTML() : "");
		//sb.append("</pre>");
		return sb.toString();
	}

    @Override
    public Geneious.MajorVersion getVersionSupport(VersionSupportType versionType) {
        Geneious.MajorVersion result = Geneious.MajorVersion.Version6_0; // update this (taking versionType into account) if toXML ever changes.
        result = Geneious.MajorVersion.max(result,traces.getVersionSupport(versionType));
        return result;
    }

    @Override
    public Element toXML() {
        return toXML(Geneious.getMajorVersion(), ProgressListener.EMPTY);
    }

    @Override
    public Element toXML(Geneious.MajorVersion version, ProgressListener progressListener) {
        // Update getVersionSupport if this method ever changes.
        Element rootElement = new Element("posteriorOutput");
		rootElement.setAttribute("program", programName);
        if(commandFile != null){
            rootElement.addContent((new Element("commandFile").addContent(commandFile)));
        }
        if(mcmcFile != null){
            rootElement.addContent(new Element(MrBayesPosteriorDocument.XML_MCMC).addContent(mcmcFile));
        }
        if(partitionFile != null){
            rootElement.addContent(new Element(MrBayesPosteriorDocument.XML_PARTITION).addContent(partitionFile));
        }
        if(traces != null){
            rootElement.addContent(traces.toXML(version, progressListener));
        }
        return rootElement;
    }

    public void fromXML(Element rootElement) throws XMLSerializationException {
		programName = rootElement.getAttributeValue(MrBayesPosteriorDocument.XML_PROGRAM);
		commandFile = rootElement.getChildText("commandFile");
		mcmcFile = rootElement.getChildText(MrBayesPosteriorDocument.XML_MCMC);
		partitionFile = rootElement.getChildText(MrBayesPosteriorDocument.XML_PARTITION);
        Element parameterElement = rootElement.getChild("parameters");
        if(parameterElement != null){
            traces = new GeneiousTraces(programName);
            try {
                traces.fromXML(rootElement);
            } catch (XMLSerializationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public GeneiousTraces getTraces() {
        return traces;
    }
}
