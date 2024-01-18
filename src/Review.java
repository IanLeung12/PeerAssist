public class Review {
    private final User user;
    private double mark;
    private String comments;

    public Review(User user, double mark, String comments) {
        this.user = user;
        this.mark = mark;
        this.comments = comments;
    }

    public User getUser() {
        return user;
    }

    public double getMark() {
        return mark;
    }

    public void setMark(double mark) {
        this.mark = mark;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
