# Quiz Application README

## Note 

I have created backend but was uanble to complete frontend part, if i get one more day then I can build it tomorrow. I have built all the api. 


### I have inserted two rows in users table prior , to avoid creating signup feature as it was not mentioned :

INSERT INTO users (username, email, password, role)
VALUES ('user', 'user@example.com', crypt('user123', gen_salt('bf')), 'USER');
INSERT INTO users (username, email, password, role)
VALUES ('admin', 'admin@example.com', crypt('admin123', gen_salt('bf')), 'ADMIN');



## Overview

This application is a RESTful API for managing quizzes. It allows administrators to create quizzes, add questions, and manage users. Users can take quizzes and submit their responses. The API is built using Spring Boot and follows RESTful principles. Rate limiting is implemented to protect the API from abuse.

## Database Schema

The application uses a relational database to store its data. Below is the schema definition for each table:

### 1. `users` Table

This table stores user information.


sql
CREATE TABLE users (
id BIGSERIAL PRIMARY KEY,
username VARCHAR(50) NOT NULL UNIQUE,
email VARCHAR(100) NOT NULL UNIQUE,
password VARCHAR(100) NOT NULL,
role VARCHAR(10) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);



**Columns:**

*   `id` (BIGSERIAL, PRIMARY KEY): Unique identifier for the user.
*   `username` (VARCHAR(50), NOT NULL, UNIQUE): User's username for login.
*   `email` (VARCHAR(100), NOT NULL, UNIQUE): User's email address.
*   `password` (VARCHAR(100), NOT NULL): User's password (hashed).
*   `role` (VARCHAR(10), NOT NULL): User's role, either 'ADMIN' or 'USER'.
*   `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the user was created.
*   `updated_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the user was last updated.

### 2. `tokens` Table

This table stores JWT tokens for user authentication.


sql
CREATE TABLE tokens (
id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
token VARCHAR(255) NOT NULL UNIQUE,
expiry TIMESTAMP NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT token_expiry_check CHECK (expiry > created_at)
);
CREATE INDEX idx_tokens_user_id ON tokens(user_id);
CREATE INDEX idx_tokens_expiry ON tokens(expiry);


**Columns:**

*   `id` (BIGSERIAL, PRIMARY KEY): Unique identifier for the token.
*   `user_id` (BIGINT, NOT NULL, FOREIGN KEY referencing `users.id`):  ID of the user associated with the token.
*   `token` (VARCHAR(255), NOT NULL, UNIQUE): JWT token string.
*   `expiry` (TIMESTAMP, NOT NULL): Token expiration timestamp.
*   `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the token was created.

**Constraints:**

*   Foreign key constraint on `user_id` referencing `users` table with `ON DELETE CASCADE`.
*   `token_expiry_check`: Ensures that the expiry time is always greater than the creation time.

### 3. `api_requests` Table

This table is used for rate limiting, tracking API requests.


sql
CREATE TABLE api_requests (
id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
endpoint VARCHAR(255) NOT NULL,
request_count INTEGER NOT NULL DEFAULT 1,
window_start TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT valid_request_count CHECK (request_count > 0)
);
CREATE INDEX idx_api_requests_user_window ON api_requests(user_id, window_start);


**Columns:**

*   `id` (BIGSERIAL, PRIMARY KEY): Unique identifier for the API request record.
*   `user_id` (BIGINT, NOT NULL, FOREIGN KEY referencing `users.id`): ID of the user who made the request.
*   `endpoint` (VARCHAR(255), NOT NULL): The API endpoint that was requested.
*   `request_count` (INTEGER, NOT NULL, DEFAULT 1): Count of requests within the rate limit window.
*   `window_start` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the rate limit window started.

**Constraints:**

*   Foreign key constraint on `user_id` referencing `users` table with `ON DELETE CASCADE`.
*   `valid_request_count`: Ensures that the request count is always positive.

### 4. `questions` Table

This table stores quiz questions.


ql
CREATE TABLE questions (
id BIGSERIAL PRIMARY KEY,
question TEXT NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);



**Columns:**

*   `id` (BIGSERIAL, PRIMARY KEY): Unique identifier for the question.
*   `question` (TEXT, NOT NULL): The text of the question.
*   `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the question was created.
*   `updated_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the question was last updated.

### 5. `question_options` Table

This table stores options for each question, including whether the option is correct.


sql
CREATE TABLE question_options (
id BIGSERIAL PRIMARY KEY,
question_id BIGINT NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
option TEXT NOT NULL,
is_correct BOOLEAN NOT NULL DEFAULT false,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


**Columns:**

*   `id` (BIGSERIAL, PRIMARY KEY): Unique identifier for the question option.
*   `question_id` (BIGINT, NOT NULL, FOREIGN KEY referencing `questions.id`): ID of the question this option belongs to.
*   `option` (TEXT, NOT NULL): The text of the option.
*   `is_correct` (BOOLEAN, NOT NULL, DEFAULT false): Indicates if this option is the correct answer.
*   `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the option was created.

