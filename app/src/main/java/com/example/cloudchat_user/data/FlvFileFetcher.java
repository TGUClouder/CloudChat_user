package com.example.cloudchat_user.data;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlvFileFetcher {

    private OkHttpClient client = new OkHttpClient();

    public void fetchFlvFileNames(String url, final FileNameCallback callback) {
        Request request = new Request.Builder()
                .url(url)  // 例如 "http://yourserver/directory/"
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    callback.onError(new IOException("Unexpected code " + response));
                    return;
                }

                String html = response.body().string();
                List<String> fileNames = parseFlvFileNames(html);
                callback.onSuccess(fileNames);
            }
        });
    }

    private List<String> parseFlvFileNames(String html) {
        List<String> fileNames = new ArrayList<>();
        // 使用 Jsoup 解析 HTML 文档
        Document document = Jsoup.parse(html);
        // 选择所有 href 属性以 .flv 结尾的 <a> 标签
        Elements links = document.select("a[href$=.flv]");

        // 遍历每个标签，提取文件名（这里可以用 href 或者标签内容，根据实际页面）
        for (Element link : links) {
            String href = link.attr("href");  // 如 "stream.1744284825294.flv"
            // 如果页面返回的是相对路径，你可以进一步处理，例如去掉路径部分只保留文件名
            // 例如：
            int lastSlash = href.lastIndexOf('/');
            String fileName = (lastSlash != -1) ? href.substring(lastSlash + 1) : href;
            fileNames.add(fileName);
        }
        return fileNames;
    }

    // 一个简单的回调接口，用于返回结果
    public interface FileNameCallback {
        void onSuccess(List<String> fileNames);
        void onError(Exception e);
    }
}
