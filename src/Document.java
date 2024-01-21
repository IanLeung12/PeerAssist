import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;

public class Document extends PDDocument {
    private String name;
    private PDDocument document;
    private double maxMark;
    private double avgMark;
    private int gradeLevel;
    private ArrayList<String> topics;
    private ArrayList<Review> reviews;


    Document(String pathname, double maxMark, int gradeLevel, ArrayList<String> topics, ArrayList<Review> reviews) {

        try {
            File file = new File(pathname);
            this.name = file.getName();
            this.document = PDDocument.load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.maxMark = maxMark;
        this.gradeLevel = gradeLevel;
        this.topics = topics;
        this.reviews = reviews;
        double sumMarks = 0;
        for (Review review: reviews) {
            sumMarks += review.getMark();
        }
        this.avgMark = sumMarks/reviews.size();

    }

    Document(String pathname, double maxMark, int gradeLevel, ArrayList<String> topics) {

        try {
            File file = new File(pathname);
            this.name = file.getName();
            this.document = PDDocument.load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.maxMark = maxMark;
        this.gradeLevel = gradeLevel;
        this.topics = topics;
        this.reviews = new ArrayList<>();
    }

    Document(String pathname, double maxMark, int gradeLevel) {

        try {
            File file = new File(pathname);
            this.name = file.getName();
            this.document = PDDocument.load(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.maxMark = maxMark;
        this.avgMark = Math.random() * 100.0;
        this.gradeLevel = gradeLevel;
        this.topics = new ArrayList<>();
        for (int i = 1; i < Math.random() * 7; i ++) {
            topics.add(DisplayConst.subjectArr[(int) (Math.random() * 11)]);
        }
        this.reviews = new ArrayList<>();
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);



    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public double getAvgMark() {
        return avgMark;
    }

    public void setAvgMark(double avgMark) {
        this.avgMark = avgMark;
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

    public static class NameComparator implements Comparator<Document> {
        @Override
        public int compare(Document o1, Document o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }

    public static class MarkComparator implements Comparator<Document> {
        @Override
        public int compare(Document o1, Document o2) {
            return (int) (o2.avgMark - o1.avgMark);
        }
    }

    public static class GradeComparator implements Comparator<Document> {
        @Override
        public int compare(Document o1, Document o2) {
            return (int) (o2.gradeLevel - o1.gradeLevel);
        }
    }

    public static class ReviewComparator implements Comparator<Document> {
        @Override
        public int compare(Document o1, Document o2) {
            return (int) (o1.reviews.size() - o2.reviews.size());
        }
    }
}
