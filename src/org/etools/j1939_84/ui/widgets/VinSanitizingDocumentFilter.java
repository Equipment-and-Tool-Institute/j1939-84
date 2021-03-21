/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui.widgets;

import static org.etools.j1939_84.utils.VinDecoder.VIN_LENGTH;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import org.etools.j1939_84.utils.VinDecoder;

/**
 * Document Filter used to sanitize the characters input in the VIN Field
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public class VinSanitizingDocumentFilter extends DocumentFilter {

    private final VinDecoder vinDecoder;

    /**
     * Constructor
     */
    public VinSanitizingDocumentFilter() {
        vinDecoder = new VinDecoder();
    }

    /**
     * Constructor exposed for testing
     *
     * @param vinDecoder the {@link VinDecoder}
     */
    public VinSanitizingDocumentFilter(VinDecoder vinDecoder) {
        this.vinDecoder = vinDecoder;
    }

    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr)
                                                                                                         throws BadLocationException {
        String string = sanitize(fb, offset, text);
        fb.insertString(offset, string, attr);
    }

    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                                                                                                                 throws BadLocationException {
        String string = sanitize(fb, offset, text);
        fb.replace(offset, length, string, attrs);
    }

    /**
     * Sanitize each character of the VIN as it's entered
     *
     * @param  fb     the FilterBypass for the Document
     * @param  offset the location in the document where the character is being
     *                    entered
     * @param  text   the text that's being entered
     * @return        the text limited to 17 characters, omitting non-alphanumeric
     *                characters and the letter I, O and Q.
     */
    private String sanitize(DocumentFilter.FilterBypass fb, int offset, String text) {
        String string = vinDecoder.sanitize(text);
        int docLength = fb.getDocument().getLength();
        if (docLength + string.length() <= VIN_LENGTH) {
            return string;
        } else {
            return string.substring(0, VIN_LENGTH - docLength);
        }
    }
}
