package kafka;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
 

public class SampleProducer extends Thread {
    private final KafkaProducer<Long, String> producer;
    private final String topic;
    private final Boolean isAsync;
 
    public static final String KAFKA_SERVER_URL = "10.98.82.229";
    public static final int KAFKA_SERVER_PORT = 9094;
    public static final String CLIENT_ID = "SampleProducer";
 
    public SampleProducer(String topic, Boolean isAsync) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER_URL + ":" + KAFKA_SERVER_PORT);
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,"pA3F9qkI5aF0");
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,"/home/chandrashekhar/Development/Streams_RH/strimzi-2-kubectl/ca.p12");
        
        // Line Number 38 should be commented when authentication type is set to scram-sha-512 and not tls
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        
        //These are required when authentication type is set to scram-sha-512 for user. Comment Line number 38 and uncomment 41,42 and 43 line.
        //properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");     
        //properties.put(SaslConfigs.SASL_MECHANISM,"SCRAM-SHA-512");
        //properties.put(SaslConfigs.SASL_JAAS_CONFIG,"org.apache.kafka.common.security.scram.ScramLoginModule required username=\"my-user\" password=\"gMBqy2GEV0kZ\";");
        producer = new KafkaProducer<Long, String>(properties);
        this.topic = topic;
        this.isAsync = isAsync;
    }
 
    public void run() {
        long messageNo = 1;
        while (true) {
            String messageStr = "Message_" + messageNo;
            long startTime = System.currentTimeMillis();
            if (isAsync) { // Send asynchronously
                producer.send(new ProducerRecord<>(topic,
                        messageNo,
                        messageStr), new DemoCallBack(startTime, messageNo, messageStr));
            } else { // Send synchronously
                try {
                	RecordMetadata metadata = producer.send(new ProducerRecord<>(topic,
                            messageNo,
                            messageStr)).get();
                    System.out.println("Sent message: (" + messageNo + ", " + messageStr + ")");
                    System.out.println("Record sent with key " + messageNo + " to partition " + metadata.partition()
                    + " with offset " + metadata.offset());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    // handle the exception
                }	
            }
            ++messageNo;
        }
    }
}
 
class DemoCallBack implements Callback {
 
    private final long startTime;
    private final long key;
    private final String message;
 
    public DemoCallBack(long startTime, long key, String message) {
        this.startTime = startTime;
        this.key = key;
        this.message = message;
    }
 
    /**
     * onCompletion method will be called when the record sent to the Kafka Server has been acknowledged.
     *
     * @param metadata  The metadata contains the partition and offset of the record. Null if an error occurred.
     * @param exception The exception thrown during processing of this record. Null if no error occurred.
     */
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        if (metadata != null) {
            System.out.println(
                    "message(" + key + ", " + message + ") sent to partition(" + metadata.partition() +
                    "), " +
                    "offset(" + metadata.offset() + ") in " + elapsedTime + " ms");
        } else {
            exception.printStackTrace();
        }
    }
}
