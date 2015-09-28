package sviolet.liba.view.slidelist;

import sviolet.liba.view.slidedriver.logic.OnActionListener;
import sviolet.liba.view.slidedriver.logic.OnLongPressListener;
import sviolet.liba.view.slidedriver.logic.OnMoveListener;
import sviolet.liba.view.slidedriver.logic.OnPageChangedListener;
import sviolet.liba.view.slidedriver.logic.OnShortClickListener;
import sviolet.liba.view.slidedriver.logic.SlideClickDispatcher;
import sviolet.liba.view.slidedriver.logic.SlideClickableView;
import sviolet.liba.view.slidedriver.logic.SlideDriver;
import sviolet.liba.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

/**
 * 
 * SlideList专用Item<p>
 * 必须使用setView指定上下层和阴影ID<p>
 * 默认abordTouchEvent=true
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */

public class SlideListItem extends RelativeLayout {

	public static final float FLIP_SPEED = 0.03F;//手指滑动弹射速度
	public static final float CONTAINER_FLIP_SPEED = 0.02F;//抽出/插入效果速度
	public static final float FLIP_ACCELERATION = 1.05F;
	public static final float CONTAINER_FLIP_ACCELERATION = 1.05F;
	
	private SlideDriver containerDriver;
	private SlideDriver driver;
	
	private SlideListAdapter adapter;
	
	private boolean isSizeGot = false;//Item长宽已获取标志
	private float containerHeight = 0;
	private float containerWidth = 0;
	
	private View upView, downView;
	private View shadowView;
	
	//配置
	private int slideRange = -1;
	private int upViewId, downViewId;
	private int shadowViewId;
	private float shadowWidth;
	private boolean upMoveable = true, downMoveable = true;
	private float containerFlipSpeed = CONTAINER_FLIP_SPEED;
	private float flipSpeed = FLIP_SPEED;
	private float containerFlipAcceleration = CONTAINER_FLIP_ACCELERATION;
	private float flipAcceleration = FLIP_ACCELERATION;
	private boolean abordTouchEvent = true;//阻断触摸事件(不分发给子View)
	
	private boolean isInflation = false;//是否启动插入效果
	private boolean isContraction = false;//是否启动抽出效果
	private boolean enableSlide = true;//允许左右滑动
	
	private Object obj;//用于设置参数
	private SlideClickDispatcher slideClickDispatcher = new SlideClickDispatcher(this);
	
	public SlideListItem(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initSetting(context, attrs);
	}

