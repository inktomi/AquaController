package com.mruno.model;

import java.io.Serializable;

public class Power implements Serializable {
    private String failed;
    private String restored;

    // TODO: Format this into a date
    public String getFailed() {
        return failed;
    }

    public void setFailed(String failed) {
        this.failed = failed;
    }

    // TODO: Format this into a date
    public String getRestored() {
        return restored;
    }

    public void setRestored(String restored) {
        this.restored = restored;
    }
}
