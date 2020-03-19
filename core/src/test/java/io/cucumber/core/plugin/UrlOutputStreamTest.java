package io.cucumber.core.plugin;

import io.cucumber.core.options.CurlOption;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@ExtendWith({VertxExtension.class})
public class UrlOutputStreamTest {
    private int port;
    private IOException exception;

    @BeforeEach
    void randomPort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
    }

    @Test
    void throws_exception_for_500_status(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        String requestBody = "hello";
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.POST, null, "application/x-www-form-urlencoded", 500, "Oh noes");
        CurlOption option = CurlOption.parse(format("http://localhost:%d", port));

        verifyRequest(option, testServer, vertx, testContext, requestBody);
        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS), is(true));
        assertThat(exception.getMessage(), is(equalTo("HTTP request failed:\n" +
            "> POST http://localhost:" + port + "\n" +
            "< HTTP/1.1 500 Internal Server Error\n" +
            "< transfer-encoding: chunked\n" +
            "Oh noes")));
    }

    @Test
    void throws_exception_for_redirects(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        String requestBody = "hello";
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.POST, null, "application/x-www-form-urlencoded", 200, "");
        CurlOption url = CurlOption.parse(format("http://localhost:%d/redirect", port));
        verifyRequest(url, testServer, vertx, testContext, requestBody);

        assertThat(testContext.awaitCompletion(5, TimeUnit.SECONDS), is(true));
        assertThat(exception.getMessage(), is(equalTo("HTTP redirect not supported:\n" +
            "> POST http://localhost:" + port + "/redirect\n" +
            "< HTTP/1.1 301 Moved Permanently\n" +
            "< content-length: 0\n" +
            "< Location: /\n")));
    }

    @Test
    void streams_request_body_in_chunks(Vertx vertx, VertxTestContext testContext) {
        String requestBody = makeOneKilobyteStringWithEmoji();
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.POST, null, "application/x-www-form-urlencoded", 200, "");
        CurlOption url = CurlOption.parse(format("http://localhost:%d", port));
        verifyRequest(url, testServer, vertx, testContext, requestBody);
    }

    @Test
    void overrides_request_method(Vertx vertx, VertxTestContext testContext) {
        String requestBody = "hello";
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.PUT, null, null, 200, "");
        CurlOption url = CurlOption.parse(format("http://localhost:%d -X PUT", port));
        verifyRequest(url, testServer, vertx, testContext, requestBody);
    }

    @Test
    void sets_request_headers(Vertx vertx, VertxTestContext testContext) {
        String requestBody = "hello";
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.POST, "foo=bar", "application/x-ndjson", 200, "");
        CurlOption url = CurlOption.parse(format("http://localhost:%d?foo=bar -H 'Content-Type: application/x-ndjson'", port));
        verifyRequest(url, testServer, vertx, testContext, requestBody);
    }

    private void verifyRequest(CurlOption url, TestServer testServer, Vertx vertx, VertxTestContext testContext, String requestBody) {
        vertx.deployVerticle(testServer, testContext.succeeding(id -> {
            try {
                OutputStream out = new UrlOutputStream(url);
                Writer w = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                w.write(requestBody);
                w.flush();
                w.close();
                testContext.completeNow();
            } catch (IOException e) {
                exception = e;
                testContext.completeNow();
            }
        }));
    }

    private String makeOneKilobyteStringWithEmoji() {
        String base = "abcå\uD83D\uDE02";
        int baseLength = base.length();
        return IntStream.range(0, 1024).mapToObj(i -> base.substring(i % baseLength, i % baseLength + 1)).collect(Collectors.joining());
    }

    public static class TestServer extends AbstractVerticle {
        private final int port;
        private final VertxTestContext testContext;
        private final String expectedBody;
        private final HttpMethod expectedMethod;
        private final String expectedQuery;
        private final String expectedContentType;
        private final int statusCode;
        private final String responseBody;

        public TestServer(
            int port,
            VertxTestContext testContext,
            String expectedBody,
            HttpMethod expectedMethod,
            String expectedQuery,
            String expectedContentType,
            int statusCode, String responseBody) {
            this.port = port;
            this.testContext = testContext;
            this.expectedBody = expectedBody;
            this.expectedMethod = expectedMethod;
            this.expectedQuery = expectedQuery;
            this.expectedContentType = expectedContentType;
            this.statusCode = statusCode;
            this.responseBody = responseBody;
        }

        @Override
        public void start(Promise<Void> startPromise) {
            Router router = Router.router(vertx);
            router.route().handler(ctx -> {
                if (ctx.request().uri().equals("/redirect")) {
                    ctx.response().setStatusCode(301);
                    ctx.response().headers().add("Location", "/");
                    ctx.response().end();
                    return;
                }
                ctx.response().setStatusCode(statusCode);

                testContext.verify(() -> {
                    assertThat(ctx.request().method(), is(equalTo(expectedMethod)));
                    assertThat(ctx.request().query(), is(equalTo(expectedQuery)));
                    assertThat(ctx.request().getHeader("Content-Type"), is(equalTo(expectedContentType)));

                    Buffer body = Buffer.buffer(0);
                    ctx.request().handler(body::appendBuffer);
                    ctx.request().endHandler(e -> {
                        String receivedBody = body.toString("utf-8");
                        ctx.response().setChunked(true);
                        ctx.response().write(responseBody);
                        ctx.response().end();
                        testContext.verify(() -> assertThat(receivedBody, is(equalTo(expectedBody))));
                    });
                });
            });
            vertx
                .createHttpServer()
                .requestHandler(router)
                .listen(port, e -> startPromise.complete());
        }
    }
}
