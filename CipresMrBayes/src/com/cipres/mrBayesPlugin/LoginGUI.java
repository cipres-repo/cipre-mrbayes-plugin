package com.cipres.mrBayesPlugin;

import com.biomatters.geneious.publicapi.plugin.Options;

public class LoginGUI extends Options {
    private StringOption username;
    private StringOption password;

    public LoginGUI() {
        username = addStringOption("username","User Name","Please enter your username");
        username.setDescription("The type of annotation to add to the sequence");
        password = addStringOption("password","Password","Please enter your password");
    }

    public String getUsername() {
        return username.getValue();
    }

    public String getPassword() {
    	return password.getValue();
    }
}