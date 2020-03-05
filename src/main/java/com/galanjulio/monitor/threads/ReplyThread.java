package com.galanjulio.monitor.threads;

import com.galanjulio.monitor.Main;
import com.galanjulio.monitor.Monitor;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReplyThread extends Thread {

    private static final int TIMEOUT_RETRY_SECONDS = 60;

    private Twitter twitter;
    private TreeMap<String, String> handlesAndReplies;
    private Map<String, Status> currentTweets;

    public ReplyThread(Monitor main, TreeMap<String, String> handlesAndReplies) {
        this.twitter = main.getTwitter();
        this.handlesAndReplies = handlesAndReplies;
        this.currentTweets = new HashMap<>();

        for (Map.Entry<String, String> entry : handlesAndReplies.entrySet()) {
            String handle = entry.getKey();

            if (handle.charAt(0) != '@') {
                handle = "@" + handle;
            }

            Main.log("Successfully set up a new reply thread for " + handle);
        }

        setName("Monitor - Reply Thread");
    }

    @Override
    public void run() {
        while (true) {
            for (Map.Entry<String, String> entry : handlesAndReplies.entrySet()) {
                String handle = entry.getKey();
                String reply = entry.getValue();
                Status currentTweet = currentTweets.get(handle);
                List<Status> tweets = null;

                try {
                    tweets = twitter.getUserTimeline(handle);
                } catch (Exception e) {
                    Main.log("Could not retrieve " + handle + "'s timeline: " + e.getLocalizedMessage());
                    Main.log("Pausing for " + TIMEOUT_RETRY_SECONDS + " seconds then retrying.");
                    System.out.println(" ");

                    try {
                        sleep(TIMEOUT_RETRY_SECONDS * 1000);
                    } catch (InterruptedException ignored) {
                    }
                }

                if (tweets != null) {
                    int index = 0;

                    // We don't want to reply to retweets
                    while (tweets.get(index).isRetweet()) {
                        index++;
                    }

                    Status tweet = tweets.get(index);

                    if (currentTweet == null) {
                        currentTweets.put(handle, tweet);

                        Main.log("Logged latest tweet from " + handle + ":\"" + tweet.getText() + "\"");
                        Main.log("Link: https://twitter.com/" + handle.substring(1) + "/status/" + tweet.getId());
                        System.out.println(" ");
                    } else {
                        if (currentTweet.getId() != tweet.getId()) {
                            replyToTweet(tweet, handle, reply);
                        }
                    }

                    try {
                        sleep(2000 * handlesAndReplies.size());
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }
    }

    private void replyToTweet(Status tweet, String handle, String reply) {
        StatusUpdate statusUpdate = new StatusUpdate(handle + " " + reply);
        statusUpdate.setInReplyToStatusId(tweet.getId());

        try {
            twitter.updateStatus(statusUpdate);

            Main.log("Reply sent to: " + handle);
            Main.log("Reply: " + reply);
            Main.log("Tweet: \"" + tweet.getText() + "\"");
            Main.log("Link: https://twitter.com/" + handle.substring(1) + "/status/" + tweet.getId());
            System.out.println(" ");
        } catch (TwitterException e) {
            Main.log("Could not send tweet: " + e.getErrorMessage());
            System.out.println(" ");
            e.printStackTrace();
            return;
        }

        currentTweets.put(handle, tweet);
    }
}
