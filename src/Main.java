import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        LoginDisplay login = new LoginDisplay();
        while (login.getUser() == null) {
            login.refresh();
            Thread.sleep(1);
        }

        User user = login.getUser();
        login.dispose();
//        Set<User> users = new HashSet<>();
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.print("Enter ID: ");
//        int ID = scanner.nextInt();
//        scanner.nextLine(); // Consume the newline character
//
//        System.out.print("Enter name: ");
//        String name = scanner.nextLine();
//
//        System.out.print("Enter education: ");
//        int grade = scanner.nextInt();
//
//        System.out.print("Enter email: ");
//        String email = scanner.nextLine();
//
//        System.out.print("Enter password: ");
//        String password = scanner.nextLine();
//
//        System.out.print("Enter topics (comma-separated): ");
//        String topicsInput = scanner.nextLine();
//        String[] topicsArray = topicsInput.split(",");
//        ArrayList<String> topics = new ArrayList<>();
//        for (String topic : topicsArray) {
//            topics.add(topic.trim());
//        }
//
//        // Creating a new User object using the input
//        users.add(new User(ID, name, grade, email, password, topics));

    }
}