**Constraints:**

*   Foreign key constraint on `question_id` referencing `questions` table with `ON DELETE CASCADE`.

### 6. `quizzes` Table

This table stores quiz details.


sql
CREATE TABLE quizzes (
id BIGSERIAL PRIMARY KEY,
title VARCHAR(100) NOT NULL,
description TEXT,
total_score INTEGER NOT NULL CHECK (total_score > 0),
duration INTEGER NOT NULL CHECK (duration > 0), -- in minutes
created_by BIGINT NOT NULL REFERENCES users(id),
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
is_active BOOLEAN NOT NULL DEFAULT true
);


**Columns:**

*   `id` (BIGSERIAL, PRIMARY KEY): Unique identifier for the quiz.
*   `title` (VARCHAR(100), NOT NULL): Title of the quiz.
*   `description` (TEXT): Description of the quiz.
*   `total_score` (INTEGER, NOT NULL): Total possible score for the quiz.
*   `duration` (INTEGER, NOT NULL): Duration of the quiz in minutes.
*   `created_by` (BIGINT, NOT NULL, FOREIGN KEY referencing `users.id`): ID of the user who created the quiz.
*   `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the quiz was created.
*   `updated_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the quiz was last updated.
*   `is_active` (BOOLEAN, NOT NULL, DEFAULT true): Indicates if the quiz is currently active.

**Constraints:**

*   Foreign key constraint on `created_by` referencing `users` table.
*   `total_score` and `duration` are checked to be greater than 0.

### 7. `quiz_questions` Table

This table links quizzes to questions and stores quiz-specific question details.


sql
CREATE TABLE quiz_questions (
id BIGSERIAL PRIMARY KEY,
quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
question_id BIGINT NOT NULL REFERENCES questions(id),
marks INTEGER NOT NULL CHECK (marks > 0),
question_order INTEGER NOT NULL,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
UNIQUE (quiz_id, question_id),
UNIQUE (quiz_id, question_order)
);

CREATE INDEX idx_quiz_questions_quiz ON quiz_questions(quiz_id);


**Columns:**

