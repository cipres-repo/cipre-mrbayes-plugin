package com.biomatters.textfiles;

import com.biomatters.geneious.publicapi.documents.AdditionalSearchContent;
import com.biomatters.geneious.publicapi.documents.DocumentField;
import com.biomatters.geneious.publicapi.documents.PluginDocument;
import com.biomatters.geneious.publicapi.documents.URN;
import org.jdom.Element;
import org.jdom.CDATA;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TextDocument implements PluginDocument, AdditionalSearchContent {

    private String text; // text contents
    private String name; // document name
    private Date creationDate; // creation date of file

    // code for the word count field
    private static final String KEY_WORD_COUNT = "WORD_COUNT";
    private static final String KEY_EVERY_SECOND_WORD = "EVERY_SECOND_WORD";

    public TextDocument(String name, Date creationDate, String text) {
        this.name = name;
        this.creationDate = creationDate;
        this.text = text;
    }

    // Empty constructor: PluginDocument requirement
    public TextDocument() {}

    String getText(){
        return text;
    }

    public Element toXML() {
        Element root = new Element("TextDocument");
        root.addContent(new Element("name").setText(name));
        root.addContent(new Element("date").setText("" + creationDate.getTime()));
        root.addContent(new Element("text").setContent(new CDATA(text)));
        return root;
    }

    public void fromXML(Element doc) {
        name = doc.getChildText("name");

        final String dateText = doc.getChildText("date");
        try {
            creationDate = new Date(Long.parseLong(dateText));
        } catch (NumberFormatException e) {
            // should not happen
        }
        text = doc.getChild("text").getText();
    }

    private int getWordCount() {
        String[] tokens = text.trim().split("\\s+");
        return tokens.length;
    }

    private String getEverySecondWord() {
        StringBuilder everySecondWord = new StringBuilder();
        String[] tokens = text.trim().split("\\s+");
        for (int i = 0; i < tokens.length; i = i + 2) {
            everySecondWord.append(tokens[i]);
            if (i != 0) everySecondWord.append(" ");
        }
        return everySecondWord.toString();
    }

    final DocumentField everySecondWordField = DocumentField.createStringField("Every second word",
            "Every second word in the document", KEY_EVERY_SECOND_WORD);
    final DocumentField wordCountField = DocumentField.createIntegerField("Word count",
            "Number of words in the document", KEY_WORD_COUNT);
    public List<DocumentField> getDisplayableFields() {
        return Arrays.asList(new DocumentField[]{wordCountField});
    }

    public Object getFieldValue(String code) {
        if( code.equals(KEY_WORD_COUNT) ) {
            return getWordCount();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public URN getURN() {
        return null;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getDescription() {
        return "(Text file)";
    }

    public String toHTML() {
        return "<pre>" + text.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;") + "</pre>";
    }

    public Result[] getSearchContent() {
        return new Result[]{
                new Result(null, text),
                new Result(everySecondWordField, getEverySecondWord())
        };
    }
}
