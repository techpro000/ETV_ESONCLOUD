package com.etv.util.guardian;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.entity.RawSourceEntity;
import com.etv.http.util.FileRawWriteRunnable;
import com.etv.listener.WriteSdListener;
import com.etv.service.EtvService;
import com.etv.util.APKUtil;
import com.etv.util.MyLog;
import com.etv.util.RootCmd;
import com.etv.util.SharedPerManager;
import com.etv.util.system.CpuModel;
import com.ys.etv.R;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 安装守护进程方法
 * InstallGuardianUtil
 */
public class GuardianUtil {

    Context context;

    public GuardianUtil(Context context) {
        this.context = context;
    }

    /***
     * 设置所有的触发IO操作
     * @param context
     * @param enable
     * 是否打开GPIO操作
     * @param ioNum
     * IO选择
     *0  IO-1
     *1  IO-2
     *2  IO-3
     *3  IO-4
     * @param isBack
     * 是否开启消息反向
     * @param speed
     * 提交返回的速度
     *0   慢
     *1   中
     *2   快
     *  @param openPermission
     *  是否开启权限检查
     */
    public static void setIoTriggleAllInfo(Context context, boolean enable, int ioNum, boolean isBack, int speed, boolean openPermission) {
        Intent intent = new Intent(AppInfo.SET_LISTENER_PERSON_INFO);
        intent.putExtra(AppInfo.SET_TRIGGLE_SPEED, speed);
        intent.putExtra(AppInfo.SET_LISTENER_MESSAGE_BACK, isBack);
        intent.putExtra(AppInfo.SET_LISTENER_PERSON_IO_CHOOICE, ioNum);
        intent.putExtra(AppInfo.SET_LISTENER_PERSON_OPEN, enable);
        intent.putExtra(AppInfo.SET_LISTENER_IO_PERMISSION, openPermission);
        context.sendBroadcast(intent);
    }

    /***
     *0   慢
     *1   中
     *2   快
     ** 设置IO触发响应速度
     * @param context
     * @param speed
     */
    public static void setIoTriggleSpeed(Context context, int speed) {
        Intent intent = new Intent(AppInfo.SET_TRIGGLE_SPEED);
        intent.putExtra(AppInfo.SET_TRIGGLE_SPEED, speed);
        context.sendBroadcast(intent);
    }

    /***
     * 设置触发通知反向
     * @param context
     * @param isBack
     */
    public static void modifyIoMessageBack(Context context, boolean isBack) {
        Intent intent = new Intent(AppInfo.SET_LISTENER_MESSAGE_BACK);
        intent.putExtra(AppInfo.SET_LISTENER_MESSAGE_BACK, isBack);
        context.sendBroadcast(intent);
    }

    /***
     * 修改触发得IO
     * @param context
     * @param ioNum
     *0  IO-1
     *1  IO-2
     *2  IO-3
     *3  IO-4
     */
    public static void modifyIoChooiceNum(Context context, int ioNum) {
        Intent intent = new Intent(AppInfo.SET_LISTENER_PERSON_IO_CHOOICE);
        intent.putExtra(AppInfo.SET_LISTENER_PERSON_IO_CHOOICE, ioNum);
        context.sendBroadcast(intent);
    }

    /***
     * 修改人体感应开关
     * @param context
     * @param enable
     */
    public static void modifyIoCheckStatues(Context context, boolean enable) {
        Intent intent = new Intent(AppInfo.SET_LISTENER_PERSON_OPEN);
        intent.putExtra(AppInfo.SET_LISTENER_PERSON_OPEN, enable);
        context.sendBroadcast(intent);
    }

    /**
     * 修改守护进程得状态
     *
     * @param context
     * @param enable
     */
    public static void modifyGuardianStatues(Context context, boolean enable) {
        Intent intent = new Intent(AppInfo.CHANGE_GUARDIAN_STATUES);
        intent.putExtra(AppInfo.CHANGE_GUARDIAN_STATUES, enable);
        context.sendBroadcast(intent);
    }

    /***
     * 修改守护进程的包名
     */
    public void setGuardianPackageName() {
        String packageName = SharedPerManager.getPackageNameBySp();
        MyLog.guardian("======packageName===" + packageName);
        Intent intent = new Intent(AppInfo.MODIFY_GUARDIAN_PACKAGENAME);
        intent.putExtra("packageName", packageName);
        context.sendBroadcast(intent);
    }

