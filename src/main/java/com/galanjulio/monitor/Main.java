package com.galanjulio.monitor;

public class Main {

    public static void main(String[] args) {
        new Monitor();
    }

    public static void log(String message) {
        System.out.println("[Monitor] " + message);
    }
}
