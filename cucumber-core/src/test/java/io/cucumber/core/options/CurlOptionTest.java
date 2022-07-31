package io.cucumber.core.options;

import io.cucumber.core.options.CurlOption.HttpMethod;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap.SimpleEntry;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurlOptionTest {

    @Test
    public void can_parse_url() {
        CurlOption option = CurlOption.parse("https://example.com");
        assertThat(option.getUri(), is(URI.create("https://example.com")));
    }

    @Test
    public void must_contain_a_url() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> CurlOption.parse(""));
        assertThat(exception.getMessage(), is("'' was not a valid curl command"));

    }

    @Test
    public void can_parse_url_with_method() {
        CurlOption option = CurlOption.parse("https://example.com -X POST");
        assertThat(option.getUri(), is(URI.create("https://example.com")));
        assertThat(option.getMethod(), is(HttpMethod.POST));
    }

    @Test
    public void must_provide_valid_method() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> CurlOption.parse("https://example.com -X NO-SUCH-METHOD"));
        assertThat(exception.getMessage(), is("NO-SUCH-METHOD was not a http method"));
    }

    @Test
    public void can_parse_url_with_header() {
        CurlOption option = CurlOption.parse("https://example.com -H 'Content-Type: application/x-ndjson'");
        assertThat(option.getUri(), is(URI.create("https://example.com")));
        assertThat(option.getHeaders(), is(singletonList(new SimpleEntry<>("Content-Type", "application/x-ndjson"))));
    }

    @Test
    public void must_provide_valid_headers() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> CurlOption.parse("https://example.com -H 'Content-Type'"));
        assertThat(exception.getMessage(), is("'Content-Type' was not a valid header"));
    }

    @Test
    public void may_only_provide_one_url() {
        String uri = "https://example.com/path https://example.com/other/path";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> CurlOption.parse(uri));
        assertThat(exception.getMessage(),
            is("'https://example.com/path https://example.com/other/path' was not a valid curl command"));
    }

    @Test
    public void must_provide_a_valid_uri() {
        String uri = "'https://example.com/path with spaces'";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> CurlOption.parse(uri));
        assertThat(exception.getCause(), instanceOf(URISyntaxException.class));
    }

    @Test
    public void can_parse_https_proxy() {
        CurlOption option = CurlOption.parse("https://example.com -x https://proxy.example.com:3129");
        assertThat(option.getProxy(), is(new Proxy(Type.HTTP, new InetSocketAddress("proxy.example.com", 3129))));
    }

    @Test
    public void can_parse_socks_proxy() {
        CurlOption option = CurlOption.parse("https://example.com -x socks://proxy.example.com:3129");
        assertThat(option.getProxy(), is(new Proxy(Type.SOCKS, new InetSocketAddress("proxy.example.com", 3129))));
    }

    @Test
    public void must_provide_proxy_address() {
        String uri = "https://example.com -x !@#%";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> CurlOption.parse(uri));
        assertThat(exception.getMessage(), is("'!@#%' was not a valid proxy address"));
    }

    @Test
    public void must_provide_proxy_protocol() {
        String uri = "https://example.com -x //proxy.example.com:3129";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> CurlOption.parse(uri));
        assertThat(exception.getMessage(), is("'//proxy.example.com:3129' did not have a valid proxy protocol"));
    }

    @Test
    public void must_provide_valid_proxy_protocol() {
        String uri = "https://example.com -x no-such-protocol://proxy.example.com:3129";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> CurlOption.parse(uri));
        assertThat(exception.getMessage(),
            is("'no-such-protocol://proxy.example.com:3129' did not have a valid proxy protocol"));
    }

    @Test
    public void must_provide_valid_proxy_domain() {
        String uri = "https://example.com -x https://:3129";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> CurlOption.parse(uri));
        assertThat(exception.getMessage(), is("'https://:3129' did not have a valid proxy host"));
    }

    @Test
    public void must_provide_valid_proxy_port() {
        String uri = "https://example.com -x https://proxy.example.com";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> CurlOption.parse(uri));
        assertThat(exception.getMessage(), is("'https://proxy.example.com' did not have a valid proxy port"));
    }

}
