package br.com.ppd.tuplespace.models;

import net.jini.core.entry.Entry;

public class Message implements Entry {

    public String content;
    public String sender;
    public String env;

    public Message() {}
}
