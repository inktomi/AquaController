package com.mruno.model;

import java.io.Serializable;

public class Outlet implements Serializable {
    public String name;
    public String state;

    public void setName(String name) {
        this.name = name;
    }

    public void setState(String state) {
        this.state = state;
    }
}
