package com.etv.activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.util.APKUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SimpleDateUtil;
import com.etv.util.ViewSizeChange;
import com.etv.util.image.ImageUtil;
import com.etv.util.rxjava.RxLifecycle;
import com.tbruyelle.rxpermissions2.RxPermissions;

import com.ys.etv.R;
import com.ys.model.dialog.OridinryDialog;
import com.ys.model.listener.OridinryDialogClick;

import io.reactivex.functions.Consumer;


/***
 * 程序启动界面
 * 里面涉及到自动登录相关功能
 */
public class StartActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_splash_low);
        MyLog.cdl("===软件启动了===", true);
        AppConfig.isOnline = false;
        initView();
    }


    private void initView() {

        int currentTime = SimpleDateUtil.getHourMin();
        if (currentTime > 234 && currentTime < 330) {
            //进入splash界面自主息屏处理
        } else {
            SharedPerManager.setSleepStatues(false);
        }
        ImageView iv_logo_show = (ImageView) findViewById(R.id.iv_logo_show);
        TextView tv_show_deac = (TextView) findViewById(R.id.tv_show_deac);
        tv_show_deac.setText("启动失败，请安装定制版本");
        ViewSizeChange.setLogoPosition(iv_logo_show);
        int showBggImage = ImageUtil.getShowBggLogo();
        iv_logo_show.setBackgroundResource(showBggImage);
        Log.e("TAG", "initView: "+tv_show_deac );
        AppInfo.startCheckTaskTag = false;
        SharedPerManager.setExitDefault(false);
        //RK的主板才检测定时开关机
        checkCpuModelInfo();
    }

    private void checkCpuModelInfo() {
        String powerOnOffPackageName = "com.adtv";  //定时开关机
        boolean isInstall = APKUtil.ApkState(StartActivity.this, powerOnOffPackageName);
        if (!isInstall) {
            showCpuErrorDialog();
            return;
        }
        checkAppIsSafe();
    }

    /***
     * 检查APP得合法性
     */
    private void checkAppIsSafe() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {  // <6.0
            checkVideoPermission();
        } else {
            MyLog.cdl("====版本过低不用检查权限==权限请求通过");
            startGoToView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Android - 11
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }

    //REQUEST_INSTALL_PACKAGES  获取未知应用权限
    private String[] permissionsGroup = new String[]{
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public void checkVideoPermission() {
        RxPermissions rxPermissions = new RxPermissions(StartActivity.this);
        rxPermissions.request(permissionsGroup)
                .compose(RxLifecycle.bindRxLifecycle(this))
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isTrue) throws Exception {
                        MyLog.cdl("======权限请求通过==" + isTrue);
                        if (isTrue) {
                            startGoToView();
                        } else {
                            finish();
                        }
                    }
                });
    }

    private void startGoToView() {
        AppInfo.PERMISSION_COMPLAIY = true;
//        startActivity(new Intent(StartActivity.this, TestActivity.class));
        startActivity(new Intent(StartActivity.this, SplashLowActivity.class));
        finish();
    }

    /**
     * 提示用户
     */
    private void showCpuErrorDialog() {
        OridinryDialog oridinryDialog = new OridinryDialog(StartActivity.this);
        oridinryDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {
                finish();
            }

            @Override
            public void noSure() {
                finish();
            }
        });
        oridinryDialog.show(getString(R.string.broad_not_allow),
                getString(R.string.close), getString(R.string.close));
    }

}
