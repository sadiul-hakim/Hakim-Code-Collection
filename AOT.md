# Make sure java 24+ is installed

## Java AOT
------------
java -Djarmode=tools -jar target/ExpanseTrackerApi-1.0.0.jar extract
java -XX:AOTMode=record -XX:AOTConfiguration=app.aotconf -cp ExpanseTrackerApi-1.0.0.jar xyz.sadiulhakim.Application
java -XX:AOTMode=create -XX:AOTConfiguration=app.aotconf -XX:AOTCache=app.aot -cp ExpanseTrackerApi-1.0.0.jar
java -XX:AOTCache=app.aot -cp ExpanseTrackerApi-1.0.0.jar xyz.sadiulhakim.Application
