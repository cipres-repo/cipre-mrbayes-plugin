package mrbayes.plugin;

import com.biomatters.geneious.publicapi.components.GButton;
import com.biomatters.geneious.publicapi.components.GPanel;
import com.biomatters.geneious.publicapi.documents.AnnotatedPluginDocument;
import com.biomatters.geneious.publicapi.plugin.DocumentSelectionSignature;
import com.biomatters.geneious.publicapi.plugin.DocumentViewer;
import com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory;
import org.suchard.gui.posterior.PosteriorDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Steven Stones-Havas
 * @version $Id$
 *          <p/>
 *          Created on 7/12/2007 15:07:29
 */
public class PosteriorOutputTextViewer extends DocumentViewerFactory {//JEditorPane is in the class name for hax so that Utils.assertIsDispatchThread() doesn't crash me
    public String getName() {
        return "Raw Posterior Output";
    }

    public String getDescription() {
        return "A view of the raw posterior output";
    }

    public String getHelp() {
        return "Shows a view of the raw posterior output from your MrBayes run.";
    }

    public DocumentSelectionSignature[] getSelectionSignatures() {
        return new DocumentSelectionSignature[]{new DocumentSelectionSignature(PosteriorDocument.class, 1, 1)};
    }

    public DocumentViewer createViewer(final AnnotatedPluginDocument[] docList) {
        return new DocumentViewer(){
            public JComponent getComponent() {
                final PosteriorDocument doc = (PosteriorDocument)docList[0].getDocumentOrNull();
                final JPanel panel = new JPanel(new BorderLayout());
                final JTextPane textPane = new JTextPane();
                textPane.setEditable(false);
                String data = doc.getString();
                if(data.length() > 100000){
                    textPane.setText(data.substring(0, 100000));
                    JPanel toolbar = new GPanel(new FlowLayout());
                    GButton button = new GButton("Show entire "+data.length()+" bytes (may be very slow)");
                    toolbar.add(button);
                    button.addActionListener(new ActionListener(){
                        public void actionPerformed(ActionEvent e) {
                            panel.remove(0);
                            panel.doLayout();
                            Thread t = new Thread(){
                                public void run(){

                                    textPane.setText(doc.getString());
                                }
                            };
                            t.start();
                        }
                    });
                    panel.add(toolbar, BorderLayout.NORTH);
                }
                else{
                    textPane.setText(data);
                }
                JPanel holder = new JPanel(new BorderLayout());
                holder.add(textPane, BorderLayout.CENTER);
                JScrollPane scroller = new JScrollPane(holder);
                panel.add(scroller, BorderLayout.CENTER);
                return panel;
            }
        };
    }
}
