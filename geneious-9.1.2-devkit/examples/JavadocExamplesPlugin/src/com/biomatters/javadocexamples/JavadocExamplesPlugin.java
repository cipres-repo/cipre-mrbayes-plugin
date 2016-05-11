package com.biomatters.javadocexamples;

import com.biomatters.geneious.publicapi.databaseservice.*;
import com.biomatters.geneious.publicapi.documents.*;
import com.biomatters.geneious.publicapi.documents.sequence.AminoAcidSequenceDocument;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.DefaultSequenceGraphFactories;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultAminoAcidSequence;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.*;
import jebl.util.SafePrintWriter;
import jebl.evolution.sequences.AminoAcids;
import jebl.util.ProgressListener;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * @author Steven Stones-Havas
 * @version $Id: JavadocExamplesPlugin.java 17464 2008-02-14 04:06:08Z tobias $
 */
public class JavadocExamplesPlugin extends GeneiousPlugin {
    public String getDescription(){
        return "Plugin examples from the PublicAPI JavaDoc Documentation";
    }

    public String getName(){
        return "Example Plugin";
    }

    public String getHelp(){
        return "";
    }

    public String getAuthors(){
        return "Biomatters";
    }

    public String getVersion(){
        return "2.0";
    }

    public String getMinimumApiVersion(){
        return "4.0";
    }

    public int getMaximumApiVersion(){
        return 4;
    }

    public DocumentOperation[] getDocumentOperations() {
        return new DocumentOperation[] {new DocumentOperation(){

            //This method is so this DocumentOperation can be returned by PluginUtilities.getDocumentOperation(String id);
            public String getUniqueId() {
                return "New Sequence";
            }

            //GeneiousActionOptions specify how the action is going to be displayed within Geneious.
            //in this case it is going to be displayed on the toolbar with the label "New Sequence".
            public GeneiousActionOptions getActionOptions(){
                return new GeneiousActionOptions("New Sequence").setInMainToolbar(true);
            }

            public String getHelp(){
                return "This operation creates a <b>New Sequence</b>";
            }

            //DocumentSelection signatures define what types of documents (and numbers of documents) the operation can take.
            //in this case we do not need to take documents, so we can just return an empty set.
            public DocumentSelectionSignature[] getSelectionSignatures(){
                return new DocumentSelectionSignature[0];
            }

            //Geneious will display the Options returned from this method before calling performOperation().
            public Options getOptions(AnnotatedPluginDocument... docs){
                Options options = new Options(this.getClass());
                Options.MultipleLineStringOption stringOption = options.addMultipleLineStringOption("residues", "", "",5,true);
                return options;
            }

            //This is the method that does all the work.  Geneious passes a list of the documents that were selected when the user
            //started the operation, a progressListener, and the options that we returned in the getOptions() method above.
            public java.util.List<AnnotatedPluginDocument> performOperation(AnnotatedPluginDocument[] docs, ProgressListener progress, Options options){
                //lets create the list that we're going to return...
                ArrayList<AnnotatedPluginDocument> sequenceList = new ArrayList<AnnotatedPluginDocument>();

                //The options that we created in the getOptions() method above has been passed to us, hopefully the user has filled in their sequence.
                //We get the option we added by using its name.  MultiLineStringOption has a String ValueType, so we can safely cast to a String object.
                String residues = (String)options.getValue("residues");

                //lets construct a new sequence document from the residues that the user entered
                AminoAcidSequenceDocument sequence = new DefaultAminoAcidSequence("New Sequence","A new Sequence",residues,new Date());

                //and add it to the list
                sequenceList.add(DocumentUtilities.createAnnotatedPluginDocument(sequence));

                //normally we would set the progress incrementally as we went, but this operation is quick so we just set it to finished when we're done.
                progress.setProgress(1.0);

                //return the list containing the sequence we just created, and we're done!
                return sequenceList;
            }
        }};
    }

