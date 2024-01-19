

public class Main {
    public static void main(String[] args) throws InterruptedException {
        LoginDisplay login = new LoginDisplay();
        while (login.getUser() == null) {
            login.refresh();
            Thread.sleep(1);
        }

        User user = login.getUser();
        login.dispose();
        System.out.println(user);


//        ArrayList<Document> documents = new ArrayList<>();
//        documents.add(new Document("Pictures/Proposal.pdf", 100, 11));
//        documents.add(new Document("Pictures/Mario Essay.pdf", 100, 7));
//        MainDisplay md = new MainDisplay(null);
//        while (true) {
//            md.refresh();
//            try {
//                Thread.sleep(5);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }
}