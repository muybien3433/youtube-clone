# YouTube Backend Clone

A clone of YouTube's backend functionality, designed to simulate key features of a video-sharing platform. 
This project implements essential backend services such as video upload, user authentication, video streaming 
and data storage using modern web technologies. 
It also includes features for managing user subscriptions, video comments, notifications.

## Features

- **User Authentication**: Sign up, log in, and log out securely.
- **Video Management**: Upload/delete videos.
- **Comments**: Add comments on videos pages.
- **Watch History**: Track and display a user’s watched video history.
- **Subscriptions**: Subscribe to other users to follow their content.
- **Notifications**: Notify subscribers when a user uploads a new video.

## Tech Stack

- **Backend**: Java 22, SpringBoot 6
- **Database**: MySQL (Relational Database)
- **Cloud Storage**: AWS S3 (v2) for video and thumbnail storage
- **Authentication**: JWT (JSON Web Tokens) for session management
- **Testing**: JUnit 5 for unit tests
- **Build Tool**: Maven for dependency management

## Prerequisites

Ensure you have the following installed before running the project:

- **Java**: 22 or higher
- **MySQL**: Installed and running locally or on a server
- **AWS Account**: S3 storage bucket set up for files storage
- **Maven**: For building and running the project

## API Endpoints

**API will be available at localhost:8080/api/v1**

- *Authentication*: `/auth`
   - `POST /register` – Create a new user.
   - `POST /login` – Log in a user and return a JWT token.
   - `POST /logout` – Log out the current user (JWT invalidation).

- *Videos*: `/videos`
   - `GET /` – Fetch all videos.
   - `GET /{videoId}` – Fetch a video (adds to watch history when authenticated).
   - `POST /upload` – Upload video.
   - `POST /{videoId}/like` - Like video.
   - `POST /{videoId}/dislike` - Dislike video.
   - `POST /{videoId}/comment` - Comment video.
   - `DELETE /{videoId}/delete` – Delete video.

- *User*: `/users`
    - `GET /history` – Display watched history.
    - `POST /toggle/subscribe/{targetUserId}` – Toggle subscription.

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/muybien3433/youtube-clone.git
   ```
2. Set up environment variables: 
   ```bash
   AWS_ACCESS_KEY_ID=your-aws-access-key
   AWS_BUCKET_NAME=your-aws-bucket-name
   AWS_DEFAULT_REGION=your-aws-region
   AWS_SECRET_ACCESS_KEY=your-aws-secret-access-key
   ```
3. Open application.yml and provide following:
   ```bash
   datasource:
    url: "jdbc:mysql://your-db-url/your-schema-name"
    username: "your-db-username"
    password: "your-db-password"
   jwt:
    secret-key: "your-jwt-secret-key"
   ```
4. Reload maven project
   ```bash
   mvn clean install
   ```
5. Run tests
   ```bash
   mvn test
   ```
6. Run the application
   ```bash
   mvn spring-boot:run
   ```

## Contributing
Submit issues and pull requests are more than welcome.

## License
This project is licensed under the MIT License. See the LICENSE file for details.


