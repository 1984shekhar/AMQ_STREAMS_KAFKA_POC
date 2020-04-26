package kafka;


import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
public class ConsumerCreator {

    public static Consumer<Long, String> createConsumer() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, IKafkaConstants.KAFKA_BROKERS);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, IKafkaConstants.GROUP_ID_CONFIG);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, IKafkaConstants.MAX_POLL_RECORDS);
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, IKafkaConstants.OFFSET_RESET_EARLIER);
        properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,"pA3F9qkI5aF0");
        properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,"/home/chandrashekhar/Development/Streams_RH/strimzi-2-kubectl/ca.p12");
       
        // Following line should be commented when authentication type is set to scram-sha-512 and not tls
        properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        
        
      //These are required when authentication type is set to scram-sha-512 for user
       // properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");     
       // properties.put(SaslConfigs.SASL_MECHANISM,"SCRAM-SHA-512");
       // properties.put(SaslConfigs.SASL_JAAS_CONFIG,"org.apache.kafka.common.security.scram.ScramLoginModule required username=\"my-user\" password=\"gMBqy2GEV0kZ\";");
       
        KafkaConsumer<Long, String> consumer = new KafkaConsumer<Long, String>(properties);
        consumer.subscribe(Collections.singletonList(IKafkaConstants.TOPIC_NAME));
        return consumer;
    }
}
