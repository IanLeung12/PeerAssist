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
        System.out.println(user);

    }
}