package org.example;

/**
 * [Review.java]
 * Object representing a review of a document
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

public class Review {
    private final User user;
    private double mark;
    private String comments;

    /**
     * Constructor for the Review class.
     *
     * @param user     The user providing the review.
     * @param mark     The numerical mark given to the document.
     * @param comments Additional comments provided by the user.
     */
    public Review(User user, double mark, String comments) {
        this.user = user;
        this.mark = mark;
        this.comments = comments;
    }

    /**
     * getUser
     * Retrieves the user who provided the review.
     *
     * @return The user object associated with the review.
     */
    public User getUser() {
        return user;
    }

    /**
     * getMark
     * Retrieves the numerical mark given to the document in the review.
     *
     * @return The mark given to the document.
     */
    public double getMark() {
        return mark;
    }

    /**
     * getComments
     * Retrieves any additional comments provided by the user in the review.
     *
     * @return Additional comments associated with the review.
     */
    public String getComments() {
        return comments;
    }

    /**
     * toString
     * Returns a string representation of the Review object in the format: "userID, mark, comments".
     *
     * @return A string containing the user's ID, mark, and comments.
     */
    @Override
    public String toString() {
        return user.getID() + "," + mark + "," + comments;
    }
}