    /***
     * 设置守护进程得时间
     * @param context
     * @param daemonProcessTime
     * 单位  秒
     */
    public static void setGuardianProjectTime(Context context, String daemonProcessTime) {
        if (context == null) {
            return;
        }
        if (daemonProcessTime == null || daemonProcessTime.contains("null")) {
            return;
        }
        if (daemonProcessTime.length() < 1) {
            return;
        }
        try {
            int guardianTime = Integer.parseInt(daemonProcessTime);
            if (guardianTime < 15) {
                return;
            }
            Intent intent = new Intent(AppInfo.MODIFY_GUARDIAN_TIME);
            intent.putExtra(AppInfo.MODIFY_GUARDIAN_TIME, guardianTime * 1000);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改守护进程的状态
     * true 打开
     * false 关闭
     *
     * @param b
     */
    public static void setGuardianStaues(Context context, boolean b) {
        Intent intent = new Intent(AppInfo.CHANGE_GUARDIAN_STATUES);
        intent.putExtra(AppInfo.CHANGE_GUARDIAN_STATUES, b);
        context.sendBroadcast(intent);
    }

    /***
     * 修改开机启动开关
     * @param context
     * @param isOpen
     */
    public static void setGuardianPowerOnStart(Context context, boolean isOpen) {
        Intent intent = new Intent(AppInfo.MODIFY_GUARDIAN_POWER_START_ETV);
        intent.putExtra(AppInfo.MODIFY_GUARDIAN_POWER_START_ETV, isOpen);
        context.sendBroadcast(intent);
    }


    /***
     * 通知守护进程，自己是那个公司的
     * @param context
     */
    public static void sendBroadToGuardianConpany(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction(AppInfo.CHANGE_ORDER_COMPANY);
            intent.putExtra(AppInfo.CHANGE_ORDER_COMPANY, AppConfig.APP_TYPE);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startInstallGuardian() {
        if (CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_MTK_M11)) {
            MyLog.guardian("MTK 不安装守护进程");
            return;
        }
        boolean isApkInstall = APKUtil.ApkState(context, AppInfo.GUARDIAN_PACKAGE_NAME);
        RawSourceEntity rawSourceEntity = getResourceGuardianEntity();
        if (rawSourceEntity == null) {
            return;
        }
        MyLog.guardian("守护进程是否安装: " + isApkInstall + " / " + rawSourceEntity);
        if (isApkInstall) {  //已经安装就去检查版本号
            checkGuardianAppVersion(rawSourceEntity);
        } else {   //没有安装，直接安装
            installGUardianApp(rawSourceEntity);
        }
    }

    /***
     * 检查版本号，需不需要升级
     */
    private void checkGuardianAppVersion(RawSourceEntity rawSourceEntity) {
        try {
            MyLog.guardian("==程序已经安装===");
            String guardianPackName = AppInfo.GUARDIAN_PACKAGE_NAME;
            int guardianCode = APKUtil.getOtherAppVersion(context, guardianPackName);
            int updateCode = rawSourceEntity.getApkVersion();
            if (updateCode > guardianCode) {
                installGUardianApp(rawSourceEntity);
            }
            MyLog.guardian("==程序已经安装00===" + guardianCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * anzhuang
     */
    private void installGUardianApp(RawSourceEntity rawSourceEntity) {
        MyLog.guardian("==获取的raw守护信息===" + rawSourceEntity.toString());
        int rourseId = rawSourceEntity.getRawId();
        long fileLength = rawSourceEntity.getFileLength();
        String savePath = AppInfo.BASE_APK() + "/" + AppInfo.GUARDIAN_APP_NAME;
        FileRawWriteRunnable runnable = new FileRawWriteRunnable(context, rourseId, savePath, fileLength, new WriteSdListener() {
            @Override
            public void writeProgress(int progress) {
                MyLog.guardian("===progress===" + progress);
            }

            @Override
            public void writeSuccess(String savePath) {
                File file = new File(savePath);
                MyLog.guardian("守护进程写入成功suucess==" + savePath + " / " + (file.exists()));
                RootCmd.writeFileToSystemApp(savePath, "/system/app/guardian.apk");
            }

            @Override
            public void writrFailed(String errorDesc) {
                MyLog.guardian("writrFailed==" + errorDesc);
            }
        });
        runnable.setIdDelOldFile(true);
        EtvService.getInstance().executor(runnable);
    }

    private RawSourceEntity getResourceGuardianEntity() {
        RawSourceEntity rawSourceEntity = null;
        try {
            List<RawSourceEntity> lists = new ArrayList<RawSourceEntity>();
            lists.add(new RawSourceEntity(R.raw.guardian_44, 3359565, "4.4通用版本", 55));         //4.4 系统通用       0
            lists.add(new RawSourceEntity(R.raw.guardian_51, 1624725, "5.1通用版本", 43));         //5.0 系统通用       1
            lists.add(new RawSourceEntity(R.raw.guardian_71, 3384046, "7.0通用版本", 55));         // 7.1 系统通用       2
            lists.add(new RawSourceEntity(R.raw.guardian_gt_81, 1597440, "8.1高通通用版本", 43));   //高通8.1版本      3
            lists.add(new RawSourceEntity(R.raw.guardian_3568, 1615652, "朗国主板", 43));            //朗国主板          4
            lists.add(new RawSourceEntity(R.raw.guardian_3568, 1593887, "视美泰", 43));             //视美泰A20  4.2    5
            lists.add(new RawSourceEntity(R.raw.guardian_81, 3389505, "8.1通用版本", 48));         //8.1 PX30 系统通用         6
            lists.add(new RawSourceEntity(R.raw.guardian_91, 3366542, "9.0", 71));                //a88 9.0版本             7
            lists.add(new RawSourceEntity(R.raw.guardian_mlogic91, 3365824, "mlogic9.0", 47));    //MLOCIC_91              8
            String cpuModel = CpuModel.getMobileType();
            MyLog.guardian("=====获取守护进程CpuModel==" + cpuModel);
            if (cpuModel.contains(CpuModel.CPU_MODEL_3568_11)) {
                // 3568
                rawSourceEntity = new RawSourceEntity(R.raw.guardian_3568, 3394812, "3568-android-3568", 76);
                return rawSourceEntity;
            }
            if (cpuModel.contains(CpuModel.CPU_MODEL_MLOGIC)) {
                rawSourceEntity = lists.get(8);
            } else if (cpuModel.contains(CpuModel.CPU_MODEL_WING)) {
                rawSourceEntity = lists.get(5);
            } else if (cpuModel.contains(CpuModel.CPU_MODEL_PX30)) {
                rawSourceEntity = lists.get(6);
            } else if (cpuModel.contains(CpuModel.CPU_MODEL_RK_DEFAULT)) {
                int sdkCode = Build.VERSION.SDK_INT;
                MyLog.guardian("==当前SDK 得版本===" + sdkCode);
                if (sdkCode > Build.VERSION_CODES.Q) {
                    // 11.0
                    rawSourceEntity = lists.get(9);
                } else if ((sdkCode > Build.VERSION_CODES.P || sdkCode == Build.VERSION_CODES.P) && sdkCode < Build.VERSION_CODES.Q) {
                    //9.0	28	Pie (Android P)
                    rawSourceEntity = lists.get(7);
                } else if (sdkCode > Build.VERSION_CODES.O) {
                    // 8.0
                    rawSourceEntity = lists.get(6);
                } else if (sdkCode > Build.VERSION_CODES.N && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    // 7.0系统需要系统签名
                    rawSourceEntity = lists.get(2);
                } else if (sdkCode > Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    // 5<Code<6.0  //需要系统签名
                    rawSourceEntity = lists.get(1);
                } else if (sdkCode > Build.VERSION_CODES.JELLY_BEAN && sdkCode < Build.VERSION_CODES.LOLLIPOP) {
                    //4.0~5.0
                    rawSourceEntity = lists.get(0);
                }
            }
        } catch (Exception e) {
            MyLog.guardian("=====获取守护进程error==" + e.toString());
            e.printStackTrace();
        }
        return rawSourceEntity;
    }

//    public void startHdmInService() {
//        boolean isSuportHdmi = SharedPerManager.getIfHdmiInSuport();
//        if (!isSuportHdmi) {
//            return;
//        }
//        try {
//            String cpuModel = CpuModel.getMobileType();
//            if (!cpuModel.contains("rk3288")) {
//                return;
//            }
//            boolean isApkInstall = APKUtil.ApkState(context, AppInfo.HDMI_IN_PACKAGE_NAME);
//            if (!isApkInstall) {
//                return;
//            }
//            Intent intent = new Intent();
//            intent.setAction(AppInfo.HDMI_IN_SERVICE_NAME);  //应用在清淡文件中注册的action
//            intent.setPackage(AppInfo.HDMI_IN_PACKAGE_NAME); //应用程序的包名
//            context.startService(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


    /***
     * 启动守护进程
     * @param contextOnly
     */
    public static void startGuardianService(Context contextOnly) {
        try {
            boolean isApkInstall = APKUtil.ApkState(contextOnly, AppInfo.GUARDIAN_PACKAGE_NAME);
            if (!isApkInstall) {
                return;
            }
            Intent intent = new Intent();
            intent.setAction("com.guardian.service.GuardianService");  //应用在清淡文件中注册的action
            intent.setPackage(AppInfo.GUARDIAN_PACKAGE_NAME); //应用程序的包名
            contextOnly.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 截图代码
     */
    public static void getCaptureImage(Context context, int screenWidth, int screenHeight, String tag) {
        try {
            Intent intent = new Intent();
            intent.putExtra("screenWidth", screenWidth);
            intent.putExtra("screenHeight", screenHeight);
            intent.putExtra("tag", tag);
            intent.setAction(AppInfo.CAPTURE_IMAGE_RECEIVE);
            context.sendBroadcast(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void gotoGuardianApp(Context context) {
        String packageName = "com.guardian";
        boolean isInstall = APKUtil.ApkState(context, packageName);
        if (!isInstall) {
            return;
        }
        Intent intent = new Intent();
        ComponentName cmp = new ComponentName("com.guardian", "com.guardian.ui.MainActivity");
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(cmp);
        context.startActivity(intent);
    }

}
