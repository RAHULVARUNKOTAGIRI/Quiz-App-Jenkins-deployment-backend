# Quiz App - Backend API

Spring Boot 3.3.2 + Spring Data JPA + MySQL, Java 17.

## Setup

1. Create the database (or let the app create it - the URL has `createDatabaseIfNotExist=true`):
   ```sql
   CREATE DATABASE quizapp;
   ```
2. Set your MySQL username and password in `src/main/resources/application.properties`.
3. Run it (Maven must be installed - the `mvnw` wrapper was removed):
   ```
   mvn spring-boot:run
   ```
4. Base URL: `http://localhost:8093`

Tests run on an in-memory H2 database, so `mvn test` works without MySQL.

There is **no login or registration in the backend**. The frontend holds the one
admin and the one user, and shows the admin screen or the quiz screen accordingly:
the admin screen calls the admin endpoints, the quiz screen calls the student ones.

## Project structure

```
src/main/java/com/example/quizapp/
  QuizappApplication.java      entry point
  config/CorsConfig.java       lets the frontend call the API from another port
  controllers/                 SubjectController, QuestionController, ResultController
  services/                    SubjectService, QuestionService (scoring), ResultService
  repositories/                SubjectRepository, QuestionRepository, QuizResultRepository
  entities/                    Subject, Question, QuizResult + the DTOs
  exceptions/                  GlobalExceptionHandler + the two exception types
```

## 1. Admin - subjects
| Method | URL | Body / result |
|---|---|---|
| POST | `/subjects` | `{ "name":"Maths" }` |
| GET | `/subjects` | list of subjects (the student picks one from here) |
| GET | `/subjects/{id}` | one subject |
| DELETE | `/subjects/{id}` | deletes the subject **and its questions** |

## 2. Admin - questions
| Method | URL | Body / result |
|---|---|---|
| POST | `/questions/subject/{subjectId}` | one question (format below) |
| POST | `/questions/subject/{subjectId}/bulk` | a JSON **array** of questions |
| GET | `/questions/subject/{subjectId}/admin` | questions **with** `correctOption` |
| PUT | `/questions/{questionId}` | updated question |
| DELETE | `/questions/{questionId}` | `"Question deleted"` |
| GET | `/questions/subject/{subjectId}/count` | number of questions |

Question body:
```json
{
  "questionText": "2 + 2 = ?",
  "optionA": "3", "optionB": "4", "optionC": "5", "optionD": "6",
  "correctOption": "B"
}
```
`questionText`, `optionA`, `optionB` are required. `correctOption` must be A, B, C or D.

## 3. Student - take the quiz
`GET /questions/subject/{subjectId}` - the same questions **without** `correctOption`:
```json
[
  { "id": 1, "questionText": "2 + 2 = ?", "optionA": "3", "optionB": "4", "optionC": "5", "optionD": "6" }
]
```

## 4. Student - submit and get the score
`POST /questions/submit`
```json
{
  "subjectId": 1,
  "studentName": "Ravi",
  "answers": [
    { "questionId": 1, "selectedOption": "B" },
    { "questionId": 2, "selectedOption": "A" }
  ]
}
```
`studentName` is optional - it only labels the saved result, and defaults to `"Student"`.

Response - this is what you show on the result screen:
```json
{
  "resultId": 7,
  "subjectId": 1, "subjectName": "Maths",
  "totalQuestions": 3, "correctAnswers": 1, "wrongAnswers": 1, "unanswered": 1,
  "score": 33,
  "review": [
    { "questionId": 1, "questionText": "2 + 2 = ?", "selectedOption": "B", "correctOption": "B", "correct": true }
  ]
}
```
- `score` is a percentage.
- Every question in the subject counts towards `totalQuestions`, so skipping
  questions cannot inflate the score.
- Only questions the student answered have a `selectedOption`; skipped ones are `null`.
- `resultId` is the saved attempt - fetch it again from `/results/{resultId}`.

## 5. Results - saved automatically on every submit
| Method | URL | Result |
|---|---|---|
| GET | `/results` | **every attempt, newest first** |
| GET | `/results/subject/{subjectId}` | attempts for one subject |
| GET | `/results/{resultId}` | one attempt |
| DELETE | `/results/{resultId}` | `"Result deleted"` |

```json
[
  {
    "id": 7,
    "subject": { "id": 1, "name": "Maths" },
    "studentName": "Ravi",
    "totalQuestions": 3, "correctAnswers": 1, "wrongAnswers": 1, "unanswered": 1,
    "score": 33,
    "attemptedAt": "2026-08-26T12:41:55.21"
  }
]
```
The per-question `review` is **not** stored - it is only in the submit response.

## Errors
All failures come back as JSON with the right HTTP status:
```json
{ "timestamp":"2026-08-26T12:01:10", "status":404, "error":"Not Found", "message":"Subject not found with id 9" }
```
- `404` - subject, question or result id does not exist
- `400` - validation failed, duplicate subject name, empty subject, bad `correctOption`
