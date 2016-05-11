package com.biomatters.helloworld;

import com.biomatters.geneious.publicapi.plugin.GeneiousPlugin;

public class HelloWorldPlugin extends GeneiousPlugin {
	
    public String getName() {
        return "Hello, World!";
    }

    public String getHelp() {
        return "Do nothing Geneious plugin";
    }

    public String getDescription() {
        return "Hello World Geneious plugin";
    }

    public String getAuthors() {
        return "you";
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
}
