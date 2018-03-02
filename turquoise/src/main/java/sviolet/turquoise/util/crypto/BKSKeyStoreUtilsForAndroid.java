package sviolet.turquoise.util.crypto;

import org.bouncycastle.operator.OperatorCreationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * <p>安卓BKS证书密钥文件工具</p>
 *
 * <p>案例: 优先使用自定义证书链验证服务端证书(解决部分手机系统证书认证有问题的情况), 当抛出握手异常的时候,
 * 尝试使用系统的SSLSocketFactory.getDefault().</p>
 */
public class BKSKeyStoreUtilsForAndroid {

    /**
     * <p>从BKS文件中创建TLS型SslSocketFactory</p>
     *
     * <p>
     * Note:<br>
     * OkHttpClient或HttpsUrlConnection如果配置了自定义证书链(keystore中可以配置多个证书链), 只要服务端
     * 证书所属的根证书与自定义证书链中的根证书相符, 均可验证通过, 及时服务端证书或二级CA证书未在自定义证书链
     * 中配置.
     * </p>
     *
     * <pre>{@code
     *  SSLSocketFactoryAndX509TrustManager sslSocketFactoryAndX509TrustManager = newTlsSslSocketFactoryFromBks(getResources().openRawResource(R.raw.keystore), "PASSWORD");
     *  OkHttpClient okHttpClient = new OkHttpClient.Builder()
     *      .sslSocketFactory(sslSocketFactoryAndX509TrustManager.getSslSocketFactory(), sslSocketFactoryAndX509TrustManager.getX509TrustManager())
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

    /**
     * SSLSocketFactory和X509TrustManager
     */
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

    /**
     * 从BKS文件中获取所有证书
     * @param inputStream 输入流
     * @param password 密码
     */
    public static List<Certificate> getCertificatesFromBks(InputStream inputStream, String password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        try {
            //keystore from bks
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(inputStream, password.toCharArray());
            Enumeration<String> enumeration = keyStore.aliases();
            List<Certificate> certificates = new ArrayList<>();
            while (enumeration.hasMoreElements()) {
                certificates.add(keyStore.getCertificate(enumeration.nextElement()));
            }
            return certificates;
        } finally {
            if (inputStream != null){
                try {
                    inputStream.close();
                } catch (Exception ignore){
                }
            }
        }
    }

    /**
     * 把证书保存到BKS文件中
     * @param outputStream 文件输出流
     * @param password 密码
     * @param aliases 别名
     * @param certificates 证书
     */
    public static void storeCertificates(OutputStream outputStream, String password, String[] aliases, Certificate[] certificates) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, OperatorCreationException {
        if (aliases == null || certificates == null) {
            throw new NullPointerException("aliases or certificates is null");
        }
        if (aliases.length != certificates.length) {
            throw new IllegalArgumentException("length of aliases and certificates mismatch");
        }
        try {
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(null);
            for (int i = 0 ; i < aliases.length ; i++) {
                keyStore.setCertificateEntry(aliases[i], certificates[i]);
            }
            keyStore.store(outputStream, password != null ? password.toCharArray() : null);
        } finally {
            if (outputStream != null){
                try {
                    outputStream.close();
                } catch (Exception ignore){
                }
            }
        }
    }

}
