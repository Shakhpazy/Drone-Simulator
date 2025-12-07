package model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sound.sampled.*;

/**
 * Singleton enum that manages and plays alert sounds for the drone monitoring system.
 * <p>
 * Features:
 * <ul>
 *     <li>Preloads alert sounds on startup.</li>
 *     <li>Queues sounds for sequential, synchronous playback.</li>
 *     <li>Supports per-alert cooldowns to prevent rapid repeats.</li>
 *     <li>Runs a dedicated daemon thread for playback and allows safe shutdown.</li>
 * </ul>
 * </p>
 *
 * Usage:
 * <pre>{@code
 * AlertPlayer.INSTANCE.addSoundToQueue("battery");
 * AlertPlayer.INSTANCE.closeAllAlerts();
 * }</pre>
 *
 * @author Natan Artemiev
 * @version 11/30/25
 */
public enum AlertPlayer {
    INSTANCE;

    /** Loaded audio clips keyed by alert name. */
    private final Map<String, Clip> myAlerts = new HashMap<>();

    /** File paths for each alert sound. */
    private static final String BATTERY_ALERT_PATH = "src/SFX/battery.wav";
    private static final String CRASH_ALERT_PATH = "src/SFX/crash.wav";
    private static final String SPOOF_ALERT_PATH = "src/SFX/spoof.wav";
    private static final String BOUNDS_ALERT_PATH = "src/SFX/out-of-bounds.wav";
    private static final String ACCELERATION_ALERT_PATH = "src/SFX/acceleration.wav";

    /** Playback queue ensuring sequential sound playback. */
    private final BlockingQueue<String> myPlaybackQueue = new LinkedBlockingQueue<>();

    /** Dedicated thread that processes the playback queue. */
    private final Thread myPlaybackThread;

    /** Flag indicating if the playback thread is running. */
    private volatile boolean isRunning = true;

    /** Cooldown durations (ms) for each alert type to prevent repeated alerts in a short time. */
    private final Map<String, Long> myCooldowns = Map.of(
            "out-of-bounds", 500L,
            "crash", 0L,
            "spoof", 500L,
            "acceleration", 3000L,
            "battery", 2000L
    );

    /** Tracks the last time each alert type was queued (timestamp in ms). */
    private final Map<String, Long> myLastQueued = new HashMap<>();

    /**
     * Constructor initializes the alert system:
     * <ul>
     *     <li>Loads all sound files into memory.</li>
     *     <li>Starts the playback thread.</li>
     * </ul>
     */
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

    /**
     * Continuously processes the playback queue.
     * Waits for sound names to appear in the queue and plays them synchronously.
     * Terminates when {@link #isRunning} is set to false.
     */
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

    /**
     * Plays the given clip synchronously, blocking until playback completes.
     * Adds a temporary {@link LineListener} to detect when the clip stops.
     *
     * @param theAlert the {@link Clip} to play
     * @param theSoundName the name of the sound
     */
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

            try {
                lock.wait(); //Wait until current clip stops before processing next sound.
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            theAlert.removeLineListener(listener); //Clean up Alert when done
        }
    }

    /**
     * Loads a sound from the specified file path and stores it in the internal alert map.
     *
     * @param theName the name of the sound
     * @param theFilePath the file path to the audio file
     */
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
        }
        catch(UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    /**
     * Queues a sound for playback, respecting the cooldown configured for the sound type.
     * If the same sound was queued within its cooldown period, the request is ignored.
     *
     * @param theSoundName the name of the sound to queue
     */
    public boolean addSoundToQueue(String theSoundName) {
        boolean result = false;
        if(myAlerts.containsKey(theSoundName)) {
            long now = System.currentTimeMillis();
            Long cooldown =  myCooldowns.getOrDefault(theSoundName, 2000L);
            Long lastTime = myLastQueued.get(theSoundName);

            if(lastTime == null || now - lastTime >= cooldown) {
                try {
                    myPlaybackQueue.put(theSoundName);
                    myLastQueued.put(theSoundName, now);
                    result = true;
                }
                catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return result;
    }

    /**
     * Stops the playback thread and releases all audio resources.
     * Ensures all {@link Clip} instances are closed.
     */
    public String closeAllAlerts() {
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
        return "All alerts closed";
    }

    /**
     * Returns the file path for a given alert name.
     *
     * @param theSoundName the alert name
     * @return the corresponding file path, or {@code null} if unknown
     */
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
