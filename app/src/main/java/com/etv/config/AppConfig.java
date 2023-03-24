package com.etv.config;

import android.text.TextUtils;

import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;

public class AppConfig {

    //用来检测当前开机时间是否是正常得时间
    public static long TIME_CHECK_POWER_REDUCE = 20220505101010L;

    public static final int APP_TYPE_DEFAULT = 5;                        //深圳皇尊年华
    public static final int APP_TYPE_SHANXI = 5008;                        //皇尊年华的陕西客户-zhongbaizhihui
    public static final int APP_TYPE_POWERONOFF = 5009;                    //单机模式-默认定时开关及
    public static final int APP_TYPE_U_INPUT= 5010;                              //单机模式-U盘导入节目需要密码
    public static final int APP_TYPE = APP_TYPE_DEFAULT;

    public static final int SHOW_DEFAULT_SIZE = 0;                     //深圳皇尊年华-原始比例
    public static final int SHOW_MATCH_SIZE = 1;                       //深圳皇尊年华-强制适配
    public static final int SHOW_DOUBLE_SCREEN_TYPE = -1;

    /***
     * Socket 连接方式
     */
    public static final int SOCKEY_TYPE_WEBSOCKET = 0;    //webSocket
    public static final int SOCKEY_TYPE_SOCKET = 1;       //socket


    // 软件是否打印日志
    public static final boolean IF_PRINT_LOG = true;


    /***
     * 守护进程的版本号================================================================================
     */
    public static final int APP_BACK_TIME_MIX = 5;  //互动节目，返回ETV的最小时间

    /***
     * 查看设备是否连接服务器
     */
    public static boolean isOnline = false;

    /***
     * 是否已经初始化腾讯socket的sdk
     */
    public static boolean isInitedTimSDK = false;


    /***
     * 查看设备和腾讯tim服务器的连接状态
     */
    public static boolean isTimServerConect = false;


    /***
     * 地图定位的间隔时间
     */
    public static final int BAIDU_MAP_LOCATION = 1000 * 10;

    /***
     * 回到主界面，自动检查播放
     */
    public static int CHECK_TIMER_TO_PLAY() {
        String exitPassword = SharedPerManager.getExitpassword();
        if (TextUtils.isEmpty(exitPassword)) {
            return 5;
        }
        return 30;
    }

    //终端发送一个一个空消息。保持服务器在线状态
    public static final int MESSAGE_AUTO_SEND_SOCKET = 3 * 1000;
    //无缝切换的时间 默认 2000
    public static final int Seamless_Switching_Time = 800;
    //单机模式，混播的切换时间  默认 400
    public static final int Seamless_Switching_Single_model = 400;

    /***
     *设备与服务器之间的心跳
     */
    public static int TIME_TO_HART_TO_WEB() {
        if (SharedPerUtil.SOCKEY_TYPE() == SOCKEY_TYPE_SOCKET) {
            return 25 * 1000;
        }
        return 25 * 1000;
    }

    //心跳超时
    public static int TIME_TO_HART_MORE_TIME() {
        return 5 * 1000;
    }

}
