package org.example;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import org.bson.types.Binary;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Updates.push;


public class MongoDB {

    private String uri;
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection userCollection;
    private MongoCollection docCollection;

    public MongoDB(String uri, String dbName) {
        this.client = MongoClients.create(uri);
        this.database = client.getDatabase(dbName);
        this.userCollection = database.getCollection("users");
        this.docCollection = database.getCollection("documents");
    }

    public void saveUser(User user) {
        long userID = userCollection.countDocuments() + 1;
        user.setID((int) userID);
        org.bson.Document userDoc = new Document()
                .append("userId", userID)
                .append("name", user.getName())
                .append("grade", user.getGradeLevel())
                .append("email", user.getEmail())
                .append("password", user.getPassword())
                .append("subjects", String.join(" ", user.getSubjects()));
        this.userCollection.insertOne(userDoc);
    }

    public void saveDoc(org.example.Document doc){
        long docID = docCollection.countDocuments() + 1;
        doc.setID((int) docID);
        String subjectStr = "";
        for (int i = 0; i < DisplayConst.subjectArr.length; i ++) {
            if (doc.getTopics().contains(DisplayConst.subjectArr[i])) {
                subjectStr = subjectStr + "1";
            } else {
                subjectStr = subjectStr + "0";
            }
        }

        org.bson.Document docDoc = new Document()
                .append("docId", docID)
                .append("userId", doc.getUser().getID())
                .append("name", doc.getName())
                .append("maxMark", doc.getMaxMark())
                .append("gradeLevel", doc.getGradeLevel())
                .append("subjects", subjectStr)
                .append("content", new Binary(doc.convertToBytes()));
        this.docCollection.insertOne(docDoc);
    }

    public void addReview(org.example.Document document, Review review) {
        org.bson.Document reviewDoc = new Document()
                .append("reviewer", review.getUser().getID())
                .append("mark", review.getMark())
                .append("comment", review.getComments());
        docCollection.updateOne(eq("docId", document.getID()), push("reviews",reviewDoc));
    }

    public ArrayList<User> loadUsers() {
        ArrayList<User> users = new ArrayList<>();
        for (Object userDoc : userCollection.find()) {
            org.bson.Document doc = (org.bson.Document) userDoc;
            ArrayList<String> subjects = new ArrayList<>();
            String str = doc.getString("subjects");
            String[] strSubjects = str.split(" ");
            for (String subject: strSubjects) {
                subjects.add(subject);
            }
            int userId = ((Long) doc.get("userId")).intValue();
            int grade = (int) doc.get("grade");
            users.add(new User(userId, doc.getString("name"), grade, doc.getString("email"), doc.getString("password"), subjects));
        }
        return users;
    }

    public ArrayList<org.example.Document> loadDocs(ArrayList<User> users) {
        ArrayList<org.example.Document> documents = new ArrayList<>();
        for (Object docDoc : docCollection.find()) {
            org.bson.Document doc = (org.bson.Document) docDoc;
            ArrayList<String> topics = new ArrayList<>();
            String str = doc.getString("subjects");
            for (int i = 0; i < str.length(); i ++) {
                if (str.charAt(i) == '1') {
                    topics.add(DisplayConst.subjectArr[i]);
                }
            }
            int docId = ((Long) doc.get("docId")).intValue();
            int userId = (int) doc.get("userId");
            int gradeLevel = (int) doc.get("gradeLevel");
            documents.add(new org.example.Document(docId, (String) doc.get("name"), users.get(userId - 1), doc.getDouble("maxMark"), gradeLevel, topics, doc.get("content", Binary.class).getData()));
            List<Document> reviewDocs = (List<Document>) doc.get("reviews");
            if (reviewDocs != null) {
                for (Document reviewDoc : reviewDocs) {
                    documents.get(documents.size() - 1).addReview(new Review(users.get(reviewDoc.getInteger("reviewer") - 1), reviewDoc.getDouble("mark"), reviewDoc.getString("comment")));
                }
            }
        }
        return documents;
    }
    public void close(){
        client.close();
    }
}
