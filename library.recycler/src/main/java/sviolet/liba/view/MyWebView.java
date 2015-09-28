package sviolet.liba.view;

import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.ValueCallback;
import android.webkit.WebView;

/**
 * (修正三星输入法在WebView中number类型无小数点的bug)<p>
 * (新旧API均用evaluateJavascript()执行JS)
 *
 * @author S.Violet (ZhuQinChao)
 *
 */

public class MyWebView extends WebView {

	private boolean useLoadUrlFunc = false;//使用loadUrl加载JS
	
	public MyWebView(Context context) {
		super(context);
		judgeJSFunc();
	}

	public MyWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		judgeJSFunc();
	}

	public MyWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		judgeJSFunc();
	}

	/**
	 * 判断用哪种方法加载JS(evaluateJavascript/loadUrl)
	 */
	private void judgeJSFunc(){
		if(hasMethod(WebView.class, "evaluateJavascript", new Class[]{String.class, ValueCallback.class})){
			useLoadUrlFunc = false;
		}else{
			useLoadUrlFunc = true;
		}
	}
	
	/**
	 * 判断一个类是否含有一个方法
	 * 
	 * @param c
	 * @param methodName
	 * @param params
	 * @return
	 */
	private boolean hasMethod(Class<?> c, String methodName, Class<?>[] params){
		try {
			Method method = c.getMethod(methodName, params);
			if(method == null)
				return false;
			else
				return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}
	
	/**
	 * 重写方法,捕获服务器传下来的"number"类型,改为"numberDecimal"类型
	 * (三星输入法number类型无小数点,numberDecimal有小数点)
	 * 
	 */
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
		InputConnection connection = super.onCreateInputConnection(outAttrs);
		//判断是否为number类型
		if(outAttrs.inputType != 0 && isContain(outAttrs.inputType,InputType.TYPE_CLASS_NUMBER)){
			outAttrs.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER;
		}
		return connection;
	}
	
	/**
	 * 两个数的二进制位是否为包含与被包含的关系
	 * (用于判断inputType中是否包含Number标志位)
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean isContain(int a,int b){
		int result = a | b;
		if(result == a || result == b)
			return true;
		else
			return false;
	}
	
	/**
	 * 执行JS(旧版本API会自动调用loadUrl,但不会回调resultCallback)
	 */
	@Override
	@SuppressLint("NewApi")
	public void evaluateJavascript(String script,ValueCallback<String> resultCallback) {
		
		if(useLoadUrlFunc){//使用loadUrl方法
			loadUrl("javascript:" + script);
		}else{//尝试使用evaluateJavascript方法
			try{
				super.evaluateJavascript(script, resultCallback);
			}catch(Error e){//调用evaluateJavascript错误,则使用loadUrl
				loadUrl("javascript:" + script);
				useLoadUrlFunc = true;
			}
		}

	}
	
}