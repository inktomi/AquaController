package com.mruno.model;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Record {
    public Date date;

    @XStreamImplicit(itemFieldName="probe")
    public List<Probe> probes;
}
