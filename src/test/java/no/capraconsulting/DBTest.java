package no.capraconsulting;
import org.testng.annotations.Test;

public class DBTest {

    @Test
    public void setUpFillAndTeardownDB(){
        createDatabase();
        insertIntoDatabase();
        deleteDatabase();
    }

    @Test
    public void createDatabase(){
        // DatabasePopulator db = new DatabasePopulator();
        // DatabaseConnector dbConnect = new DatabaseConnector();
        String create = "CREATE DATABASE TestDB";
        //db.createDatabase(dbConnect, create);
        //String delete = "DROP DATABASE TestDB";
        //db.commitToDatabase(dbConnect, delete);
    }

    @Test
    public void deleteDatabase(){
        // DatabasePopulator db = new DatabasePopulator();
        // DatabaseConnector dbConnect = new DatabaseConnector();
        String delete = "DROP DATABASE TestDB";
        //db.commitToDatabase(dbConnect, delete);
    }

    @Test
    public void insertIntoDatabase(){
        createDatabase();
        // DatabasePopulator db = new DatabasePopulator();
        // DatabaseConnector dbConnect = new DatabaseConnector();

        String insert = "INSERT INTO QUESTIONS VALUES (4, 'Test'," +
            " 'TEST'," +
            " 'TEST'," +
            " 'TEST'," +
            " 'TEST'," +
            " 'TEST')";

        //db.commitToDatabase(dbConnect, insert);
    }
}
