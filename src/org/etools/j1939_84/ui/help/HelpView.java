/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.ui.help;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;

import org.etools.j1939_84.J1939_84;
import org.etools.j1939_84.resources.Resources;

/**
 * {@link JFrame} used to view the embedded help
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public class HelpView extends JFrame {

	private static final long serialVersionUID = 2335611840888485245L;

	private JPanel buttonsPanel;

	private JButton closeButton;

	private JEditorPane editorPane;

	private URL helpURL;

	public HelpView() {
		super("J1939-84 Tool Help");
		initialize();
	}

	private JPanel getButtonsPanel() {
		if (buttonsPanel == null) {
			buttonsPanel = new JPanel();
			buttonsPanel.add(getCloseButton());
		}
		return buttonsPanel;
	}

	JButton getCloseButton() {
		if (closeButton == null) {
			closeButton = new JButton("Close");
			closeButton.addActionListener(e -> {
				processWindowEvent(new WindowEvent(HelpView.this, WindowEvent.WINDOW_CLOSING));
			});
		}
		return closeButton;
	}

	JEditorPane getEditorPane() {
		if (editorPane == null) {
			editorPane = new JEditorPane();
			editorPane.setEditable(false);
			editorPane.addHyperlinkListener(ev -> {
				if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					try {
						String desc = ev.getDescription();
						if (desc != null && desc.startsWith("#")) {
							desc = desc.substring(1);
							editorPane.scrollToReference(desc);
						} else {
							Desktop.getDesktop().browse(ev.getURL().toURI());
						}
					} catch (Exception e) {
						getLogger().log(Level.SEVERE, "Unable to display help", e);
						JOptionPane.showMessageDialog(HelpView.this, "Unable to open link.", "Error Opening Link",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		}
		return editorPane;
	}

	private URL getHelpURL() {
		if (helpURL == null) {
			helpURL = Resources.class.getResource("Help.html");
		}
		return helpURL;
	}

	private Logger getLogger() {
		return J1939_84.getLogger();
	}

	private void initialize() {
		setIconImage(Resources.getLogoImage());
		getContentPane().add(new JScrollPane(getEditorPane()));
		getContentPane().add(getButtonsPanel(), BorderLayout.SOUTH);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(new Dimension(600, 400));
		setLocationRelativeTo(null);
	}

	private void setPage(URL url) {
		try {
			getEditorPane().setPage(url);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Unable to load help", e);
			JOptionPane.showMessageDialog(HelpView.this, "Unable to open link.", "Error Opening Link",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			setPage(getHelpURL());
		}
		super.setVisible(b);
	}

}
