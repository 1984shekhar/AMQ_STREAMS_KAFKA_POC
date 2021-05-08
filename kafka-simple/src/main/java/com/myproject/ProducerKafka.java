package com.myproject;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public class ProducerKafka {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
	    // https://kafka.apache.org/27/documentation.html#producerconfigs
		
		String bootserver_url = "127.0.0.1:9092";
		String topic = "myTopic";
		// Set kafka properties
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootserver_url);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
	    
		//create Kakfa Producer
		KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);
		
		//create Kafka ProducerRecord
		ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(topic,"hello_world");
		
		// Send kafka Message 
		Future<RecordMetadata> metadata = kafkaProducer.send(producerRecord);
		System.out.println("message sent with metadata: partiton: "+metadata.get().partition()+" offset: "+metadata.get().offset());
		// flush and close kafkaProducer
		kafkaProducer.flush();
		kafkaProducer.close();
	}

}
