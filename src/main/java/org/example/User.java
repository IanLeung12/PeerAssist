package org.example; /**
 * [User.java]
 * User class representing a PeerAssist user
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import java.util.ArrayList;
public class User {
    private int ID;
    private String name;
    private int gradeLevel;
    private String email;
    private String password;
    private ArrayList<String> subjects;

    /**
     * Constructor for the User class.
     *
     * @param ID         The unique identifier for the user.
     * @param name       The name of the user.
     * @param gradeLevel The grade level of the user.
     * @param email      The email address of the user.
     * @param password   The password associated with the user's account.
     * @param subjects   The list of subjects that the user is interested in or associated with.
     */
    public User(int ID, String name, int gradeLevel, String email,  String password, ArrayList<String> subjects) {
        this.ID = ID;
        this.name = name;
        this.email = email;
        this.gradeLevel = gradeLevel;
        this.password = password;
        this.subjects = subjects;
    }

    /**
     * toString
     * Returns a string representation of the User object.
     *
     * @return A string containing user information in the format: "ID, name, gradeLevel, email, password, subjects".
     */
    @Override
    public String toString() {
        String str = ID + "," + name + "," + gradeLevel + "," + email + "," + password + ",";

        for (String subject: subjects) {
            str = str + subject + " ";
        }
        return str;
    }

    /**
     * getID
     * Retrieves the unique identifier of the user.
     *
     * @return The user's ID.
     */
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
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

    /**
     * getPassword
     * Retrieves the password associated with the user's account.
     *
     * @return The user's password.
     */
    public String getPassword() {
        return password;
    }

    public ArrayList<String> getSubjects() {
        return subjects;
    }
}