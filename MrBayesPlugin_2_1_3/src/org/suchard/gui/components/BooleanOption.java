package org.suchard.gui.components;

import org.suchard.gui.OptionsPanel;

import javax.swing.*;
import java.util.prefs.Preferences;

public class BooleanOption implements Option {

    private String name;
    private boolean defaultValue;
    private final JCheckBox checkBox;
    private Preferences PREFS;
    
    
    /**
     *
     * @param name to be displayed and for saving to prefs
     * @param defaultValue which the field will initially have and may be defaulted to
     * if user's input is invalid.
     */
    public BooleanOption(String name, boolean defaultValue, Preferences PREFS) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.PREFS = PREFS;
        boolean def = PREFS.getBoolean(name, defaultValue);
        checkBox = new JCheckBox(name, def);
        checkBox.setOpaque(false);
        checkBox.setSelected(def);
    }

    public boolean getDefaultValue() {
        return defaultValue;
    }

    public boolean isSelected() {
        return checkBox.isSelected();
    }

    public void saveToPrefs() {
        PREFS.putBoolean(name, isSelected());
    }

    public void addTo(OptionsPanel panel) {
        panel.addComponent(checkBox, false);
    }

	public JComponent getExtraComponent() {
		// TODO Auto-generated method stub
		return null;
	}

	public JLabel getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public JTextField getTextField() {
		// TODO Auto-generated method stub
		return null;
	}

    public void checkInput() throws InvalidInputException {
    }
}
