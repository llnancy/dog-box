package com.sunchaser.dog.box;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GitHubBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

/**
 * dog-box
 *
 * @author sunchaser admin@lilu.org.cn
 * @since JDK8 2022/7/27
 */
public class DogBox {

    private static final String[] WEEK = {"一", "二", "三", "四", "五", "六", "日"};

    public static void main(String[] args) throws Exception {
        String ghToken = System.getenv("GH_TOKEN");
        String gistId = System.getenv("DOG_GIST_ID");
        String dogWidth = Optional.ofNullable(System.getenv("DOG_WIDTH")).orElse("65");
        String dogFillLen = Optional.ofNullable(System.getenv("DOG_FILL_LEN")).orElse("106");
        System.out.println("ghToken=" + ghToken);
        System.out.println("gistId=" + gistId);
        GHGist gist = getGhGist(ghToken, gistId);
        String fileName = "🐶 舔狗日记";
        String data = getDogContent();
        String format = getFormatContext(data, Integer.parseInt(dogWidth), Integer.parseInt(dogFillLen));
        writeToGist(gist, fileName, format);
        writeToMarkdown(gistId, fileName, format);
    }

    private static void writeToMarkdown(String gistId, String title, String format) {
        String markdownFile = System.getenv("MARKDOWN_FILE");
        String startTag = Optional.ofNullable(System.getenv("DOG_BOX_START_TAG")).orElse("<!-- dog-box start -->");
        String endTag = Optional.ofNullable(System.getenv("DOG_BOX_END_TAG")).orElse("<!-- dog-box end -->");
        if (Objects.nonNull(markdownFile)) {
            String old = FileUtil.readUtf8String(markdownFile);
            int startIndex = old.indexOf(startTag) + startTag.length();
            int endIndex = old.indexOf(endTag);
            String markdownTitle = StrUtil.format("\n#### <a href=\"https://gist.github.com/{}\" target=\"_blank\">{}</a>\n", gistId, title);
            String markdown = old.substring(0, startIndex) + markdownTitle + "```text\n" + format + "\n```\n" + old.substring(endIndex);
            FileUtil.writeUtf8String(markdown, markdownFile);
        }
    }

    private static void writeToGist(GHGist gist, String fileName, String format) throws IOException {
        gist.update()
                .updateFile(fileName, format)
                .update();
    }

    private static String getFormatContext(String data, Integer width, Integer fillLen) {
        LocalDate now = LocalDate.now();
        String[] chunks = data.split("(?<=\\G.{" + width + "})");
        StringJoiner joiner = new StringJoiner("\n");
        for (String chunk : chunks) {
            joiner.add(chunk);
        }
        String date = StrUtil.fillBefore(StrUtil.format("{} 星期{}", now, WEEK[now.getDayOfWeek().getValue() - 1]), ' ', fillLen);
        return StrUtil.format("{}\n\n{}", joiner, date);
    }

    private static GHGist getGhGist(String ghToken, String gistId) throws IOException {
        return new GitHubBuilder()
                .withOAuthToken(ghToken)
                .build()
                .getGist(gistId);
    }

    private static String getDogContent() throws Exception {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder(URI.create("https://api.lilu.org.cn/shushan/huaying/today/tgrj"))
                .GET()
                .build();
        HttpResponse.BodyHandler<String> bodyHandler = HttpResponse.BodyHandlers.ofString();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, bodyHandler);
        String body = httpResponse.body();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        return (String) jsonObject.get("data");
    }
}
