# Passwd as a Service

A minimal HTTP service that exposes the user and group information on a UNIX-like system that is usually locked away in the UNIX /etc/passwd and /etc/groups files.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

Java 1.8+ is required to build and start this service.
I recommend using openjdk 1.8.0_181 as that is the version I used.

Maven is required to build the service.
I recommend using maven 3.5.2 as that is the version I used.

### Building
The project will build using the standard maven lifecycle. The resulting executable jar will be available in the target/ directory.
Please ensure that the build node either has access to maven central or a maven repository with all the dependencies noted in the pom.xml file.

```
mvn clean install
```

### Running the Service

Starting the service with default configuration.
Please note that this will start the service on port 8080 reading /etc/passwd for user information and /etc/group for group information.
Please ensure the running user has read permissions on these files.

```
java -jar <path to passwd-service-<version>.jar>
java -jar target/passwd-service-1.0.0.jar
```

Starting the service with custom configuration
```
java -jar <path to passwd-service-<version>.jar> --server.port=8090 --passwd.users.filepath=<path to user information file> --passwd.groups.filepath=<path to group information file>
java -jar target/passwd-service-1.0.0.jar --server.port=8090 --passwd.users.filepath=/home/adonofero/testPasswd --passwd.groups.filepath=/home/adonofero/testGroup
```

### Hitting the service
The service will be available to service requests on either the default port (8080) or the configured server.port.
Please see the API documentation for a comprehensive list of endpoints and expected responses.

```
curl http://localhost:<server port>/<desired endpoint>
curl http://localhost:8080/users
```