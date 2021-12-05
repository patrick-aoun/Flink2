package ds_project;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.aggregation.Aggregations;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.rabbitmq.*;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class WikiWordCount {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        final HttpRequestFunction httpRequestFunction = new HttpRequestFunction("https://en.wikipedia.org/w/api.php?format=json&action=query&prop=extracts&exintro&explaintext&redirects=1&titles=",true);
        final StringDeserializationScheme stringDeserializationScheme = new StringDeserializationScheme();

        final RMQConnectionConfig connectionConfig = new RMQConnectionConfig.Builder()
                .setHost("localhost")
                .setPort(5672)
                .setUserName("guest")
                .setPassword("guest")
                .setVirtualHost("/")
                .build();
                
        final DataStream<String> stream = env
                .addSource(new RMQSource<String>(
                        connectionConfig,
                        "wikiWordCount",
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

        DataStream<Tuple2<String, Integer>> wordCountStream = resultStream
                .flatMap(new LineSplitter())
                .keyBy(new KeySelector<Tuple2<String, Integer>, Object>() {
                    @Override
                    public Object getKey(Tuple2<String, Integer> value) throws Exception {
                        return value.f0;
                    }
                })
                .window(TumblingProcessingTimeWindows.of(Time.seconds(5)))
                .sum(1);


        // emit result
        wordCountStream.print();

        stream.print();
        env.execute("Wikipedia Word Count");
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
