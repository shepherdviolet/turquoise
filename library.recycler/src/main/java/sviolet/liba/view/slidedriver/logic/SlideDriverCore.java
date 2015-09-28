package sviolet.liba.view.slidedriver.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;

/**
 * 翻页驱动<<destroy()销毁>>(核心逻辑,不建议使用,与正常思维逻辑相反)<p>
 * 
 * 利用MotionEvent驱动View的滑动/翻页,务必destroy()销毁<p>
 * 
 * FLIP弹射:松手后自动移动/归位到驻点<p>
 * TargetFlip标的弹射:直接弹射至目标位置<p>
 * 越界:拉到底后继续拖动(<min/>max)<p>
 * 
 * @author S.Violet (ZhuQinChao)
 *
 */
public abstract class SlideDriverCore{
	
	public static final int AXIS_X = 0;
	public static final int AXIS_Y = 1;
	
	public static final int DIRECTION_LEFT = 0;
	public static final int DIRECTION_TOP = 1;
	public static final int DIRECTION_RIGHT = 2;
	public static final int DIRECTION_BOTTOM = 3;
	
	//弹射速度px/s(默认)
	private static final float FLIP_SPEED = 4000f;
	//标的弹射速度px/s(默认)
	private static final float TARGET_FLIP_SPEED = 8000f;
	//弹射动画刷新间隔(ms)
	private static final long FLIP_INTERVAL = 15;
	//判定为近距离的容差范围(点击事件容差)
	private static final float NEAR_RANGE = 5F;
	//长按判定事件
	private static final long LONG_CLICK_TIME = 800;
	
	//当前触点坐标位置(最新一次)
	private float x;
	private float y;
	
	//action_down时触点坐标
	private float startX;
	private float startY;
	
	//上一次action_move时的触点坐标
	private float forwardX;
	private float forwardY;
	
	//位移距离(累计)
	private float distanceX;
	private float distanceY;
	
	//位移上下限
	private float minX;
	private float minY;
	private float maxX;
	private float maxY;
	
	//XY轴方向是否移动(一个down-move-up周期)
	private boolean xMoved = false;
	private boolean yMoved = false;
	
	//XY轴方向是否允许移动
	private boolean xMoveEnable = true;
	private boolean yMoveEnable = true;
	
	//驻点吸力范围(例:100,当前位置离前进方向驻点100以内,弹射到目标驻点,否则回退)
	private float xFlipMagRange;
	private float yFlipMagRange;
	
	//弹射开关
	private boolean xFlipEnable = false;
	private boolean yFlipEnable = false;
	
	//弹射驻点(在这些位置停驻)
	protected List<Float> xFlipPoints = new ArrayList<Float>();
	protected List<Float> yFlipPoints = new ArrayList<Float>();
	
	//XY轴页码
	protected int xPage;
	protected int yPage;
	
	//弹射方向
	private boolean xFlipRight = true;
	private boolean yFlipDown = true;
	
	//弹射速度(设定)
	private float xFlipSpeed = FLIP_SPEED;
	private float yFlipSpeed = FLIP_SPEED;
	//弹射当前速度
	private float xSpeed;
	private float ySpeed;
	//加速度px/s2
	private float xAcceleration;
	private float yAcceleration;
	//标的弹射速度(设定)
	private float xTargetFlipSpeed = TARGET_FLIP_SPEED;
	private float yTargetFlipSpeed = TARGET_FLIP_SPEED;
	//标的弹射当前速度
	private float xTargetSpeed;
	private float yTargetSpeed;
	
	//标的弹射目标(页码)
	private int xFlipTarget = -1;
	private int yFlipTarget = -1;
	
	//是否已截获事件(用于up事件时决定是否拦截)
	private boolean handled = false;
	
	//越界允许
	private boolean overEnable = false;
	//越界阻力
	private float overResistance;
	
	//边界触摸模式(仅接受屏幕边缘开始触发的事件)
	private boolean edgeTouchMode = false;
	//本次是从边缘触发
	private boolean touchFromEdge = false;
	//触发边缘范围
	private float edgeLeftRange;
	private float edgeRightRange;
	private float edgeTopRange;
	private float edgeBottomRange;
	
	//ACTION_DOWN时的默认返回值
	private boolean actionDownReturn;
	//ACTION_MOVE时的默认返回值
	private boolean actionMoveReturn;
	
	//允许/禁止下层控件触发长按事件(第三方用标记)
	private boolean enableOtherLongClick = true;
	
	/**
	 * 计时器:
	 */
	private Timer timer;
	private TimerTask timerTask;
	
	/**
	 * 不建议使用
	 * min=max时禁止该方向移动(且不会截获该方向事件)
	 * 
	 * @param minX 左移动界限
	 * @param maxX 右移动界限
	 * @param minY 上移动界限
	 * @param maxY 下移动界限
	 */
	@Deprecated
	public SlideDriverCore(float minX,float maxX,float minY,float maxY){
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
		reset();
	}
	
