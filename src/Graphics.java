import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
    
public class Graphics {

    public static void main(String[] args) {
        // Create a JFrame
        JFrame frame = new JFrame("File Save Example");
        frame.setSize(400, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create a JButton
        JButton chooseButton = new JButton("Choose File");

        // Create a ActionListener for the button
        chooseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create a file chooser
                JFileChooser fileChooser = new JFileChooser();

                // Show the file chooser dialog
                int result = fileChooser.showOpenDialog(null);

                // Check if a file is selected
                if (result == JFileChooser.APPROVE_OPTION) {
                    // Get the selected file
                    File selectedFile = fileChooser.getSelectedFile();

                    // Read the contents of the selected file
                    try {
                        byte[] fileContent = Files.readAllBytes(selectedFile.toPath());

                        // Save the contents to a new file (e.g., in the same directory with "_copy" appended)
                        Path destinationPath = new File(selectedFile.getParentFile(),
                                selectedFile.getName() + "_copy").toPath();

                        Files.write(destinationPath, fileContent);

                        System.out.println("File saved to: " + destinationPath);
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });

        // Add the button to the frame
        frame.add(chooseButton);

        // Set the frame visibility
        frame.setVisible(true);
    }
}
