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

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(int gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<String> getSubjects() {
        return subjects;
    }

    public void setSubjects(ArrayList<String> subjects) {
        this.subjects = subjects;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}