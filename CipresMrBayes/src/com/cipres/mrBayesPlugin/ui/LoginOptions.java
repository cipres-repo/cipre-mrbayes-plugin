package com.cipres.mrBayesPlugin.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.biomatters.geneious.publicapi.plugin.Options;

/**
 * Login display model where it asks for the user's CIPRES login information
 * @author rjzheng
 *
 */
public class LoginOptions extends Options{

    private JPasswordField passwordField;
    private StringOption username;
    JLabel usernameLabel;
    JLabel passwordLabel;
    
    public LoginOptions() {
	    super(LoginOptions.class);
	    username = addStringOption("username","User Name","Please enter your username");
	    passwordField = new JPasswordField("Please enter your password");
	    passwordField.setEchoChar((char)0);
	    passwordField.addKeyListener(new KeyListener() {
	    	 
	        @Override
	        public void keyTyped(KeyEvent event) {
	            passwordField.setEchoChar('*');
	        }
	     
	        @Override
	        public void keyReleased(KeyEvent event) {
	            // do something when a key has been released
	        }
	     
	        @Override
	        public void keyPressed(KeyEvent event) {
	            // do something when a key has been pressed
	        }
	    });
    }
    	
    
    protected JPanel createPanel() {
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        
        usernameLabel = new JLabel(username.getLabel());
        passwordLabel = new JLabel("Password");
        
        panel.add(username.getComponent());

        panel.add(passwordLabel);
        panel.add(passwordField);
        
        layout.setHorizontalGroup(
    		layout.createSequentialGroup()
    			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(usernameLabel)
					.addComponent(passwordLabel))
    			.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
					.addComponent(username.getComponent())
					.addComponent(passwordField))
        );
        
        layout.setVerticalGroup(
        		layout.createParallelGroup()
        		.addGroup(layout.createSequentialGroup()
    				.addComponent(usernameLabel)
    				.addComponent(passwordLabel))
        		.addGroup(layout.createSequentialGroup()
        			.addComponent(username.getComponent())
        			.addComponent(passwordField))
		);
        return panel;
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
        return new String(passwordField.getPassword());
     }
    	
}