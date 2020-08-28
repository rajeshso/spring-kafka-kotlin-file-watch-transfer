# Spring Kafka Kotlin - File Watch and Transfer using Kafka

This project has consumer and producer examples of the simplest possible
spring kafka implementation using defaults 

External properties
-Dbootstrapservers=localhost\:9092 -Dschemaregistryurl=http://localhost:8081 -Dagentid=1 -Dtopic=simple-message-topic -Dfilepath=/Users/rajesh/temp

./gradlew clean generateAvroJava build

Note: If there is an error in build, delete the .idea, ./gradlew clean , idea, generateAvroJava and build

bin/kafka-avro-console-consumer --topic simple-message-topic \
                 --bootstrap-server localhost:9092 \
                 --from-beginning
                 
courtesy : https://github.com/vishna/watchservice-ktx