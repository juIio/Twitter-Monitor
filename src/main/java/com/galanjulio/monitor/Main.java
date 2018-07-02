package com.galanjulio.monitor;

public class Main {

    public static final String TITLE = "Monitor";
    public static final String VERSION = "v1.0";

    public static void main(String[] args) {
        new Monitor();
    }

    public static void log(String message) {
        System.out.println("[" + TITLE + "] " + message);
    }
}
