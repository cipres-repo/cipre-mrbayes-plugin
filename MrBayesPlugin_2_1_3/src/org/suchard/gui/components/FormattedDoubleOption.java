package org.suchard.gui.components;

import org.suchard.gui.OptionsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class FormattedDoubleOption extends DoubleOption {

    private String distribution;
    private JLabel l;
	
    public FormattedDoubleOption(String name, String dist, double defaultValue, double min, double max, Preferences PREF) {
        super(name, defaultValue, min, max, PREF);
        distribution = dist;
    }

    @Override
    public JComponent getExtraComponent() { return l; }
    
    @Override
    public void addTo(OptionsPanel panel) {
        JPanel p = new JPanel(new BorderLayout(5, 0));
        p.setOpaque(false);
        p.add(textField, BorderLayout.CENTER);
        l = new JLabel(")");
        l.setOpaque(false);
        p.add(l, BorderLayout.EAST);
        label = panel.addComponentWithLabel(name + " ~ "+distribution+" (", p, false);
    }
}
