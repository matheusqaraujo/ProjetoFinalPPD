package br.com.ppd.tuplespace.service;

public class ServiceUnavailable extends Exception {
    public ServiceUnavailable(String msg) {
        super(msg);
    }
}
