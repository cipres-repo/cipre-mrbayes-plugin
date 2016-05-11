package com.biomatters.backTranslation;

import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;

/**
 * The bootstrap class for the BackTranslationPlugin. Its function is to act as a
 * factory class for the BackTranslationOperation class and to provide basic
 * information about this plugin to Geneious.
 * <p>
 * It translates a protein sequence into a DNA sequence using the IUPAC
 * ambiguity codes where the translation from amino acid to codon is not
 * determinate.
 * <p>
 * Date: 2007-11-28
 * <p>
 * Copyright (C): Biomatters Ltd, New Zealand
 * 
 * @author Bruce Ashton
 */
public class BackTranslationPlugin extends GeneiousPlugin {
    public String getAuthors() {
        return "Biomatters";
    }

    public String getDescription() {
        return "Runs the EMBOSS tool backtranambig on a protein sequence";
    }

    public DocumentOperation[] getDocumentOperations() {
        return new DocumentOperation[] {  new BackTranslationOperation() };
    }

    public String getHelp() {
        return BackTranslationOperation.HELP;
    }

    /**
     * The maximum API version is checked only against the major version number.
     * Thus, if getMaximumApiVersion() returns 4, the plugin will work with API
     * versions 4.1, 4.2, 4.3 and any other version 4.x, but not version 5.0
     * or above.
     */
    public int getMaximumApiVersion() {
        return 4;
    }

    /**
     * The minimum API version is checked against the entire API version string.
     * If the minimum API version is 4.0, the plugin will not work with API
     * versions 3.9 or below.
     */
    public String getMinimumApiVersion() {
        return "4.0";
    }

    public String getName() {
        return "Back Translation";
    }

    public String getVersion() {
        return "0.1";
    }
}