    public DocumentFileExporter[] getDocumentFileExporters(){
        return new DocumentFileExporter[]{
            new DocumentFileExporter(){
                public String getFileTypeDescription(){
                    return "Export sequence as string";
                }

                public String getDefaultExtension(){
                    return ".txt";
                }

                //DocumentFileExporters tell Geneious what types of documents they can accept with a list of DocumentSelectionSignature objects.
                //This particular exporter accepts sequence documents (in a list containing one or more documents)
                public DocumentSelectionSignature[] getSelectionSignatures(){
                    return new DocumentSelectionSignature[] {new DocumentSelectionSignature(SequenceDocument.class,1,Integer.MAX_VALUE)};
                }

                //This is where the work is done.  Geneious gives us the File to write to, the list of AnnotatedPluginDocument objects to export, and a jebl.util.ProgressListener.
                public void export(File file, AnnotatedPluginDocument[] docs, ProgressListener progress, Options options) throws IOException{
                    //First we create a writer using the supplied file handle.
                    SafePrintWriter out = new SafePrintWriter(new FileWriter(file));

                    //Lets give the ProgressListener an informative message.
                    progress.setMessage("Exporting sequences...");

                    //loop through all the documents in the list.
                    int count = docs.length;
                    for(int i=0; i < count; i++){

                        //update the ProgressListener
                        progress.setProgress(((double)i)/count);

                        //Geneious stores documents as AnnotatedPluginDocument objects, which
                        //contain an internal PluginDocument, as well as extra information (eg annotations).
                        //in this case we need the internal document, so we extract it using the AnnotatedPluginDocument.getDocument()
                        PluginDocument internalDoc = docs[i].getDocumentOrThrow(IOException.class);

                        //since we specified that only SequenceDocuments be passed to our exporter in the getSelectionSignatures() method,we can be sure that the document is a SequenceDocument.
                        //so we can cast it without worrying about exceptions.
                        String residues = ((SequenceDocument)internalDoc).getSequenceString();

                        //write the sequence string to the file.
                        out.println(residues);
                    }
                    //its always good to perform housekeeping when we're done.
                    out.close();
                }
            }
        };
    }


    public DocumentFileImporter[] getDocumentFileImporters(){
        return new DocumentFileImporter[] {
            new DocumentFileImporter(){
                public String[] getPermissibleExtensions(){
                    return new String[] {".txt"};
                }

                public String getFileTypeDescription(){
                    return "sequence txt file";
                }

                //this method returns an {@link com.biomatters.geneious.publicapi.plugin.AutoDetectStatus} showing whether it can import the file.
                //REJECT_FILE means that the importer will definitely not import the file, ACCEPT_FILE means that it definitely will, and MAYBE means that we're not sure yet.
                public AutoDetectStatus tentativeAutoDetect(File f, String fileContentsStart){
                    //if the beginning of the file contains a character that is not a valid protein character, reject the file
                    for(int i=0; i < fileContentsStart.length(); i++){
                        char c = fileContentsStart.charAt(i);
                        if (AminoAcids.getState(c) == null && c != '\n'){
                            return AutoDetectStatus.REJECT_FILE;
                        }
                    }
                    //otherwise we are unsure.
                    return AutoDetectStatus.MAYBE;
                }

                //When importing documents, the documents are added to an {@link com.biomatters.geneious.publicapi.plugin.ImportCallback}
                //instead of the importer returning them in a list or directly adding them to a repository.
                //The callback adds the documents to the local repository while the file is still being read.
                public void importDocuments(File f, ImportCallback callback, ProgressListener progress) throws DocumentImportException, IOException{
                    try{
                        //First we need to create a reader from the file that Geneious gives us.
                        BufferedReader br = new BufferedReader(new FileReader(f));

                        //we'll use these two numbers to calculate the progress.
                        long fileSize = f.length();
                        long count = 0;

                        //give the ProgressListener a descriptive message
                        progress.setMessage("Importing sequence data");

                        //we need to loop through all lines in the file, and create a sequence from each file
                        String residues = "";

                        //We need to check whether the user has pressed cancel on the ProgressListener, and stop reading the file if they have.
                        while((residues = br.readLine()) != null && !progress.isCanceled()){
                            progress.setProgress(((double)count)/fileSize);
                            try{
                                //create a new sequence from the line
                                DefaultAminoAcidSequence seq = new DefaultAminoAcidSequence("New Sequence", "",residues,new Date(f.lastModified()));

                                //increase the count for the ProgressListener
                                count += residues.length();

                                //add the document to the callback
                                callback.addDocument(seq);
                            }
                            catch(IllegalArgumentException ex){
                                //the document was invalid, so we won't add it.
                                //in a more robust implementation we would do something here...
                            }
                        }
                        //housekeeping...
                        br.close();
                    }
                    catch(FileNotFoundException ex){
                        throw new DocumentImportException("File not found: "+ f.getName(),ex);
                    }
                }
            }
        };
    }

