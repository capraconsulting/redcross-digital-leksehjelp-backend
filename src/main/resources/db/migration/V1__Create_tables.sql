CREATE TABLE QUESTIONS
  (id INTEGER not null IDENTITY(1,1),
  title VARCHAR(255),
  question_text TEXT,
  is_public BIT DEFAULT 0,
  student_grade VARCHAR(7),
  state INTEGER DEFAULT 1,
  subject_id INTEGER not null FOREIGN KEY REFERENCES SUBJECTS(id),
  question_date DATETIME,
CONSTRAINT [PK_Questions_id] PRIMARY KEY CLUSTERED (id ASC));

--Setup fulltext search catalog and indexes, Language 1044 = Norwegian
CREATE FULLTEXT CATALOG SearchCatalog AS DEFAULT;
CREATE FULLTEXT INDEX ON Questions (question_text Language 1044)
    KEY INDEX PK_Questions_id
    ON SearchCatalog
    WITH STOPLIST = SYSTEM;
CREATE FULLTEXT INDEX ON Answers (answer_text Language 1044)
    KEY INDEX PK_Answers_id
    ON SearchCatalog
    WITH STOPLIST = SYSTEM;

--Used to store the Azure files for the questions
CREATE TABLE FILES
  (id INTEGER not null IDENTITY(1,1),
  share VARCHAR(255),
  directory VARCHAR(1000),
  name VARCHAR(255),
  url VARCHAR(1000),
question_id INTEGER not null FOREIGN KEY REFERENCES QUESTIONS(id));

-- The questions can have only one answer
CREATE TABLE ANSWERS
  (id INTEGER not null IDENTITY(1,1),
  answer_text TEXT,
  answer_date DATETIME,
  question_id INTEGER not null FOREIGN KEY REFERENCES QUESTIONS(id) UNIQUE,
CONSTRAINT [PK_Answers_id] PRIMARY KEY CLUSTERED (id ASC));

-- The questions can only have one email
CREATE TABLE EMAILS
  (id INTEGER not null IDENTITY(1,1),
   question_id INTEGER not null FOREIGN KEY REFERENCES QUESTIONS(id),
    "email VARCHAR(255))";

CREATE TABLE FEEDBACK
  (id INTEGER not null IDENTITY(1,1),
    question_id INTEGER not null FOREIGN KEY REFERENCES QUESTIONS(id),
    feedback_text VARCHAR(255));

CREATE TABLE SUBJECTS
  (id INTEGER not null IDENTITY(1,1) PRIMARY KEY,
   is_mestring BIT DEFAULT 0,
   subject VARCHAR(255));

CREATE TABLE THEMES
  (id INTEGER not null IDENTITY(1,1) PRIMARY KEY,
   theme VARCHAR(255),
   subject_id INTEGER not null FOREIGN KEY REFERENCES SUBJECTS(id));

CREATE TABLE QUESTION_THEMES
    (id INTEGER not null IDENTITY(1,1) PRIMARY KEY,
    question_id INTEGER not null FOREIGN KEY REFERENCES QUESTIONS(id),
    theme_id INTEGER not null FOREIGN KEY REFERENCES THEMES(id));

CREATE TABLE VOLUNTEERS
    (id VARCHAR(255) not null PRIMARY KEY,
    name VARCHAR(255),
    bio_text VARCHAR(1000),
    img_url VARCHAR(1000),
    email VARCHAR(255),
    role VARCHAR(255));

CREATE TABLE VOLUNTEER_SUBJECTS
    (subject_id INTEGER not null FOREIGN KEY REFERENCES SUBJECTS(id),
    volunteer_id VARCHAR(255) not null FOREIGN KEY REFERENCES VOLUNTEERS(id),
    PRIMARY KEY (subject_id, volunteer_id));
