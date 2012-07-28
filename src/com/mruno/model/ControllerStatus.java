package com.mruno.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ControllerStatus implements Serializable {
    public String hostname;
    public String serial;
    public Date date;
    public Power power;
    public List<Probe> probes;
    public List<Outlet> outlets;

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setPower(Power power) {
        this.power = power;
    }

    public void setProbes(List<Probe> probes) {
        this.probes = probes;
    }

    public void setOutlets(List<Outlet> outlets) {
        this.outlets = outlets;
    }
}
