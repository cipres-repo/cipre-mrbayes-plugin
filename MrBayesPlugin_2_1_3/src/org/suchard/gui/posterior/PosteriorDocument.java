package org.suchard.gui.posterior;

import com.biomatters.geneious.publicapi.components.GEditorPane;
import com.biomatters.geneious.publicapi.documents.PluginDocument;

import javax.swing.*;
import java.awt.*;

/**
 * Document containing posterior results from MrBayes.
 */
public interface PosteriorDocument extends PluginDocument {

	public GeneiousTraces getTraces();

    public String getString();

    public static final class Utilities {

        static final String[] textLines = {
                "If you publish MrBayes results, please",
                "cite Huelsenbeck and Ronquist (2001)",
                "Bioinformatics, 17:754-755.",
                "This plugin was developed by Marc Suchard",
                "and the Geneious team.",
        };

        public static Component getViewerCitationText() {
            StringBuilder message = new StringBuilder();
            message.append("<font size=-1>");
            message.append("<CENTER>");
            for (String line : textLines) {
                message.append(line).append("<br>");
            }
            message.append("</CENTER>");
            message.append("</font>");

            JEditorPane p = new GEditorPane("text/html", message.toString());
            p.setEditable(false);
            p.setOpaque(false);
            return p;
        }
    }
}
