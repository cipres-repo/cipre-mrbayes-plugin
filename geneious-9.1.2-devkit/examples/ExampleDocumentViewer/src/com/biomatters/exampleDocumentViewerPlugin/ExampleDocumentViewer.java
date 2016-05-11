package com.biomatters.exampleDocumentViewerPlugin;

import com.biomatters.geneious.publicapi.plugin.*;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.print.PrinterException;
import java.awt.print.Printable;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.geom.AffineTransform;

/**
 * @author Matt Kearse
 * @version $Id$
 */

public class ExampleDocumentViewer extends DocumentViewer {
    final Font font = new Font("monospaced",Font.PLAIN,48);
    final String residues;

    public ExampleDocumentViewer(SequenceDocument sequence) {
        residues = sequence.getSequenceString().toUpperCase();
    }

    public JPanel getComponent(){

        JPanel canvas = new JPanel(){

            public void paintComponent(Graphics g){
                //we need to clear the background
                g.setColor(Color.white);
                g.fillRect(0,0,getWidth(),getHeight());

                //then draw the sequence string onto the canvas
                g.setColor(Color.black);
                g.setFont(font);
                g.drawString(residues,10,50);
            }

            public Dimension getPreferredSize(){
                //this calculates how wide the text is going to be.
                FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
                Rectangle2D fontBounds = font.getStringBounds(residues, frc);
                return new Dimension((int)fontBounds.getWidth()+20,(int)fontBounds.getHeight()+60);
            }
        };

        //we put the main canvas into a JScrollPane in case the sequence is bigger than the viewer window.
        JScrollPane scroller = new JScrollPane(canvas);

        //since we return a JPanel, we need to put the JScrollPane into a new JPanel
        JPanel holder = new JPanel(new BorderLayout());
        holder.add(scroller,BorderLayout.CENTER);
        return holder;
    }

    //DocumentViewer contains two methods for printing, getExtendedPrintable and getPrintable
    //it is suggested that you use getExtendedPrintable for more full-featured printing
    public ExtendedPrintable getExtendedPrintable(){
        return new ExtendedPrintable(){

            //This {@link Options} appears at the top of the print options dialog that is opened when
            //the user selects "Print"
            public Options getOptions(boolean isSavingToFile) {
                Options options = new Options(this.getClass());
                options.addLabel("It is ludicrous to print this");
                return options;
            }

            //we need to be aware of what page we are on, and print the correct section of the sequence.
            //in a more robust implementation we would deal with characters being split between two pages.
            public int print(Graphics2D g, Dimension d, int pageIndex, Options options) throws PrinterException {

                //clear the background
                g.setColor(Color.white);
                g.fillRect(0,0,d.width,d.height);

                //set a clip to make sure that the graphics does not draw outside the printable area
                g.setClip(0,0,d.width,d.height);

                //move the graphics so that the correct section of the sequence is drawn to the page
                g.translate(-d.width*pageIndex,0);

                //paint the page
                g.setColor(Color.black);
                g.setFont(font);
                g.drawString(residues,10,50);

                //tell the printing subsystem whether the page requested was a valid page or not.
                return (getPagesRequired(d, options) < pageIndex)? Printable.PAGE_EXISTS : Printable.NO_SUCH_PAGE;
            }

            public int getRequiredWidth(Options options) {
                FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
                Rectangle2D fontBounds = font.getStringBounds(residues, frc);
                return (int)fontBounds.getWidth()+20;
            }


            //these two methods are used by the save to image feature
            public int getRequiredHeight(int width, Options options) {
                FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
                Rectangle2D fontBounds = font.getStringBounds(residues, frc);
                return (int)fontBounds.getHeight()+60;
            }

            //returns the number of pages required to print the entire document.
            public int getPagesRequired(Dimension dimensions, Options options) {
                return (int)Math.ceil(((double)getRequiredWidth(options))/dimensions.width);
            }
        };
    }

    public NoLongerViewedListener getNoLongerViewedListener(){
        return new NoLongerViewedListener(){
            public void noLongerViewed(boolean isTemporary){
                //return JOptionPane.showConfirmDialog(null,"It is ludicrous to leave!\nAre you sure?") == JOptionPane.YES_OPTION;
            }
        };
    }

    public ActionProvider getActionProvider() {
        return new ActionProvider(){
            public GeneiousAction getCopyAction() {
                return new GeneiousAction("Copy"){
                    public void actionPerformed(ActionEvent e){
                        StringSelection ss = new StringSelection(residues);
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
                    }
                };
             }
        };
    }



    
}
