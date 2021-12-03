package ds_project;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.rabbitmq.*;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;
import org.apache.flink.api.common.serialization.SerializationSchema;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class WikiSearch {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final HttpRequestFunction httpRequestFunction = new HttpRequestFunction();
        final RQMSerializationSchema rqmSerializationSchema = new RQMSerializationSchema();
        final StringDeserializationScheme stringDeserializationScheme = new StringDeserializationScheme();

        final RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
                .setHost("localhost")
                .setPort(5672)
                .setUserName("guest")
                .setPassword("guest")
                .setVirtualHost("/")
                .build();
        final RMQSinkPublishOptions options = new RMQSinkPublishOptions() {
            @Override
            public String computeRoutingKey(Object o) {
                return "resWiki";
            }

            @Override
            public AMQP.BasicProperties computeProperties(Object o) {
                return null;
            }

            @Override
            public String computeExchange(Object o) {
                return "";
            }
        };
        final DataStream<String> stream = env
                .addSource(new RMQSource<String>(
                        connectionConfig,
                        "searchWiki",
                        stringDeserializationScheme))
                .setParallelism(1);


        DataStream<String> resultStream = AsyncDataStream.orderedWait(
                // Original stream
                stream,
                // The function
                httpRequestFunction,
                // Tiemout length
                5,
                // Timeout unit
                TimeUnit.SECONDS
        );

        RMQSink rmqSink = new RMQSink<String>(
                connectionConfig,
                "resWiki",
                new RQMSerializationSchema()
        );

        resultStream.addSink(rmqSink);

        stream.print();
        env.execute("Wikipedia Search");
    }

    private static class RQMSerializationSchema  implements SerializationSchema<String> {
        @Override
        public byte[] serialize(String element) {
            return element.getBytes(StandardCharsets.UTF_8);
        }
    }
    private static class RQMSerializableReturnListener implements SerializableReturnListener {
        @Override
        public void handleReturn(int i, String s, String s1, String s2, AMQP.BasicProperties basicProperties, byte[] bytes) throws IOException {

        }
    }
    private static class StringDeserializationScheme implements RMQDeserializationSchema<String> {
        @Override
        public TypeInformation<String> getProducedType() {
            return TypeExtractor.getForClass(String.class);
        }

        @Override
        public void deserialize(
                Envelope envelope,
                AMQP.BasicProperties properties,
                byte[] body,
                RMQCollector<String> collector) throws IOException {
            String s = new String(body, StandardCharsets.UTF_8);
            if(s.isEmpty()) return;
            collector.collect(s);
        }

        @Override
        public boolean isEndOfStream(String record) {
            return record.equals("FINISH");
        }
    }
}