	/**
	 * 销毁
	 */
	public void destroy(){
		
		if(timerTask != null){
			timerTask.cancel();
			timerTask = null;
		}
		if(timer != null){
			timer.cancel();
			timer = null;
		}
		callbackHandler.removeCallbacksAndMessages(null);
	}
	
	/**
	 * 重置弹射速度/允许弹射(内部调用)
	 */
	private void resetFliping(){
		callbackHandler.sendEmptyMessage(HANDLER_ON_FLIP_START);
		
		if(timerTask != null){
			timerTask.cancel();
			timerTask = null;
		}
		if(timer != null){
			timer.cancel();
			timer = null;
		}
		
		xStopCounter = 0;
		yStopCounter = 0;
		
		timer = new Timer();
		xSpeed = xFlipSpeed;
		ySpeed = yFlipSpeed;
		xTargetSpeed = xTargetFlipSpeed;
		yTargetSpeed = yTargetFlipSpeed;
	}
	
	/**
	 * 暂停弹射
	 */
	private void pauseFliping(){
		
		if(timerTask != null){
			timerTask.cancel();
			timerTask = null;
		}
		if(timer != null){
			timer.cancel();
			timer = null;
		}
		callbackHandler.sendEmptyMessage(HANDLER_ON_FLIP_STOP);
	}
	
	/**
	 * 
	 * 初始化
	 *
	 */
	public void reset() {
		distanceX = 0;
		distanceY = 0;
		
		xMoved = false;
		yMoved = false;

		xMoveEnable = true;
		yMoveEnable = true;

		xFlipRight = true;
		yFlipDown = true;
		
		xPage = 0;
		yPage = 0;

		xAcceleration = 1;
		yAcceleration = 1;
		
		actionDownReturn = false;
		actionMoveReturn = false;
		
		pauseFliping();
		
		cancelEdgeTouchMode();

		handled = false;

		if (minX == maxX)
			xMoveEnable = false;
		if (minY == maxY)
			yMoveEnable = false;
	}
	
	/**
	 * 取消边界触摸模式
	 */
	public void cancelEdgeTouchMode(){
		edgeTouchMode = false;
		edgeLeftRange = 0;
		edgeRightRange = Float.MAX_VALUE;
		edgeTopRange = 0;
		edgeBottomRange = Float.MAX_VALUE;
	}
	
	/**
	 * 设置为边界触摸模式
	 * 仅捕获/响应从屏幕边缘开始(down)触发的事件
	 * 否则不捕获/响应任何事件
	 * 建议0.05~0.1
	 * 
	 * @param left 建议width * 0.05f
	 * @param top 建议height * 0.05f
	 * @param right 建议width * 0.95f
	 * @param bottom 建议height * 0.95f
	 */
	public void setEdgeTouchMode(float left,float top,float right,float bottom){
		edgeTouchMode = true;
		edgeLeftRange = left;
		edgeTopRange = top;
		edgeRightRange = right;
		edgeBottomRange = bottom;
	}
	
	/**
	 * 设置为边界触摸模式
	 * 仅捕获/响应从屏幕边缘开始(down)触发的事件
	 * 否则不捕获/响应任何事件
	 * 建议0.05~0.1
	 * 
	 * @param width 屏幕宽度 (0:不设置左右边缘)
	 * @param height 屏幕高度 (0:不设置上下边缘)
	 * @param range 边界占比(建议0.05f)
	 */
	public void setEdgeTouchMode(float width, float height, float range){
		edgeTouchMode = true;
		if(width != 0){
			edgeLeftRange = width * range;
			edgeRightRange = width * (1 - range);
		}
		if(height != 0){
			edgeTopRange = height * range;
			edgeBottomRange = height * (1 - range);
		}
	}
	
	/**
	 * 设置越界范围(拖到底时,可再多拖一点的距离,
	 * 通常配合弹射使用,越界后弹回)
	 *
	 * @param overResistance 越界阻力(建议5以上,值越大越难拖动)
	 */
	public void setOverRange(float overResistance){
		this.overEnable = true;
		this.overResistance = overResistance;
	}
	
	/**
	 * 设置XY轴的页码(无动画)
	 * 
	 * @param xPage
	 * @param yPage
	 */
	public void setPage(int xPage, int yPage){
		this.xPage = xPage;
		this.distanceX = xFlipPoints.get(xPage);
		this.yPage = yPage;
		this.distanceY = yFlipPoints.get(yPage);
		callbackHandler.sendEmptyMessage(HANDLER_ON_MOVE);
		callbackHandler.sendEmptyMessage(HANDLER_ON_PAGE_X_CHANGED);
		callbackHandler.sendEmptyMessage(HANDLER_ON_PAGE_Y_CHANGED);
	}
	
	/**
	 * 设置当前X轴上的页码(无动画)
	 * 
	 * @param page
	 */
	public void setXPage(int page){
		xPage = page;
		distanceX = xFlipPoints.get(xPage);
		callbackHandler.sendEmptyMessage(HANDLER_ON_MOVE);
		callbackHandler.sendEmptyMessage(HANDLER_ON_PAGE_X_CHANGED);
	}
	
