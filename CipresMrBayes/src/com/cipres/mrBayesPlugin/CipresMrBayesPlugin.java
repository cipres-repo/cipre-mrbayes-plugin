package com.cipres.mrBayesPlugin;

import java.io.File;

import com.biomatters.geneious.publicapi.plugin.DocumentOperation;
import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;
import com.biomatters.geneious.publicapi.plugin.Icons;
import com.biomatters.geneious.publicapi.utilities.IconUtilities;

public class CipresMrBayesPlugin extends GeneiousPlugin{
	public String getName() {
        return "Cipres MrBayes Plugin";
    }

    public String getHelp() {
        return "Getting Help";
    }

    public String getDescription() {
        return "Descriptiton Here";
    }

    public String getAuthors() {
        return "Richard Zheng";
    }

    public String getVersion() {
        return "0.1";
    }

    public String getMinimumApiVersion() {
        return "4.0";
    }

    public int getMaximumApiVersion() {
        return 4;
    }
    
    @Override
	public DocumentOperation[] getDocumentOperations() {
		return new DocumentOperation[]{
				new CipresMrBayesToolbar(),
				new CipresMrBayesTree()};
	}
    
	private File pluginDirectory;
	public static Icons documentIcons;
	public static Icons toolbarIcons;
    public static Icons lightbulbIcon;
    public static Icons warningIcon;

	public void initialize(File INpluginUserDirectory, File pluginDirectory) {
        this.pluginDirectory = pluginDirectory;

        String posterior16Loc = "posterior16.png";
        String posterior32Loc = "posterior32.png";
        String posterior24Loc = "posterior24.png";
        String lightbulb16Loc = "lightbulb16.png";
        String warning16Loc = "warning.png";
//        if (new File(posterior16Loc).exists()) { //running from IntelliJ
        documentIcons = IconUtilities.getIcons(posterior16Loc, posterior32Loc);
        toolbarIcons = IconUtilities.getIcons(posterior16Loc, posterior24Loc);
        lightbulbIcon = IconUtilities.getIcons(lightbulb16Loc);
        warningIcon = IconUtilities.getIcons(warning16Loc);
//        } else {
//            documentIcons = IconUtilities.getIconsFromJar(getClass(), "/posterior16.png", "/posterior32.png");
//            toolbarIcons = IconUtilities.getIconsFromJar(getClass(), "/posterior16.png", "/posterior24.png");
//            lightbulbIcon = IconUtilities.getIconsFromJar(getClass(), "/lightbulb16.png");
//            warningIcon = IconUtilities.getIconsFromJar(getClass(), "/warning.png");
//        }
    }
}
