package com.myproject;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public class ProducerKafka {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		// https://kafka.apache.org/27/documentation.html#producerconfigs

		String bootserver_url = "192.168.1.25:9092";
		String topic = "myTestTopic";
		int messageCount =10;
		// Set kafka properties
		Properties properties = new Properties();
		properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootserver_url);
		properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

		// create Kakfa Producer
		KafkaProducer<String, String> kafkaProducer = new KafkaProducer<String, String>(properties);

		// create Kafka ProducerRecord
		for (int i = 0; i < messageCount; i++) {
			ProducerRecord<String, String> producerRecord = new ProducerRecord<String, String>(topic, "message: " + i);

			// Send kafka Message
			kafkaProducer.send(producerRecord, new Callback() {
				public void onCompletion(RecordMetadata recordMetadata, Exception e) {
					if (e == null) {
						System.out.println("topic: " + recordMetadata.topic() + "\n partition: "
								+ recordMetadata.partition() + "\n offset: " + recordMetadata.offset()
								+ "\n timestamp: " + recordMetadata.timestamp());

					} else {
						e.printStackTrace();
					}
				}
			});
		}
		// flush and close kafkaProducer
		kafkaProducer.flush();
		kafkaProducer.close();
	}

}