	/**
	 * 设置当前Y轴上的页码(无动画)
	 * 
	 * @param page
	 */
	public void setYPage(int page){
		yPage = page;
		distanceY = yFlipPoints.get(yPage);
		callbackHandler.sendEmptyMessage(HANDLER_ON_MOVE);
		callbackHandler.sendEmptyMessage(HANDLER_ON_PAGE_Y_CHANGED);
	}
	
	/**
	 * 跳转到X轴的某一页(有动画过程)
	 * 
	 * @param page
	 */
	public void jumpXPage(int page){
		xFlipTarget = page;
		targetFlip(FLIP_INTERVAL);
	}
	
	/**
	 * 跳转到Y轴的某一页(有动画过程)
	 * 
	 * @param page
	 */
	public void jumpYPage(int page){
		yFlipTarget = page;
		targetFlip(FLIP_INTERVAL);
	}
	
	public int getXPage(){
		return xPage;
	}
	
	public int getYPage(){
		return yPage;
	}
	
	/**
	 * 复写该方法改变X值的输入
	 * 
	 * @param event
	 * @return
	 */
	public float onInputX(MotionEvent event){
		return event.getRawX();
	}
	
	/**
	 * 复写该方法改变Y值的输入
	 * 
	 * @param event
	 * @return
	 */
	public float onInputY(MotionEvent event){
		return event.getRawY();
	}

	/**
	 * 设置弹射加速度px/s2
	 * 
	 * @param xAcceleration
	 * @param yAcceleration
	 */
	public void setFlipAcceleration(float xAcceleration, float yAcceleration){
		this.xAcceleration = xAcceleration;
		this.yAcceleration = yAcceleration;
	}
	
	/**
	 * 设置X方向弹射
	 * 
	 * magRange不要大于驻点间隔
	 * 
	 * @param magRange 驻点吸力范围(例:100,当前位置离前进方向驻点100以内,弹射到目标驻点,否则回退)
	 * @param speed 弹射速度px/s
	 */
	public void setXFlip(float magRange, float speed){
		xFlipEnable = true;
		xFlipSpeed = speed;
		xFlipMagRange = magRange;
		xFlipPoints.add(Float.valueOf(minX));
		xFlipPoints.add(Float.valueOf(maxX));
	}
	
	/**
	 * 添加X轴方向上的弹射驻点
	 * (必须增量顺序添加10-20-30,无需设置起点终点)
	 * 
	 * @param flipPoint 驻点位置(请设置在min/max之间)
	 */
	public void addXFlipPoint(float flipPoint){
		xFlipPoints.add(xFlipPoints.size() - 1, Float.valueOf(flipPoint));
	}
	
	/**
	 * 设置Y方向弹射
	 * 
	 * magRange不要大于驻点间隔
	 * 
	 * @param magRange 驻点吸力范围(例:100,当前位置离前进方向驻点100以内,弹射到目标驻点,否则回退)
	 * @param speed 弹射速度px/s
	 */
	public void setYFlip(float magRange, float speed){
		yFlipEnable = true;
		yFlipSpeed = speed;
		yFlipMagRange = magRange;
		yFlipPoints.add(Float.valueOf(minY));
		yFlipPoints.add(Float.valueOf(maxY));
	}
	
	/**
	 * 添加Y轴方向上的弹射驻点
	 * (必须增量顺序添加10-20-30,无需设置起点终点)
	 * 
	 * @param flipPoint 驻点位置(请设置在min/max之间)
	 */
	public void addYFlipPoint(float flipPoint){
		yFlipPoints.add(yFlipPoints.size() - 1, Float.valueOf(flipPoint));
	}
	
	/**
	 * 设置标的弹射速度(通过setX/YPage()方法触发时的弹射速度)
	 * 
	 * @param xTargetFlipSpeed px/s2
	 * @param yTargetFlipSpeed px/s2
	 */
	public void setTargetFlipSpeed(float xTargetFlipSpeed, float yTargetFlipSpeed){
		this.xTargetFlipSpeed = xTargetFlipSpeed;
		this.yTargetFlipSpeed = yTargetFlipSpeed;
	}
	
	/**
	 * 设置ACTION_DOWN时的默认返回值(是否截获事件),默认false
	 * 
	 * @param actionDownReturn
	 */
	public void setActionDownReturn(boolean actionDownReturn){
		this.actionDownReturn = actionDownReturn;
	}
	
	/**
	 * 设置ACTION_MOVE时的默认返回值, 即无效移动时的返回值(移动距离过小,判断为未移动的情况),默认false
	 * 
	 * @param actionMoveReturn
	 */
	public void setActionMoveReturn(boolean actionMoveReturn){
		this.actionMoveReturn = actionMoveReturn;
	}
	
