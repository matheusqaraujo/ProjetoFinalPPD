package br.com.ppd.tuplespace.models;

import net.jini.core.entry.Entry;

public class User implements Entry {

    public String name;
    public Environment environment;
    public Float latitude;
    public Float longitude;

    public User() {}
    public User(String name, Environment environment, Float latitude, Float longitude) {
        this.name = name;
        this.environment = environment;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public User(String name) {
        this.name = name;
    }
}
