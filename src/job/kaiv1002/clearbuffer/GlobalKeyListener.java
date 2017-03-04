package job.kaiv1002.clearbuffer;


import org.jnativehook.NativeInputEvent;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;

public class GlobalKeyListener extends clearBuffer implements NativeKeyListener
{
    public void nativeKeyPressed(NativeKeyEvent e) {
        if (e.getKeyCode() == NativeKeyEvent.VC_Q && NativeInputEvent.getModifiersText(e.getModifiers()).equals("Ctrl"))
        {
            paused = !paused;

            if (paused)
            {
                trayIcon.displayMessage(APPLICATION_NAME, "Program started again!", TrayIcon.MessageType.INFO);
                frame.setTitle(APPLICATION_NAME);
                item3.setLabel("Pause / Restore [running]");
                trayIcon.setImage(icon);
            }
            else
            {
                trayIcon.displayMessage(APPLICATION_NAME_Paused, "Program paused!", TrayIcon.MessageType.INFO);
                item3.setLabel("Pause / Restore [paused]");
                frame.setTitle(APPLICATION_NAME_Paused);
                trayIcon.setImage(iconP);
            }
        }
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
    }

    public void nativeKeyTyped(NativeKeyEvent e) {
    }
}