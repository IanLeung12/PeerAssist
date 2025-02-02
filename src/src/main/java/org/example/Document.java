package org.example; /**
 * [Document.java]
 * Document object that students can review
 * @author Ian Leung
 * @version 1.0 January 22, 2024
 */

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
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
        this.user = user;
        this.pathname = pathname;
        try {
            File file = new File(pathname);
            this.name = file.getName();
            this.document = Loader.loadPDF(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.maxMark = maxMark;
        this.gradeLevel = gradeLevel;
        this.topics = topics;
        this.reviews = new ArrayList<>();
    }

    /**
     * toString
     * {@inheritDoc}
     * Overrides the toString method to provide a formatted string representation of the document.
     * @return String A formatted string representing the document
     */
    @Override
    public String toString() {
        String str = "";
        str = str + ID + "," + user.getID() + "," + pathname + "," + maxMark + "," + gradeLevel + ",";
        for (int i = 0; i < DisplayConst.subjectArr.length; i ++) {
            if (topics.contains(DisplayConst.subjectArr[i])) {
                str = str + "1";
            } else {
                str = str + "0";
            }
        }

        return str;
    }

    public byte[] convertToBytes(){
        byte[] bytePDF = null;
        try {
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            document.save(byteStream);
            bytePDF = byteStream.toByteArray();
        } catch(Exception e) {
            e.printStackTrace();
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
            return (int) (o2.avgMark - o1.avgMark);
        }
    }

    /**
     * GradeComparator
     * Inner class representing a comparator for sorting documents based on grade level.
     */
    public static class GradeComparator implements Comparator<Document> {
        @Override
        public int compare(Document o1, Document o2) {
            return (int) (o2.gradeLevel - o1.gradeLevel);
        }
    }

    /**
     * ReviewComparator
     * Inner class representing a comparator for sorting documents based on review count.
     */
    public static class ReviewComparator implements Comparator<Document> {
        @Override
        public int compare(Document o1, Document o2) {
            return (int) (o2.reviews.size() - o1.reviews.size());
        }
    }
}
