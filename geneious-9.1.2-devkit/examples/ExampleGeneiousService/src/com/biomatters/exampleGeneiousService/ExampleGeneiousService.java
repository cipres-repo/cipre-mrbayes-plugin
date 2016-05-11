package com.biomatters.exampleGeneiousService;

import com.biomatters.geneious.publicapi.databaseservice.*;
import com.biomatters.geneious.publicapi.documents.Condition;
import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.sequence.SequenceDocument;
import com.biomatters.geneious.publicapi.implementations.sequence.DefaultNucleotideSequence;
import com.biomatters.geneious.publicapi.plugin.Icons;
import com.biomatters.geneious.publicapi.utilities.FileUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

/**
 * This class is an example of a DatabaseService which provides access to the contents of a fasta file.
 *
 * @version $Id$
 */

public class ExampleGeneiousService extends DatabaseService {
    private final File fasta;

    public ExampleGeneiousService(File fastaFile) {
        this.fasta = fastaFile;
    }

    public String getUniqueID() {
        return "SampleService";
    }

    public String getName() {
        return "Sample Geneious Service";
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


    //we find rsults based on the given query, and return them using the callback supplied.
    public void retrieve(Query query, RetrieveCallback callback, URN[] urnsToNotRetrieve) throws DatabaseServiceException {
        //some basic error handling
        if(!fasta.exists()) {
            throw new DatabaseServiceException("Fasta file does not exist (file name="+fasta+")",false);
        }

        try {
            System.out.println("text="+fasta);
            System.out.println(FileUtilities.getTextFromFile(fasta));
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            BufferedReader in = new BufferedReader(new FileReader(fasta));
            String currentLine = "";
            String name = "";
            String residues = "";
            ArrayList<String> nameToMatch = new ArrayList<String>();
            ArrayList<String> residuesToMatch = new ArrayList<String>();
            boolean matchEverything = false; //this is true if we want both the name and the residues to match for a document to be included in the search results

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
                    System.out.println("name="+name);
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
