import java.util.ArrayList;
public class User {
    private int ID;
    private String name;
    private int gradeLevel;
    private String email;
    private String password;
    private ArrayList<String> subjects;
    private boolean visible;

    public User(int ID, String name, int gradeLevel, String email,  String password, ArrayList<String> subjects) {
        this.ID = ID;
        this.name = name;
        this.email = email;
        this.gradeLevel = gradeLevel;
        this.password = password;
        this.subjects = subjects;
    }

    @Override
    public String toString() {
        String str = ID + "," + name + "," + gradeLevel + "," + email + "," + password;
        for (String subject: subjects) {
            str = str + "," + subject;
        }
        return str;
    }
}