# Apache-Kafka-Client-JAVA-Example
./zookeeper-server-start.sh ../config/zookeeper.properties
./kafka-server-start.sh ../config/server.properties 

### Requirement
- maven
- java 1.8

### To build the jar file
```
mvn clean package
```
### To run the program 
```
java -jar kafka-client-example-1.0-SNAPSHOT.jar 0.0.0.0:2181

```

