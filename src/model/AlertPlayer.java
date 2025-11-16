package model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

public enum AlertPlayer {
    INSTANCE;

    private final Map<String, Clip> myAlerts = new HashMap<>();
    private static final String BATTERY_ALERT_PATH = "src/SFX/BatteryAlert.wav";
    private static final String CRASH_ALERT_PATH = "src/SFX/CrashAlert.wav";

    AlertPlayer() {
        //Load sounds on startup
        loadSound("battery", BATTERY_ALERT_PATH);
        loadSound("crash", CRASH_ALERT_PATH);
    }

    private void loadSound(String theName, String theFilePath) {
        try {
            File file = new File(theFilePath);
            if(!file.exists()) {
                System.err.println("This sound file was not found: " + theFilePath);
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
            Clip alert = AudioSystem.getClip();
            alert.open(audioInputStream);

            /*
            * FIXME: Add listener class to handle the closing and reopening
            *  of clips for proper resource management.
            */
//            //Add listener to close clip when it ends
//            alert.addLineListener(event -> {
//               if(event.getType() == LineEvent.Type.CLOSE) {
//                   if(alert.getFramePosition() >= alert.getFrameLength()) {
//                       alert.close();
//                       System.out.println("This clip has been closed: " + theName);
//                   }
//               }
//            });

            //Add clip to myAlerts
            myAlerts.put(theName, alert);
            System.out.println("Successfully loaded sound: " +  theName);
        }
        catch(UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public synchronized void playSound(String theSoundName) {
        Clip alert = myAlerts.get(theSoundName);
        if(alert != null) {
            if(alert.isOpen()) {
                if(alert.isRunning()) {
                    alert.stop(); //Stop current playback
                }
                alert.setMicrosecondPosition(0); // Rewind to start
                alert.start(); // Play
            }
            else {
                System.err.println("Clip " + theSoundName + " is closed. Cannot replay without manual reopen logic.");
            }
        }
        else {
            System.err.println("Sound not found in manager: " + theSoundName);
        }
    }

    private synchronized boolean areAlertsPlaying() {
        for(Clip alert : myAlerts.values()) {
            if(alert != null && alert.isRunning()) {
                return true;
            }
        }
        return false;
    }

    public void closeAllAlerts() {
        for(Clip alert : myAlerts.values()) {
            if(alert != null && alert.isOpen()) {
                alert.stop(); //Stop alert if currently playing.
                alert.close(); //Close clip
                System.out.println("Successfully closed alert: " + alert.toString());
            }
        }
        myAlerts.clear(); //Clear map and release resources
        System.out.println("All alerts closed.");
    }
}
