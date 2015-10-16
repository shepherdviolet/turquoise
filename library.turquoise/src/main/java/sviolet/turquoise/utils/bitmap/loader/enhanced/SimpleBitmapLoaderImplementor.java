package sviolet.turquoise.utils.bitmap.loader.enhanced;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderImplementor;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoaderMessenger;
import sviolet.turquoise.utils.conversion.ByteUtils;
import sviolet.turquoise.utils.crypt.DigestCipher;

/**
 * 简易BitmapLoaderImplementor实现<br/>
 * 1.实现HTTP加载图片<br/>
 * 2.实现sha1缓存key<br/>
 * 3.日志打印方式处理异常<br/>
 *
 * Created by S.Violet on 2015/10/12.
 */
public class SimpleBitmapLoaderImplementor implements BitmapLoaderImplementor {

    private int timeout;

    public SimpleBitmapLoaderImplementor(int timeout){
        if (timeout <= 0)
            throw new NullPointerException("[SimpleBitmapLoaderImplementor] timeout <= 0");
        this.timeout = timeout;
    }

    @Override
    public String getCacheKey(String url) {
        //url->SHA1->hex->key
        return ByteUtils.byteToHex(DigestCipher.digest(url, DigestCipher.TYPE_SHA1));
    }

    @Override
    public void loadFromNet(String url, int reqWidth, int reqHeight, BitmapLoaderMessenger messenger) {
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        HttpURLConnection conn = null;
        try {
            URL httpUrl = new URL(url);
            conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setConnectTimeout(timeout);
            conn.setRequestMethod("GET");
            inputStream = conn.getInputStream();
            if(conn.getResponseCode() == HttpURLConnection.HTTP_OK){
                outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while((len = inputStream.read(buffer)) != -1){
                    outputStream.write(buffer, 0, len);
                }
                byte[] data = outputStream.toByteArray();
                if (data == null || data.length <= 0){
                    //设置结果返回[重要]
                    messenger.setResultFailed(new Exception("[SimpleBitmapLoaderImplementor]data is null"));
                    return;
                }
                //设置结果返回[重要]
                messenger.setResultSucceed(BitmapUtils.decodeFromByteArray(data, reqWidth, reqHeight));
                return;
            }
        } catch (MalformedURLException e) {
            //设置结果返回[重要]
            messenger.setResultFailed(e);
            return;
        } catch (ProtocolException e) {
            //设置结果返回[重要]
            messenger.setResultFailed(e);
            return;
        } catch (IOException e) {
            //设置结果返回[重要]
            messenger.setResultFailed(e);
            return;
        }finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignored) {
                }
            }
            if (inputStream != null)
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            if (conn != null)
                conn.disconnect();
        }
        //设置结果返回[重要]
        messenger.setResultFailed(null);
    }

    @Override
    public void onException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onCacheWriteException(Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void onDestroy() {

    }
}