	public SlideListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		initSetting(context, attrs);
	}

	public SlideListItem(Context context) {
		super(context);
	}
	
	/**
	 * 初始化配置
	 * 
	 * @param context
	 * @param attrs
	 */
	private void initSetting(Context context, AttributeSet attrs) {
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SlideListItem);
		slideRange = typedArray.getDimensionPixelSize(R.styleable.SlideListItem_SlideListItem_slideRange, -1);
		flipSpeed = typedArray.getFloat(R.styleable.SlideListItem_SlideListItem_flipSpeed, FLIP_SPEED);
		containerFlipSpeed  = typedArray.getFloat(R.styleable.SlideListItem_SlideListItem_containerFlipSpeed, CONTAINER_FLIP_SPEED);
		flipAcceleration = typedArray.getFloat(R.styleable.SlideListItem_SlideListItem_flipAcceleration, FLIP_ACCELERATION);
		containerFlipAcceleration  = typedArray.getFloat(R.styleable.SlideListItem_SlideListItem_containerFlipAcceleration, CONTAINER_FLIP_ACCELERATION);
		shadowWidth  = typedArray.getFloat(R.styleable.SlideListItem_SlideListItem_shadowWidth, 0.01F);
		upMoveable = typedArray.getBoolean(R.styleable.SlideListItem_SlideListItem_upMoveable, false);
		downMoveable = typedArray.getBoolean(R.styleable.SlideListItem_SlideListItem_downMoveable, false);
		abordTouchEvent = typedArray.getBoolean(R.styleable.SlideListItem_abordTouchEvent, true);
        typedArray.recycle();
	}
	
	/**
	 * 强制初始化(执行init()),
	 * 需在init(slideRange,upViewId,downViewId,upMoveable,downMoveable)设置后执行
	 * 若View未显示则初始化失败
	 */
	public void forceInit(){
		init();
	}
	
	/**
	 * 设置内容(必须设置)
	 * 
	 * @param upViewId
	 * @param downViewId
	 * @param shadowViewId
	 */
	public void setView(int upViewId, int downViewId, int shadowViewId){
		this.upViewId = upViewId;
		this.downViewId = downViewId;
		this.shadowViewId = shadowViewId;
	}
	
	/**
	 * 设置上下View是否可滑动
	 * 
	 * @param up
	 * @param down
	 */
	public void setMoveable(boolean up, boolean down){
		this.upMoveable = up;
		this.downMoveable = down;
	}
	
	/**
	 * 设置弹射动画速度
	 * (设置0为默认)
	 * 
	 * @param slideSpeed 左右滑动弹射速度(默认0.03F)
	 * @param animationSpeed 抽出插入效果速度(默认0.02F)
	 */
	public void setAnimationSpeed(float slideSpeed, float animationSpeed){
		if(slideSpeed != 0)
			this.flipSpeed = slideSpeed;
		if(animationSpeed != 0)
			this.containerFlipSpeed = animationSpeed;
	}
	
	/**
	 * 设置动画加速度
	 * (设置0为默认)
	 * 
	 * @param slideAcceleration 左右滑动加速度(默认1.05F)
	 * @param animationAcceleration 抽出插入效果加速度(默认1.05F)
	 */
	public void setAcceleration(float slideAcceleration, float animationAcceleration){
		if(slideAcceleration != 0)
			this.flipAcceleration = slideAcceleration;
		if(animationAcceleration != 0)
			this.containerFlipAcceleration = animationAcceleration;
	}
	
	/**
	 * 设置阴影效果
	 * 
	 * @param rightShadowViewId
	 */
	public void setShadow(float shadowWidth, int rightShadowViewId){
		this.shadowWidth = shadowWidth;
		this.shadowViewId = rightShadowViewId;
	}
	
	/**
	 * 初始化(实际初始化操作)
	 */
	private void init(){
		if(!isSizeGot){
			this.containerWidth = getWidth();
			this.containerHeight = getHeight();
			if(containerWidth != 0 && containerHeight != 0 && slideRange != -1){
				isSizeGot = true;
				initView();
				initDriver();
				initContainerDriver();
				driver.setXPage(1);
				containerDriver.setPage(1, 1);
				if(isInflation){
					inflation();
				}else if(isContraction){
					contraction();
				}
			}
		}
	}
	
	/**
	 * 初始化View
	 */
	private void initView(){
		containerParams = (AbsListView.LayoutParams)SlideListItem.this.getLayoutParams();
		if(upViewId != 0){
			upView = findViewById(upViewId);
			upViewParams = (RelativeLayout.LayoutParams)upView.getLayoutParams();
		}
		if(downViewId != 0){
			downView = findViewById(downViewId);
			downViewParams = (RelativeLayout.LayoutParams)downView.getLayoutParams();
		}
		if(shadowViewId != 0){
			shadowView = findViewById(shadowViewId);
			shadowView.setVisibility(View.VISIBLE);
			shadowViewParams = (RelativeLayout.LayoutParams)shadowView.getLayoutParams();
		}
	}
	
	AbsListView.LayoutParams containerParams;
	RelativeLayout.LayoutParams upViewParams;
	RelativeLayout.LayoutParams shadowViewParams;
	RelativeLayout.LayoutParams downViewParams;
	
	/**
	 * 初始化插入抽出效果
	 */
	private void initContainerDriver(){
		containerDriver = new SlideDriver(containerWidth, containerHeight - 1);
		containerDriver.setXFlip(Float.MAX_VALUE, containerFlipSpeed * containerWidth);
		containerDriver.setYFlip(Float.MAX_VALUE, containerFlipSpeed * containerHeight);
		containerDriver.setTargetFlipSpeed(containerFlipSpeed * containerWidth, containerFlipSpeed * containerHeight);
		containerDriver.setFlipAcceleration(containerFlipAcceleration * containerWidth, containerFlipAcceleration * containerHeight);
		containerDriver.setOnMoveListener(new OnMoveListener() {
			@Override
			public boolean onMove(float endX, float endY, float distanceX, float distanceY) {
				containerParams.height = (int) (distanceY + containerHeight);
				
				float[] distance = driver.getDistance();
				if(upView != null){
					if(upMoveable){
						upViewParams.leftMargin = (int)(distanceX + distance[0]);
						upViewParams.rightMargin = (int)-(distanceX + distance[0]);
					}else{
						upViewParams.leftMargin = (int)distanceX;
						upViewParams.rightMargin = (int)-distanceX;
					}
				}
				if(shadowView != null){
					if(upMoveable){
						shadowViewParams.leftMargin = (int)(distanceX + containerWidth + distance[0]);
						shadowViewParams.rightMargin = (int)-(distanceX + containerWidth * shadowWidth + distance[0]);
					}else{
						shadowViewParams.leftMargin = (int)(distanceX + containerWidth);
						shadowViewParams.rightMargin = (int)-(distanceX + containerWidth * shadowWidth);
					}
				}
				if(downView != null){
					if(downMoveable){
						downViewParams.leftMargin = (int)(distanceX + distance[0]);
						downViewParams.rightMargin = (int)-(distanceX + distance[0]);
					}else{
						downViewParams.leftMargin = (int)distanceX;
						downViewParams.rightMargin = (int)-distanceX;
					}
				}
				
				SlideListItem.this.requestLayout();
				return true;
			}
		});
		containerDriver.setOnPageChangedListener(new OnPageChangedListener() {
			@Override
			public void onPageChanged(int axis, int page) {
				if(isInflation){//插入效果
					switch(axis){
					case SlideDriver.AXIS_X:
						if(page == 1){
							enableSlide = true;
							isInflation = false;
							adapter.notifyDataSetChanged();
						}
						break;
					case SlideDriver.AXIS_Y:
						if(page == 1){
							if(upView != null){
								upView.setVisibility(View.VISIBLE);
							}
							if(downView != null){
								downView.setVisibility(View.VISIBLE);
							}
							containerDriver.jumpXPage(1);
						}
						break;
					}
				}else if(isContraction){//删除效果
					switch(axis){
					case SlideDriver.AXIS_X:
						if(page == 2){
							containerDriver.jumpYPage(2);
						}
						break;
					case SlideDriver.AXIS_Y:
						if(page == 2){
							enableSlide = true;
							isContraction = false;
							driver.setXPage(1);
							containerDriver.setPage(1, 1);
							adapter.slidingItem = null;
							if(mContractionCallBacker != null){
								mContractionCallBacker.onContractionFinish();
								mContractionCallBacker = null;
							}
							adapter.notifyDataSetChanged();
						}
						break;
					}
				}
			}
		});
	}
	
	/**
	 * 初始化Item手势滑动
	 */
	private void initDriver(){
		driver = new SlideDriver(slideRange, 0);
		driver.setXFlip(Float.MAX_VALUE, flipSpeed * containerWidth);
		driver.setTargetFlipSpeed(flipSpeed * containerWidth, 0);
		driver.setFlipAcceleration(flipAcceleration * containerWidth, 0F);
		driver.setActionDownReturn(true);
		driver.setActionMoveReturn(true);
		driver.setOnMoveListener(new OnMoveListener() {
			@Override
			public boolean onMove(float endX, float endY, float distanceX, float distanceY) {
				if(upView != null && upMoveable){
					upViewParams.leftMargin = (int)distanceX;
					upViewParams.rightMargin = (int)-distanceX;
				}
				if(shadowView != null && upMoveable){
					shadowViewParams.leftMargin = (int)(distanceX + containerWidth);
					shadowViewParams.rightMargin = (int)-(distanceX + containerWidth * shadowWidth);
				}
				if(downView != null && downMoveable){
					downViewParams.leftMargin = (int)distanceX;
					downViewParams.rightMargin = (int)-distanceX;
				}
				SlideListItem.this.requestLayout();
				return true;
			}
		});
		driver.setOnShortClickListener(new OnShortClickListener(){
			@Override
			public boolean onClick(float x, float y) {
				//手动分发点击事件
				if(!slideClickDispatcher.dispatchClick(x, y))
					if(driver.getXPage() == 2){
						driver.jumpXPage(1);
					}
				return true;
			}
		});
		driver.setOnLongPressListener(new OnLongPressListener() {
			@Override
			public void OnLongPress(float x, float y) {
				//手动分发长按事件
				if(!slideClickDispatcher.dispatchLongPress(x, y))
					if(driver.getXPage() == 2){
						driver.jumpXPage(1);
					}
			}
		});
		driver.setOnActionListener(new OnActionListener() {
			@Override
			public void onUp(float x, float y) {
				//重置动画
				slideClickDispatcher.resetAnimation(SlideListItem.this);
			}
			@Override
			public void onMove(float x, float y) {
				//将自己设置为捕获事件的Item
				adapter.slidingItem = SlideListItem.this;
			}
			@Override
			public void onDown(float x, float y) {
				//重置上一个item的动画
				if(getAdapter().touchedSlideItem != null)
					getAdapter().touchedSlideItem.resetAnimation();
				//手动分发按下动画
				slideClickDispatcher.dispatchAnimation(x, y, true);
				//设置本Item为最后触摸的Item
				getAdapter().touchedSlideItem = SlideListItem.this;
			}
		});
		driver.setOnPageChangedListener(new OnPageChangedListener() {
			@Override
			public void onPageChanged(int axis, int page) {
				switch(axis){
				case SlideDriver.AXIS_X:
					if(page == 1){
						adapter.slidingItem = null;
					}
					break;
				case SlideDriver.AXIS_Y:
					
					break;
				}
			}
		});
	}
	
	/**
	 * 设置对应的SlideListAdapter(已由SlideListAdapter调用),
	 * 用于通过SlideListAdapter和SlideListView传递数据
	 * 
	 * @param adapter
	 */
	protected void setAdapter(SlideListAdapter adapter){
		this.adapter = adapter;
	}
	
	public SlideListAdapter getAdapter(){
		return adapter;
	}
	
	/**
	 * 重写onMeasure() 尝试初始化driver
	 * 
	 * @param widthMeasureSpec
	 * @param heightMeasureSpec
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		
		init();//若driver未初始化,则尝试初始化
		
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	/**
	 * 重写事件分发
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		
		init();//若driver未初始化,则尝试初始化
		
		if(driver != null && enableSlide && driver.drive(ev)){
			if(abordTouchEvent)
				return true;
		}
		
		return super.dispatchTouchEvent(ev);
	}
	
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}
	
	/**
	 * Item加入效果(出现)
	 */
	public void inflation(){
		isInflation = true;
		if(containerDriver != null){
			enableSlide = false;
			if(upView != null){
				upView.setVisibility(View.INVISIBLE);
			}
			if(downView != null){
				downView.setVisibility(View.INVISIBLE);
			}
			driver.setXPage(1);
			containerDriver.setPage(2, 2);
			containerDriver.jumpYPage(1);
		}else{
			init();
		}
	}
	
	/**
	 * Item抽出效果(消失),
	 * 
	 * 收缩完毕后,
	 * 自动复位(containerDriver.setPage(1, 1)),
	 * 自动刷新SlideList(adapter.notifyDataSetChanged())
	 * 自动释放adapter.slidingItem = null;
	 * 
	 * 只需手动删除项数据(?.remove(position)),无需调用notifyDataSetChanged()
	 * 
	 */
	private void contraction(){
		isContraction = true;
		if(containerDriver != null){
			enableSlide = false;
			containerDriver.jumpXPage(2);
		}else{
			init();
		}
	}
	
	/**
	 * @param mContractionCallBacker 抽出后回调
	 */
	public void contraction(ContractionCallBacker mContractionCallBacker){
		this.mContractionCallBacker = mContractionCallBacker;
		contraction();
	}
	
	//抽出回调
	private ContractionCallBacker mContractionCallBacker;
	public interface ContractionCallBacker{
		public void onContractionFinish();
	}
	
	/**
	 * 左右滑动复位
	 */
	public void slideBack(){
		driver.jumpXPage(1);
	}
	
	public void setObject(Object obj){
		this.obj = obj;
	}
	
	public Object getObject(){
		return obj;
	}
	
	/**
	 * 务必清理,否则会重复添加()(已由SlideListAdapter调用),
	 * 清除所有可点击对象ClickableViewList & LongPressableViewList
	 */
	protected void wipeClickableView(){
		slideClickDispatcher.wipeClickableView();
	}
	
	/**
	 * 添加一个可点击对象(setOnClickListener()方法无效)
	 * 先添加的对象优先级高,优先级高的对象消费掉onclick事件后,优先级低的对象将不会调用onclick,
	 * 建议先添加上层对象,后添加下层对象,同一个对象同时有点击和长按事件,则需要分别添加
	 * 
	 * @param clickableView
	 */
	public void addClickableView(SlideClickableView clickableView){
		slideClickDispatcher.addClickableView(clickableView);
	}
	
	public void resetAnimation(){
		slideClickDispatcher.resetAnimation(this);
	}
}
