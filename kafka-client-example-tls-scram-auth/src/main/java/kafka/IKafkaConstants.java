package kafka;


public interface IKafkaConstants {
    public static final String KAFKA_SERVER_URL = "10.98.82.229";
    public static final int KAFKA_SERVER_PORT = 9094;
    public static String KAFKA_BROKERS = KAFKA_SERVER_URL + ":" + KAFKA_SERVER_PORT;
    public static Integer MESSAGE_COUNT=1000;
    public static String CLIENT_ID="client1";
    public static String TOPIC_NAME="cs-topic";
    public static String GROUP_ID_CONFIG="my-group";
    public static Integer MAX_NO_MESSAGE_FOUND_COUNT=100;
    public static String OFFSET_RESET_LATEST="latest";
    public static String OFFSET_RESET_EARLIER="earliest";
    public static Integer MAX_POLL_RECORDS=1;
}