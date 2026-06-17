package org.example; /**
 * [User.java]
 * User class representing a PeerAssist user
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import java.util.ArrayList;
public class User {
    private String ID;
    private String name;
    private int gradeLevel;
    private String email;
    private ArrayList<String> subjects;

    /**
     * Constructor for the User class.
     *
     * @param ID         The unique identifier for the user.
     * @param name       The name of the user.
     * @param gradeLevel The grade level of the user.
     * @param email      The email address of the user.
     * @param subjects   The list of subjects that the user is interested in or associated with.
     */
    public User(String ID, String name, int gradeLevel, String email, ArrayList<String> subjects) {
        this.ID = ID;
        this.name = name;
        this.email = email;
        this.gradeLevel = gradeLevel;
        this.subjects = subjects;
    }

    /**
     * toString
     * Returns a string representation of the User object.
     *
     * @return A string containing user information in the format: "ID, name, gradeLevel, email, subjects".
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(ID).append(",")
                .append(name).append(",")
                .append(gradeLevel).append(",")
                .append(email).append(",");

        if (subjects != null) {
            for (String subject : subjects) {
                str.append(subject).append(" ");
            }
        }
        return str.toString();
    }

    /**
     * getID
     * Retrieves the unique identifier of the user.
     *
     * @return The user's ID.
     */
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * getName
     * Retrieves the name of the user.
     *
     * @return The user's name.
     */
    public String getName() {
        return name;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public String getEmail() {
        return email;
    }

    public ArrayList<String> getSubjects() {
        return subjects;
    }
}
