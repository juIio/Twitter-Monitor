package com.galanjulio.monitor;

import com.galanjulio.monitor.threads.ReplyThread;
import lombok.Getter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Monitor {

    private static final boolean DEBUG_MODE = false;

    private Map<String, Object> settings = new HashMap<>();

    private static Monitor instance;

    private Twitter twitter;

    Monitor() {
        instance = this;

        setupSettings();
        setupTwitter();
        startThread();

        if (DEBUG_MODE) {
            openDebugFrame();
        }
    }

    private void setupSettings() {
        File file = new File("settings.json");

        if (!file.exists()) {
            Main.log("Could not find file 'options.json' please make sure it exists!");
            return;
        }

        if (!file.getName().endsWith(".json")) {
            Main.log("File extension must be .json!");
            return;
        }

        JSONParser parser = new JSONParser();
        Object object = null;

        try {
            object = parser.parse(new FileReader(file));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        if (object == null) {
            Main.log("Could not read " + file.getName() + "!");
            return;
        }

        JSONObject jsonObject = (JSONObject) object;

        for (Object key : jsonObject.keySet()) {
            settings.put(String.valueOf(key), jsonObject.get(key));
        }
    }

    private void setupTwitter() {
        ConfigurationBuilder config = new ConfigurationBuilder();

        config.setDebugEnabled(true)
                .setOAuthConsumerKey(getString("consumer_key"))
                .setOAuthConsumerSecret(getString("consumer_key_secret"))
                .setOAuthAccessToken(getString("access_token"))
                .setOAuthAccessTokenSecret(getString("access_token_secret"));

        twitter = new TwitterFactory(config.build()).getInstance();
    }

    private void openDebugFrame() {
        JFrame frame = new JFrame();
        frame.setLocationRelativeTo(null);
        frame.setSize(200, 200);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.toFront();
    }

    private void startThread() {
        new ReplyThread(this).start();
    }

    public String getString(String setting) {
        return (String) settings.get(setting);
    }

    public int getInt(String setting) {
        return (int) settings.get(setting);
    }

    public static Monitor getInstance() {
        return instance;
    }
}
