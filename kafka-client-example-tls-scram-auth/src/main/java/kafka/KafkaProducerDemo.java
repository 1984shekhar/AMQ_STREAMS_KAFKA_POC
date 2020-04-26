package kafka;

public class KafkaProducerDemo {
    public static final String TOPIC = "cs-topic";
    
    public static void main(String[] args) {
        boolean isAsync = true;
        SampleProducer producerThread = new SampleProducer(TOPIC, isAsync);
        // start the producer
        producerThread.start();
 
    }
}
