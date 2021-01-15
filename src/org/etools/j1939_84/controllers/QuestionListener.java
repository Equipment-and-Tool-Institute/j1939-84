/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

import javax.swing.JOptionPane;

/**
 * The Interface for an listener that is notified when a {@link Controller} has
 * something to report
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public interface QuestionListener {
    enum AnswerType {
        // The values correspond to JOptionPane Types
        YES(JOptionPane.YES_OPTION), NO(JOptionPane.NO_OPTION), CANCEL(JOptionPane.CANCEL_OPTION);

        public final int value;

        AnswerType(int value) {
            this.value = value;
        }

        public static AnswerType getType(int value) {
            switch (value) {
            case 0:
                return YES;
                case 1:
                    return NO;
                case 2:
                case -1:
                default:
                    return CANCEL;
            }
        }
    }

    void answered(AnswerType answerType);
}
