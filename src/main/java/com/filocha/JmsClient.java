package com.filocha;

public interface JmsClient {
    public void send(String msg);
    public String receive();
}
