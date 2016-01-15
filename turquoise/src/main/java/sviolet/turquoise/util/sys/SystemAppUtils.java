package sviolet.turquoise.util.sys;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * 调用系统APP工具
 * Created by S.Violet on 2015/7/27.
 */
public class SystemAppUtils {

    /**
     * 打开联系人列表
     * @param context activity
     */
    public static void openContacts(Activity context) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        context.startActivity(intent);
    }

    /**
     * 打开联系人列表(startActivityForResult方式)
     * @param context activity
     * @param requestCode 请求吗
     */
    public static void openContactsForResult(Activity context, int requestCode){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 打开电话, 并拨打号码
     *
     * @param activity activity
     * @param number 电话号码
     */
    public static void openPhoneAndCall(Activity activity, String number) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        activity.startActivity(intent);
    }

    /**
     * 打开电话(不直接拨打)
     *
     * @param activity activity
     * @param number 电话号码
     */
    public static void openPhone(Activity activity, String number) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        activity.startActivity(intent);
    }

    /**
     * 打开浏览器
     *
     * @param activity
     * @param url
     */
    public static void openBrowser(Activity activity, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);
    }

}
