# Echoloop

Echoloop is a web-based social platform that facilitates community engagement and event management. The application provides features for user interaction, event organization, and real-time messaging.

## Features

- User Authentication and Authorization
- Real-time Messaging System
- Event Creation and Management
- Community Feed with Posts and Comments
- User Profile Management
- Notification System
- Search Functionality
- Responsive UI Design

## Technology Stack

- **Backend**: Java Spring Boot
- **Frontend**: HTML, CSS, JavaScript
- **Database**: JPA/Hibernate
- **Real-time Communication**: WebSocket
- **Build Tool**: Maven

## Prerequisites

- Java JDK 11 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

## Setup and Installation

1. Clone the repository:
```bash
git clone https://github.com/yourusername/echoloop.git
cd echoloop
```

2. Configure the database:
   - Create a MySQL database
   - Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties`
   - Update the database configuration in `application.properties`

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`

## Project Structure

- `src/main/java/com/echoloop/` - Contains the Java source files
  - `config/` - Configuration classes
  - `controller/` - REST API controllers
  - `model/` - Entity classes
  - `repository/` - Data access layer
  - `service/` - Business logic layer
  - `dto/` - Data Transfer Objects
- `src/main/resources/` - Contains application resources
  - `static/` - Frontend files (HTML, CSS, JS)
  - `db/` - Database migration scripts

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 