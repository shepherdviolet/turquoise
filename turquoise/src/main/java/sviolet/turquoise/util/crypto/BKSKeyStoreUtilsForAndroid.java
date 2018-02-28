package sviolet.turquoise.util.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * 安卓BKS证书密钥文件工具
 */
public class BKSKeyStoreUtilsForAndroid {

    /**
     * 从BKS文件中创建TLS型SslSocketFactory
     *
     * <pre>{@code
     *  SSLSocketFactoryAndX509TrustManager x509TrustManagerAndSSLSocketFactory = newTlsSslSocketFactoryFromBks(getResources().openRawResource(R.raw.keystore), "PASSWORD");
     *  OkHttpClient okHttpClient = new OkHttpClient.Builder()
     *      .sslSocketFactory(x509TrustManagerAndSSLSocketFactory.getSslSocketFactory(), x509TrustManagerAndSSLSocketFactory.getX509TrustManager())
     *      .build();
     * }</pre>
     *
     * @param inputStream 输入流
     * @param password 密码
     */
    public static SSLSocketFactoryAndX509TrustManager newTlsSslSocketFactoryFromBks(InputStream inputStream, String password) throws NoSuchAlgorithmException, KeyStoreException, IOException, CertificateException, UnrecoverableKeyException, KeyManagementException {
        try {
            //keystore from bks
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(inputStream, password.toCharArray());
            //key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password.toCharArray());
            //trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new NoSuchAlgorithmException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
            //ssl context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{trustManager}, null);
            return new SSLSocketFactoryAndX509TrustManager(trustManager, sslContext.getSocketFactory());
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (Exception ignore){
                }
            }
        }
    }

    public static class SSLSocketFactoryAndX509TrustManager {
        private X509TrustManager x509TrustManager;
        private SSLSocketFactory sslSocketFactory;

        private SSLSocketFactoryAndX509TrustManager(X509TrustManager x509TrustManager, SSLSocketFactory sslSocketFactory) {
            this.x509TrustManager = x509TrustManager;
            this.sslSocketFactory = sslSocketFactory;
        }

        public X509TrustManager getX509TrustManager() {
            return x509TrustManager;
        }

        public SSLSocketFactory getSslSocketFactory() {
            return sslSocketFactory;
        }
    }

}
