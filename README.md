To build the application: `mvn clean package`

To run the application: `java -jar target/paymentTracker-1.0-SNAPSHOT-jar-with-dependencies.jar [OPTIONS]`

To get help: `java -jar target/paymentTracker-1.0-SNAPSHOT-jar-with-dependencies.jar -h`

To run from source code: `mvn clean compile exec:java -Dexec.mainClass=net.bytemix.PaymentTracker -Dexec.args="[OPTIONS]"`
