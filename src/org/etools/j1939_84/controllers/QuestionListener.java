/**
 * Copyright 2019 Equipment & Tool Institute
 */
package org.etools.j1939_84.controllers;

/**
 * The Interface for an listener that is notified when a {@link Controller} has
 * something to report
 *
 * @author Matt Gumbel (matt@soliddesign.net)
 */
public interface QuestionListener {
    enum AnswerType {
        // The values correspond to JOptionPane Types
        YES(2), NO(1), CANCEL(-1);

        public final int value;

        AnswerType(int value) {
            this.value = value;
        }

        public static AnswerType getType(int value) {
            switch (value) {
                case 1:
                    return NO;
                case 2:
                    return YES;
                case -1:
                default:
                    return CANCEL;
            }
        }
    }

    void answered(AnswerType answerType) throws InterruptedException;
}
