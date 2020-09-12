package br.com.ppd.tuplespace.models;

import net.jini.core.entry.Entry;

public class Environment implements Entry {

    public String name;
    public Float latitude;
    public Float longitude;

    public Environment() {}

    public Environment(String name, Float latitude, Float longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Environment(String name) {
        this.name = name;
    }
}
