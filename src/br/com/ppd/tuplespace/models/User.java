package br.com.ppd.tuplespace.models;

import net.jini.core.entry.Entry;

import javax.sound.sampled.Port;

public class User implements Entry {

    public String name;
    public Environment environment;
    public Float latitude;
    public Float longitude;
    public String IP;
    public Integer Porta;
    public String PATH;

    public User() {}

    public User(String name, Environment environment, Float latitude, Float longitude, String IP, Integer Porta, String PATH) {
        this.name = name;
        this.environment = environment;
        this.latitude = latitude;
        this.longitude = longitude;
        this.IP = IP;
        this.Porta = Porta;
        this.PATH = PATH;
    }

    public User(String name) {
        this.name = name;
    }
}
