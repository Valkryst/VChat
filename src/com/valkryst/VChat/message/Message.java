package com.valkryst.VChat.message;

import lombok.Getter;

import java.io.Serializable;

public class Message implements Serializable {
    /** The message. */
    @Getter private final String message;

    /**
     * Constructs a new Message.
     *
     * @param message
     *          The message.
     */
    public Message(final String message) {
        this.message = (message.isEmpty() ? "" : message);
    }

    @Override
    public String toString() {
        return message;
    }
}
