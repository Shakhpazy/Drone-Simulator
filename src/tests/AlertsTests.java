package tests;

import model.AlertPlayer;

public class AlertsTests {
    public static void main(String[] args) throws InterruptedException {
        AlertPlayer.INSTANCE.playSound("battery");
        Thread.sleep(5000);
        AlertPlayer.INSTANCE.playSound("crash");
        Thread.sleep(5000);
        AlertPlayer.INSTANCE.closeAllAlerts();
    }
}
