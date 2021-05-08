package com.myproject;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.admin.OffsetSpec.EarliestSpec;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class ConsumerKafka {

	public static void main(String[] args) {
		// https://kafka.apache.org/27/documentation.html#consumerconfigs
		
		String bootserver_url = "127.0.0.1:9092";
		String groupId = "kafka-consumer-group-1";
		String topic = "myTopic";
		// Set kafka consumer properties
		Properties properties = new Properties();
		properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootserver_url);
		properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
		properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
	    properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
	
	    // Create kafka Consumer
	    KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(properties);
	    
	    // subscriber to a topic
	    kafkaConsumer.subscribe(Arrays.asList(topic));
	    //kafkaConsumer.subscribe(Arrays.asList("topic1","topic2"));
	    
	    //poll for data
	    while(true) {
	    	ConsumerRecords<String, String> consumerRecords = kafkaConsumer.poll(Duration.ofMillis(100));
	    	
	    	for(ConsumerRecord<String, String> record : consumerRecords)
	    	{
	    		System.out.println("key: "+record.key()+" value: "+record.value());
	    		System.out.println("offset: "+record.offset()+" partition: "+record.partition());
	    	}
	    }
	    
	}

}
