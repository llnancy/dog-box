package io.github.llnancy.dog.box;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.github.llnancy.dog.box.EnvEnum.*;

/**
 * dog-box
 *
 * @author sunchaser admin@lilu.org.cn
 * @since JDK8 2022/7/27
 */
public class DogBox {

    private static final Logger LOGGER = LoggerFactory.getLogger(DogBox.class);

    private static final String DOG_BOX = "🐶 舔狗日记";

    enum WeekEnum {

        ONE("一"),

        TWO("二"),

        THREE("三"),

        FORE("四"),

        FIVE("五"),

        SIX("六"),

        SEVEN("日"),

        ;

        private final String week;

        private static final Map<Integer, WeekEnum> ORDINAL_MAP;

        static {
           ORDINAL_MAP = Arrays.stream(WeekEnum.values())
                   .collect(Collectors.toMap(WeekEnum::ordinal, Function.identity()));
        }

        public static String toChinese(Integer dayOfWeek) {
            return "星期" + ORDINAL_MAP.get(dayOfWeek - 1).getWeek();
        }

        WeekEnum(String week) {
            this.week = week;
        }

        public String getWeek() {
            return week;
        }
    }

    enum EnvEnum {
        GITHUB_TOKEN(StringUtils.EMPTY),

        DOG_GIST_ID("e85d2e5765110be1d8cfe57f2557a130"),

        DOG_WIDTH("65"),

        DOG_FILL_LEN("106"),

        MARKDOWN_FILE("README.md"),

        DOG_BOX_START_TAG("<!-- dog-box start -->"),

        DOG_BOX_END_TAG("<!-- dog-box end -->"),

        TIMEZONE("Asia/Shanghai"),

        ;
        private final String defaultValue;

        EnvEnum(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        public String getDefaultValue() {
            return defaultValue;
        }
    }

    public static void main(String[] args) throws Exception {
        String githubToken = getenv(GITHUB_TOKEN);
        String gistId = getenv(DOG_GIST_ID);
        String dogWidth = getenv(DOG_WIDTH);
        String dogFillLen = getenv(DOG_FILL_LEN);
        LOGGER.info("githubToken: {}, gistId: {}, dogWidth: {}, dogFillLen: {}", githubToken, gistId, dogWidth, dogFillLen);
        GHGist gist = getGhGist(githubToken, gistId);
        String data = getDogContent();
        String format = getFormatContext(data, Integer.parseInt(dogWidth), Integer.parseInt(dogFillLen));
        writeToGist(gist, format);
        writeToMarkdown(gistId, format);
    }

    private static String getenv(EnvEnum envEnum) {
        return Optional.ofNullable(System.getenv(envEnum.name())).orElse(envEnum.getDefaultValue());
    }

    private static void writeToMarkdown(String gistId, String format) {
        String markdownFile = getenv(MARKDOWN_FILE);
        String startTag = getenv(DOG_BOX_START_TAG);
        String endTag = getenv(DOG_BOX_END_TAG);
        LOGGER.info("markdownFile: {}, startTag: {}, endTag: {}", markdownFile, startTag, endTag);
        if (Objects.nonNull(markdownFile)) {
            String old = FileUtil.readUtf8String(markdownFile);
            int startIndex = old.indexOf(startTag) + startTag.length();
            int endIndex = old.indexOf(endTag);
            String markdownTitle = StrUtil.format("\n#### <a href=\"https://gist.github.com/{}\" target=\"_blank\">{}</a>\n", gistId, DOG_BOX);
            String markdown = old.substring(0, startIndex) + markdownTitle + "```text\n" + format + "\n```\n" + old.substring(endIndex);
            FileUtil.writeUtf8String(markdown, markdownFile);
        }
    }

    private static void writeToGist(GHGist gist, String format) throws IOException {
        gist.update()
                .updateFile(DOG_BOX, format)
                .update();
    }

    private static String getFormatContext(String data, Integer width, Integer fillLen) {
        String timeZone = getenv(TIMEZONE);
        LOGGER.info("timeZone: {}", timeZone);
        LocalDate now = LocalDate.now(ZoneId.of(timeZone));
        String[] chunks = data.split("(?<=\\G.{" + width + "})");
        StringJoiner joiner = new StringJoiner("\n");
        for (String chunk : chunks) {
            joiner.add(chunk);
        }
        String date = StrUtil.fillBefore(StrUtil.format("{} {}", now, WeekEnum.toChinese(now.getDayOfWeek().getValue())), CharPool.SPACE, fillLen);
        return StrUtil.format("{}\n\n{}", joiner, date);
    }

    private static GHGist getGhGist(String githubToken, String gistId) throws IOException {
        return new GitHubBuilder()
                .withOAuthToken(githubToken)
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
