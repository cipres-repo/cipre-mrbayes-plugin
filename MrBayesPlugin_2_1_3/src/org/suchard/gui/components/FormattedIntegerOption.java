package org.suchard.gui.components;


import org.suchard.gui.OptionsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * looks like:    Option Name ~ [TEXT]( [TEXT FIELD] )
 *
 */
public class FormattedIntegerOption extends IntegerOption {

	private String distribution;
	
    public FormattedIntegerOption(String name, String dist, int defaultValue, int min, int max, Preferences PREFS) {
        super(name, defaultValue, min, max, PREFS);
        distribution = dist;
    }

    @Override
    public void addTo(OptionsPanel panel) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);
        p.add(textField, BorderLayout.CENTER);
        JLabel l = new JLabel(")");
        l.setOpaque(false);
        p.add(l, BorderLayout.EAST);
        panel.addComponentWithLabel(name + " ~ "+distribution+" (", p, false);
    }
}

