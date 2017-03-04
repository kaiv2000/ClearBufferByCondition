package job.kaiv1002.clearbuffer;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLDecoder;


public class clearBuffer {

    public static JFrame frame;
    public static TrayIcon trayIcon;
    public static Image icon;
    public static Image iconP;
    public static final String APPLICATION_NAME = "clearBuffer";
    public static final String APPLICATION_NAME_Paused = "clearBuffer [paused]";
    public static final String ICON_STR = "/images/icon32x32.png";
    public static final String ICON_STR_PAUSED = "/images/iconp32x32.png";
    public static volatile boolean paused = true;
    public static MenuItem item4;
    public static MenuItem item3;
    public static SystemTray tray;
    public static boolean autorunenabled;
    public static String regkey;
    public static String fileName;
    public static String startdir = System.getProperty("user.dir");
    public static String softfullpath;
    public static String autoruntextEnabled;
    public static String autoruntextDisabled;
    public static Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    public static String message = "whoops! It could be bad raw here! But I blocked it!";
    public static String result;
    public static StringSelection stringSelection = new StringSelection(message);

    public static void main(String[] args) {
        threadNativeHook.start();
        startGui();
        scanBuffer();
    }


    public static void scanBuffer() {
        Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener(new FlavorListener() {
            @Override
            public void flavorsChanged(FlavorEvent e) {
                if (paused) {
                    try {
                        result = (String) clipboard.getData(DataFlavor.stringFlavor);
                        if  (result.startsWith("www") | result.startsWith("http")) {
                            clipboard.setContents(stringSelection, null);
                            trayIcon.displayMessage(APPLICATION_NAME, message, TrayIcon.MessageType.INFO);
                        }
                    } catch (Exception x) {

                    }
                }

            }
        });

    }

    static void readRegistry() {
        try {
            regkey = WinRegistry.readString(WinRegistry.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", "clearBuffer");
        } catch (Exception e) {
            System.out.println("Problem with registry reading");
        }

        if (regkey == null) {
            autorunenabled = false;
        } else {
            autorunenabled = true;
        }
    }

    static Thread threadNativeHook = new Thread() {
        @Override
        public void run() {
            try {
                GlobalScreen.registerNativeHook();
            } catch (NativeHookException ex) {
                System.err.println("There was a problem registering the native hook.");
                System.exit(1);
            }
            GlobalScreen.addNativeKeyListener(new GlobalKeyListener());
        }
    };


    public static void getfilename() {
        String path = clearBuffer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            int index = decodedPath.lastIndexOf("/");
            fileName = decodedPath.substring(index + 1);

            softfullpath = (startdir + "\\" + fileName);
            autoruntextDisabled = (fileName + "\nwas deleted from autostart in registry");
            autoruntextEnabled = (fileName + "\nwas added to autostart in registry with it's current location:\n" + startdir);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    static void startGui() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                readRegistry();
                getfilename();
                setTrayIcon();
                trayIcon.displayMessage(APPLICATION_NAME, "Program started! Press Ctrl+Q for pause.", TrayIcon.MessageType.INFO);
            }
        });
    }

    static void setTrayIcon() {

        frame = new JFrame(APPLICATION_NAME);
        frame.setVisible(false);
        if (!SystemTray.isSupported()) {
            return;
        }

        PopupMenu exitMenu = new PopupMenu();
        MenuItem item = new MenuItem("Exit");
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                trayIcon.displayMessage(APPLICATION_NAME, "Bye-Bye!", TrayIcon.MessageType.INFO);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                System.exit(0);
            }
        });

        PopupMenu trayMenu = new PopupMenu();

        if (paused) {
            item3 = new MenuItem("Pause / Restore [running]");
            frame.setTitle(APPLICATION_NAME);
        } else {
            item3 = new MenuItem("Pause / Restore [paused]");
            frame.setTitle(APPLICATION_NAME_Paused);
        }
        item3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                paused = !paused;
                if (paused) {
                    trayIcon.displayMessage(APPLICATION_NAME, "Program started again!", TrayIcon.MessageType.INFO);
                    frame.setTitle(APPLICATION_NAME);
                    item3.setLabel("Pause / Restore [running]");
                    trayIcon.setImage(icon);
                } else {
                    trayIcon.displayMessage(APPLICATION_NAME_Paused, "Program paused!", TrayIcon.MessageType.INFO);
                    item3.setLabel("Pause / Restore [paused]");
                    frame.setTitle(APPLICATION_NAME_Paused);
                    trayIcon.setImage(iconP);
                }
            }
        });


        PopupMenu trayMenu4 = new PopupMenu();
        if (regkey == null) {
            item4 = new MenuItem("Autostart program");
        } else {
            item4 = new MenuItem("Autostart program [enabled]");
        }
        item4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autorunenabled = !autorunenabled;
                if (autorunenabled) {
                    JOptionPane.showMessageDialog(frame, autoruntextEnabled, "Autostart enabled", JOptionPane.INFORMATION_MESSAGE);
                    item4.setLabel("Autostart program [enabled]");
                } else {
                    JOptionPane.showMessageDialog(frame, autoruntextDisabled, "About disabled", JOptionPane.INFORMATION_MESSAGE);
                    item4.setLabel("Autostart program");
                }
                addAutorun();
            }
        });

        PopupMenu trayMenu5 = new PopupMenu();
        MenuItem item5 = new MenuItem("About");
        item5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(frame, String.format("<html><center>Ivan Kavatsiv<br>2016<br></center></html>"), "About", JOptionPane.INFORMATION_MESSAGE);

            }
        });

        trayMenu.add(item5);
        trayMenu.add(item4);
        trayMenu.add(item3);
        trayMenu.add(item);


        URL imageURLPaused = clearBuffer.class.getResource(ICON_STR_PAUSED);
        iconP = Toolkit.getDefaultToolkit().getImage(imageURLPaused);
        URL imageURL = clearBuffer.class.getResource(ICON_STR);
        icon = Toolkit.getDefaultToolkit().getImage(imageURL);

        if (paused) trayIcon = new TrayIcon(icon, APPLICATION_NAME, trayMenu);
        else {
            trayIcon = new TrayIcon(iconP, APPLICATION_NAME_Paused, trayMenu);
        }
        trayIcon.setImageAutoSize(true);
        tray = SystemTray.getSystemTray();
        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        trayIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setVisible(true);
            }
        });
    }

    public static void addAutorun() {
        if (autorunenabled) {
            try {
                WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", "clearBuffer", softfullpath);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } else {

            try {
                WinRegistry.deleteValue(WinRegistry.HKEY_CURRENT_USER, "Software\\Microsoft\\Windows\\CurrentVersion\\Run", "clearBuffer");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}