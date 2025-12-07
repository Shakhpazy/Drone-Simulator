package tests;

import model.AlertPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertsTests {

    @AfterEach
    void tearDown() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testDifferentSoundsPLay() {
        assertTrue(AlertPlayer.INSTANCE.addSoundToQueue("crash"));
        assertTrue(AlertPlayer.INSTANCE.addSoundToQueue("out-of-bounds"));
        assertTrue(AlertPlayer.INSTANCE.addSoundToQueue("battery"));
        assertTrue(AlertPlayer.INSTANCE.addSoundToQueue("acceleration"));
        assertTrue(AlertPlayer.INSTANCE.addSoundToQueue("spoof"));
    }

    @Test
    void testSameSoundCoolDown() {
        //Test on one instance: Should be true since this is the first time
        assertEquals(true, AlertPlayer.INSTANCE.addSoundToQueue("acceleration"));

        //Should be false since these sounds are being added in quick succession
        AlertPlayer.INSTANCE.addSoundToQueue("acceleration");
        assertEquals(false, AlertPlayer.INSTANCE.addSoundToQueue("acceleration"));
    }
}