*   `id` (BIGSERIAL, PRIMARY KEY): Unique identifier for the quiz question entry.
*   `quiz_id` (BIGINT, NOT NULL, FOREIGN KEY referencing `quizzes.id`): ID of the quiz.
*   `question_id` (BIGINT, NOT NULL, FOREIGN KEY referencing `questions.id`): ID of the question.
*   `marks` (INTEGER, NOT NULL): Marks awarded for this question in the quiz.
*   `question_order` (INTEGER, NOT NULL): Order of the question in the quiz.
*   `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the quiz question entry was created.

**Constraints:**

*   Foreign key constraints on `quiz_id` and `question_id` referencing `quizzes` and `questions` tables respectively with `ON DELETE CASCADE` for `quiz_id`.
*   `marks` is checked to be greater than 0.
*   Uniqueness constraints on `(quiz_id, question_id)` and `(quiz_id, question_order)` to ensure each question and order is unique within a quiz.

### 8. `quiz_attempts` Table

This table stores user attempts at quizzes.


sql
CREATE TABLE quiz_attempts (
id BIGSERIAL PRIMARY KEY,
user_id BIGINT NOT NULL REFERENCES users(id),
quiz_id BIGINT NOT NULL REFERENCES quizzes(id),
status VARCHAR(20) NOT NULL CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'EXPIRED')),
start_time TIMESTAMP,
end_time TIMESTAMP,
total_score INTEGER,
obtained_score INTEGER,
created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
CONSTRAINT valid_score CHECK (obtained_score <= total_score),
CONSTRAINT valid_attempt_time CHECK (end_time IS NULL OR end_time > start_time)
);
CREATE INDEX idx_quiz_attempts_user ON quiz_attempts(user_id);
CREATE INDEX idx_quiz_attempts_quiz ON quiz_attempts(quiz_id);



**Columns:**

*   `id` (BIGSERIAL, PRIMARY KEY): Unique identifier for the quiz attempt.
*   `user_id` (BIGINT, NOT NULL, FOREIGN KEY referencing `users.id`): ID of the user taking the quiz.
*   `quiz_id` (BIGINT, NOT NULL, FOREIGN KEY referencing `quizzes.id`): ID of the quiz being attempted.
*   `status` (VARCHAR(20), NOT NULL): Status of the quiz attempt (e.g., 'NOT_STARTED', 'IN_PROGRESS', 'COMPLETED', 'EXPIRED').
*   `start_time` (TIMESTAMP): Timestamp when the quiz attempt started.
*   `end_time` (TIMESTAMP): Timestamp when the quiz attempt ended.
*   `total_score` (INTEGER): Total score of the quiz at the time of attempt.
*   `obtained_score` (INTEGER): Score obtained by the user in this attempt.
*   `created_at` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the quiz attempt record was created.

**Constraints:**

*   Foreign key constraints on `user_id` and `quiz_id` referencing `users` and `quizzes` tables respectively.
*   `status` is checked to be one of the allowed values.
*   `valid_score`: Ensures that the obtained score is not greater than the total score.
*   `valid_attempt_time`: Ensures that the end time is after the start time, if both are present.

### 9. `quiz_responses` Table

This table stores user responses to quiz questions in each attempt.


sql
CREATE TABLE quiz_responses (
id BIGSERIAL PRIMARY KEY,
quiz_attempt_id BIGINT NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
question_id BIGINT NOT NULL REFERENCES questions(id),
user_option TEXT NOT NULL,
is_correct BOOLEAN NOT NULL DEFAULT false,
marks INTEGER CHECK (marks >= 0),
response_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
UNIQUE (quiz_attempt_id, question_id)
);
CREATE INDEX idx_quiz_responses_attempt ON quiz_responses(quiz_attempt_id);


**Columns:**

*   `id` (BIGSERIAL, PRIMARY KEY): Unique identifier for the quiz response.
*   `quiz_attempt_id` (BIGINT, NOT NULL, FOREIGN KEY referencing `quiz_attempts.id`): ID of the quiz attempt.
*   `question_id` (BIGINT, NOT NULL, FOREIGN KEY referencing `questions.id`): ID of the question answered.
*   `user_option` (TEXT, NOT NULL): The option selected by the user.
*   `is_correct` (BOOLEAN, NOT NULL, DEFAULT false): Indicates if the user's option was correct.
*   `marks` (INTEGER): Marks awarded for this response.
*   `response_time` (TIMESTAMP, NOT NULL, DEFAULT CURRENT_TIMESTAMP): Timestamp when the response was recorded.

**Constraints:**

*   Foreign key constraints on `quiz_attempt_id` and `question_id` referencing `quiz_attempts` and `questions` tables respectively with `ON DELETE CASCADE` for `quiz_attempt_id`.
*   `marks` is checked to be non-negative.
*   Uniqueness constraint on `(quiz_attempt_id, question_id)` to ensure only one response per question per attempt.

---

## API Endpoints

All API endpoints are prefixed with `/api`.

### Authentication Endpoints (`/api/auth`)

1.  **Login** (`POST /api/auth/login`)
    *   **Request Body**:
        ```json
        {
          "username": "string",
          "password": "string"
        }
        ```
    *   **Success Response (200 OK)**:
        ```json
        {
          "token": "jwt_token_string",
          "id": integer,
          "username": "string",
          "email": "string",
          "role": "ADMIN" or "USER"
        }
        ```
    *   **Error Response (400 Bad Request)**:
        ```
        "Error message string"
        ```

### Quiz Endpoints (`/api/quizzes`)

1.  **Get All Quizzes** (`GET /api/quizzes`) - *Rate Limited*
    *   **Response Body (200 OK)**: Array of `QuizDTO` objects.
        ```json
        [
          {
            "id": integer,
            "title": "string",
            "description": "string",
            "totalScore": integer,
            "duration": integer,
            "createdByUsername": "string",
            "active": boolean,
            "createdAt": "timestamp",
            "updatedAt": "timestamp"
          },
          ...
        ]
        ```

2.  **Create Quiz** (`POST /api/quizzes`) - *Authenticated (ADMIN)*
    *   **Request Body**: `Quiz` object (without `id`, `createdBy`, `createdAt`, `updatedAt`).
        ```json
        {
          "title": "string",
          "description": "string",
          "totalScore": integer,
          "duration": integer,
          "active": boolean
        }
        ```
    *   **Success Response (200 OK)**: Created `Quiz` object.
        ```json
        {
          "id": integer,
          "title": "string",
          "description": "string",
          "totalScore": integer,
          "duration": integer,
          "createdBy": { /* User object */ },
          "quizQuestions": [],
          "attempts": [],
          "active": boolean,
          "createdAt": "timestamp",
          "updatedAt": "timestamp"
        }
        ```
    *   **Authentication**: Requires a valid JWT token for an ADMIN user.

3.  **Get Quiz Questions** (`GET /api/quizzes/{quizId}/questions`)
    *   **Path Parameter**: `quizId` (integer)
    *   **Response Body (200 OK)**: Array of `QuizQuestionDTO` objects.
        ```json
        [
          {
            "id": integer,
            "quizId": integer,
            "questionId": integer,
            "question": "string",
            "marks": integer,
            "questionOrder": integer,
            "createdAt": "timestamp"
          },
          ...
        ]
        ```
    *   **Error Response (404 Not Found)**: If quiz with `quizId` not found.

4.  **Add Questions to Quiz** (`POST /api/quizzes/{quizId}/questions`) - *Authenticated (ADMIN)*
    *   **Path Parameter**: `quizId` (integer)
    *   **Request Body**: Array of `QuizQuestion` objects (each should contain `question.id`, `marks`, `questionOrder`).
        ```json
        [
          {
            "question": {
              "id": integer
            },
            "marks": integer,
            "questionOrder": integer
          },
          ...
        ]
        ```
    *   **Success Response (200 OK)**: Array of created `QuizQuestionDTO` objects.
        ```json
        [
          {
            "id": integer,
            "quizId": integer,
            "questionId": integer,
            "question": "string",
            "marks": integer,
            "questionOrder": integer,
            "createdAt": "timestamp"
          },
          ...
        ]
        ```
    *   **Error Response (400 Bad Request)**: For invalid input (e.g., missing question ID, invalid marks, etc.).
    *   **Authentication**: Requires a valid JWT token for an ADMIN user.

5.  **Get Quiz Participants** (`GET /api/quizzes/{quizId}/participants`) - *Authenticated (ADMIN)*
    *   **Path Parameter**: `quizId` (integer)
    *   **Response Body (200 OK)**: Array of `QuizAttempt` objects.
        ```json
        [
          {
            "id": integer,
            "user": { /* User object */ },
            "quiz": { /* Quiz object */ },
            "status": "NOT_STARTED" or "IN_PROGRESS" or "COMPLETED" or "EXPIRED",
            "startTime": "timestamp",
            "endTime": "timestamp",
            "totalScore": integer,
            "obtainedScore": integer,
            "responses": [],
            "createdAt": "timestamp"
          },
          ...
        ]
        ```
     *   **Authentication**: Requires a valid JWT token for an ADMIN user.
    *   **Error Response (404 Not Found)**: If quiz with `quizId` not found.

6.  **Get Quiz Response for User** (`GET /api/quizzes/{quizId}/response/{userId}`) - *Authenticated (ADMIN)*
    *   **Path Parameters**: `quizId` (integer), `userId` (integer)
    *   **Response Body (200 OK)**: `QuizAttempt` object.
        ```json
        {
          "id": integer,
          "user": { /* User object */ },
          "quiz": { /* Quiz object */ },
          "status": "NOT_STARTED" or "IN_PROGRESS" or "COMPLETED" or "EXPIRED",
          "startTime": "timestamp",
          "endTime": "timestamp",
          "totalScore": integer,
          "obtainedScore": integer,
          "responses": [ /* Array of QuizResponse objects if available */ ],
          "createdAt": "timestamp"
        }
        ```
     *   **Authentication**: Requires a valid JWT token for an ADMIN user.
    *   **Error Response (404 Not Found)**: If quiz with `quizId`, user with `userId`, or attempt not found.

### Quiz Attempt Endpoints (`/api/attempts`)

1.  **Create Quiz Attempt** (`POST /api/attempts`) - *Authenticated (USER)*
    *   **Request Body**:
        ```json
        {
          "user": {
            "id": integer
          },
          "quiz": {
            "id": integer
          },
          "status": "IN_PROGRESS",
          "startTime": "timestamp",
          "totalScore": integer
        }
        ```
    *   **Success Response (200 OK)**: Created `QuizAttemptDTO` object.
        ```json
        {
          "id": integer,
          "status": "NOT_STARTED" or "IN_PROGRESS" or "COMPLETED" or "EXPIRED",
          "startTime": "timestamp",
          "endTime": "timestamp",
          "totalScore": integer,
          "obtainedScore": integer,
          "responses": []
        }
        ```
    *   **Authentication**: Requires a valid JWT token for a USER.

2.  **Get Attempts by User** (`GET /api/attempts/{userId}`) - *Authenticated (ADMIN or USER - for own attempts)*
    *   **Path Parameter**: `userId` (integer)
    *   **Response Body (200 OK)**: Array of `QuizAttempt` objects.
        ```json
        [
          {
            "id": integer,
            "user": { /* User object */ },
            "quiz": { /* Quiz object */ },
            "status": "NOT_STARTED" or "IN_PROGRESS" or "COMPLETED" or "EXPIRED",
            "startTime": "timestamp",
            "endTime": "timestamp",
            "totalScore": integer,
            "obtainedScore": integer,
            "responses": [],
            "createdAt": "timestamp"
          },
          ...
        ]
        ```
    *   **Authentication**: Requires a valid JWT token. ADMIN can access any user's attempts. USER can only access their own attempts.
    *   **Error Response (404 Not Found)**: If user with `userId` not found.

3.  **Submit Quiz Attempt** (`POST /api/attempts/{attemptId}/submit`) - *Authenticated (USER)*
    *   **Path Parameter**: `attemptId` (integer)
    *   **Request Body**:
        ```json
        {
          "responses": [
            {
              "questionId": integer,
              "userOption": "string"
            },
            ...
          ],
          "endTime": "timestamp"
        }
        ```
    *   **Success Response (200 OK)**: Updated `QuizAttempt` object.
        ```json
        {
          "id": integer,
          "user": { /* User object */ },
          "quiz": { /* Quiz object */ },
          "status": "NOT_STARTED" or "IN_PROGRESS" or "COMPLETED" or "EXPIRED",
          "startTime": "timestamp",
          "endTime": "timestamp",
          "totalScore": integer,
          "obtainedScore": integer,
          "responses": [ /* Array of QuizResponse objects with evaluation results */ ],
          "createdAt": "timestamp"
        }
        ```
    *   **Authentication**: Requires a valid JWT token for the USER who started the attempt.
    *   **Error Response (404 Not Found)**: If attempt with `attemptId` not found.
    *   **Error Response (400 Bad Request)**: If quiz attempt is already completed.

### Rate Limiting

The API is rate-limited to protect against abuse. The rate limit configuration is defined in `application.properties` or `application.yml`:


properties
rate.limit.requests=X # Maximum number of requests
rate.limit.duration=Y # Duration in seconds


When the rate limit is exceeded, the API will return a `429 Too Many Requests` response with the following headers and body:

*   **Status Code**: `429 Too Many Requests`
*   **Headers**:
    *   `Retry-After`: `<seconds>` - Suggests when to retry.
    *   `X-RateLimit-Limit`: Maximum allowed requests in the window.
    *   `X-RateLimit-Remaining`: Remaining requests in the current window.
*   **Response Body**:
    ```json
    {
      "error": "Rate limit exceeded. Try again in [Y] seconds."
    }
    ```

---



## Here is a screenshot for rate limiting response

when everything works great :

![image](https://github.com/user-attachments/assets/069b0bce-bfe0-45b8-bbaa-023bd9bac5db)

when limit exceed 
![image](https://github.com/user-attachments/assets/fa52ec5c-4c94-4bcb-b859-a736af563cf6)
