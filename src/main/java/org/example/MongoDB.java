package org.example;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import org.bson.Document;
import org.bson.types.Binary;
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
                .append("password", user.getPassword());
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

    public void close(){
        client.close();
    }
}
