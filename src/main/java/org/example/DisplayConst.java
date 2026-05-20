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
import java.io.IOException;
import java.io.InputStream;

public class DisplayConst {


    static BufferedImage profile = image("/images/profile.png");
    static BufferedImage logo = image("/images/logo.png");
    static String[] subjectArr = new String[]{"Math", "English", "Physics", "Chemistry", "Biology", "Art", "Music",
            "Computer Science", "Business", "French", "Social Studies"};
    static Dimension size = Toolkit.getDefaultToolkit().getScreenSize();

    /**
     * Image
     * Loads an image
     * @param path image location
     * @return the image
     */
    public static BufferedImage image(String path){
        try (InputStream in = DisplayConst.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Image not found on classpath: " + path);
            }
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
