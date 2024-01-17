import java.util.ArrayList;
public class User {
    private int ID;
    private String name;
    private int gradeLevel;
    private String email;
    private String password;
    private ArrayList<String> topics;
    private boolean visible;

    public User(int ID, String name, int gradeLevel, String email,  String password, ArrayList<String> topics) {
        this.ID = ID;
        this.name = name;
        this.email = email;
        this.gradeLevel = gradeLevel;
        this.password = password;
        this.topics = topics;
    }

}