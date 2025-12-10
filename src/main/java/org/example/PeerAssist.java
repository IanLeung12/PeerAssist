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


        MongoDB db = new MongoDB("enter api key here", "PeerAssist");
        users = db.loadUsers();

        // Login loop
        LoginDisplay login = new LoginDisplay(users, db);
        while (login.getUser() == null) {
            login.refresh();
            Thread.sleep(5);
        }
        User user = login.getUser();
        if (login.isNewUser()) {
            users.add(user);
        }
        login.dispose();
        documents = db.loadDocs(users);


        // Main loop
        MainDisplay md = new MainDisplay(user, documents, users, db);
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
