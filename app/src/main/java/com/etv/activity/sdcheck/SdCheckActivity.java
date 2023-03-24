package com.etv.activity.sdcheck;

import static com.etv.config.AppConfig.APP_TYPE_U_INPUT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.etv.activity.BaseActivity;
import com.etv.activity.MainActivity;
import com.etv.adapter.SdCheckAdapter;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.entity.SdCheckEntity;
import com.etv.entity.StorageInfo;
import com.etv.service.EtvService;
import com.etv.task.db.DBTaskUtil;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.SharedPerManager;
import com.etv.util.poweronoff.PowerOnOffManager;
import com.etv.util.poweronoff.db.PowerDbManager;
import com.etv.util.poweronoff.entity.TimerDbEntity;
import com.etv.util.sdcard.FileFilter;
import com.etv.util.sdcard.MySDCard;
import com.etv.util.system.CpuModel;
import com.ys.etv.R;
import com.ys.model.dialog.EditTextDialog;
import com.ys.model.dialog.MyToastView;

import com.ys.model.listener.EditTextDialogListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/***
 * 用来检测SD卡插入动作
 */
public class SdCheckActivity extends BaseActivity implements SdCheckView {

    MySDCard mySDCard;
    private ListView lv_sd_info;
    SdCheckAdapter adapter;
    List<SdCheckEntity> list = new ArrayList<>();
    private static final int FINISH_TIME_DISTANCE = 5000;
    public static final String TAG_CHECK_URL = "TAG_CHECK_URL";

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                MyLog.e("SDCARD", "检测到U盘插拔==========");
                if (sdCheckParsener != null) {
                    sdCheckParsener.stopZipFile();
                }
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.activity_sd_check);
        initView();
        getDataInfo();
        initReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppInfo.startCheckTaskTag = false;
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addDataScheme("file");
        registerReceiver(receiver, filter);
    }

    SdCheckParsener sdCheckParsener;
    private LinearLayout lin_progress;
    private TextView tv_progress;
    private ProgressBar progress_write;
    String checkUrl;

    private void initView() {
        Intent intent = getIntent();
        checkUrl = intent.getStringExtra(TAG_CHECK_URL);
        mySDCard = new MySDCard(SdCheckActivity.this);
        lv_sd_info = (ListView) findViewById(R.id.lv_sd_info);
        adapter = new SdCheckAdapter(SdCheckActivity.this, list);
        lv_sd_info.setAdapter(adapter);
        lin_progress = (LinearLayout) findViewById(R.id.lin_progress);
        tv_progress = (TextView) findViewById(R.id.tv_progress);
        progress_write = (ProgressBar) findViewById(R.id.progress_write);
        addInfoToList(getString(R.string.start_ckeckout));
        sdCheckParsener = new SdCheckParsener(SdCheckActivity.this, this);
    }
    String filePathToExit ;
    private void getDataInfo() {
        if (!CpuModel.getMobileType().startsWith(CpuModel.CPU_MODEL_3568_11)) {
            List<String> list = mySDCard.getAllExternalStorage();  //获取所有的磁盘路径
            if (list.size() < 2 || list == null) {
                setThreeClose(getString(R.string.no_sdmemory));
                return;
            }
        }
        StorageInfo storageInfoType = FileFilter.jujleFileIsExict(SdCheckActivity.this, checkUrl);
        int exDevType = storageInfoType.getSdType();
        if (exDevType == StorageInfo.TYPE_USB) {
            addInfoToList(getString(R.string.check_usb));
        } else if (exDevType == StorageInfo.TYPE_SD) {
            addInfoToList(getString(R.string.check_sd));
        }
        String filePath = storageInfoType.getPath();
        filePathToExit = filePath;
        if (filePath == null || filePath.length() < 2) {
            setThreeClose(getString(R.string.no_filematch));
            return;
        }
        String desc = getLanguageFromResurceWithPosition(R.string.check_info, filePath);
        addInfoToList(desc);
        StorageInfo storageInfo = FileUtil.getStorageFileAction(filePath);
        if (storageInfo == null) {
            MyLog.sdckeck("=======action====storageInfo==null=");
            addInfoToList(getString(R.string.get_actionfailed));
            return;
        }
        MyLog.sdckeck("=======action==" + storageInfo.toString());
        int action = storageInfo.getAction();
        MyLog.sdckeck("=======action=====" + action);
        if (action == StorageInfo.ACTION_MODIFY_IP) {  //修改IP
            try {
                SharedPerManager.setWorkModel(AppInfo.WORK_MODEL_NET, "U盘修改IP");
                addInfoToList("\nModify working mode to network distribution\n", true);
                addInfoToList(getString(R.string.modify_action_ip));
                //因为替换了Apk所以需要删除任务
                String taskPath = AppInfo.BASE_TASK_URL();

                FileUtil.deleteDirOrFilePath(taskPath, "U盘修改IP==StorageInfo.ACTION_MODIFY_IP");
                DBTaskUtil.clearAllDbInfo("");

                PowerDbManager.clearTimeDb("用户修改IP");
                PowerOnOffManager.getInstance().clearPowerOnOffTime("用户修改IP");
                sdCheckParsener.modifySharedIpAddress(filePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (action == StorageInfo.ACTION_INSTALL_ETV_APK) {  //更新APK
            lin_progress.setVisibility(View.VISIBLE);
            addInfoToList(getString(R.string.modify_action_apk));
            sdCheckParsener.installApk(filePath);
        } else if (action == StorageInfo.ACTION_TASK_DISONLINE) {  //离线任务
            MyLog.sdckeck("=======客户修改离线任务=====");

//            PowerDbManager.clearTimeDb("切换离线任务");
            PowerOnOffManager.getInstance().clearPowerOnOffTime("切换离线任务");
            sendBroadcast(new Intent(AppInfo.STOP_DOWN_TASK_RECEIVER));  //停止下载APK，和任务
            int workModel = SharedPerManager.getWorkModel();
            if (workModel != AppInfo.WORK_MODEL_NET_DOWN) {
                SharedPerManager.setWorkModel(AppInfo.WORK_MODEL_NET_DOWN, "网络离线任务");
                SharedPerManager.setShowNetDownTask(true);
                addInfoToList("\n" + getString(R.string.change_load) + "\n", true);
            }
            MyLog.sdckeck("=======修改工作模式=====");
            DBTaskUtil.clearAllDbInfo("=====SDCHECK_ACTIVITY===");
            String checkPath = AppInfo.BASE_SD_PATH();
            File file = new File(filePath);
            long fileLength = file.length() / 1024 / 1024;
            long lastUseSize = mySDCard.getAvailableExternalMemorySize(checkPath, 1024 * 1024);
            if (fileLength > (lastUseSize * 1.2)) {
                setThreeClose(getString(R.string.mo_cache_size));
                return;
            }
            sdCheckParsener.zipFileToSdTask(filePath);
            lin_progress.setVisibility(View.VISIBLE);
        } else if (action == StorageInfo.ACTION_TASK_SINGLE) { //单机版本离线任务
            if (AppConfig.APP_TYPE == APP_TYPE_U_INPUT){
                checkPassword();
            }else {
                addInfoToList(getString(R.string.modify_action_single));
                sendBroadcast(new Intent(AppInfo.STOP_DOWN_TASK_RECEIVER));  //停止下载APK，和任务
                int workModel = SharedPerManager.getWorkModel();
                if (workModel != AppInfo.WORK_MODEL_SINGLE) {
                    SharedPerManager.setWorkModel(AppInfo.WORK_MODEL_SINGLE, "单机版本离线任务");
                    addInfoToList("\n" + getString(R.string.change_single) + "\n", true);
                }
                String singleTaskPath = AppInfo.TASK_SINGLE_PATH();
                FileUtil.deleteDirOrFilePath(singleTaskPath, "===单机离线任务====");
                sdCheckParsener.copyDisTaskToLocal(filePathToExit);
                lin_progress.setVisibility(View.VISIBLE);
                return;
            }
//            PowerOnOffManager.getInstance().clearPowerOnOffTime("切换单机模式");
//            PowerDbManager.clearTimeDb("切换单机模式");

        } else if (action == StorageInfo.ACTION_VOICE_MEDIA) {  //触沃-语音替换
            addInfoToList(getString(R.string.action_modify_welcome));
            lin_progress.setVisibility(View.VISIBLE);
            sdCheckParsener.modifyWelcomeMediaFile(filePath);
        }
    }
    boolean isExitUsb = false;
    private void checkPassword() {
        String exitUsbPass = SharedPerManager.getExitpassword();
        if (exitUsbPass == null || exitUsbPass.length() < 1){
            addInfoToList(getString(R.string.modify_action_single));
            sendBroadcast(new Intent(AppInfo.STOP_DOWN_TASK_RECEIVER));  //停止下载APK，和任务
            int workModel = SharedPerManager.getWorkModel();
            if (workModel != AppInfo.WORK_MODEL_SINGLE) {
                SharedPerManager.setWorkModel(AppInfo.WORK_MODEL_SINGLE, "单机版本离线任务");
                addInfoToList("\n" + getString(R.string.change_single) + "\n", true);
            }
            String singleTaskPath = AppInfo.TASK_SINGLE_PATH();
            FileUtil.deleteDirOrFilePath(singleTaskPath, "===单机离线任务====");
            sdCheckParsener.copyDisTaskToLocal(filePathToExit);
            lin_progress.setVisibility(View.VISIBLE);
            return;
        }
        Log.e("TAG", "checkPassword: "+58588958+exitUsbPass );
        EditTextDialog editTextDialog = new EditTextDialog(SdCheckActivity.this);
        Log.e("TAG", "checkPassword: "+585836558 );
        editTextDialog.setOnDialogClickListener(new EditTextDialogListener() {
            @Override
            public void commit(String content) {
                String exitUsb = SharedPerManager.getExitpassword();
                Log.e("TAG", "checkPassword: "+exitUsb+3545 );
                if (content.trim().contains(exitUsb) || content.trim().contains("000")){
                    Log.e("TAG", "checkPassword: "+exitUsb+789 );
                    isExitUsb =true;
                    if (isExitUsb == false){
                        Log.e("TAG", "checkPassword: "+7814528 );
                        return;
                    }
                    Log.e("TAG", "checkPassword: "+585564858 );
                    addInfoToList(getString(R.string.modify_action_single));
                    sendBroadcast(new Intent(AppInfo.STOP_DOWN_TASK_RECEIVER));  //停止下载APK，和任务
                    int workModel = SharedPerManager.getWorkModel();
                    if (workModel != AppInfo.WORK_MODEL_SINGLE) {
                        SharedPerManager.setWorkModel(AppInfo.WORK_MODEL_SINGLE, "单机版本离线任务");
                        addInfoToList("\n" + getString(R.string.change_single) + "\n", true);
                    }
                    String singleTaskPath = AppInfo.TASK_SINGLE_PATH();
                    FileUtil.deleteDirOrFilePath(singleTaskPath, "===单机离线任务====");
                    sdCheckParsener.copyDisTaskToLocal(filePathToExit);
                    lin_progress.setVisibility(View.VISIBLE);
                    return;
                }else {
                    finish();
                }
                MyToastView.getInstance().Toast(SdCheckActivity.this, getString(R.string.password_error));
            }
            @Override
            public void clickHiddleView() {

            }
        });
        editTextDialog.show(getString(R.string.exit_password), "", getString(R.string.submit));
    }

    @Override
    public void setThreeClose(String desc) {
        MyLog.cdl("===desc=" + desc);
        addInfoToList(desc + getString(R.string.close_three_min), true);
        handler.sendEmptyMessageDelayed(FINISH_MY_SELF, FINISH_TIME_DISTANCE);
    }

    private static final int FINISH_MY_SELF = 89;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FINISH_MY_SELF:
                    startToMainActivity();
                    break;
            }
        }
    };

    private void startToMainActivity() {
        MainActivity.IS_ORDER_REQUEST_TASK = true;
        startActivity(new Intent(SdCheckActivity.this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sdCheckParsener != null) {
            sdCheckParsener.stopZipFile();
        }
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if (handler != null) {
            handler.removeMessages(FINISH_MY_SELF);
        }
    }

    @Override
    public void addInfoToList(String info) {
        addInfoToList(info, false);
    }

    public void addInfoToList(String info, boolean isRed) {
        MyLog.cdl("===info=" + info, true);
        list.add(new SdCheckEntity(info, list.size() + 1 + "", isRed));
        adapter.setList(list);
    }

    @Override
    public void writeFileProgress(int progress) {
        progress_write.setProgress(progress);
        tv_progress.setText(progress + " %");
    }

    @Override
    public void showToastView(String toast) {
        MyToastView.getInstance().Toast(SdCheckActivity.this, toast);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (sdCheckParsener != null) {
                sdCheckParsener.stopZipFile();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
