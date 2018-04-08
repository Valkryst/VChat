package com.valkryst.VChat.message;

import java.io.Serializable;

public class DummyMessage extends Message implements Serializable {
    /** Constructs a new DummyMessage. */
    public DummyMessage() {
        super("DUMMY MESSAGE");
    }
}
