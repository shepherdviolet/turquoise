package sviolet.demoa.image.utils;

/**
 *
 * Created by S.Violet on 2015/7/7.
 */
public class AsyncImageItem {

    private String title;
    private String content;
    private String url[] = new String[5];
    private String key[] = new String[5];

    public String getTitle() {
        return title;
    }

    public String getUrl(int index) {
        return url[index];
    }

    public String getKey(int index) {
        return key[index];
    }

    public String[] getUrls(){
        return url;
    }

    public String[] getKeys(){
        return key;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(int index, String url) {
        this.url[index] = url;
    }

    public void setKey(int index, String key) {
        this.key[index] = key;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
