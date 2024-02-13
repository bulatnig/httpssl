package org.bulatnig.httpssl.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.tls.Certificates;
import okhttp3.tls.HandshakeCertificates;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class SelfSignedCertificateTest {

    @Test
    public void clientSslCertificateOkHttp() throws Exception {
        var resourceAsStream = OkHttpTest.class.getResourceAsStream("/badssl-com.pem");
        var certPem = new String(resourceAsStream.readAllBytes(), StandardCharsets.UTF_8);
        var certificate = Certificates.decodeCertificatePem(certPem);
        var certificates = new HandshakeCertificates.Builder().addTrustedCertificate(certificate).build();
        var client = new OkHttpClient.Builder().sslSocketFactory(certificates.sslSocketFactory(), certificates.trustManager()).build();

        var request = new Request.Builder().url("https://self-signed.badssl.com/").build();
        var response = client.newCall(request).execute();

        assertThat(response.isSuccessful()).isTrue();
    }
}
