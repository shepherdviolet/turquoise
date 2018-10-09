package sviolet.turquoise.model.net;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import sviolet.thistle.util.judge.CheckUtils;
import sviolet.turquoise.x.common.tlogger.TLogger;

/**
 * 简易的HostnameVerifier
 * 1.实现hostname与CN的匹配
 * 2.实现hostname与subjectAlternativeNames的匹配
 * 3.支持通配符域名(*)
 *
 * @author S.Violet
 */
public class SimpleHostnameVerifier implements HostnameVerifier {

    private static final Integer DNS_NAME = 2;

    @Override
    public boolean verify(String hostname, SSLSession session) {
        try {
            Certificate[] certificates = session.getPeerCertificates();
            if (certificates == null || certificates.length <= 0) {
                return false;
            }

            //第一个证书是站点证书
            X509Certificate x509Certificate = (X509Certificate) certificates[0];
            String dn = x509Certificate.getSubjectX500Principal().getName();
            String cn = getCn(dn);

            //验证CN与域名是否相符
            if (isHostnameMatch(hostname, cn)) {
                return true;
            }

            //获取subjectAlternativeNames
            Collection<List<?>> subjectAlternativeNames;
            try {
                subjectAlternativeNames = x509Certificate.getSubjectAlternativeNames();
            } catch (CertificateParsingException ignore) {
                return false;
            }

            if (subjectAlternativeNames == null) {
                return false;
            }

            //遍历subjectAlternativeNames
            for (List<?> item : subjectAlternativeNames) {
                //正常的格式类似于[2, *.test.com], 2表示该项是DNS Name, 后面是域名
                if (item == null || item.size() < 2 || !DNS_NAME.equals(item.get(0))) {
                    continue;
                }
                if (isHostnameMatch(hostname, String.valueOf(item.get(1)))) {
                    return true;
                }
            }

        } catch (Throwable t) {
            TLogger.get(this).w("Error while verify hostname", t);
        }

        return false;
    }

    private String getCn(String dn) {
        if (CheckUtils.isEmptyOrBlank(dn)) {
            return "";
        }
        String[] dnArray = dn.split(",");
        for (String dnItem : dnArray) {
            dnItem = dnItem.trim();
            if (dnItem.startsWith("CN=")) {
                return dnItem.substring(3);
            }
        }
        return "";
    }

    private boolean isHostnameMatch(String hostname, String cn) {
        if (CheckUtils.isEmptyOrBlank(hostname) || CheckUtils.isEmptyOrBlank(cn)) {
            return false;
        }
        if (cn.charAt(0) == '*') {
            cn = cn.substring(1);
            return hostname.endsWith(cn);
        } else {
            return hostname.equals(cn);
        }
    }

}
