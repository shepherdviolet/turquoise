/*
 * Copyright (C) 2015-2017 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.turquoise.uix.slideengine.impl;

import android.content.Context;

import sviolet.turquoise.common.exception.DeprecatedException;
import sviolet.turquoise.uix.slideengine.abs.SlideView;

/**
 * 
 * 多页/多阶段式线性滑动引擎(有惯性, 惯性滑动至停止点)<br>
 * 滑动全程可分为多个等分阶段, 每个阶段停止, 类似于ViewPager<br>
 * <br/>
 * 基本设置:<br/>
 * setStageNum()<br/>
 * setStageRange()<br/>
 * setInitPosition()<br/>
 * setSlidingDirection()<br/>
 * setStageDuration()<br/>
 * <br>
 * {@link SlideView} <br/>
 **************************************************************************************<br>
 * 输出定义::<br>
 * <br>
 * position方向:<br>
 * DIRECTION_LEFT_OR_TOP				:	手势上/左::递增  手势下/右::递减<br>
 * DIRECTION_RIGHT_OR_BOTTOM	: 	手势上/左::递减  手势下/右::递增<br>
 * <br>
 * stage方向:<br>
 * DIRECTION_LEFT_OR_TOP				:	手势上/左::递增  手势下/右::递减<br>
 * DIRECTION_RIGHT_OR_BOTTOM	: 	手势上/左::递减  手势下/右::递增<br>
 * 
 * @author S.Violet
 *
 */

public class LinearStageScrollEngine extends LinearScrollEngine {

	private int stageRange = 0;//一个阶段的滑动距离
	private int stageNum = 2;//等分阶段数
	
	/**
	 * @param context ViewGroup上下文
	 * @param slideView 通知刷新的View
	 */
	public LinearStageScrollEngine(Context context, SlideView slideView) {
		super(context, slideView);
	}

	/**********************************************************
	 * settings
	 */

	/**
	 * 禁用设置最大可滑动范围, 改用setStageNum()和setStageRange()
	 */
	@Override
	@Deprecated
	public void setMaxRange(int maxRange) {
		throw new DeprecatedException("LinearStageScorllEngine use setStageNum() & setStageRange() instead");
	}

	/**
	 *	[基本设置]<br/>
	 * @param stageNum 等分阶段数 [2, maxRange + 1]
	 */
	public void setStageNum(int stageNum) {
		if(stageNum > 1) {
            this.stageNum = stageNum;
        } else {
            this.stageNum = 2;
        }

		//计算最大滑动范围
		range = stageRange * ( stageNum - 1 );
	}

	/**
	 * [基本设置]<br/>
	 * @param stageRange 一个阶段的滑动距离 >=0
	 */
	public void setStageRange(int stageRange) {
		if(stageRange >= 0) {
            this.stageRange = stageRange;
        } else {
            this.stageRange = 0;
        }

		//计算最大滑动范围
		range = stageRange * ( stageNum - 1 );
	}

	/*********************************************************
	 * Override
	 */
	
	/**
	 * 获得当前的阶段(页数)<br>
	 * <br>
	 * position = 0 时 stage = 0;<br>
	 * position = range 时 stage = stageNum - 1<br>
	 */
	@Override
	public float getCurrentStage() {
		if(stageRange > 0) {
            return (float) position / (float) stageRange;
        } else {
            return 0;
        }
	}

	/**
	 * 根据速度计算惯性滑动目标位置(被calculateSlideTarget调用)
	 * 
	 * @param velocity
	 * @return
	 */
	@Override
	protected int calculateSlideTargetByVelocity(int velocity) {
		//计算相邻的两个驻点位置
		int[] arrestPoint = calculateSlideArrestPoint(position);
		int target = ORIGIN_POSITION;
		//根据速度判断目标位置
		if(velocity == 0){
			//速度为0根据距离判断目标
			if(Math.abs(position - arrestPoint[1]) < Math.abs(position - arrestPoint[0])) {
                target = arrestPoint[1];
            } else {
                target = arrestPoint[0];
            }
		}else if(velocity < 0){
			target = arrestPoint[1];
		}else{
			target = arrestPoint[0];
		}
		
		return target;
	}

	/**
	 * 计算惯性滑动时间
	 * 
	 * @param target
	 * @return
	 */
	@Override
	protected int calculateSlideDuration(int target) {
		if(range > 0) {
            return (int) ((float) stageDuration * (float) Math.abs(position - target) / (float) stageRange);
        } else {
            return 0;
        }
	}
	
	/**
	 * 判断当前位置是否在停止点(由checkSlideStop调用)
	 * @return
	 */
	@Override
	protected boolean isOnArrestPoint() {
		//增加条件
		return super.isOnArrestPoint() || 
				stageRange == 0 || 
				position % stageRange == 0;
	}
	
	/**
	 * 获得阶段对应的位置
	 * 
	 * @param stage [0, stageNum - 1]
	 * @return
	 */
	@Override
	public int getPositionOfStage(int stage) {
		return limit(stage, ORIGIN_POSITION, stageNum - 1) * stageRange;
	}
	
	/**
	 * 获得引擎的阶段数<br>
	 * @return
	 */
	@Override
	public int getStageNum(){
		return stageNum;
	}
	
	/*********************************************************
	 * public
	 */
	
	/**
	 * 获得引擎一个阶段滑动范围(距离)
	 * @return
	 */
	public int getStageRange(){
		return stageRange;
	}
	
	/*********************************************************
	 * private
	 */
	
	/**
	 * 计算惯性滑动相邻的两个驻点位置
	 * 
	 * @return [0]下界驻点 [1]上界驻点
	 */
	private int[] calculateSlideArrestPoint(int position){
		int[] arrestPoint = new int[]{ORIGIN_POSITION, ORIGIN_POSITION};
		
		if(stageRange <= 0) {
            return arrestPoint;
        }
		
		//计算上下界的stage
		int lowerLimitStage = limit(position / stageRange, ORIGIN_POSITION, stageNum - 1);
		int higherLimitStage = limit(position / stageRange + 1, ORIGIN_POSITION, stageNum - 1);
		
		//根据stage计算位置
		arrestPoint[0] = getPositionOfStage(lowerLimitStage);
		arrestPoint[1] = getPositionOfStage(higherLimitStage);
		
		return arrestPoint;
	}

}
