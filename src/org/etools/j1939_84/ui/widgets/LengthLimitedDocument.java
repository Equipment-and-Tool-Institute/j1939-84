/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui.widgets;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

/**
 * Document used to limit the total number of characters in a
 * {@link JTextComponent}
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class LengthLimitedDocument extends PlainDocument {

    private static final long serialVersionUID = 6656200483294742063L;

    private final int limit;

    /**
     * Constructor
     *
     * @param limit the maximum number of characters allowed in the
     *                  {@link JTextComponent}
     */
    public LengthLimitedDocument(int limit) {
        this.limit = limit;
    }

    @Override
    public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
        if (str != null && (getLength() + str.length()) <= limit) {
            super.insertString(offset, str, attr);
        }
    }

}
