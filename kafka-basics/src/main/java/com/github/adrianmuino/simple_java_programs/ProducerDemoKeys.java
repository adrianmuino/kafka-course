package com.github.adrianmuino.simple_java_programs;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProducerDemoKeys 
{
    public static void main( String[] args ) throws InterruptedException, ExecutionException
    {
        final Logger logger = LoggerFactory.getLogger(ProducerDemoKeys.class);
        String bootStrapServers = "ubuntu-vm:9092";

        // create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.PARTITIONER_CLASS_CONFIG, RoundRobinPartitioner.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for(int i=0; i<10; i++){
            String topic = "second_topic";
            String value = "hello world" + Integer.toString(i);
            String key = "id_" + Integer.toString(i); // send with key will always hit same partition

            logger.info("Key: " + key); // asynch - keys logged without waiting on request

            // create a producer record
            ProducerRecord<String, String> record = 
                new ProducerRecord<String,String>(topic, key, value);

            // send data with callback - synchronous b/c the .get() blocks the .send()
            producer.send(record, new Callback() {
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (e == null) {
                        logger.info("Received new metadata.\n" +
                                    "Topic: " + recordMetadata.topic() + "\n" +
                                    "Partition: " + recordMetadata.partition() + "\n" +
                                    "Offset: " + recordMetadata.offset() + "\n" +
                                    "Timestamp: " + recordMetadata.timestamp());      
                    } else {
                        logger.error("Error while producing.\n", e);
                    }
                }
            }).get(); // needed so that keys are logged synchronously - don't do this is production
        }

        // flush buffered data
        producer.flush();

        // flush and close
        producer.close();
    }
}
