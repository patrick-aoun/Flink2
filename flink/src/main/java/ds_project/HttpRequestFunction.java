package ds_project;

import com.squareup.okhttp.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.io.IOException;
import java.util.Collections;
public class HttpRequestFunction extends RichAsyncFunction<String, String> {

    private transient OkHttpClient client;

    @Override
    public void open(Configuration parameters) {
        client = new OkHttpClient();
    }

    @Override
    public void asyncInvoke(String input, final ResultFuture<String> resultFuture) {
        Request request = new Request.Builder()
                .get()
                .url("https://en.wikipedia.org/w/api.php?action=query&list=search&prop=info&inprop=url&utf8=&format=json&origin=*&srlimit=20&srsearch="
                        + input)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                resultFuture.complete(Collections.<String>emptyList());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                resultFuture.complete(Collections.singleton(response.body().string()));
            }
        });
    }

}