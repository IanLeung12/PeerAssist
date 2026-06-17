package org.example; /**
 * [Document.java]
 * Document object that students can review
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

public class Document{
    private int ID;
    private String name;
    private String pathname;
    private PDDocument document;
    private double maxMark;
    private double avgMark;
    private int gradeLevel;
    private ArrayList<String> topics;
    private ArrayList<Review> reviews;
    private User user;

    /**
     * Document
     * Creates a Document object with the specified parameters.
     * @param ID The ID of the document
     * @param user The user associated with the document
     * @param pathname The pathname of the document file
     * @param maxMark The maximum mark achievable for the document
     * @param gradeLevel The grade level associated with the document
     * @param topics The topics associated with the document
     */
    Document(int ID, User user, String pathname, double maxMark, int gradeLevel, ArrayList<String> topics) {
        this.ID = ID;
        this.user = user;
        this.pathname = pathname;
        try {
            File file = new File(pathname);
            this.name = file.getName();
            this.document = Loader.loadPDF(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load PDF from " + pathname, e);
        }
        this.maxMark = maxMark;
        this.gradeLevel = gradeLevel;
        this.topics = topics;
        this.reviews = new ArrayList<>();
    }

    Document(int ID, String name, User user, double maxMark, int gradeLevel, ArrayList<String> topics, byte[] content) {
        this.ID = ID;
        this.user = user;
        this.name = name;
        this.maxMark = maxMark;
        this.gradeLevel = gradeLevel;
        this.topics = topics;
        this.reviews = new ArrayList<>();
        try {
            this.document = Loader.loadPDF(content);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load PDF content for document " + ID, e);
        }
    }

    /**
     * toString
     * {@inheritDoc}
     * Overrides the toString method to provide a formatted string representation of the document.
     * @return String A formatted string representing the document
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(ID).append(",")
                .append(user != null ? user.getID() : "")
                .append(",")
                .append(name).append(",");
        for (int i = 0; i < DisplayConst.subjectArr.length; i++) {
            if (topics != null && topics.contains(DisplayConst.subjectArr[i])) {
                str.append("1");
            } else {
                str.append("0");
            }
        }
        return str.toString();
    }

    public byte[] convertToBytes(){
        byte[] bytePDF = null;
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            document.save(byteStream);
            bytePDF = byteStream.toByteArray();
        } catch(Exception e) {
            throw new RuntimeException("Failed to serialize PDF for document " + ID, e);
        }
        return bytePDF;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    /**
     * addReview
     * Adds a review to the document and updates the average mark.
     * @param review The review to be added to the document
     */
    public void addReview(Review review) {
        avgMark = (avgMark * reviews.size() + review.getMark())/(reviews.size() + 1);
        reviews.add(review);
    }

    /**
     * getDocument
     * Returns the PDDocument associated with the document.
     * @return PDDocument The PDDocument associated with the document
     */
    public PDDocument getDocument() {
        return document;
    }

    /**
     * getName
     * Returns the name of the document.
     * @return String The name of the document
     */
    public String getName() {
        return name;
    }

    /**
     * getUser
     * Returns the user associated with the document.
     * @return User The user associated with the document
     */
    public User getUser() {
        return user;
    }

    /**
     * getAvgMark
     * Returns the average mark of the document.
     * @return double The average mark of the document
     */
    public double getAvgMark() {
        return avgMark;
    }

    /**
     * getMaxMark
     * Returns the maximum mark achievable for the document.
     * @return double The maximum mark achievable for the document
     */
    public double getMaxMark() {
        return maxMark;
    }

    /**
     * avgPercent
     * Returns the average mark as a fraction of the maximum mark, in the range
     * 0..1. Returns 0 when the maximum mark is not positive.
     * @return double The average mark divided by the maximum mark (0..1)
     */
    public double avgPercent() {
        return maxMark <= 0 ? 0 : avgMark / maxMark;
    }

    /**
     * getGradeLevel
     * Returns the grade level associated with the document.
     * @return int The grade level associated with the document
     */
    public int getGradeLevel() {
        return gradeLevel;
    }

    /**
     * getTopics
     * Returns the topics associated with the document.
     * @return ArrayList<String> The topics associated with the document
     */
    public ArrayList<String> getTopics() {
        return topics;
    }

    /**
     * getReviews
     * Returns the reviews associated with the document.
     * @return ArrayList<Review> The reviews associated with the document
     */
    public ArrayList<Review> getReviews() {
        return reviews;
    }

    /**
     * NameComparator
     * Inner class representing a comparator for sorting documents based on name.
     */
    public static class NameComparator implements Comparator<Document> {
        @Override
        public int compare(Document o1, Document o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    /**
     * MarkComparator
     * Inner class representing a comparator for sorting documents based on average mark.
     */
    public static class MarkComparator implements Comparator<Document> {
        @Override
        public int compare(Document o1, Document o2) {
            return Double.compare(o2.avgMark, o1.avgMark);
        }
    }

    /**
     * GradeComparator
     * Inner class representing a comparator for sorting documents based on grade level.
     */
    public static class GradeComparator implements Comparator<Document> {
        @Override
        public int compare(Document o1, Document o2) {
            return Integer.compare(o2.gradeLevel, o1.gradeLevel);
        }
    }

    /**
     * ReviewComparator
     * Inner class representing a comparator for sorting documents based on review count.
     */
    public static class ReviewComparator implements Comparator<Document> {
        @Override
        public int compare(Document o1, Document o2) {
            return Integer.compare(o2.reviews.size(), o1.reviews.size());
        }
    }
}
