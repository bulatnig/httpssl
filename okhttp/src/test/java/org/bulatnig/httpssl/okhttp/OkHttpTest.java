package org.bulatnig.httpssl.okhttp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.Test;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class OkHttpTest {

    @Test
    public void clientSslCertificateOkHttp() throws Exception {
        var trustManager = initDefaultTrustManager();
        var keyManagerFactory = initKeyManagerFactory();
        var socketFactory = initSslSocketFactory(keyManagerFactory, trustManager);
        var client = new OkHttpClient.Builder().sslSocketFactory(socketFactory, trustManager).build();

        var request = new Request.Builder().url("https://client.badssl.com/").build();
        var response = client.newCall(request).execute();

        assertThat(response.isSuccessful()).isTrue();
    }

    private static SSLSocketFactory initSslSocketFactory(KeyManagerFactory keyManagerFactory,
                                                         X509TrustManager trustManager) throws NoSuchAlgorithmException, KeyManagementException {
        var sslContext = SSLContext.getInstance("SSL");
        sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{trustManager}, null);
        return sslContext.getSocketFactory();
    }

    private static KeyManagerFactory initKeyManagerFactory() throws KeyStoreException, IOException,
            NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(OkHttpTest.class.getResourceAsStream("/badssl.com-client.p12"), "badssl.com".toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, "badssl.com".toCharArray());
        return keyManagerFactory;
    }

    private static X509TrustManager initDefaultTrustManager() throws NoSuchAlgorithmException, KeyStoreException {
        var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        var trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers: " + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];
    }
}
