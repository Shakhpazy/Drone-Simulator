package model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sound.sampled.*;

public enum AlertPlayer {
    INSTANCE;

    private final Map<String, Clip> myAlerts = new HashMap<>();
    private static final String BATTERY_ALERT_PATH = "src/SFX/battery.wav";
    private static final String CRASH_ALERT_PATH = "src/SFX/crash.wav";
    private static final String SPOOF_ALERT_PATH = "src/SFX/spoof.wav";
    private static final String BOUNDS_ALERT_PATH = "src/SFX/out-of-bounds.wav";
    private static final String ACCELERATION_ALERT_PATH = "src/SFX/acceleration.wav";

    //Set up playback queue to make sure each sound is played in order.
    private final BlockingQueue<String> myPlaybackQueue = new LinkedBlockingQueue<>();
    private final Thread myPlaybackThread;
    private volatile boolean isRunning = true;

    //Track when each sound has been queued
    private final Map<String, Long> myCooldowns = Map.of(
            "out-of-bounds", 500L,
            "crash", 0L,
            "spoof", 500L,
            "acceleration", 3000L,
            "battery", 2000L
    );

    private final Map<String, Long> myLastQueued = new HashMap<>();

    AlertPlayer() {
        //Load sounds on startup and store in myAlerts
        loadSound("battery", BATTERY_ALERT_PATH);
        loadSound("crash", CRASH_ALERT_PATH);
        loadSound("spoof", SPOOF_ALERT_PATH);
        loadSound("out-of-bounds", BOUNDS_ALERT_PATH);
        loadSound("acceleration", ACCELERATION_ALERT_PATH);

        // Initialize and start the dedicated playback thread to process queue
        myPlaybackThread = new Thread(this::playbackLoop, "SoundPlaybackThread");
        myPlaybackThread.setDaemon(true); //Allow program to exit even if thread runs
        myPlaybackThread.start();
    }

    private void playbackLoop() {
        while (isRunning) {
            try {
                //Wait till a sound name enters the queue
                String soundName = myPlaybackQueue.take();
                //Get clip based on soundName that entered queue
                Clip alert = myAlerts.get(soundName);

                //If the sound exists then play sound.
                if(alert != null) {
                    playClipSynchronously(alert, soundName);
                }
                else {
                    System.err.println("Sound not found in manager: " + soundName); //Else print sound not found
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Playback thread interrupted");
                break;
            }
        }
    }

    private synchronized void playClipSynchronously(Clip theAlert, String theSoundName) {
        final Object lock = new Object(); //Make a lock for synchronization

        LineListener listener = event -> {
            if(event.getType() == LineEvent.Type.STOP) {
                synchronized(lock) {
                    lock.notifyAll();
                }
            }
        }; // Notify all listeners on STOP

        if(!theAlert.isOpen()) { // Try reopening alerts that aren't open
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(new File(getPathForSound(theSoundName)));
                theAlert.open(audioStream);
            }
            catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
                return;
            }
        }
        theAlert.addLineListener(listener); //Add line listener to the Alert

        synchronized(lock) {
            theAlert.stop(); //Stop Alert if playing
            theAlert.setFramePosition(0); // Restart to 0
            theAlert.start(); // PLay Alert
            System.out.println(theSoundName + " started playing");

            try {
                lock.wait(); //Wait until current clip stops before processing next sound.
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            theAlert.removeLineListener(listener); //Clean up Alert when done
            System.out.println(theSoundName + " finished playing");
        }
    }

    private void loadSound(String theName, String theFilePath) {
        try {
            File file = new File(theFilePath); //Try to get the file
            if(!file.exists()) {
                System.err.println("This sound file was not found: " + theFilePath);
                return;
            }
            //If file exists make an audio stream
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip alert = AudioSystem.getClip(); //Get clip empty clip
            alert.open(audioInputStream); //Open audio input stream

            //Add clip to myAlerts
            myAlerts.put(theName, alert);
            System.out.println("Successfully loaded sound: " +  theName);
        }
        catch(UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public void addSoundToQueue(String theSoundName) {
        if(myAlerts.containsKey(theSoundName)) {
            long now = System.currentTimeMillis();
            Long cooldown =  myCooldowns.getOrDefault(theSoundName, 2000L);
            Long lastTime = myLastQueued.get(theSoundName);

            if(lastTime == null || now - lastTime >= cooldown) {
                try {
                    myPlaybackQueue.put(theSoundName);
                    myLastQueued.put(theSoundName, now);
                    System.out.println("Added " + theSoundName + " to the queue");
                }
                catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Failed to add " + theSoundName + " to the queue");
                }
            }
            else {
                System.out.println("Skipped " + theSoundName + " due to cooldown");
            }
        }
    }

    public void closeAllAlerts() {
        isRunning = false; //Stop Running queue
        myPlaybackThread.interrupt(); //Interrupt thread if it gets stuck waiting for queue (even though we're done).

        for(Clip theAlert : myAlerts.values())  { //Close all alerts
            if(theAlert != null && theAlert.isOpen()) {
                theAlert.stop();
                theAlert.close();
                System.out.println("Successfully closed alert: " + theAlert);
            }
        }
        myAlerts.clear();
        System.out.println("All alerts closed");
    }

    private String getPathForSound(String theSoundName) { //Helper method to return sound paths based on sound name
        return switch (theSoundName) {
            case "battery" -> BATTERY_ALERT_PATH;
            case "crash" -> CRASH_ALERT_PATH;
            case "spoof" -> SPOOF_ALERT_PATH;
            case "out-of-bounds" -> BOUNDS_ALERT_PATH;
            case "acceleration" -> ACCELERATION_ALERT_PATH;
            default -> null;
        };
    }
}
