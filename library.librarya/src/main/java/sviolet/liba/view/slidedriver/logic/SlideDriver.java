package sviolet.liba.view.slidedriver.logic;


/**
 * 翻页驱动<<destroy()销毁>><p>
 * 
 * (经过反向包装,与正常思维方式相同,
 * page为View从左往右1、2、3...)<p>
 * 
 * 利用MotionEvent驱动View的滑动/翻页,务必destroy()销毁<p>
 * 
 * FLIP弹射:松手后自动移动/归位到驻点<p>
 * TargetFlip标的弹射:直接弹射至目标位置<p>
 * 越界:拉到底后继续拖动(<min/>max)<p>
 * 
 * @author S.Violet (ZhuQinChao)
 */

/* 
 * 务必destroy()销毁
 * 
 * 用法:
 * 1.创建对象:new PageTouchDriver(float minX,float maxX,float minY,float maxY){...}
 * 2.设置弹射方向:setXFlip(?)/setYFlip(?) (驻点吸力范围必须小于驻点间隔)
 * 3.添加驻点:addXFlipPoint(?)/addXFlipPoint(?); (起点/终点默认为驻点,无需添加)
 * 4.在程序事件回调处用drive(MotionEvent)方法驱动
 * 5.输出结果回调onMove() (同时设置View的left/rightMargin可避免View被压扁)
 * 
 * 其他:
 * 1.setX/YPage(),设置当前页码(无动画)
 * 2.jumpX/YPage(),设置当前页码(有弹射动画)
 * 3.setOverRange()设置越界范围
 * 4.setEdgeTouchMode() 设置仅捕捉边界触摸
 * 5.setFlipAcceleration(-width*4F, -width*4F) 设置弹射加速度px/s2
 * 6.setNoMinSpeed() 设置为无速度下限模式(速度减至0停止弹射)
 * 7.setActionDownReturn() 设置ACTION_DOWN时的返回值(用于截获事件)
 * 8.setActionMoveReturn() 设置ACTION_MOVE时的默认返回值, 即无效移动时的返回值(移动距离过小,判断为未移动的情况)
 * 9.setLongPressListener() 设置长按事件监听
 * 10.setLongPressReturn() 设置长按事件触发后, driver对UP前剩余事件的返回值(是否截获事件),默认true
 * 11.getDistance() 获取当前位移量
 * 
 * Tips:当需要实现整体翻页到底时,能翻动其中的子控件,安装onOverListener监听器,返回false,
 *      在越界发生时,事件即可穿透SlideDriver,分发给下层控件
 * 
 * Tips:为了解决滑动时触发下层OnLongClick,在下层OnLongClickListener - OnLongClick()方法中,
 * 		增加一层判断即可解决问题:
 * 		if(SlideDriver.filterLongClickEvent()){
 * 			//处理长按事件
 *		}
 */

/* 例:
		mainView = findViewById(R.id.main);
		//建议params为成员变量
		listViewParams = (RelativeLayout.LayoutParams) listView.getLayoutParams();
		menuViewParams = (RelativeLayout.LayoutParams) menuView.getLayoutParams();
		shadowViewParams = (RelativeLayout.LayoutParams)shadowView.getLayoutParams();
		
		menuViewParams.rightMargin = (int)(windowWidth * (1 - MENU_WIDTH));
		menuView.setLayoutParams(menuViewParams);
		
		slideDriver = new SlideDriver(windowWidth * MENU_WIDTH, 0);
		slideDriver.setActionDownReturn(true);
		slideDriver.setXFlip(windowWidth * MENU_WIDTH, windowWidth * FLIP_SPEED);
		slideDriver.setTargetFlipSpeed(windowWidth * FLIP_SPEED, windowWidth * FLIP_SPEED);
		slideDriver.setFlipAcceleration(windowWidth * FLIP_ACCELERATION, windowWidth * FLIP_ACCELERATION);
		slideDriver.setOnMoveListener(new OnMoveListener() {
			@Override
			public boolean onMove(float endX, float endY, float distanceX, float distanceY) {
				//复用params
				listViewParams.leftMargin = (int)(distanceX + windowWidth * MENU_WIDTH);
				listViewParams.rightMargin = (int)-(distanceX + windowWidth * MENU_WIDTH);
				menuViewParams.leftMargin = (int)(distanceX / 3);
				menuViewParams.rightMargin = (int)(-distanceX / 3 + windowWidth * (1 - MENU_WIDTH));
				shadowViewParams.leftMargin = (int)(distanceX + windowWidth * MENU_WIDTH - windowWidth * SHADOW_WIDTH);
				shadowViewParams.rightMargin = (int)(-distanceX + windowWidth * (1 - MENU_WIDTH));
				//调父控件或任意一个子控件的requestLayout()即可刷新显示
				mainView.requestLayout();
				return true;
			}
		});
		slideDriver.setOnPageChangedListener(new OnPageChangedListener() {
			@Override
			public void onPageChanged(int axis, int page) {
				slidePage = page;
				if(axis == SlideDriver.AXIS_X)
					switch(page){
					case 1:
						slideDriver.cancelEdgeTouchMode();
						slideDriver.setEdgeTouchMode(0, 0, windowWidth * MENU_WIDTH, windowHeight);
						break;
					case 2:
						slideDriver.cancelEdgeTouchMode();
						slideDriver.setEdgeTouchMode(windowWidth * 0.1F, 0, windowWidth, windowHeight);
						break;
					}
			}
		});
		slideDriver.setOnShortClickListener(new OnShortClickListener(){
			@Override
			public boolean onClick(float endX, float endY) {
				if(slidePage == 1){
					slideDriver.jumpXPage(2);
					return true;
				}
				return false;
			}
			
		});
		slideDriver.setOnFlipStartListener(new OnFlipStartListener(){
			@Override
			public void OnFlipStart() {
				slideDriver.cancelEdgeTouchMode();	
			}
		});
//		//抛弃并向下分发越界事件
//		slideDriver.setOnOverListener(new OnOverListener() {
//			@Override
//			public boolean onOver(int overDirection) {
//				return false;
//			}
//		});
		slideDriver.setXPage(1);
 */

