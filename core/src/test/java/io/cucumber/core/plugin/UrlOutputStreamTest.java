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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith({ VertxExtension.class })
public class UrlOutputStreamTest {

    private static final int TIMEOUT_SECONDS = 15;
    private int port;
    private Exception exception;

    @BeforeEach
    void randomPort() throws IOException {
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();
    }

    @Test
    void throws_exception_for_500_status(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
        String requestBody = "hello";
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.PUT, null, null, 500,
            "Oh noes");
        CurlOption option = CurlOption.parse(format("http://localhost:%d/s3", port));

        verifyRequest(option, testServer, vertx, testContext, requestBody);
        assertThat(testContext.awaitCompletion(TIMEOUT_SECONDS, TimeUnit.SECONDS), is(true));
        assertThat(exception.getMessage(), equalTo("HTTP request failed:\n" +
                "> PUT http://localhost:" + port + "/s3\n" +
                "< HTTP/1.1 500 Internal Server Error\n" +
                "< transfer-encoding: chunked\n" +
                "Oh noes"));
    }

    private void verifyRequest(
            CurlOption url, TestServer testServer, Vertx vertx, VertxTestContext testContext, String requestBody
    ) {
        vertx.deployVerticle(testServer, testContext.succeeding(id -> {
            try {
                OutputStream out = new UrlOutputStream(url, null);
                Writer w = new UTF8OutputStreamWriter(out);
                w.write(requestBody);
                w.flush();
                w.close();
                testContext.completeNow();
            } catch (Exception e) {
                exception = e;
                testContext.completeNow();
            }
        }));
    }

    @Test
    void it_sends_the_body_twice_for_307_redirect_with_put(Vertx vertx, VertxTestContext testContext) throws Exception {
        String requestBody = "hello";
        TestServer testServer = new TestServer(port, testContext, requestBody + requestBody, HttpMethod.PUT, null, null,
            200, "");
        CurlOption url = CurlOption.parse(format("http://localhost:%d/redirect", port));
        verifyRequest(url, testServer, vertx, testContext, requestBody);

        assertThat(testContext.awaitCompletion(TIMEOUT_SECONDS, TimeUnit.SECONDS), is(true));
    }

    @Test
    void it_sends_the_body_once_for_202_and_location_with_get_without_token(Vertx vertx, VertxTestContext testContext)
            throws Exception {
        String requestBody = "hello";
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.PUT, null, null, 200, "");
        CurlOption url = CurlOption
                .parse(format("http://localhost:%d/accept -X GET -H 'Authorization: Bearer s3cr3t'", port));
        verifyRequest(url, testServer, vertx, testContext, requestBody);

        assertThat(testContext.awaitCompletion(TIMEOUT_SECONDS, TimeUnit.SECONDS), is(true));
        if (exception != null) {
            throw exception;
        }
        assertThat(testServer.receivedBody.toString("utf-8"), is(equalTo(requestBody)));
    }

    @Test
    @Disabled
    void throws_exception_for_307_temporary_redirect_without_location(Vertx vertx, VertxTestContext testContext)
            throws InterruptedException {
        String requestBody = "hello";
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.POST, null,
            "application/x-www-form-urlencoded", 200, "");
        CurlOption url = CurlOption.parse(format("http://localhost:%d/redirect-no-location -X POST", port));
        verifyRequest(url, testServer, vertx, testContext, requestBody);

        assertThat(testContext.awaitCompletion(TIMEOUT_SECONDS, TimeUnit.SECONDS), is(true));
        assertThat(exception.getMessage(), equalTo("HTTP request failed:\n" +
                "> POST http://localhost:" + port + "/redirect-no-location\n" +
                "< HTTP/1.1 307 Temporary Redirect\n" +
                "< content-length: 0\n"));
    }

    @Test
    void streams_request_body_in_chunks(Vertx vertx, VertxTestContext testContext) {
        String requestBody = makeOneKilobyteStringWithEmoji();
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.PUT, null, null, 200, "");
        CurlOption url = CurlOption.parse(format("http://localhost:%d", port));
        verifyRequest(url, testServer, vertx, testContext, requestBody);
    }

    private String makeOneKilobyteStringWithEmoji() {
        String base = "abcå\uD83D\uDE02";
        int baseLength = base.length();
        return IntStream.range(0, 1024).mapToObj(i -> base.substring(i % baseLength, i % baseLength + 1))
                .collect(Collectors.joining());
    }

    @Test
    void overrides_request_method(Vertx vertx, VertxTestContext testContext) {
        String requestBody = "hello";
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.POST, null,
            "application/x-www-form-urlencoded", 200, "");
        CurlOption url = CurlOption.parse(format("http://localhost:%d -X POST", port));
        verifyRequest(url, testServer, vertx, testContext, requestBody);
    }

    @Test
    void sets_request_headers(Vertx vertx, VertxTestContext testContext) {
        String requestBody = "hello";
        TestServer testServer = new TestServer(port, testContext, requestBody, HttpMethod.PUT, "foo=bar",
            "application/x-ndjson", 200, "");
        CurlOption url = CurlOption
                .parse(format("http://localhost:%d?foo=bar -H 'Content-Type: application/x-ndjson'", port));
        verifyRequest(url, testServer, vertx, testContext, requestBody);
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
        private final Buffer receivedBody = Buffer.buffer(0);

        public TestServer(
                int port,
                VertxTestContext testContext,
                String expectedBody,
                HttpMethod expectedMethod,
                String expectedQuery,
                String expectedContentType,
                int statusCode, String responseBody
        ) {
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
            router.route("/accept").handler(ctx -> {
                ctx.request().handler(receivedBody::appendBuffer);

                String contentLengthString = ctx.request().getHeader("Content-Length");
                int contentLength = contentLengthString == null ? 0 : Integer.parseInt(contentLengthString);
                if (contentLength > 0) {
                    ctx.response().setStatusCode(500);
                    ctx.response().end("Unexpected body");
                } else {
                    ctx.response().setStatusCode(202);
                    ctx.response().headers().add("Location", "http://localhost:" + port + "/s3");
                    ctx.response().end();
                }
            });
            router.route("/redirect").handler(ctx -> {
                ctx.request().handler(receivedBody::appendBuffer);
                ctx.response().setStatusCode(307);
                ctx.response().headers().add("Location", "http://localhost:" + port + "/s3");
                ctx.response().end();
            });
            router.route("/redirect-no-location").handler(ctx -> {
                ctx.request().handler(receivedBody::appendBuffer);
                ctx.response().setStatusCode(307);
                ctx.response().end();
            });

            router.route("/s3").handler(ctx -> {
                ctx.response().setStatusCode(statusCode);
                testContext.verify(() -> {
                    assertThat(ctx.request().method(), is(equalTo(expectedMethod)));
                    assertThat(ctx.request().query(), is(equalTo(expectedQuery)));
                    assertThat(ctx.request().getHeader("Content-Type"), is(equalTo(expectedContentType)));
                    // We should never send the Authorization header.
                    assertThat(ctx.request().getHeader("Authorization"), is(nullValue()));

                    ctx.request().handler(receivedBody::appendBuffer);
                    ctx.request().endHandler(e -> {
                        String receivedBodyString = receivedBody.toString("utf-8");
                        ctx.response().setChunked(true);
                        ctx.response().write(responseBody);
                        ctx.response().end();
                        testContext.verify(() -> assertThat(receivedBodyString, is(equalTo(expectedBody))));
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
