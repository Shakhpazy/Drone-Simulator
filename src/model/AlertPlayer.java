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
//    private final Map<String, Clip> myClips = new HashMap<>();
//
//    public static synchronized AlertPlayer getInstance() {
//        if(myInstance == null) {
//            myInstance =  new AlertPlayer();
//        }
//        return myInstance;
//    }
//
//    private AlertPlayer() {}
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
////    public void stop() {
////        if(myClip != null && myClip.isRunning()) {
////            myClip.stop();
////        }
////    }
//}
