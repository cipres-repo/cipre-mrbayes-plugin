package org.suchard.gui.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;


class DoubleOnlyDocument extends PlainDocument {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6650227149424866524L;
	private double min;
	private double max;
	
	public DoubleOnlyDocument(double min, double max) {
		super();
		this.min = min;
		this.max = max;
	}
	
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        try {
          	String newStr = getText( 0, getLength() ) + str;
           	double f = Double.parseDouble( newStr );
            if( f >= min && f <= max )   
            	super.insertString( offs, str, a );
        } catch (NumberFormatException e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}