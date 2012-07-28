package com.mruno.model;

import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

public class Datalog {
    public String hostname;
    public String serial;

    @XStreamImplicit(itemFieldName="record")
    public List<Record> records;
}
