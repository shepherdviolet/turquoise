package sviolet.demoa;

import sviolet.turquoise.annotation.ApplicationSettings;
import sviolet.turquoise.annotation.DebugSettings;
import sviolet.turquoise.annotation.ReleaseSettings;
import sviolet.turquoise.app.TApplication;

@ApplicationSettings(
        DEBUG = true //Debug模式, 装载DebugSetting配置
)
//发布配置
@ReleaseSettings(
        enableStrictMode = false,
        enableCrashRestart = true,
        enableCrashHandle = true,
        logTag = "Demoa",
        enableLogDebug = false,
        enableLogInfo = false,
        enableLogError = false
)
//调试配置
@DebugSettings(
        enableStrictMode = true,
        enableCrashRestart = true,
        enableCrashHandle = true,
        logTag = "Demoa",
        enableLogDebug = true,
        enableLogInfo = true,
        enableLogError = true
)
public class MyApplication extends TApplication {

    @Override
    public void onUncaughtException(Throwable ex, boolean isCrashRestart) {
        //TODO 异常处理
    }

}
