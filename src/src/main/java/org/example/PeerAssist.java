package org.example; /**
 * [PeerAssist.java]
 * Runs the PeerAssist Platform, a platform designed for students to publish and review assignments and other documents
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;


public class PeerAssist {
    public static void main(String[] args) throws InterruptedException {
        ArrayList<User> users = new ArrayList<>();
        ArrayList<Document> documents = new ArrayList<>();
       
        // Reading from save files
        
        try {
          
          Scanner reader = new Scanner(new File("Saves/users.txt"));
          while (reader.hasNext()) { // User save
            String line = reader.nextLine();
            String[] words = line.split(",");
            ArrayList<String> subjects = new ArrayList<>();
            String lastWord = words[words.length - 1];
            if (lastWord.charAt(lastWord.length() - 1) != ',') {
              String[] strSubjects = lastWord.split(" ");
              for (String subject: strSubjects) {
                subjects.add(subject);
              }
            }
            users.add(new User(Integer.parseInt(words[0]), words[1], Integer.parseInt(words[2]), words[3], words[4], subjects));
            
          }
          reader.close();
          
          reader = new Scanner(new File("Saves/documents.txt"));
          Document document = null;
          while(reader.hasNext()) { // Document save
            String line = reader.nextLine();
            if (document == null) { // Next thing to add is based on line contents
              String[] words = line.split(",");
              ArrayList<String> topics = new ArrayList<>();
              String lastWord = words[words.length - 1];
              for (int i = 0; i < lastWord.length(); i ++) {
                if (lastWord.charAt(i) == '1') { // Binary representation converted to array
                  topics.add(DisplayConst.subjectArr[i]);
                }
              }
              
              documents.add(new Document(Integer.parseInt(words[0]), users.get(Integer.parseInt(words[1])), words[2],
                                         Double.parseDouble(words[3]), Integer.parseInt(words[4]), topics));
              document = documents.get(documents.size() - 1);
              
            } else if (line.equals("end")) {
              document = null;
            } else {
              String[] words = line.split(",");
              document.addReview(new Review(users.get(Integer.parseInt(words[0])), Double.parseDouble(words[1]), words[2]));
            }

          }
          reader.close();
        } catch (Exception e) {
          e.printStackTrace();
        }


        // Login loop
        LoginDisplay login = new LoginDisplay(users);
        while (login.getUser() == null) {
            login.refresh();
            Thread.sleep(5);
        }
        User user = login.getUser();
        if (login.isNewUser()) {
            users.add(user);
        }
        login.dispose();

        // Main loop
        MainDisplay md = new MainDisplay(user, documents, users);
        while (true) {
            md.refresh();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}