    public DocumentViewerFactory[] getDocumentViewerFactories() {
        //this method returns a list of {@link com.biomatters.geneious.publicapi.plugin.DocumentViewerFactory}
        // objects
        return new DocumentViewerFactory[]{new DocumentViewerFactory(){
            public DocumentViewer createViewer(final AnnotatedPluginDocument[] docs){
                return new DocumentViewer(){
                    final Font font = new Font("monospaced",Font.PLAIN,48);
                    final String residues = ((SequenceDocument)docs[0].getDocumentOrNull()).getSequenceString().toUpperCase();
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
                };
            }

            public DocumentSelectionSignature[] getSelectionSignatures(){
                return new DocumentSelectionSignature[]{new DocumentSelectionSignature(SequenceDocument.class,1,1)};
            }

            public String getHelp(){
                return "A Viewer";
            }

            public String getDescription(){
                return "Ludicrous, isn't it?";
            }

            public String getName(){
                return "Ludicrously Giant Sequence View";
            }

        }};
    }

    File pluginDir;
    public void initialize(File pluginUserDirectory, File pluginDirectory) {
        pluginDir = pluginDirectory;
    }

     public GeneiousService[] getServices() {
		return new GeneiousService[]{
			new DatabaseService(){
				public String getUniqueID() {
					return "SampleGeneiousService";
				}

				public String getName() {
					return "Empty Service";
				}

				public String getDescription() {
					return "An empty service";
				}

				public String getHelp() {
					return "Help with the service!";
				}

				public Icons getIcons() {
					return null;
				}

				public QueryField[] getSearchFields() {
					return new QueryField[]{
						new QueryField(new DocumentField("Name","The sequence name","name",String.class,false,false),new Condition[]{Condition.CONTAINS}),
						new QueryField(new DocumentField("Residues","The sequence residues","residues",String.class,false,false),new Condition[]{Condition.CONTAINS})
					};
				}

                File fasta = new File(pluginDir.getAbsolutePath()+File.separator+"sampledb.fasta");

                //we find results based on the given query, and return them using the callback supplied.
                public void retrieve(Query query, RetrieveCallback callback, URN[] urnsToNotRetrieve) throws DatabaseServiceException {
					//some basic error handling
					if(!fasta.exists()) {
                        throw new DatabaseServiceException("Fasta file does not exist (file name="+fasta+")",false);
                    }

                    try{
						BufferedReader in = new BufferedReader(new FileReader(fasta));
						String currentLine = "";
						String name = "";
						String residues = "";
                        ArrayList<String> nameToMatch = new ArrayList<String>();
						ArrayList<String> residuesToMatch = new ArrayList<String>();
                        boolean matchEverything = false; //this is true if we want both the name and the residues to match for a document to be included in the seaarch results

                        //we will store a list of the queries
                        java.util.List<Query> queries;

                        //a compoundSearchQuery consists of a number of queries
                        //we'll put them in the list
                        if(query instanceof CompoundSearchQuery){
                            CompoundSearchQuery cQuery = (CompoundSearchQuery)query;
                            matchEverything = cQuery.getOperator() == CompoundSearchQuery.Operator.AND;
                            queries = (java.util.List<Query>) cQuery.getChildren();
                        }
                        //if the query is not a CompoundSearchQuery, then we can create a one-element list containing the query
                        else {
                            queries = new ArrayList<Query>();
                            queries.add(query);
                        }

                        //we'll loop through all the queries, and set the nameToMatch and residuesToMatch
                        for(Query q : queries){
                            //we have the sequence and name, do the searching
                            if(q instanceof AdvancedSearchQueryTerm){
                                AdvancedSearchQueryTerm advancedQuery = (AdvancedSearchQueryTerm)q;
                                if(advancedQuery.getField().getCode().equals("name")){
                                    nameToMatch.add(advancedQuery.getValues()[0].toString().toUpperCase());
                                }
                                else if(advancedQuery.getField().getCode().equals("residues")){
                                    residuesToMatch.add(advancedQuery.getValues()[0].toString().toUpperCase());
                                }
                            }

                            //a {@link BasicSearchQuery} consists of one field (search text)
                            //you can extend a basic query, for example using a {@link CompoundSearchQuery}
                            else if (q instanceof BasicSearchQuery) {
                                //set both the name and the residue searches to the query entered
                                BasicSearchQuery bq = (BasicSearchQuery)query;
                                nameToMatch.add(bq.getSearchText().toUpperCase());
                                residuesToMatch.add(bq.getSearchText().toUpperCase());
                            }
                            else{
                                //do nothing
                            }
                        }

                        //if neither nameToMatch or residuesToMatch are set at this point, the search will return no results.

                        //now lets loop through the fasta file
                        while((currentLine = in.readLine()) != null){

                            //fasta files consist of 'name' lines (denoted by a beginning '>' character), and then a number of 'residue' lines
                            if(currentLine.startsWith(">")){
								if(!name.equals("")){
                                    //we get to this part of the code once we have read in one sequence (a name line and the residue lines)
                                    //so we must now do a match on the name and residues that we have read in
                                    SequenceDocument doc = match(nameToMatch,name,residuesToMatch,residues, matchEverything);
                                    if(doc != null){
                                        //add a search result if there is one
                                        callback.add(doc, Collections.<String,Object>emptyMap());
                                    }

                                }

                                //set the name variable to the new sequence name, and reset the residues variable
                                name = currentLine.substring(1,currentLine.length());
								residues = "";
							}
							else{
                                //keep concatenating the residues as we move through the fasta file
                                residues += currentLine;
							}
						}
                        //we need to do the match one last time once we reach the end of the file
                        SequenceDocument doc = match(nameToMatch,name,residuesToMatch,residues, matchEverything);
                        if(doc != null){
                            callback.add(doc, Collections.<String,Object>emptyMap());
                        }
                    }
					catch(IOException e){
                        //pass on any exceptions we get reading the file
                        throw new DatabaseServiceException(e,e.getMessage(),false);
					}
				}

                //this utility method returns a SequenceDocument based on the given name and residues, if they match the search parameters
                //or null if there is no match
                private SequenceDocument match(ArrayList<String> namesToMatch, String name, ArrayList<String> residuesToMatch, String residues, boolean matchBoth){
                    boolean nameMatch = false;
                    boolean residueMatch = false;
                    if(namesToMatch.size() > 0){
                        for(String nameToMatch : namesToMatch){
                            if(name.toUpperCase().contains(nameToMatch)){
                                nameMatch = true;
                            }
                        }
                    }

                    if(residuesToMatch.size() > 0){
                        for(String residueToMatch : residuesToMatch){
                            if(residues.toUpperCase().contains(residueToMatch)){
                                residueMatch = true;
                            }
                        }
                    }
                    boolean match;
                    if (matchBoth) {
                        match=residueMatch && nameMatch;
                    }
                    else {
                        match=residueMatch || nameMatch;
                    }
                    if (match) {
                        return new DefaultNucleotideSequence(name.substring(0,name.indexOf(" ")),name,residues,new Date(fasta.lastModified()));
                    }
                    return null;
                }
            }
		};
    }


    public SequenceGraphFactory[] getSequenceGraphFactories(){
        //the scorer allows you to assign a score for each residue, and also assign a color to each score.
        DefaultSequenceGraphFactories.SingleSequenceScorer scorer = new DefaultSequenceGraphFactories.SingleSequenceScorer(){
            public Double getScore(char residue) {
                return Math.random();
            }

            public Color getColor(double score) {
                return Color.black;
            }
        };

        return new SequenceGraphFactory[]{
            DefaultSequenceGraphFactories.createSingleSequenceGraphFactory("Nucleotide Ludicrousity", "What a ludicrous graph!", true, 25, SequenceDocument.Alphabet.NUCLEOTIDE, scorer, false), //a bar graph
            DefaultSequenceGraphFactories.createSingleSequenceGraphFactory("Protein Ludicrousity", "What a ludicrous graph!", true, 25, SequenceDocument.Alphabet.PROTEIN, scorer, true) //a line graph
        };
    }

}
