package com.linkvault.linkvaultserver.component.metadata;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class UrlMetadataFetcher {

    private static final String DESKTOP_UA =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";
    private static final String MOBILE_WECHAT_UA =
            "Mozilla/5.0 (iPhone; CPU iPhone OS 16_0 like Mac OS X) "
                    + "AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 "
                    + "MicroMessenger/8.0.40";

    private static final Pattern BILIBILI_BV = Pattern.compile("(BV[0-9A-Za-z]{10})");
    private static final Pattern YOUTUBE_VIDEO_ID =
            Pattern.compile("(?:v=|youtu\\.be/|embed/)([0-9A-Za-z_-]{11})");
    private static final Pattern X_STATUS =
            Pattern.compile("^/(?:i/web/)?([^/]+)/status/(\\d+)");
    private static final int X_TITLE_MAX_CHARS = 40;

    private final HttpClient client = buildHttpClient();
    private final String youtubeApiKey; // YouTube Data API Key，开发配置可为空

    public UrlMetadataFetcher(@Value("${app.metadata.youtube-api-key:}") String youtubeApiKey) {
        this.youtubeApiKey = youtubeApiKey;
    }

    private HttpClient buildHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .version(HttpClient.Version.HTTP_1_1);

        resolveProxyUri().ifPresent(proxyUri -> {
            if (proxyUri.getHost() != null && proxyUri.getPort() > 0) {
                builder.proxy(ProxySelector.of(new InetSocketAddress(proxyUri.getHost(), proxyUri.getPort())));
            }
        });
        return builder.build();
    }

    private Optional<URI> resolveProxyUri() {
        String[] proxyValues = new String[]{
                System.getenv("HTTPS_PROXY"),
                System.getenv("https_proxy"),
                System.getenv("HTTP_PROXY"),
                System.getenv("http_proxy")
        };
        for (String value : proxyValues) {
            if (value != null && !value.isBlank()) {
                try {
                    return Optional.of(URI.create(value));
                } catch (IllegalArgumentException e) {
                    log.info("外网代理配置无法解析，proxy={}", value);
                    return Optional.empty();
                }
            }
        }
        return Optional.empty();
    }

    public UrlMetadataResult fetch(String originalUrl) {
        try {
            URI originalUri = URI.create(originalUrl);
            String originalHost = Optional.ofNullable(originalUri.getHost()).orElse("").toLowerCase();

            if (originalHost.contains("youtu.be") || originalHost.contains("youtube.com")) {
                return extractYouTube(originalUrl, originalUri);
            }
            if (originalHost.contains("x.com") || originalHost.contains("twitter.com")) {
                return extractX(originalUrl, originalUri);
            }
            if (originalHost.contains("mp.weixin.qq.com")) {
                return extractWeChat(originalUrl);
            }

            ResolvedUrl resolved = resolve(originalUrl, DESKTOP_UA);
            String host = Optional.ofNullable(resolved.uri().getHost()).orElse("").toLowerCase();
            if (host.contains("bilibili.com") || host.contains("b23.tv")) {
                return extractBilibili(originalUrl, resolved);
            }
            return extractGeneric(originalUrl, resolved);
        } catch (Exception e) {
            log.info("链接元信息抓取失败，url={}, error={}", originalUrl, e.getMessage());
            return UrlMetadataResult.builder()
                    .platform("UNKNOWN")
                    .publisher("未知作者")
                    .title("未解析标题")
                    .metaStatus("FAILED")
                    .metaError(abbreviate(e.getClass().getSimpleName() + ": " + e.getMessage(), 500))
                    .build();
        }
    }

    private UrlMetadataResult extractBilibili(String originalUrl, ResolvedUrl resolved)
            throws IOException, InterruptedException {
        String finalUrl = resolved.uri().toString();
        String bvid = extractFirstGroup(BILIBILI_BV, finalUrl).orElseThrow(
                () -> new IllegalStateException("No BV id found in Bilibili URL"));
        String api = "https://api.bilibili.com/x/web-interface/view?bvid=" + bvid;

        String body = send(api, Map.of(
                "User-Agent", DESKTOP_UA,
                "Referer", "https://www.bilibili.com/"
        )).body();

        String title = jsonValue(body, "title").orElse("未解析标题");
        String publisher = extractNestedJsonValue(body, "\"owner\"\\s*:\\s*\\{.*?\"name\"\\s*:\\s*\"(.*?)\"")
                .orElse("未知作者");
        LocalDateTime publishedAt = jsonNumber(body, "pubdate")
                .map(Long::parseLong)
                .map(this::epochSecondsToUtc)
                .orElse(null);

        return success(finalUrl, "BILIBILI", publisher, title, publishedAt);
    }

    private UrlMetadataResult extractYouTube(String originalUrl, URI originalUri)
            throws IOException, InterruptedException {
        String videoId = extractVideoId(originalUri.toString()).orElseThrow(
                () -> new IllegalStateException("No YouTube video id found"));
        String canonicalUrl = "https://www.youtube.com/watch?v=" + videoId;

        Optional<UrlMetadataResult> officialApiMetadata = fetchYouTubeByOfficialApi(videoId, canonicalUrl);
        if (officialApiMetadata.isPresent()) {
            return officialApiMetadata.get();
        }

        String api = "https://www.youtube.com/oembed?url="
                + URLEncoder.encode(canonicalUrl, StandardCharsets.UTF_8)
                + "&format=json";

        String body = send(api, Map.of("User-Agent", DESKTOP_UA)).body();
        String title = jsonValue(body, "title").orElse("未解析标题");
        String publisher = jsonValue(body, "author_name").orElse("未知作者");
        return partial(canonicalUrl, "YOUTUBE", publisher, title, null,
                "YouTube API key is empty, publishedAt not fetched");
    }

    private UrlMetadataResult extractX(String originalUrl, URI originalUri)
            throws IOException, InterruptedException {
        String path = Optional.ofNullable(originalUri.getPath()).orElse("");
        Matcher matcher = X_STATUS.matcher(path);
        if (!matcher.find()) {
            throw new IllegalStateException("Unsupported X/Twitter status URL");
        }

        String handle = matcher.group(1);
        String statusId = matcher.group(2);
        String canonicalUrl = "https://twitter.com/" + handle + "/status/" + statusId;
        String syndicationApi = "https://cdn.syndication.twimg.com/tweet-result?id=" + statusId + "&token=x";

        try {
            String body = send(syndicationApi, Map.of(
                    "User-Agent", DESKTOP_UA,
                    "Referer", "https://publish.twitter.com/"
            )).body();

            String publisher = jsonValue(body, "name").orElse(handle);
            LocalDateTime publishedAt = jsonValue(body, "created_at")
                    .flatMap(this::parseUtcTime)
                    .orElse(null);
            String title = firstNonBlank(List.of(
                    extractNestedJsonValue(body, "\"article\"\\s*:\\s*\\{.*?\"title\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"")
                            .map(this::normalizeTweetText)
                            .map(text -> abbreviate(text, X_TITLE_MAX_CHARS)),
                    extractNestedJsonValue(body, "\"preview_text\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"")
                            .map(this::normalizeTweetText)
                            .map(text -> abbreviate(text, X_TITLE_MAX_CHARS)),
                    jsonValue(body, "text")
                            .map(this::normalizeTweetText)
                            .filter(text -> !text.isBlank())
                            .filter(text -> !text.matches("https?://\\S+"))
                            .map(text -> abbreviate(text, X_TITLE_MAX_CHARS))
            )).orElse("Tweet by @" + handle);

            if (publishedAt == null || title.equals("Tweet by @" + handle)) {
                return partial(canonicalUrl, "X", publisher, title, publishedAt, "X metadata partially available");
            }
            return success(canonicalUrl, "X", publisher, title, publishedAt);
        } catch (IOException e) {
            String oembedApi = "https://publish.twitter.com/oembed?url="
                    + URLEncoder.encode(canonicalUrl, StandardCharsets.UTF_8);
            String body = send(oembedApi, Map.of("User-Agent", DESKTOP_UA)).body();
            String publisher = jsonValue(body, "author_name").orElse(handle);
            String html = jsonValue(body, "html").orElse("");
            String title = extractTweetText(html)
                    .map(this::normalizeTweetText)
                    .filter(text -> !text.isBlank())
                    .filter(text -> !text.matches("https?://\\S+"))
                    .map(text -> abbreviate(text, X_TITLE_MAX_CHARS))
                    .orElse("Tweet by @" + handle);

            return partial(canonicalUrl, "X", publisher, title, null,
                    "X syndication API unavailable, fallback to oEmbed");
        }
    }

    private UrlMetadataResult extractWeChat(String originalUrl) throws IOException, InterruptedException {
        HttpResponse<String> response = send(originalUrl, Map.of(
                "User-Agent", MOBILE_WECHAT_UA,
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                "Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8",
                "Referer", "https://mp.weixin.qq.com/"
        ));
        String finalUrl = response.uri().toString();
        String html = response.body();

        String title = firstNonBlank(
                List.of(
                        capture(html, "<h1[^>]*id=\"activity-name\"[^>]*>(.*?)</h1>", Pattern.DOTALL)
                                .map(this::stripHtml),
                        capture(html, "var\\s+msg_title\\s*=\\s*'(.+?)'\\.html\\(false\\);", Pattern.DOTALL),
                        Optional.of("未解析标题")
                )
        ).orElse("未解析标题");

        String publisher = firstNonBlank(
                List.of(
                        capture(html, "<a[^>]*id=\"js_name\"[^>]*>(.*?)</a>", Pattern.DOTALL)
                                .map(this::stripHtml),
                        capture(html, "<span[^>]*id=\"js_author_name_text\"[^>]*>(.*?)</span>", Pattern.DOTALL)
                                .map(this::stripHtml),
                        Optional.of("未知作者")
                )
        ).orElse("未知作者");

        LocalDateTime publishedAt = capture(html, "var\\s+ct\\s*=\\s*\"(\\d+)\"", 0)
                .map(Long::parseLong)
                .map(this::epochSecondsToUtc)
                .orElse(null);

        if (publishedAt == null) {
            return partial(finalUrl, "WECHAT_OFFICIAL_ACCOUNT", publisher, title, null,
                    "WeChat publish time not found");
        }
        return success(finalUrl, "WECHAT_OFFICIAL_ACCOUNT", publisher, title, publishedAt);
    }

    private UrlMetadataResult extractGeneric(String originalUrl, ResolvedUrl resolved) {
        String html = resolved.body();
        String host = Optional.ofNullable(resolved.uri().getHost()).orElse("unknown");
        String title = firstNonBlank(
                List.of(
                        captureMetaContent(html, "og:title"),
                        captureMetaContent(html, "twitter:title"),
                        capture(html, "<title[^>]*>(.*?)</title>", Pattern.DOTALL).map(this::stripHtml),
                        Optional.of("未解析标题")
                )
        ).orElse("未解析标题");

        String publisher = firstNonBlank(
                List.of(
                        captureMetaContent(html, "author"),
                        captureMetaContent(html, "article:author"),
                        Optional.of("未知作者")
                )
        ).orElse("未知作者");

        LocalDateTime publishedAt = firstNonBlank(
                List.of(
                        captureMetaContent(html, "article:published_time"),
                        captureMetaContent(html, "publish_time"),
                        captureMetaContent(html, "og:published_time")
                )
        ).flatMap(this::parseUtcTime).orElse(null);

        return partial(resolved.uri().toString(), "WEB", publisher, title, publishedAt, "Generic extractor used");
    }

    private ResolvedUrl resolve(String url, String userAgent) throws IOException, InterruptedException {
        HttpResponse<String> response = send(url, Map.of("User-Agent", userAgent));
        return new ResolvedUrl(response.uri(), response.body());
    }

    private HttpResponse<String> send(String url, Map<String, String> headers)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .GET()
                .timeout(Duration.ofSeconds(30))
                .uri(URI.create(url));
        headers.forEach(builder::header);
        HttpRequest request = builder.build();

        IOException lastIOException = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            } catch (IOException e) {
                lastIOException = e;
                if (attempt == 3) {
                    throw e;
                }
                Thread.sleep(500L * attempt);
            }
        }
        throw lastIOException == null ? new IOException("HTTP request failed") : lastIOException;
    }

    private UrlMetadataResult success(String finalUrl, String platform, String publisher,
                                      String title, LocalDateTime publishedAt) {
        return UrlMetadataResult.builder()
                .finalUrl(finalUrl)
                .platform(platform)
                .publisher(publisher)
                .title(title)
                .publishedAt(publishedAt)
                .metaStatus("SUCCESS")
                .metaError("")
                .build();
    }

    private UrlMetadataResult partial(String finalUrl, String platform, String publisher, String title,
                                      LocalDateTime publishedAt, String note) {
        return UrlMetadataResult.builder()
                .finalUrl(finalUrl)
                .platform(platform)
                .publisher(publisher)
                .title(title)
                .publishedAt(publishedAt)
                .metaStatus("PARTIAL")
                .metaError(abbreviate(note, 500))
                .build();
    }

    private Optional<String> extractVideoId(String url) {
        return extractFirstGroup(YOUTUBE_VIDEO_ID, url);
    }

    private Optional<UrlMetadataResult> fetchYouTubeByOfficialApi(String videoId, String canonicalUrl)
            throws IOException, InterruptedException {
        if (youtubeApiKey == null || youtubeApiKey.isBlank()) {
            return Optional.empty();
        }

        String api = "https://www.googleapis.com/youtube/v3/videos?part=snippet&id="
                + URLEncoder.encode(videoId, StandardCharsets.UTF_8)
                + "&key=" + URLEncoder.encode(youtubeApiKey, StandardCharsets.UTF_8);

        String body = send(api, Map.of("User-Agent", DESKTOP_UA)).body();
        Optional<String> title = extractNestedJsonValue(
                body, "\"snippet\"\\s*:\\s*\\{.*?\"title\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"");
        Optional<String> publisher = extractNestedJsonValue(
                body, "\"snippet\"\\s*:\\s*\\{.*?\"channelTitle\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"");
        Optional<LocalDateTime> publishedAt = extractNestedJsonValue(
                body, "\"snippet\"\\s*:\\s*\\{.*?\"publishedAt\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"")
                .flatMap(this::parseUtcTime);

        if (title.isPresent() || publisher.isPresent() || publishedAt.isPresent()) {
            return Optional.of(success(
                    canonicalUrl,
                    "YOUTUBE",
                    publisher.orElse("未知作者"),
                    title.orElse("未解析标题"),
                    publishedAt.orElse(null)
            ));
        }

        return Optional.of(partial(canonicalUrl, "YOUTUBE", "未知作者", "未解析标题", null,
                "YouTube Data API did not return snippet"));
    }

    private Optional<String> extractTweetText(String oembedHtml) {
        return capture(oembedHtml, "<p[^>]*>(.*?)</p>", Pattern.DOTALL)
                .map(this::stripHtml)
                .map(this::decodeHtmlEntities)
                .map(String::trim);
    }

    private Optional<String> captureMetaContent(String html, String property) {
        String quoted = Pattern.quote(property);
        List<Pattern> patterns = List.of(
                Pattern.compile("<meta[^>]+(?:property|name)=[\"']" + quoted
                        + "[\"'][^>]+content=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE),
                Pattern.compile("<meta[^>]+content=[\"']([^\"']+)[\"'][^>]+(?:property|name)=[\"']"
                        + quoted + "[\"'][^>]*>", Pattern.CASE_INSENSITIVE)
        );
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                return Optional.of(decodeHtmlEntities(matcher.group(1).trim()));
            }
        }
        return Optional.empty();
    }

    private Optional<String> capture(String text, String regex, int flags) {
        Matcher matcher = Pattern.compile(regex, flags).matcher(text);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private Optional<String> extractFirstGroup(Pattern pattern, String value) {
        Matcher matcher = pattern.matcher(value);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private Optional<String> jsonValue(String json, String key) {
        String regex = "\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:\\\\.|[^\\\\\"])*)\"";
        return capture(json, regex, 0).map(this::decodeJsonString);
    }

    private Optional<String> jsonNumber(String json, String key) {
        String regex = "\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)";
        return capture(json, regex, 0);
    }

    private Optional<String> extractNestedJsonValue(String json, String regex) {
        return capture(json, regex, Pattern.DOTALL).map(this::decodeJsonString);
    }

    private Optional<String> firstNonBlank(List<Optional<String>> values) {
        for (Optional<String> value : values) {
            if (value.isPresent() && !value.get().trim().isBlank()) {
                return Optional.of(value.get().trim());
            }
        }
        return Optional.empty();
    }

    private String stripHtml(String value) {
        return decodeHtmlEntities(value.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim());
    }

    private String normalizeTweetText(String value) {
        return decodeHtmlEntities(value)
                .replaceAll("https?://\\S+", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String abbreviate(String value, int maxCodePoints) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int total = value.codePointCount(0, value.length());
        if (total <= maxCodePoints) {
            return value;
        }
        int endIndex = value.offsetByCodePoints(0, maxCodePoints);
        return value.substring(0, endIndex) + "...";
    }

    private String decodeJsonString(String value) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case '"', '\\', '/' -> sb.append(next);
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (i + 4 < value.length()) {
                            String hex = value.substring(i + 1, i + 5);
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        }
                    }
                    default -> sb.append(next);
                }
            } else {
                sb.append(ch);
            }
        }
        return decodeHtmlEntities(sb.toString());
    }

    private String decodeHtmlEntities(String value) {
        return value
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ")
                .replace("&mdash;", "-");
    }

    private Optional<LocalDateTime> parseUtcTime(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDateTime.ofInstant(Instant.parse(value), ZoneOffset.UTC));
        } catch (DateTimeParseException ignored) {
        }
        try {
            return Optional.of(LocalDateTime.ofInstant(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value, Instant::from),
                    ZoneOffset.UTC));
        } catch (DateTimeParseException ignored) {
        }
        return Optional.empty();
    }

    private LocalDateTime epochSecondsToUtc(long epochSeconds) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }

    private record ResolvedUrl(URI uri, String body) {
    }
}
