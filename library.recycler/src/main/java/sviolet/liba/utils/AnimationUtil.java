package sviolet.liba.utils;

import sviolet.liba.R;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationUtil {

	public static final int TYPE_LEFT_IN_RIGHT_OUT = 0;
	public static final int TYPE_RIGHT_IN_LEFT_OUT = 1;
	public static final int TYPE_LEFT_IN = 2;
	public static final int TYPE_RIGHT_IN = 3;
	public static final int TYPE_LEFT_OUT = 4;
	public static final int TYPE_RIGHT_OUT = 5;
	
	/**
	 * activity之间切换动画
	 **/
	public static void ActivityAnimation(Activity activity, int type) {
		switch(type){
		case TYPE_LEFT_IN_RIGHT_OUT:
			activity.overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
			break;
		case TYPE_RIGHT_IN_LEFT_OUT:
			activity.overridePendingTransition(R.anim.in_from_right,R.anim.out_to_left);
			break;
		case TYPE_LEFT_IN:
			activity.overridePendingTransition(R.anim.in_from_left, R.anim.stay);
			break;
		case TYPE_RIGHT_IN:
			activity.overridePendingTransition(R.anim.in_from_right, R.anim.stay);
			break;
		case TYPE_LEFT_OUT:
			activity.overridePendingTransition(R.anim.stay, R.anim.out_to_left);
			break;
		case TYPE_RIGHT_OUT:
			activity.overridePendingTransition(R.anim.stay, R.anim.out_to_right);
			break;
		}
	}

	/**
	 * 左侧推入动画
	 * */
	public static Animation getAnimationLeftIn(Context context) {
		return AnimationUtils.loadAnimation(context, R.anim.push_left_in);
	}

	/**
	 * 右侧推入动画
	 * */
	public static Animation getAnimationRightIn(Context context) {
		return AnimationUtils.loadAnimation(context, R.anim.push_right_in);
	}

	/**
	 * 左侧推出动画
	 * */
	public static Animation getAnimationLeftOut(Context context) {
		return AnimationUtils.loadAnimation(context, R.anim.push_left_out);
	}

	/**
	 * 右侧推出动画
	 * */
	public static Animation getAnimationRightOut(Context context) {
		return AnimationUtils.loadAnimation(context, R.anim.push_right_out);
	}

	/**
	 * 上侧推出动画
	 * */
	public static Animation getAnimationUpIn(Context context) {
		return AnimationUtils.loadAnimation(context, R.anim.push_up_in);
	}

	/**
	 * 位置移动动画
	 * */
	public static TranslateAnimation getTranslateAnimation(float fromx,
			float tox, float fromy, float toy, long time) {
		TranslateAnimation translateAnimation = new TranslateAnimation(fromx,
				tox, fromy, toy);
		translateAnimation.setDuration(time);
		return translateAnimation;
	}

	/**
	 * 渐变动画
	 * */
	public static ScaleAnimation getScaleAnimation(float fromx, float tox,
			float fromy, float toy, long time) {
		ScaleAnimation scaleAnimation = new ScaleAnimation(fromx, tox, fromy,
				toy);
		scaleAnimation.setDuration(time);
		return scaleAnimation;
	}

	public static void applyRotation(View view, float start, float end,
			float centerx, float centery) {
		// 计算中心点
		float centerX = centerx;
		float centerY = centery;
		Rotate3dAnimation rotation = null;
		rotation = new Rotate3dAnimation(start, end, centerX, centerY, 1.0f,
				false);
		rotation.setDuration(1000);
		rotation.setFillAfter(true);
		rotation.setInterpolator(new DecelerateInterpolator());
		// 开始动画
		view.startAnimation(rotation);
	}
}
