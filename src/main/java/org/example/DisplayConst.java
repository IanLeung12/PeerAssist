package org.example; /**
 * [DisplayConst.java]
 * Constant variables that are mainly used for display purposes
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DisplayConst {

    private static final Logger LOG = Logger.getLogger(DisplayConst.class.getName());

    static BufferedImage profile = image("/images/profile.png");
    static BufferedImage logo = image("/images/logo.png");
    static String[] subjectArr = new String[]{"Math", "English", "Physics", "Chemistry", "Biology", "Art", "Music",
            "Computer Science", "Business", "French", "Social Studies"};
    public static final int SUBJECT_COUNT = subjectArr.length;
    static Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * Image
     * Loads an image from the classpath. Never throws: if the image is missing
     * or cannot be decoded, a 1x1 transparent placeholder is returned so the
     * application can still start and existing references stay valid.
     * @param path image location
     * @return the loaded image, or a 1x1 transparent placeholder on failure
     */
    public static BufferedImage image(String path){
        try (InputStream in = DisplayConst.class.getResourceAsStream(path)) {
            if (in == null) {
                LOG.log(Level.FINE, "Image not found on classpath: {0}", path);
                return blankImage();
            }
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                LOG.log(Level.FINE, "Image could not be decoded: {0}", path);
                return blankImage();
            }
            return img;
        } catch (Exception e) {
            LOG.log(Level.FINE, "Failed to load image: " + path, e);
            return blankImage();
        }
    }

    /**
     * blankImage
     * Returns a 1x1 fully transparent image used as a safe fallback.
     * @return a 1x1 transparent BufferedImage
     */
    private static BufferedImage blankImage() {
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * enableFullScreen
     * On macOS, enables the native full-screen capability (the green title-bar
     * button / View ▸ Enter Full Screen) for the given window. This is a no-op
     * on other platforms or if the Apple API is unavailable.
     * @param window the window to make full-screen capable
     */
    public static void enableFullScreen(java.awt.Window window) {
        try {
            Class<?> util = Class.forName("com.apple.eawt.FullScreenUtilities");
            java.lang.reflect.Method m = util.getMethod(
                    "setWindowCanFullScreen", java.awt.Window.class, boolean.class);
            m.invoke(null, window, Boolean.TRUE);
        } catch (Throwable t) {
            LOG.log(Level.FINE, "Native full-screen not available", t);
        }
    }
}