/* 例:
		@Override
		public boolean dispatchTouchEvent(MotionEvent ev) {
			if(slideDriver.drive(ev))
				return true;
			
			return super.dispatchTouchEvent(ev);
		}
 */

@SuppressWarnings("deprecation")
public class SlideDriver extends SlideDriverCore {
	
	public SlideDriver(float xRange, float yRange) {
		super(-xRange, 0, -yRange, 0);
		xPage = 1;
		yPage = 1;
	}
	
	@Override
	public int getXPage() {
		return xFlipPoints.size() - super.getXPage();
	}

	@Override
	public int getYPage() {
		return yFlipPoints.size() - super.getYPage();
	}

	@Override
	public void jumpXPage(int page) {
		if(page < 1 || page > xFlipPoints.size())
			return;
		super.jumpXPage(xFlipPoints.size() - page);
	}

	@Override
	public void jumpYPage(int page) {
		if(page < 1 || page > yFlipPoints.size())
			return;
		super.jumpYPage(yFlipPoints.size() - page);
	}

	public void setPage(int xPage, int yPage){
		if(xPage < 1 || xPage > xFlipPoints.size())
			return;
		if(yPage < 1 || yPage > yFlipPoints.size())
			return;
		super.setPage(xFlipPoints.size() - xPage, yFlipPoints.size() - yPage);
	}
	
	@Override
	public void setXPage(int page) {
		if(page < 1 || page > xFlipPoints.size())
			return;
		super.setXPage(xFlipPoints.size() - page);
	}

	@Override
	public void setYPage(int page) {
		if(page < 1 || page > yFlipPoints.size())
			return;
		super.setYPage(yFlipPoints.size() - page);
	}

	@Override
	public void addXFlipPoint(float flipPoint) {
		xFlipPoints.add(1, Float.valueOf(-flipPoint));
		xPage = xFlipPoints.size() - 1;
	}

	@Override
	public void addYFlipPoint(float flipPoint) {
		yFlipPoints.add(1, Float.valueOf(-flipPoint));
		yPage = yFlipPoints.size() - 1;
	}

	/******************************************************
	 * 监听器
	 ******************************************************/
	
	OnShortClickListener mOnShortClickListener;
	OnMoveListener mOnMoveListener;
	OnPageChangedListener mOnPageChangedListener;
	
	public void setOnMoveListener(OnMoveListener mOnMoveListener){
		this.mOnMoveListener = mOnMoveListener;
	}
	
	public void setOnShortClickListener(OnShortClickListener mOnShortClickListener){
		this.mOnShortClickListener = mOnShortClickListener;
	}
	
	public void setOnPageChangedListener(OnPageChangedListener mOnPageChangedListener){
		this.mOnPageChangedListener = mOnPageChangedListener;
	}
	
	@Override
	protected boolean onMove(float endX, float endY, float distanceX,
			float distanceY) {
		if(mOnMoveListener != null)
			return mOnMoveListener.onMove(endX, endY, distanceX, distanceY);
		return false;
	}

	@Override
	protected boolean onClick(float endX, float endY) {
		if(mOnShortClickListener != null)
			return mOnShortClickListener.onClick(endX, endY);
		return false;
	}

	@Override
	protected void onPageChanged(int axis, int page) {
		if(mOnPageChangedListener != null){
			if(axis == AXIS_X)
				mOnPageChangedListener.onPageChanged(axis,xFlipPoints.size() - page);
			else if(axis == AXIS_Y)
				mOnPageChangedListener.onPageChanged(axis,yFlipPoints.size() - page);
		}
	}
}