	/**
	 * 获得当前位移情况
	 * 
	 * @return [0]distanceX [1]distanceY
	 */
	public float[] getDistance(){
		float[] result = new float[2];
		result[0] = distanceX;
		result[1] = distanceY;
		return result;
	}
	
	/**
	 * 用event驱动
	 * 
	 * @param event
	 * @return
	 */
	public boolean drive(MotionEvent event) {
		//根据事件计算当前触点XY
		x = onInputX(event);
		y = onInputY(event);
		
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			return onActionDown(x, y);
		case MotionEvent.ACTION_MOVE:
			return onActionMove(x, y);
		case MotionEvent.ACTION_UP:
			return onActionUp(x, y);
		}
		return false;
	}
	
	public boolean onActionDown(float x,float y){
		pauseFliping();
		enableOtherLongClick = true;//允许下层控件触发长按事件
		this.xMoved = false;
		this.yMoved = false;
		this.startX = x;
		this.startY = y;
		this.forwardX = x;
		this.forwardY = y;
		
		if(edgeTouchMode){
			if(x < edgeLeftRange || x > edgeRightRange || y < edgeTopRange || y > edgeBottomRange){
				touchFromEdge = true;
			}else{
				touchFromEdge = false;
				return false;
			}
		}
		
		callbackHandler.sendEmptyMessage(HANDLER_ON_ACTION_DOWN);
		
		longPresslaunch();//长按判定计时开始
		
		return actionDownReturn;
	}
	
	public boolean onActionMove(float x,float y){
		if(edgeTouchMode && !touchFromEdge){
			return false;
		}
		
		//触发了长按事件
		if(isLongPressed){
			if(timer == null)
				flip(FLIP_INTERVAL);
			return longPressReturn;
		}
		
		//至少一个方向上有位移
		if (isMoved(x, startX) || isMoved(y, startY)) {
			//禁止下层控件触发长按事件
			enableOtherLongClick = false;
			//一个方向位移大于另一方向时捕获该方向事件
			//防止异方向比需求方向早位移导致无法拖动需求方向
			if(Math.abs(x - startX) > Math.abs(y - startY)){
				xMoved = true;
			}else{
				yMoved = true;
			}
		}
		if(xMoved && xMoveEnable){
			longPressCancel();//长按判定计时取消
			callbackHandler.sendEmptyMessage(HANDLER_ON_ACTION_MOVE);
			float xStep = x - this.forwardX;
			if(xStep > 0)
				xFlipRight = true;
			else
				xFlipRight = false;
			if(!moveDistanceX(xStep))//OverListener返回false时,Driver不继续处理事件,传递给下层
				return false;
			this.forwardX = x;
			callbackHandler.sendEmptyMessage(HANDLER_ON_MOVE);
			return handled;
		}else if(yMoved && yMoveEnable){
			longPressCancel();//长按判定计时取消
			callbackHandler.sendEmptyMessage(HANDLER_ON_ACTION_MOVE);
			float yStep = y - this.forwardY;
			if(yStep > 0)
				yFlipDown = true;
			else
				yFlipDown = false;
			if(!moveDistanceY(yStep))//OverListener返回false时,Driver不继续处理事件,传递给下层
				return false;
			this.forwardY = y;
			callbackHandler.sendEmptyMessage(HANDLER_ON_MOVE);
			return handled;
		}else{
			return actionMoveReturn;
		}
	}
	
	public boolean onActionUp(float x,float y){
		
		longPressCancel();//长按判定计时取消
		
		//不在有效区内触摸
		if(edgeTouchMode && !touchFromEdge){
			if(timer == null)
				flip(FLIP_INTERVAL);
			return false;
		}
		
		//触发了长按事件
		if(isLongPressed){
			if(timer == null)
				flip(FLIP_INTERVAL);
			callbackHandler.sendEmptyMessage(HANDLER_ON_ACTION_UP);
			return longPressReturn;
		}

		//有效点击事件
		if(!isMoved(x,startX) && !isMoved(y,startY)){
			if(timer == null)
				flip(FLIP_INTERVAL);
			callbackHandler.sendEmptyMessage(HANDLER_ON_ACTION_UP);
			return onClick(x,y);
		}
		
		//有效滑动事件
		if((xMoved && xMoveEnable) || (yMoved && yMoveEnable)){
			if(timer == null)
				flip(FLIP_INTERVAL);
			callbackHandler.sendEmptyMessage(HANDLER_ON_ACTION_UP);
			return handled;
		}
		
		if(timer == null)
			flip(FLIP_INTERVAL);
		
		return false;
	}
	
	/**
	 * 松手弹射(松手手自动移动至驻点)
	 * 
	 * @param interval
	 */
	private void flip(long interval){
		resetFliping();
		refreshPassTime();
		timerTask = new FlipTimerTask();
		timer.schedule(timerTask, 10, interval);
	}
	
	/**
	 * 标的弹射(弹射至目标位置)
	 * 
	 * @param interval
	 */
	private void targetFlip(long interval){
		resetFliping();
		refreshPassTime();
		timerTask = new TargetFlipTimerTask();
		timer.schedule(timerTask, 10, interval);
	}
	
	/**
	 * X轴是否在驻点上
	 * 
	 * @return
	 */
	private boolean isXatPoint(){
		for(int i = 0 ; i < xFlipPoints.size() ; i ++){
			if(distanceX == xFlipPoints.get(i)){
				xPage = i;
				callbackHandler.sendEmptyMessage(HANDLER_ON_PAGE_X_CHANGED);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Y轴是否在驻点上
	 * 
	 * @return
	 */
	private boolean isYatPoint(){
		for(int i = 0 ; i < yFlipPoints.size() ; i ++){
			if(distanceY == yFlipPoints.get(i)){
				yPage = i;
				callbackHandler.sendEmptyMessage(HANDLER_ON_PAGE_Y_CHANGED);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获得当前位置的左右驻点
	 * 
	 * @return [0]左 [1]右
	 */
	private float[] getLRPoint(){
		float point[] = {0,0};
		//超出左边界
		if(distanceX < xFlipPoints.get(0)){
			point[0] = -Float.MAX_VALUE;
			point[1] = xFlipPoints.get(0);
		}
		for(int i = 0 ; i < xFlipPoints.size() ; i ++){
			point[1] =  xFlipPoints.get(i);
			if(distanceX == point[1]){
				point[0] = point[1];
				return point;
			}
			else if(distanceX > point[1]){
				point[0] = point[1];
			}else{
				return point;
			}
		}
		//超出右边界
		point[0] = xFlipPoints.get(xFlipPoints.size() - 1);
		point[1] = Float.MAX_VALUE;
		return point;
	} 
	
	/**
	 * 获得当前位置的上下驻点
	 * 
	 * @return [0]上 [1]下
	 */
	private float[] getTBPoint(){
		float point[] = {0,0};
		//超出上边界
		if(distanceY < yFlipPoints.get(0)){
			point[0] = -Float.MAX_VALUE;
			point[1] = yFlipPoints.get(0);
		}
		for(int i = 0 ; i < yFlipPoints.size() ; i ++){
			point[1] =  yFlipPoints.get(i);
			if(distanceY == point[1]){
				point[0] = point[1];
				return point;
			}
			else if(distanceY > point[1]){
				point[0] = point[1];
			}else{
				return point;
			}
		}
		//超出下边界
		point[0] = yFlipPoints.get(yFlipPoints.size() - 1);
		point[1] = Float.MAX_VALUE;
		return point;
	}

	/**
	 * 根据左右/上下驻点限制距离
	 * 
	 * @param point
	 * @return
	 */
	private float limitByPoint(float distance, float[] point){
		if(distance < point[0]){
			return point[0];
		}else if(distance > point[1]){
			return point[1];
		}
		return distance;
	}
	
	/**
	 * X轴弹射
	 * @return
	 */
	private boolean xFlip(){
		if(!xFlipEnable || isXatPoint()){
			return false;
		}
		float[] LRPoint = getLRPoint();
		
		float step = calculateStep(xSpeed, xAcceleration);
		xSpeed = calculateSpeed(xSpeed, xAcceleration);
		
		if(calculateIfXStop(step))
			return false;
		
		if(LRPoint[0] == Float.MIN_VALUE){//左边越界
			this.distanceX = limitByPoint(this.distanceX + step, LRPoint);
		}else if(LRPoint[1] == Float.MAX_VALUE){//右边越界
			this.distanceX = limitByPoint(this.distanceX - step, LRPoint);
		}else if(xFlipRight){//向右
			if((LRPoint[1] - distanceX) < xFlipMagRange){
				this.distanceX = limitByPoint(this.distanceX + step, LRPoint);
			}else{
				this.distanceX = limitByPoint(this.distanceX - step, LRPoint);
			}
		}else{//向左
			if((distanceX - LRPoint[0]) < xFlipMagRange){
				this.distanceX = limitByPoint(this.distanceX - step, LRPoint);
			}else{
				this.distanceX = limitByPoint(this.distanceX + step, LRPoint);
			}
		}
		return true;
	}
	
	/**
	 * y轴弹射
	 * @return
	 */
	private boolean yFlip(){
		if(!yFlipEnable || isYatPoint()){
			return false;
		}
		float[] TBPoint = getTBPoint();
		
		float step = calculateStep(ySpeed, yAcceleration);
		ySpeed = calculateSpeed(ySpeed, yAcceleration);
		
		if(calculateIfYStop(step))
			return false;
		
		if(TBPoint[0] == Float.MIN_VALUE){//上边越界
			this.distanceY = limitByPoint(this.distanceY + step, TBPoint);
		}else if(TBPoint[1] == Float.MAX_VALUE){//下边越界
			this.distanceY = limitByPoint(this.distanceY - step, TBPoint);
		}else if(yFlipDown){//向下
			if((TBPoint[1] - distanceY) < yFlipMagRange){
				this.distanceY = limitByPoint(this.distanceY + step, TBPoint);
			}else{
				this.distanceY = limitByPoint(this.distanceY - step, TBPoint);
			}
		}else{//向上
			if((distanceY - TBPoint[0]) < yFlipMagRange){
				this.distanceY = limitByPoint(this.distanceY - step, TBPoint);
			}else{
				this.distanceY = limitByPoint(this.distanceY + step, TBPoint);
			}
		}
		
		return true;
	}
	
	/**
	 * X轴标的弹射
	 * @return
	 */
	private boolean xTargetFlip(){
		if(!xFlipEnable || xFlipTarget == -1){
			return false;
		}
		
		if(distanceX == xFlipPoints.get(xFlipTarget)){
			xPage = xFlipTarget;
			xFlipTarget = -1;
			callbackHandler.sendEmptyMessage(HANDLER_ON_PAGE_X_CHANGED);
			return false;
		}
		
		float[] point = {xFlipPoints.get(xFlipTarget),xFlipPoints.get(xFlipTarget)};
		
		float step = calculateStep(xTargetSpeed, xAcceleration);
		xTargetSpeed = calculateSpeed(xTargetSpeed, xAcceleration);
		
		if(calculateIfXStop(step))
			return false;
		
		if(distanceX < xFlipPoints.get(xFlipTarget)){//向右
			point[0] = minX;
			this.distanceX = limitByPoint(this.distanceX + step, point);
		}else{//向左
			point[1] = maxX;
			this.distanceX = limitByPoint(this.distanceX - step, point);
		}
		
		return true;
	}

	/**
	 * y轴标的弹射
	 * @return
	 */
	private boolean yTargetFlip(){
		if(!yFlipEnable || yFlipTarget == -1){
			return false;
		}
		
		if(distanceY == yFlipPoints.get(yFlipTarget)){
			yPage = yFlipTarget;
			yFlipTarget = -1;
			callbackHandler.sendEmptyMessage(HANDLER_ON_PAGE_Y_CHANGED);
			return false;
		}
		
		float[] point = {yFlipPoints.get(yFlipTarget),yFlipPoints.get(yFlipTarget)};
		
		float step = calculateStep(yTargetSpeed, yAcceleration);
		yTargetSpeed = calculateSpeed(yTargetSpeed, yAcceleration);
		
		if(calculateIfYStop(step))
			return false;
		
		if(distanceY < yFlipPoints.get(yFlipTarget)){//向下
			point[0] = minY;
			this.distanceY = limitByPoint(this.distanceY + step, point);
		}else{//向上
			point[1] = maxY;
			this.distanceY = limitByPoint(this.distanceY - step, point);
		}
		
		return true;
	}
	
	/******************************************************
	 * 物理运算
	 ******************************************************/
	
	private long lastTime;//最后一次弹射的时间
	private float passTime;//本次经过的时间
	private int xStopCounter;//X轴一直停滞不动的次数
	private int yStopCounter;//Y轴一直停滞不动的次数
	
	/**
	 * 计算X轴运动是否已经停止(连续50次停滞不动)
	 * @return
	 */
	private boolean calculateIfXStop(float step){
		if(step < 1)
			xStopCounter++;
		if(xStopCounter > 50){
			xStopCounter = 0;
			return true;
		}
		return false;
	}
	
	/**
	 * 计算Y轴运动是否已经停止(连续50次停滞不动)
	 * @return
	 */
	private boolean calculateIfYStop(float step){
		if(step < 1)
			yStopCounter++;
		if(yStopCounter > 50){
			yStopCounter = 0;
			return true;
		}
		return false;
	}
	
	private void refreshPassTime(){
		long thisTime = System.currentTimeMillis();
		passTime = (thisTime - lastTime) / 1000f;
		lastTime = thisTime;
	}
	
	private float calculateSpeed(float speed, float acceleration){
		float _speed = speed + passTime * acceleration;
		if ((_speed >= 0 && speed <= 0) || (_speed <= 0 && speed >= 0)) {
			_speed = 0;
		}
		return _speed;
	}
	
	private float calculateStep(float speed, float acceleration){
		float _passTime = passTime;
		float _stopTime = Math.abs(speed) / Math.abs(acceleration);
		if (_passTime >= _stopTime) {
			_passTime = _stopTime;
		}
		float _step = speed * _passTime + (acceleration * _passTime * _passTime) / 2;
		return _step;
	}
	
	/******************************************************
	 * 回调
	 ******************************************************/
	
	private static final int HANDLER_ON_ACTION_DOWN = 101;
	private static final int HANDLER_ON_ACTION_MOVE = 102;
	private static final int HANDLER_ON_ACTION_UP = 103;
	private static final int HANDLER_ON_FLIP_START = 2;
	private static final int HANDLER_ON_FLIP_STOP = 3;
	private static final int HANDLER_ON_LONG_PRESS = 4;
	private static final int HANDLER_ON_MOVE = 5;
	private static final int HANDLER_ON_PAGE_X_CHANGED = 601;
	private static final int HANDLER_ON_PAGE_Y_CHANGED = 602;
	
	/**
	 * 回调主线程
	 */
	@SuppressLint("HandlerLeak")
	final private Handler callbackHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			synchronized (SlideDriverCore.this) {
				super.handleMessage(msg);
				switch (msg.what) {
				case HANDLER_ON_ACTION_DOWN:
					if (mOnActionListener != null)
						mOnActionListener.onDown(x, y);
					break;
				case HANDLER_ON_ACTION_MOVE:
					if (mOnActionListener != null)
						mOnActionListener.onMove(x, y);
					break;
				case HANDLER_ON_ACTION_UP:
					if (mOnActionListener != null)
						mOnActionListener.onUp(x, y);
					break;
				case HANDLER_ON_FLIP_START:
					if (mOnFlipStartListener != null)
						mOnFlipStartListener.OnFlipStart();
					break;
				case HANDLER_ON_FLIP_STOP:
					if (mOnFlipStopListener != null)
						mOnFlipStopListener.OnFlipStop(distanceX, distanceY);
					break;
				case HANDLER_ON_LONG_PRESS:
					if (mOnLongPressListener != null) {
						isLongPressed = true;
						mOnLongPressListener.OnLongPress(x, y);
					}
					break;
				case HANDLER_ON_MOVE:
					handled = onMove(x, y, distanceX, distanceY);
					break;
				case HANDLER_ON_PAGE_X_CHANGED:
					onPageChanged(AXIS_X, xPage);
					break;
				case HANDLER_ON_PAGE_Y_CHANGED:
					onPageChanged(AXIS_Y, yPage);
					break;
				default:
				}
			}
		}
		
	};
	
	/**
	 * 限制X距离(总范围限制)
	 *
	 * @return true 事件继续由Driver处理 false 本Driver放弃处理事件(向下传递)
	 */
	private boolean moveDistanceX(float step){
		if((distanceX + step) < minX){
			if(mOnOverListener != null){
				if(!mOnOverListener.onOver(DIRECTION_RIGHT))
					return false;
			}
			if(overEnable){
				distanceX = distanceX + step / overResistance;
			}else{
				distanceX = minX;
			}
		}else if((distanceX + step) > maxX){
			if(mOnOverListener != null){
				if(!mOnOverListener.onOver(DIRECTION_LEFT))
					return false;
			}
			if(overEnable){
				distanceX = distanceX + step / overResistance;
			}else{
				distanceX = maxX;
			}
		}else{
			distanceX = distanceX + step;
		}
		return true;
	}
	
	/**
	 * 限制Y距离(总范围限制)
	 *
	 * @return true 事件继续由Driver处理 false 本Driver放弃处理事件(向下传递)
	 */
	private boolean moveDistanceY(float step){
		if((distanceY + step) < minY){
			if(mOnOverListener != null){
				if(!mOnOverListener.onOver(DIRECTION_BOTTOM))
					return false;
			}
			if(overEnable){
				distanceY = distanceY + step / overResistance;
			}else{
				distanceY = minY;
			}
		}else if((distanceY + step) > maxY){
			if(mOnOverListener != null){
				if(!mOnOverListener.onOver(DIRECTION_TOP))
					return false;
			}
			if(overEnable){
				distanceY = distanceY + step / overResistance;
			}else{
				distanceY = maxY;
			}
		}else{
			distanceY = distanceY + step;
		}
		return true;
	}

	/**
	 * 判断是否移动(有容差)
	 * 
	 * @param start 其实位置
	 * @param end 当前位置
	 * @return
	 */
	private boolean isMoved(float start,float end) {
		if (end < (start - NEAR_RANGE) || end > (start + NEAR_RANGE)) {
			return true;
		}
		return false;
	}
	
	/**
	 * (第三方用)长按事件过滤器(标志)
	 * 当SlideDriver ACTION_DOWN但未MOVE时返回true,
	 * 当SlideDriver ACTION_MOVE后返回false
	 * 
	 * 为了解决长按事件从SlideDriver错误的漏到下层控件(即滑动时触发下层OnLongClick),
	 * 开放该方法给下层事件的OnLongClickListener使用,在下层OnLongClick()方法中,增加一层判断即可解决问题
	 * if(SlideDriver.filterLongClickEvent()){
	 * 		//处理长按事件
	 * }
	 * 
	 * @return true 建议触发长按事件 false 不建议触发长按事件
	 */
	public boolean filterLongClickEvent(){
		return enableOtherLongClick;
	}
	
	/******************************************************
	 * 监听器
	 ******************************************************/
	
	OnOverListener mOnOverListener;
	OnFlipStopListener mOnFlipStopListener;
	OnFlipStartListener mOnFlipStartListener;
	OnActionListener mOnActionListener;
	OnLongPressListener mOnLongPressListener;
	
	/**
	 * 越界事件监听器
	 * 
	 * 监听器onOver()返回true时: SlideDriver继续处理事件
	 * 返回false时: SlideDriver抛弃事件,交由下层控件处理
	 * 
	 * Tips:当需要实现整体翻页到底时,能翻动其中的子控件,安装该监听器,返回false,
	 *      在越界发生时,事件即可穿透SlideDriver,分发给下层控件
	 * 
	 * @param mOnOverListener
	 */
	public void setOnOverListener(OnOverListener mOnOverListener){
		this.mOnOverListener = mOnOverListener;
	}
	
	/**
	 * 弹射停止事件监听器
	 * 
	 * @param mOnFlipStopListener
	 */
	public void setOnFlipStopListener(OnFlipStopListener mOnFlipStopListener){
		this.mOnFlipStopListener = mOnFlipStopListener;
	}
	
	/**
	 * 弹射开始事件监听器
	 * 
	 * @param mOnFlipStartListener
	 */
	public void setOnFlipStartListener(OnFlipStartListener mOnFlipStartListener){
		this.mOnFlipStartListener = mOnFlipStartListener;
	}
	
	/**
	 * down move up事件监听器
	 */
	public void setOnActionListener(OnActionListener mOnActionListener){
		this.mOnActionListener = mOnActionListener;
	}
	
	/**
	 * 设置长按事件监听器
	 * 
	 * @param mOnLongPressListener
	 */
	public void setOnLongPressListener(OnLongPressListener mOnLongPressListener){
		this.mOnLongPressListener = mOnLongPressListener;
	}
	
	/**
	 * 当移动时回调
	 * 
	 * 利用endX/Y可使View跟随触点移动
	 * 利用distanceX/Y控制View移动,可实现翻页和弹射
	 * 
	 * @param endX 当前触点坐标(原始数据)
	 * @param endY 当前触点坐标(原始数据)
	 * @param distanceX X方向位移(输出数据)
	 * @param distanceY Y方向位移(输出数据)
	 * @return 返回true截获事件
	 */
	protected abstract boolean onMove(float endX,float endY,float distanceX,float distanceY);
	
	/**
	 * 当点击时回调
	 * 
	 * @param x 当前触点坐标(原始数据)
	 * @param y 当前触点坐标(原始数据)
	 * @return 返回true截获事件
	 */
	protected abstract boolean onClick(float x,float y);
	
	/**
	 * 翻页事件
	 * 
	 * @param axis 轴(0:X/1:Y)
	 * @param page 当前页码
	 */
	protected abstract void onPageChanged(int axis, int page);
	
	/******************************************************
	 * 弹射任务
	 ******************************************************/

	//普通弹射任务
	public class FlipTimerTask extends TimerTask{

		private boolean isCanceled = false;
		
		@Override
		public boolean cancel() {
			isCanceled = true;
			return super.cancel();
		}

		@Override
		public void run() {
			if(!isCanceled){
				refreshPassTime();
				boolean x = xFlip();
				boolean y = yFlip();
				if(x || y){
					callbackHandler.sendEmptyMessage(HANDLER_ON_MOVE);
				}else{
					pauseFliping();
				}
			}
		}
		
	}
	
	//标的弹射任务
	public class TargetFlipTimerTask extends TimerTask{

		private boolean isCanceled = false;
		
		@Override
		public boolean cancel() {
			isCanceled = true;
			return super.cancel();
		}

		@Override
		public void run() {
			if(!isCanceled){
				refreshPassTime();
				boolean x = xTargetFlip();
				boolean y = yTargetFlip();
				if(x || y){
					callbackHandler.sendEmptyMessage(HANDLER_ON_MOVE);
				}else{
					pauseFliping();
				}
			}
		}
		
	}
	
	/******************************************************
	 * 长按事件处理
	 ******************************************************/
	
	private Timer longPressTimer;
	private TimerTask longPressTimerTask;
	private boolean isLongPressed = false;
	private boolean longPressReturn = true;
	
	/**
	 * 长按判定计时开始
	 */
	private void longPresslaunch(){
		longPressCancel();
		
		if(mOnLongPressListener != null){
			isLongPressed = false;
			longPressTimer = new Timer();
			longPressTimerTask = new TimerTask() {
				@Override
				public void run() {
					callbackHandler.sendEmptyMessage(HANDLER_ON_LONG_PRESS);
				}
			};
			longPressTimer.schedule(longPressTimerTask, LONG_CLICK_TIME);
		}
	}
	
	/**
	 * 长按判定计时取消
	 */
	private void longPressCancel(){
		if(longPressTimer != null){
			longPressTimer.cancel();
			longPressTimer = null;
		}
	}
	
	/**
	 * 设置长按事件触发后, driver对UP前剩余事件的返回值(是否截获事件),默认true
	 * 
	 * @param value
	 */
	public void setLongPressReturn(boolean value){
		this.longPressReturn = value;
	}
}
