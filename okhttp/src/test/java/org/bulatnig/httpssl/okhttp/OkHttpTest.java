package org.bulatnig.httpssl.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.Test;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

public class OkHttpTest {

    @Test
    public void clientSslCertificateOkHttp() throws Exception {
        var sslContext = initSslContext("/badssl.com-client.p12", "badssl.com");

        var client = new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), noopTrustManager())
                .build();
        var request = new Request.Builder()
                .url("https://client.badssl.com/")
                .build();

        var response = client.newCall(request).execute();

        assertThat(response.isSuccessful()).isTrue();
    }

    private static SSLContext initSslContext(String fileName, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(OkHttpTest.class.getResourceAsStream(fileName), password.toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        var sslContext = SSLContext.getInstance("SSL");

        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        return sslContext;
    }

    private static X509TrustManager noopTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        String trustManagerFactoryAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerFactoryAlgorithm);
        trustManagerFactory.init(KeyStore.getInstance("PKCS12"));
        return (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
    }
}
