package org.suchard.gui.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;

class IntegerOnlyDocument extends PlainDocument {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3073997573798776167L;
	private int min;
	private int max;
	
	public IntegerOnlyDocument(int min, int max) {
		super();
		this.min = min;
		this.max = max;
	}
	
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        try {
//            boolean isLeadingZero = false;
//           	try {
//           		isLeadingZero = offs == 0 && Integer.parseInt(str) == 0;
//           	} catch (NumberFormatException e) {
//            		// Do nothing at this point
//           	}
         	String newStr = getText( 0, getLength() ) + str;
           	int i = Integer.parseInt( newStr );
            //
            // Make sure we are within our limits.
            //
            if( //!isLeadingZero && 
            		i >= min && i <= max )   
            	super.insertString( offs, str, a );
        } catch (NumberFormatException e) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
