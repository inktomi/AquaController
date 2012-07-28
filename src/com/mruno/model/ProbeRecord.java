package com.mruno.model;

import java.util.Date;

public class ProbeRecord extends Probe {
    public ProbeRecord(Date date, String name, double value) {
        this.date = date;
        this.name = name;
        this.value = value;
    }

    public Date date;
}
