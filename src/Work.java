import com.sun.org.apache.xpath.internal.operations.String;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class Work extends PDDocument {
    PDDocument document;
    private double maxMark;
    private double avgMark;
    private int gradeLevel;
    private ArrayList<String> topics;
    private ArrayList<Review> reviews;


    Work(String pathname, double maxMark, double avgMark, int gradeLevel, ArrayList<String> topics, ArrayList<Review> reviews) {
        try {
            this.document = PDDocument.load(new File(java.lang.String.valueOf(pathname)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.maxMark = maxMark;
        this.avgMark = avgMark;
        this.gradeLevel = gradeLevel;
        this.topics = topics;
        this.reviews = reviews;
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);



    }

    public void setDocument(PDDocument document) {
        this.document = document;
    }

    public double getMaxMark() {
        return maxMark;
    }

    public void setMaxMark(double maxMark) {
        this.maxMark = maxMark;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(int gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public ArrayList<String> getTopics() {
        return topics;
    }

    public void setTopics(ArrayList<String> topics) {
        this.topics = topics;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }

    public static class MarkComparator implements Comparator<Work> {
        @Override
        public int compare(Work o1, Work o2) {
            return (int) (o1.avgMark - o2.avgMark);
        }
    }

    public static class GradeComparator implements Comparator<Work> {
        @Override
        public int compare(Work o1, Work o2) {
            return (int) (o1.gradeLevel - o2.gradeLevel);
        }
    }

    public static class ReviewComparator implements Comparator<Work> {
        @Override
        public int compare(Work o1, Work o2) {
            return (int) (o1.reviews.size() - o2.reviews.size());
        }
    }
}
