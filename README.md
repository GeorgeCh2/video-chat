# WebRTC Video Chat Demo
A simple WebRTC video chat demo using Spring Boot, Spring Web, and Spring WebSockets.

![SCR-20231110-sgop](https://github.com/GeorgeCh2/video-chat/assets/22410736/68c9b4bf-ea7c-413a-91c3-36160d2256ae)

# Running the Demo
1. Clone the repository
2. Run `mvn clean install` to build the project
3. Run `mvn spring-boot:run` to start the application
4. Open a second browser window and navigate to `http://localhost:8080/index.html`

# How it Works
The application uses Spring WebSockets to establish a two-way communication channel between the client and server. The client sends an offer to the server, which then sends an answer back to the client. Once the offer and answer have been exchanged, the client and server can begin sending video data to each other using WebRTC.

