package tests;

import model.AlertPlayer;

public class AlertsTests {
    public static void main(String[] args) throws InterruptedException {
        AlertPlayer.INSTANCE.addSoundToQueue("spoof");
        Thread.sleep(2000);
        AlertPlayer.INSTANCE.addSoundToQueue("battery");
        Thread.sleep(2000);
        AlertPlayer.INSTANCE.addSoundToQueue("crash");
        Thread.sleep(2000);
        AlertPlayer.INSTANCE.closeAllAlerts();
    }
}
