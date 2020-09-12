package br.com.ppd.tuplespace.models;

import net.jini.core.entry.Entry;

public class User implements Entry {

    public String name;
    public Environment environment;

    public User() {}
    public User(String name, Environment environment) {
        this.name = name;
        this.environment = environment;
    }
    public User(String name) {
        this.name = name;
    }
}
