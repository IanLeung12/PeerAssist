import java.sql.*;

public class SQLiteDB {
    public SQLiteDB() {
        Connection c = null;

        try {
            Class.  forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }

    }
}