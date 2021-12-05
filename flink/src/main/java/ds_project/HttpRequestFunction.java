package ds_project;

import com.squareup.okhttp.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
public class HttpRequestFunction extends RichAsyncFunction<String, String> {
    String url;
    Boolean wordCount = false;
    public HttpRequestFunction(String url, Boolean wordCount){
        this.url = url;
        this.wordCount = wordCount;
        return;
    }
    private transient OkHttpClient client;

    @Override
    public void open(Configuration parameters) {
        client = new OkHttpClient();
    }

    @Override
    public void asyncInvoke(String input, final ResultFuture<String> resultFuture) {
        System.out.println("sent request " + this.url + input);
        Request request = new Request.Builder()
                .get()
                .url(this.url + input)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                resultFuture.complete(Collections.<String>emptyList());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if(!wordCount) {
                    resultFuture.complete(Collections.singleton(response.body().string()));
                    return;
                }
                String jsonData = response.body().string();
                JSONObject Jobject = new JSONObject(jsonData);
                JSONObject querry = (JSONObject) Jobject.get("query");
                JSONObject pages = (JSONObject) querry.get("pages");
                resultFuture.complete(Collections.singleton(pages.toString()));

            }
        });
    }

}