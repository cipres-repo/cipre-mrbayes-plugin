package com.cipres.mrBayesPlugin.models;

import com.biomatters.geneious.publicapi.plugin.Options;

public class LoginGUIModel extends Options {
    private StringOption username;
    private StringOption password;
    

    public LoginGUIModel() {
        username = addStringOption("username","User Name","Please enter your username");
        password = addStringOption("password","Password","Please enter your password");
    }

    public String getUsername() {
        return username.getValue();
    }

    public String getPassword() {
    	return password.getValue();
    }
}