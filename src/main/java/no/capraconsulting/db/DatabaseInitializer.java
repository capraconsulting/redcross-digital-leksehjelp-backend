package no.capraconsulting.db;
import no.capraconsulting.db.Database;
import java.sql.*;

public class DatabaseInitializer {

    public static void init() {
        // Base table for questions
        // A better way to solve states would probably be to put into seperate table
        String questionTable = 
            "CREATE TABLE QUESTIONS " +
            "(id INTEGER not null IDENTITY(1,1), " +
            "title VARCHAR(255), " +
            "question_text TEXT, " +
            "is_public BIT DEFAULT 0, " +
            "student_grade VARCHAR(7), " +
            "state INTEGER DEFAULT 1, " +
            "subject_id INTEGER not null FOREIGN KEY REFERENCES SUBJECTS(id), " +
            "question_date DATETIME, " +
            "CONSTRAINT [PK_Questions_id] PRIMARY KEY CLUSTERED (id ASC))";
        
        // Setup fulltext search catalog and indexes, Language 1044 = Norwegian
        String createCatalog  = "CREATE FULLTEXT CATALOG SearchCatalog AS DEFAULT";
        String createQuestionIndex  = "CREATE FULLTEXT INDEX ON Questions (question_text Language 1044)  " +
            "KEY INDEX PK_Questions_id " +
            "ON SearchCatalog " +
            "WITH STOPLIST = SYSTEM";
        String createAnswerIndex  = "CREATE FULLTEXT INDEX ON Answers (answer_text Language 1044)  " +
            "KEY INDEX PK_Answers_id " +
            "ON SearchCatalog " +
            "WITH STOPLIST = SYSTEM";

        // Used to store the Azure files for the questions
        String fileTable = 
            "CREATE TABLE FILES " +
            "(id INTEGER not null IDENTITY(1,1), " +
            "share VARCHAR(255), " +
            "directory VARCHAR(1000), " +
            "name VARCHAR(255), " +
            "url VARCHAR(1000), " +
            "question_id INTEGER not null FOREIGN KEY REFERENCES QUESTIONS(id))";

        // The questions can have only one answer
        String answerTable = "CREATE TABLE ANSWERS " +
            "(id INTEGER not null IDENTITY(1,1), " +
            "answer_text TEXT, " +
            "answer_date DATETIME, " +
            "question_id INTEGER not null FOREIGN KEY REFERENCES QUESTIONS(id) UNIQUE, " +
            "CONSTRAINT [PK_Answers_id] PRIMARY KEY CLUSTERED (id ASC))";

        // The questions can only have one email
        String emailTable = "CREATE TABLE EMAILS " +
            "(id INTEGER not null IDENTITY(1,1), " +
            "question_id INTEGER not null FOREIGN KEY REFERENCES QUESTIONS(id), " +
            "email VARCHAR(255))";

        String feedbackTable = "CREATE TABLE FEEDBACK " +
            "(id INTEGER not null IDENTITY(1,1), " +
            "question_id INTEGER not null FOREIGN KEY REFERENCES QUESTIONS(id), " +
            "feedback_text VARCHAR(255))";

        String subjectTable = "CREATE TABLE SUBJECTS " +
            "(id INTEGER not null IDENTITY(1,1) PRIMARY KEY, " +
            "is_mestring BIT DEFAULT 0, " +
            "subject VARCHAR(255))";

        String themeTable = "CREATE TABLE THEMES" +
            "(id INTEGER not null IDENTITY(1,1) PRIMARY KEY, " +
            "theme VARCHAR(255), " +
            "subject_id INTEGER not null FOREIGN KEY REFERENCES SUBJECTS(id))";

        String questionThemesTable = "CREATE TABLE QUESTION_THEMES " +
            "(id INTEGER not null IDENTITY(1,1) PRIMARY KEY, " +
            "question_id INTEGER not null FOREIGN KEY REFERENCES QUESTIONS(id), " +
            "theme_id INTEGER not null FOREIGN KEY REFERENCES THEMES(id))";

        String volunteerTable = "CREATE TABLE VOLUNTEERS " +
            "(id VARCHAR(255) not null PRIMARY KEY, " +
            "name VARCHAR(255), " +
            "bio_text VARCHAR(1000)," +
            "img_url VARCHAR(1000)," +
            "email VARCHAR(255)," +
            "role VARCHAR(255))";

        String volunteerSubjectsTable = "CREATE TABLE VOLUNTEER_SUBJECTS" +
            "(subject_id INTEGER not null FOREIGN KEY REFERENCES SUBJECTS(id), " +
            "volunteer_id VARCHAR(255) not null FOREIGN KEY REFERENCES VOLUNTEERS(id), " +
            "PRIMARY KEY (subject_id, volunteer_id))";

        // Init the database
        try {
            Connection con = Database.INSTANCE.getConnection();
            Statement stmt = con.createStatement();
            stmt.execute(subjectTable);
            stmt.execute(questionTable);
            stmt.execute(fileTable);
            stmt.execute(createCatalog);
            stmt.execute(emailTable);
            stmt.execute(feedbackTable);
            stmt.execute(answerTable);
            stmt.execute(themeTable);
            stmt.execute(volunteerTable);
            stmt.execute(volunteerSubjectsTable);
            stmt.execute(createQuestionIndex);
            stmt.execute(createAnswerIndex);
            stmt.execute(questionThemesTable);
        } catch (SQLException e) {
            e.printStackTrace();
        } 
    }
}
