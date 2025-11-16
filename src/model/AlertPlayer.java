//package model;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import javax.sound.sampled.*;
//
//public enum AlertPlayer {
//    INSTANCE;
//
//    private final Map<String, Clip> myAlerts = new HashMap<>();
//    private static final String BATTERY_ALERT_PATH = "src/SFX/BatteryAlert.wav";
//    private static final String CRASH_ALERT_PATH = "src/SFX/CrashAlert.wav";
//
//    private AlertPlayer() {
////        myAlerts.put(BATTERY_ALERT_PATH, )
//    }
//
//    public void newClip(String theFilePath) {
//        try {
//            File file =  new File(theFilePath);
//            if(!file.exists()) {
//                System.err.println("File doesn't exist: " + theFilePath);
//                return;
//            }
//            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
//
//            myClip = AudioSystem.getClip();
//            myClip.open(audioInputStream);
//
//            myClip.addLineListener(event -> {
//                if (event.getType() == LineEvent.Type.STOP) {
//                    myClip.close();
//                }
//            });
//        }
//        catch(UnsupportedAudioFileException |  IOException | LineUnavailableException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void play() {
//        if(myClip != null) {
//            if(myClip.isRunning()) {
//                myClip.stop();
//            }
//            myClip.setMicrosecondPosition(0);
//            myClip.start();
//        }
//    }
//
//    public void stop() {
//        if(myClip != null && myClip.isRunning()) {
//            myClip.stop();
//        }
//    }
//}
