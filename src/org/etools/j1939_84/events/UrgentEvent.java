/*
 * Copyright (c) 2021. Equipment & Tool Institute
 */

package org.etools.j1939_84.events;

import java.util.Objects;

import org.etools.j1939_84.controllers.QuestionListener;
import org.etools.j1939_84.controllers.ResultsListener.MessageType;

public class UrgentEvent implements Event {

    private final String message;
    private final String title;
    private final MessageType messageType;
    private final QuestionListener questionListener;

    public UrgentEvent(String message, String title, MessageType messageType, QuestionListener questionListener) {
        this.message = message;
        this.title = title;
        this.messageType = messageType;
        this.questionListener = questionListener;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public QuestionListener getQuestionListener() {
        return questionListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UrgentEvent that = (UrgentEvent) o;
        return Objects.equals(getMessage(), that.getMessage())
                && Objects.equals(getTitle(), that.getTitle())
                && getMessageType() == that.getMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessage(), getTitle(), getMessageType());
    }
}
