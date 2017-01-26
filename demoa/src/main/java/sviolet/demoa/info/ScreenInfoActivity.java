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

package sviolet.demoa.info;

import android.os.Bundle;
import android.widget.TextView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.info.view.RulerView;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.util.droid.MeasureUtils;

/**
 * 显示信息
 */
@DemoDescription(
        title = "Screen Info",
        type = "Info",
        info = "Screen info"
)

@ResourceId(R.layout.screen_info_main)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class ScreenInfoActivity extends TActivity {

    @ResourceId(R.id.screen_info_main_text)
    private TextView textView;
    @ResourceId(R.id.screen_info_main_ruler)
    private RulerView rulerView;

    private int centimeterPixels = 0;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        refresh();
    }

    @Override
    protected void afterDestroy() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 刷新
     */
    private void refresh() {
        StringBuilder stringBuilder = new StringBuilder();
        printScreen(stringBuilder);
        textView.setText(stringBuilder.toString());
        rulerView.setCentimeterPixels(centimeterPixels);
    }

    /**
     * 输出显示信息
     */
    private void printScreen(StringBuilder stringBuilder){
        int screenWidthPixels = MeasureUtils.getRealScreenWidth(this);
        int screenHeightPixels = MeasureUtils.getRealScreenHeight(this);
        float screenPhysicalSize = MeasureUtils.getPhysicalScreenSize(this);

        stringBuilder.append("real width: ");
        stringBuilder.append(screenWidthPixels);
        stringBuilder.append("px");
        stringBuilder.append("\nreal height: ");
        stringBuilder.append(screenHeightPixels);
        stringBuilder.append("px");
        stringBuilder.append("\n");
        stringBuilder.append("\ndisplay width: ");
        stringBuilder.append(MeasureUtils.getScreenWidth(this));
        stringBuilder.append("px");
        stringBuilder.append("\ndisplay height: ");
        stringBuilder.append(MeasureUtils.getScreenHeight(this));
        stringBuilder.append("px");
        stringBuilder.append("\nstatus height: ");
        stringBuilder.append(MeasureUtils.getStatusBarHeight(this));
        stringBuilder.append("px");
        stringBuilder.append("\nnavigation height: ");
        stringBuilder.append(MeasureUtils.getNavigationBarHeight(this));
        stringBuilder.append("px");
        stringBuilder.append("\n");
        stringBuilder.append("\ndisplay width: ");
        stringBuilder.append(MeasureUtils.getScreenWidthDp(this));
        stringBuilder.append("dp");
        stringBuilder.append("\ndisplay height: ");
        stringBuilder.append(MeasureUtils.getScreenHeightDp(this));
        stringBuilder.append("dp");
        stringBuilder.append("\n");
        stringBuilder.append("\ndensity: ");
        stringBuilder.append(MeasureUtils.getDensity(this));
        stringBuilder.append("\ndpi: ");
        stringBuilder.append(MeasureUtils.getDensityDpi(this));

        float diagonalPixels = (float) Math.sqrt(screenWidthPixels * screenWidthPixels + screenHeightPixels * screenHeightPixels);
        float realDpi = diagonalPixels / screenPhysicalSize;
        stringBuilder.append("\n");
        stringBuilder.append("\nphysical size: ");
        stringBuilder.append(screenPhysicalSize);
        stringBuilder.append(" inch");
        stringBuilder.append("\nphysical dpi: ");
        stringBuilder.append((int) (realDpi + 0.5f));
        stringBuilder.append(" dot/inch");
        stringBuilder.append("\nphysical dpcm: ");
        centimeterPixels = (int) (realDpi / 2.54f + 0.5f);
        stringBuilder.append(centimeterPixels);
        stringBuilder.append(" dot/cm");
        stringBuilder.append("\nscale: ");
        stringBuilder.append(MeasureUtils.getDensityDpi(this) / realDpi);
        stringBuilder.append("\n1cm: ");
        stringBuilder.append((int) (realDpi / MeasureUtils.getDensity(this) / 2.54f + 0.5f));
        stringBuilder.append("dp");

    }

}
