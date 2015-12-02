package org.suchard.gui.components;

import org.suchard.gui.OptionsPanel;

import javax.swing.*;

public interface Option {

    void addTo(OptionsPanel panel);

    void saveToPrefs();
   
    JTextField getTextField();
    
    JLabel getLabel();
    
    JComponent getExtraComponent();
    
    String getName();

    void checkInput() throws InvalidInputException;
}