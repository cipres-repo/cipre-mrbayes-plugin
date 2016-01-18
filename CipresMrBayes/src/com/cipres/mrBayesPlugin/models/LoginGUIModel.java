package com.cipres.mrBayesPlugin.models;

import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * Login display model where it asks for the user's CIPRES login information
 * @author rjzheng
 *
 */
public class LoginGUIModel extends Options {
    private StringOption username;
    private StringOption password;
    
    /**
     * Add username and password fields to the Login interface
     */
    public LoginGUIModel() {
        username = addStringOption("username","User Name","Please enter your username");
        password = addStringOption("password","Password","Please enter your password");
    }

    /**
     * Get the user name
     * @return user name
     */
    public String getUsername() {
        return username.getValue();
    }

    /**
     * Get the password
     * @return password
     */
    public String getPassword() {
    	return password.getValue();
    }
}