package com.mruno.model;

import java.io.Serializable;

public class OutletState implements Serializable {
    public String name;
    public int value; // 0, 1, 2 - Auto, Off, On

    public String getPostName() {
        return name + "_state";
    }
}
