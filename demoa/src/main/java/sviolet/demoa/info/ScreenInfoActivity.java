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
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.util.common.CheckUtils;
import sviolet.turquoise.util.droid.MeasureUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

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

    @ResourceId(R.id.screen_info_main_screen_dimension)
    private EditText screenDimensionEditText;
    @ResourceId(R.id.screen_info_main_text)
    private TextView textView;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        initEditText();
        refresh();
    }

    @Override
    protected void afterDestroy() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initEditText(){
        screenDimensionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                refresh();
            }
        });
    }

    /**
     * 刷新
     */
    private void refresh() {
        StringBuilder stringBuilder = new StringBuilder();
        printScreen(stringBuilder);
        textView.setText(stringBuilder.toString());
    }

    /**
     * 输出显示信息
     */
    private void printScreen(StringBuilder stringBuilder){
        int screenWidthPixels = MeasureUtils.getScreenRealWidth(this);
        int screenHeightPixels = MeasureUtils.getScreenRealHeight(this);

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

        stringBuilder.append("\n");
        String screenDimension = screenDimensionEditText.getText().toString();
        if (!CheckUtils.isEmpty(screenDimension)){
            try {
                float screenDimensionFloat = Float.parseFloat(screenDimension);
                float diagonalPixels = (float) Math.sqrt(screenWidthPixels * screenWidthPixels + screenHeightPixels * screenHeightPixels);
                float realDpi = diagonalPixels / screenDimensionFloat;
                stringBuilder.append("\nreal dpi: ");
                stringBuilder.append((int) (realDpi + 0.5f));
                stringBuilder.append(" dot/inch");
                stringBuilder.append("\nreal dpcm: ");
                stringBuilder.append((int)(realDpi / 2.54f + 0.5f));
                stringBuilder.append(" dot/cm");
                stringBuilder.append("\nscale: ");
                stringBuilder.append(MeasureUtils.getDensityDpi(this) / realDpi);
                stringBuilder.append("\n1cm: ");
                stringBuilder.append((int)(realDpi / MeasureUtils.getDensity(this) / 2.54f + 0.5f));
                stringBuilder.append("dp");
            } catch (Exception e){
                TLogger.get(this).e("error while parsing screen dimension", e);
                stringBuilder.append("\nreal dpi: error");
                stringBuilder.append("\nreal dpcm: error");
                stringBuilder.append("\nscale: error");
            }
        }
    }

}
