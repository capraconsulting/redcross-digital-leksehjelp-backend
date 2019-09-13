# redcross-digital-leksehjelp-backend

This project uses some of the microservice baseline, and has the following
dependencies/frameworks:

* [Jetty Web Server](https://www.eclipse.org/jetty/)
* [Jersey JAX-RS](https://jersey.github.io/)
* [TestNG](https://github.com/cbeust/testng)
* [REST-assured: Java DSL for easy testing of REST services](
  https://github.com/rest-assured/rest-assured)
* [Maven Shade Plugin](https://maven.apache.org/plugins/maven-shade-plugin/)
* [jose4j](https://javalibs.com/artifact/org.bitbucket.b_c/jose4j)


### Starting development build
This will start both the server and the database, both running as a docker container.
```bash
cd redcross-digital-leksehjelp-backend
mvn clean install
cd Docker
sudo ./test_docker.sh
```

# Database
We use MSSQL database. For the development build, it is populated and initiated using the endpoints described below. 

ER diagram:
![ER](https://user-images.githubusercontent.com/22448857/62771104-b40e2080-ba9c-11e9-98f4-61a3a4b34c27.png)

# Endpoints

### Populate endpoints
Simple endpoints used for creating a database, tables and filling with some test data,
for development purposes. Only the DatabaseInitalizer should be used for prod (together with
a migration script), and these endpoints should be removed. The DatabaseInitializer will also initialize
the fulltext index and catalogs used for the fulltextsearch. The stoplist is set to system, and the language is
set to norwegian. 

`POST /populate`  
Initializes the database and popoulates with some test data. To ensure the database is populated with a volunteer,
remember to change the DatabasePopulator.java to insert a valid volunteer.

`/populate/stress?total=total`  
Populates the database with ? random questions. Useful for stress testing.

### Questions endpoints
These endpoints give access to everything relevant to the listing and updating of existing questions, and creating new ones. Most of these endpoints are annotated with @JwtFilter, which means the header needs a valid authorization token from Azure AD. If not the server will simply respond with `401 unauthorized`.

Questions have state, to simplify the flow through the status board. The states are as following:  

| Index | Name | Description |
|-------|-----------|---------------------------------------------------------------------------------------------------|
| 1 | NEW | A question without an answer |
| 2 | WIP | Work in progress, these questions have an answer |
| 3 | ANSWERED | Submitted for approval |
| 4 | APPROVED | Approved by a volunteer, as soon as the question is approved the mail will be sent to the student |
| 5 | PUBLISHED | Published and now visible without JWT token, only available for questions tagged with is_public=1 |

`@JwtFilter`  
`GET /questions?state=state&includeAll=includeAll`  
Lists all the questions with the selected `state`. If `includeAll` is `true`, all questions will return. Default is `false`, which results in only the questions which have a `subject_id`, that the logged in volunteer has selected as a subject, will return.  
Example response:  
```json
[
  {
  "id":"2",
  "questionText":"Hva er meningen med livet?",
  "questionDate":"2019-08-08 07:19:55.987",
  "title":"Filosofisk spørsmål",
  "studentGrade":"10",
  "subject":"RLE",
  "themes": [
    {
    "theme": "Etikk",
    "id": 1
    }]
  }
]
```

`@JwtFilter`  
`GET /questions/{questionID}`
Gets more detailed information of a single question with ID `questionID`, including answer and files.
Example response:  
```json
{
"id":"2",
"state":"2",
"questionText":"Hva er meningen med livet?",
"questionDate":"2019-08-08 07:19:55.987",
"answerText":"42",
"answerDate":"2019-08-09 06:14:53.253",
"title":"Filosofisk spørsmål",
"answer":"42",
"studentGrade":"10",
"subject":"RLE",
"themes": [
  {
  "theme": "Etikk",
  "id": 1
  }],
"files": [
  {
  "share": "azureShare",
  "directory": "azureDirectory",
  "fileName": "azureFileName",
  "fileUrl": "azureFileUrl"
  }],
"isSent": "1",
"isPublic": "1"
}
```

`GET /questions/public?searchText=searchText&orderByDate=orderByDate&subjectID=subjectID&page=page&grade=grade`  
Search after `searchText` in all the public questions, and list the results. Can filter by `subjectID` and `grade`. If `orderByDate` is set to `true`, the response will be ordered by the date, the default is `false`, which results in the response being sorted by relevance. The result is pagenated in pages of 10, select the page by using the `page` parameter. The response will include the number of total rows in the result.
Example response:  
```json
[
{
"themes":[],
"questionDate":"2019-08-08 07:19:57.357",
"studentGrade":"10",
"answerText":"4",
"answerDate":"2019-08-08 07:19:57.363",
"subject":"Matte",
"id":"105",
"totalRows":"2",
"title":"Gangespørsmål",
"questionText":"2*2?",
}
]
```

`GET /questions/public/{questionID}`  
Gets more detailed information of a single public question with ID `questionID`, including answer.
Example response:  
```json
{
"id":"2",
"state":"5",
"questionText":"Hva er meningen med livet?",
"questionDate":"2019-08-08 07:19:55.987",
"answerText":"42",
"answerDate":"2019-08-09 06:14:53.253",
"title":"Filosofisk spørsmål",
"answer":"42",
"studentGrade":"10",
"subject":"RLE",
"themes": [
  {
  "theme": "Etikk",
  "id": 1
  }]
}
```

`@JwtFilter`  
`POST /questions/{questionID}`
Edits an exisiting question with the id `questionID`, this is also where the answer will first be inserted.
Example payload:  
```
{
"themes":[],
"studentGrade":"10",
"subject":"Matte",
"isPublic":"true",
"files":[],
"id":"1",
"state":2,
"title":"Addisjon",
"questionText":"Hva er 2+2?",
"answerText":"4",
"questionId":"1"
}
```  
Illegal state transistions (implemented in the `EndpointUtils` class):  
State {1, 2, 3, 4, 5}  -> 1  
State {1,  2}  -> 4  
State {1,  2,  3} -> 5  
State {1, 2, 3, 4} -> 5 hvis is_public = 0  

`POST /questions`
Posts a new question. Note this is a public endpoint.  
Example payload:  
```
{
"email":"email@email.com",
"studentGrade":"12",
"subjectID":5,
"questionText":"Hvorfor faller epler?",
"isPublic":true,
"files":[],
"themes": [
  {
  "theme":"Mekanikk",
  "id":17
  },
  {
  "theme":"Tyngdekraft",
  "id":15}]
}
```

### Volunteer endpoints
These endpoints give access to everything relevant to the volunteers, including registering new volunteers in the database. All of these endpoints are annotated with @JwtFilter, which means the header needs a valid authorization token from Azure AD. If not the server will simply respond with `401 unauthorized`.

`@JwtFilter`  
`GET /volunteers`  
Lists all volunteers.
Example response:  
```json
[
{
"id":"2",
"name":"Morten",
"bio_text":"Min profil.",
"img_url":"URL til mitt profilbilde",
"email":"mail@mail.com"
}
]
```

`@JwtFilter`  
`GET /volunteers/self`  
Gets information about the logged in volunteer.
Example response:  
```json
{
"id":"2",
"name":"Morten",
"bio_text":"Min profil.",
"img_url":"URL til mitt profilbilde",
"email":"mail@mail.com"
}
```

`@JwtFilter`  
`POST /volunteers`  
Updates the information of the logged in volunteer.
Example payload:  
```json
{
"name":"Morten",
"bio_text":"Min profil.",
"img_url":"URL til mitt profilbilde",
"email":"mail@mail.com"
}
```

`@JwtFilter`  
`GET /volunteers/subjects`  
Lists the subjects of the logged in volunteer.
Example response:  
```json
[
{
"id":"2",
"subject":"Norsk",
"is_mestring":"1"
}
]
```

`@JwtFilter`  
`POST /volunteers/subjects`  
Changes the subjects of the logged in volunteer.
Example response:  
```json
{
"subjects": [1, 7, 4, 18]
}
```

### Subjects endpoints
`GET /subjects?isMestring=isMestring`  
Lists all subjects with themes. Can filter by `isMestring`.

`@JwtFilter`  
`POST /subjects`  
Creates a new subject.

### Subjects endpoints
`GET /subjects?isMestring=isMestring`  
Lists all subjects with themes. Can filter by `isMestring`.

`@JwtFilter`  
`POST /subjects`  
Creates a new subject.

### Feedback endpoints
`@JwtFilter`  
`GET /feedback?includeAll=includeAll`  
Lists all feedback. If `includeAll` is `true`, all questions will return. Default is `false`, which results in only the feedback which have a `subject_id`  that the logged in volunteer has selected as a subject. 

`@JwtFilter`  
`POST /feedback/{feedbackID}/delete`  
Deletes the feedback with the ID `feedbackID`.

`POST /question/{questionID}`  
Adds new feedback to the question with ID `questionID`.

`GET /question/{questionID}`  
Gets all feedback to the question with ID `questionID`.

# Other

### Properties

This baseline is set up without Spring, Constretto or similar tools.
Instead the properties are read through the class `PropertiesHelper` in
the Main class.

Properties are read from the following locations, in the following order:

1. `application.properties` from `classpath` (resources/application.properties).
2. `config_override/application.properties` file.


### Logback

The default `logback.xml` in this repository only appends statements to `STDOUT`.
This is intended, and covers the two main use cases:

* Running locally, and in IDE
* Running in [ECS](https://aws.amazon.com/ecs/) with the [awslogs Log Driver](
  https://docs.aws.amazon.com/AmazonECS/latest/developerguide/using_awslogs.html)

If you wish to run the application with logging to file, you should use a
properly set up file appender:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="60 seconds">
  <property name="LOG_DIR" value="logs/"/>
  <property name="appName" value="app"/>
  <appender name="logfile" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_DIR}/${appName}.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${LOG_DIR}/${appName}-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
      <maxHistory>7</maxHistory>
      <totalSizeCap>250MB</totalSizeCap>
      <timeBasedFileNamingAndTriggeringPolicy
        class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
        <maxFileSize>50MB</maxFileSize>
      </timeBasedFileNamingAndTriggeringPolicy>
    </rollingPolicy>
    <encoder>
      <pattern>%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} [%thread] %-5level %logger{35} - %msg%n</pattern>
    </encoder>
  </appender>
  <logger name="org.eclipse.jetty" level="WARN"/>
  <logger name="no.capraconsulting" level="INFO"/>
  <root level="info">
    <appender-ref ref="logfile"/>
  </root>
</configuration>
```
