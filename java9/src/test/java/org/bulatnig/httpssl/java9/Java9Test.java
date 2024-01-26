package org.bulatnig.httpssl.java9;

import org.apache.hc.core5.ssl.SSLContexts;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyStore;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Java9Test {

    @Test
    public void validSslCertificate() throws Exception {
        var request = HttpRequest.newBuilder().uri(new URI("https://sha256.badssl.com/")).build();
        var client = HttpClient.newHttpClient();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    public void expiredSslCertificate() throws Exception {
        var request = HttpRequest.newBuilder().uri(new URI("https://expired.badssl.com/")).build();
        var client = HttpClient.newHttpClient();
        assertThatThrownBy(() -> client.send(request, HttpResponse.BodyHandlers.ofString()))
                .isInstanceOf(SSLHandshakeException.class).hasMessageContaining("validation failed");
    }

    @Test
    public void clientSslCertificateApacheHttp() throws Exception {
        var request = HttpRequest.newBuilder().uri(new URI("https://client.badssl.com/")).build();

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(Java9Test.class.getResourceAsStream("/badssl.com-client.p12"),
                "badssl.com".toCharArray());
        var sslContext =
                SSLContexts.custom().loadKeyMaterial(keyStore, "badssl.com".toCharArray()).build();

        var client = HttpClient.newBuilder().sslContext(sslContext).build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
    }

    @Test
    public void clientSslCertificateVanilla() throws Exception {
        var sslContext = initSslContext("/badssl.com-client.p12", "badssl.com");
        var client = HttpClient.newBuilder().sslContext(sslContext).build();

        var request = HttpRequest.newBuilder().uri(new URI("https://client.badssl.com/")).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode()).isEqualTo(200);
    }

    private static SSLContext initSslContext(String fileName, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(Java9Test.class.getResourceAsStream(fileName), password.toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        var sslContext = SSLContext.getInstance("SSL");

        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        return sslContext;
    }
}
