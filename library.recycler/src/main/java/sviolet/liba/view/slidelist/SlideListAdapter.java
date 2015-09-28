package sviolet.liba.view.slidelist;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * 
 * SlideList专用适配器
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

/*
 * SlideListItem的XML

<sviolet.lib.android.view.slidelist.SlideListItem
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:sviolet="http://schemas.android.com/apk/res/sviolet.VNote.android"
    android:layout_width="match_parent"
    android:layout_height="@dimen/main_list_list_item_height"
    android:orientation="vertical"
    
    sviolet:slide_slideRange="100dp"
    sviolet:slide_shadowWidth="0.01"
    sviolet:slide_upMoveable="true"
    sviolet:slide_flipSpeed="3"
    sviolet:slide_containerFlipSpeed="0.1"
    sviolet:slide_flipAcceleration="-6"
    sviolet:slide_containerFlipAcceleration="15">
    
    ......

 */

/*
 * SlideListItem对象必须进行.setView(R.id.main_list_list_item_up, R.id.main_list_list_item_down, R.id.main_list_list_item_shadow);
 * 指定上下层和阴影ID
 * 
 * 
 * 	//构造
  	public MyAdapter(Activity activity, ...) {
		super(activity);
		...
	}
 * 
	//holder内部类
	private static class ViewHolder {
		public TextView textView;
	}
*/

/*
 * 	示例:
	@Override
	public View paddingView(final int position, View convertView, ViewGroup parent) {
		SlideListItem view = (SlideListItem)convertView;
		
		if(数据为空)//装载空View, 为了完成初始化
			view = null;
		
		ViewHolder holder;
		if (view == null) {
			holder = new ViewHolder();
			view = (SlideListItem)LayoutInflater.from(getContext()).inflate(R.layout.item, null);
			view.setView(R.id.main_list_list_item_up, R.id.main_list_list_item_down, R.id.main_list_list_item_shadow);
			holder.textView = (TextView) view.findViewById(R.id.up_text);
			holder.downButton = (TextView) view.findViewById(R.id.down_button);
			holder.upButton = (TextView) view.findViewById(R.id.up_button);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		if(数据为空){//装载空View, 为了完成初始化
			view.setVisibility(View.INVISIBLE);
			return view;
		}else{
			view.setVisibility(View.VISIBLE);
		}
		
		//装载数据
		holder.textView.setText(titles.get(position));
		
		//新增插入效果(假设position == 3为新增的项目)
		if(position == 3)
			view.inflation();
		
		//用holder.upButton.setOnClickListener()方法无效
		//先添加上层对象[点击]
		view.addClickableView(new SlideClickableView(R.id.up_button, false) {
			@Override
			public void onClick(View root, View view) {
				System.out.println(position + "upbutton");
			}
			@Override
			public void onAnimation(View root, View view, boolean isPressed) {
				if(isPressed){
					((TextView)view).setTextColor(0xFFFF8080);
				}else{
					((TextView)view).setTextColor(0xFF202020);
				}
			}
		});
		//后添加下层对象[点击]
		view.addClickableView(new SlideClickableView(R.id.down_button, false) {
			@Override
			public void onClick(View root, View view) {
				((SlideListItem)root).contraction(new SlideListItem.ContractionCallBacker() {
					@Override
					public void onContractionFinish() {
						titles.remove(position);
					}
				});
			}
			@Override
			public void onAnimation(View root, View view, boolean isPressed) {
				if(isPressed){
					((TextView)view).setTextColor(0xFFFF8080);
				}else{
					((TextView)view).setTextColor(0xFF202020);
				}
			}
		});
		//先添加长按对象
		view.addClickableView(new SlideClickableView(R.id.item, true) {
			@Override
			public void onClick(View root, View view) {
				System.out.println(position + "long press");
			}
			@Override
			public void onAnimation(View root, View view, boolean isPressed) {
				
			}
		});
		return view;
	}	
 */

/*
 * 若contraction(),	inflation()在执行时异常,由于当时列表为空,SlideListItem无法init()导致
 * 尝试在Adapter中展示若干个空白的项,使得SlideListItem完成初始化
 * 
	@Override
	public int getCount() {
		if(数据为空)//装载空View, 为了完成初始化
			return 10;
		return item.obtainSubItems().size();
	}

 */

/*
 * 阴影布局(阴影条图片)
 * 
    <LinearLayout
        android:id="@+id/main_list_list_item_shadow"
        android:orientation="vertical"
    	android:layout_width="wrap_content"
    	android:layout_height="80dp"
    	android:background="@null"
    	android:baselineAligned="false">
    	
        <ImageView
            android:layout_width="wrap_content"
            android:layout_height="match_parent"
            android:src="@drawable/slide_shadow_right"
            android:scaleType="fitXY"
            android:contentDescription="@null"/>
	</LinearLayout>
 */
public abstract class SlideListAdapter extends BaseAdapter {

	public SlideListItem slidingItem;//当前正在截获事件(滑动中)的Item
	public SlideListItem touchedSlideItem;//最后一次触摸过的Item
	public Context context;

	/**
	 * 需要复写
	 */
	public SlideListAdapter(Activity activity){
		this.context = activity;
	}
	
	public Context getContext(){
		return context;
	}
	
	/**
	 * 不要复写该方法
	 * 1.清除可点击对象
	 * 2.paddingView()
	 * 3.给每个Item持有本Adapter
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if(convertView != null){
			((SlideListItem)convertView).wipeClickableView();//清除可点击对象防止重复添加
		}
		
		convertView = paddingView(position, convertView, parent);

		((SlideListItem)convertView).setAdapter(this);//让Item持有本Adapter
		
		return convertView;
	}

	/**
	 * 初始化View, 建立view与holder的关系(等同于原先getView())
	 */
	public abstract View paddingView(int position, View convertView, ViewGroup parent);
	
//	/**
//	 * 拖动影子的布局ID(R.id.xxx)
//	 * 
//	 * @return
//	 */
//	public abstract int getShadowViewId();
	
//	@SuppressLint("HandlerLeak")
//	final public Handler callBackHandler = new Handler(){
//		@Override
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			switch(msg.what){
//			
//			}
//		}
//		
//	};

}
