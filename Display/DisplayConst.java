import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DisplayConst {


    static BufferedImage profile = image("Pictures/profile.png");
    static String[] subjectArr = new String[]{"Math", "English", "Physics", "Chemistry", "Biology", "Art", "Music",
            "Computer Science", "Business", "French", "Social Studies"};

    /**
     * Image
     * Loads an image
     * @param path image location
     * @return the image
     */
    public static BufferedImage image(String path){
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
