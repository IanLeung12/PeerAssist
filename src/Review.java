public class Review {
    private User user;
    private double mark;
    private String comments;

    public Review(User user, double mark, String comments) {
        this.user = user;
        this.mark = mark;
        this.comments = comments;
    }
}
