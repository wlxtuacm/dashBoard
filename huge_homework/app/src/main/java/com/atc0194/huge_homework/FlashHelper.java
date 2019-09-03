package com.atc0194.huge_homework;

import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;

/**
 * Title:FlashHelper
 * Description:控件闪烁辅助类
 * Created by atc0194
 * Date: 2019/8/22
 */
public class FlashHelper {

    private FlashHelper() {}

    private static class Holder {
        private static final FlashHelper instance = new FlashHelper();
    }

    public static FlashHelper getInstance() {
        return FlashHelper.Holder.instance;
    }


    /**开启View闪烁效果**/
    public void startFlick( View view ) {
        if (null == view) {
            return;
        }
        Animation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(300);
        alphaAnimation.setInterpolator(new LinearInterpolator());
        alphaAnimation.setRepeatCount(Animation.INFINITE);
        alphaAnimation.setRepeatMode(Animation.REVERSE);
        view.startAnimation(alphaAnimation);
    }

    /**取消View闪烁效果**/
    public void stopFlick( View view ) {
        if (null == view) {
            return;
        }
        view.clearAnimation();
    }

}
