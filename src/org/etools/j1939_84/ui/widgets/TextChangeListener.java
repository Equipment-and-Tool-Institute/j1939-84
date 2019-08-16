/**
 * Copyright (c) 2019. Equipment & Tool Institute
 */
package org.etools.j1939_84.ui.widgets;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Listener for changes in text component
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 *
 */
public abstract class TextChangeListener implements DocumentListener {

	@Override
	public void changedUpdate(DocumentEvent e) {
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		textChanged();
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		textChanged();
	}

	/**
	 * Called when the text has changed.
	 */
	public abstract void textChanged();
}
