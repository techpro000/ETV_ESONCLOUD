package com.etv.service.parsener;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.db.DbStatiscs;
import com.etv.entity.StatisticsEntity;
import com.etv.police.activity.PoliceCacheActivity;
import com.etv.service.EtvService;
import com.etv.service.TaskWorkService;
import com.etv.service.util.EtvServerModule;
import com.etv.service.util.EtvServerModuleImpl;
import com.etv.task.activity.PlayTaskTriggerActivity;
import com.etv.task.activity.PlayerTaskActivity;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.SimpleDateUtil;
import com.etv.util.media.AudioPlayerUtil;
import com.etv.util.media.MediaPlayerListener;
import com.ys.model.util.ActivityCollector;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;


public class EtvParsener {

    Context context;
    private Handler handler;

    public EtvParsener(Context context) {
        this.context = context;
        initOther();
    }

    /***
     * 处理红外感应，GPIO触发，
     * 人来了得状况
     */
    public void dealRedGpioInfoPeronComeIn() {
        if (!AppInfo.isAppRun) {
            MyLog.phone("IO广播==软件没起来，中断操作==");
            return;
        }
        if (!EtvService.isServerStart) {
            MyLog.phone("=Server还没有起来，这里不往下走了==");
            return;
        }
        boolean isOpenPoliceEnable = SharedPerManager.getGpioAction();
        if (isOpenPoliceEnable) {
            callNetPolice();
            MyLog.phone("=一键报警开关未打开,请操作==");
        }
        MyLog.phone("IO广播==人来了开始处理业务逻辑==");
        startToPlayTriggleActivity(0);
    }

    private void startToPlayTriggleActivity(int playPosition) {
        int model = TaskWorkService.getCurrentTaskType();
        if (model != TaskWorkService.TASK_TYPE_DEFAULT) {
            return;
        }
        if (PlayerTaskActivity.ISVIEW_FORST) {
            MyLog.phone("startToPlayTriggleActivity: "+playPosition);
            //在前台  发广播  把位置发过去
            Intent intentSendPosition = new Intent();
            Log.e("TAG", "startToPlayTriggleActivity: "+456 );
            intentSendPosition.setAction("thePositionGpio");
            intentSendPosition.putExtra("theGpioDeskPosition", playPosition);
            context.sendBroadcast(intentSendPosition);
            return;
        } else {
            //不在前台  启动界面， 传递位置
            Intent intent = new Intent(context, PlayTaskTriggerActivity.class);
            Log.e("TAG", "startToPlayTriggleActivity: "+123 );
            intent.putExtra("theGpioNotDeskPosition", playPosition);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }


    private void callNetPolice() {
        if (PoliceCacheActivity.isViewFront) {
            MyLog.phone("=正在通话中，中断操作==");
            return;
        }
        try {
            MyLog.phone("=开始拨打电话==");
            Intent intent = new Intent(context, PoliceCacheActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SDorUSBcheckIn(Context context, String path) {
        initOther();
        etvServerModule.SDorUSBcheckIn(context, path);
    }

    /***
     * 删除任务
     * 提交给服务器，
     * 删除本地文件
     *删除本地数据库
     */
    public void deleteEquipmentTaskById(String tag, String taskId) {
        initOther();
        MyLog.del("=======帅选并且删除任务标记==tag");
        try {
            if (etvServerModule != null) {
                etvServerModule.deleteEquipmentTaskServer(taskId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 上传进度给服务器
     *
     * @param taskId
     * @param progress int taskId, int progress, int downKb
     */
    public void updateProgressToWebRegister(String tag, String taskId, String totalNum, int progress, int downKb, String type) {
//        MyLog.cdl("===下载进度标记==" + tag + " /taskId=" + taskId + " /totalNum=" + totalNum + " /progress= " + progress + " /downKb=" + downKb);
        initOther();
        etvServerModule.updateProgressToWebRegister(taskId, totalNum, progress, downKb, type);
    }


    /***
     * 提交设备信息到统计服务器
     */
    public void updateDevInfoToAuthorServer(String version) {
        initOther();
        etvServerModule.upodateDevInfoToAuthorServer(version);
    }

    public void updateDevStatuesToWeb(Context context) {
        initOther();
        etvServerModule.updateDevInfoToWeb(context, "EtvService 界面调用");
    }

    public void updateDownApkImgProgress(int percent, int downKb, String fileName, String tag) {
        initOther();
        etvServerModule.updateDownApkImgProgress(percent, downKb, fileName);
    }

    /**
     * 即时提交统计到服务器
     *
     * @param midId
     * @param addType
     * @param time
     * @param count
     */
    public void addFileNumToTotalOnTime(String midId, String addType, int time, int count) {
        if (!AppInfo.isAppRun) {
            MyLog.update("==统计===软件没起来");
            return;
        }
        int playModel = SharedPerManager.getWorkModel();
        if (playModel != AppInfo.WORK_MODEL_NET) {
            MyLog.update("==统计===非网路模式");
            return;
        }
        try {
            initOther();
            String timeUpdate = SimpleDateUtil.parsenerSratisDate(System.currentTimeMillis());
            etvServerModule.addFileNumTotal(midId, addType, time, count, timeUpdate);
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    /**
     * 添加统计信息到服务器
     */
    public void addPlayNumToWeb() {
        try {
            if (!AppInfo.isAppRun) {
                MyLog.update("==统计===软件没起来");
                return;
            }
            int playModel = SharedPerManager.getWorkModel();
            if (playModel != AppInfo.WORK_MODEL_NET) {
                MyLog.update("==统计===非网路模式");
                return;
            }
            MyLog.update("==统计===准备提交统计");
            StatisticsEntity statisticsEntity = DbStatiscs.getLastUpdateStaEntity();
            if (statisticsEntity == null) {
                MyLog.update("==统计===提交的参数==null");
                return;
            }
            MyLog.update("=====提交的参数==" + statisticsEntity.toString());
            initOther();
            String stMtId = statisticsEntity.getMtid();
            if (stMtId == null || stMtId.contains("null") || stMtId.length() < 5) {
                DbStatiscs.clearNullData();
                return;
            }
            String addType = statisticsEntity.getAddtype();
            int time = statisticsEntity.getPmtime();
            int count = statisticsEntity.getCount();
            long updteTime = statisticsEntity.getCreatetime();
            String timeUpdate = SimpleDateUtil.parsenerSratisDate(updteTime);
            etvServerModule.addFileNumTotal(stMtId, addType, time, count, timeUpdate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //=============GPIO相关操作===============================================================================================

    EtvServerModule etvServerModule;

    private void initOther() {
        if (handler == null) {
            handler = new Handler();
        }
        if (etvServerModule == null) {
            etvServerModule = new EtvServerModuleImpl();
        }
    }

}
