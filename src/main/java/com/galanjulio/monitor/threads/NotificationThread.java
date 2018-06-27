package com.galanjulio.monitor.threads;

import com.galanjulio.monitor.Main;
import com.galanjulio.monitor.Monitor;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.List;

public class NotificationThread extends Thread {

    private Twitter twitter;
    private String handle;
    private String reply;

    private Status currentStatus;

    public NotificationThread(Monitor main) {
        setName("Monitor - Reply Thread");

        twitter = main.getTwitter();
        handle = main.getString("handle_to_track");
        reply = main.getString("tweet_reply");

        if (handle.charAt(0) != '@') {
            handle = "@" + handle;
        }
    }

    @Override
    public void run() {
        while (true) {
            List<Status> tweets;

            try {
                tweets = twitter.getUserTimeline(handle);
            } catch (TwitterException e) {
                Main.log("Could not retrieve " + handle + "'s timeline: " + e.getErrorMessage());
                e.printStackTrace();
                return;
            }

            Status tweet = tweets.get(0);

            if (!tweet.isRetweet()) {
                if (currentStatus == null) {
                    currentStatus = tweet;

                    Main.log("Found new tweet: \"" + tweet.getText() + "\"");
                } else {
                    if (currentStatus.getId() != tweet.getId()) {
                        replyToTweet(tweet);
                    }
                }
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void replyToTweet(Status status) {
        StatusUpdate statusUpdate = new StatusUpdate(reply);
        statusUpdate.setInReplyToStatusId(status.getId());

        try {
            twitter.updateStatus(statusUpdate);

            Main.log("Reply sent to: " + handle);
        } catch (TwitterException e) {
            Main.log("Could not send tweet: " + e.getErrorMessage());
            e.printStackTrace();
        }
    }
}
