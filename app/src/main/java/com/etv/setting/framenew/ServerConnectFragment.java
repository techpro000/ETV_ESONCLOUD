package com.etv.setting.framenew;

import static com.etv.config.ApiInfo.IP_DEFAULT_URL_SOCKET;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.etv.activity.SettingSysActivity;
import com.etv.activity.model.RegisterDevListener;
import com.etv.config.ApiInfo;
import com.etv.config.AppConfig;
import com.etv.config.AppInfo;
import com.etv.service.EtvService;
import com.etv.service.TcpService;
import com.etv.service.TcpSocketService;
import com.etv.setting.VoiceSettingsActivity;
import com.etv.socket.mine.SocketUtil;
import com.etv.task.db.DBTaskUtil;
import com.etv.util.AppLinkSer;
import com.etv.util.CodeUtil;
import com.etv.util.ConsumerUtil;
import com.etv.util.FileUtil;
import com.etv.util.MyLog;
import com.etv.util.NetWorkUtils;
import com.etv.util.RootCmd;
import com.etv.util.SharedPerManager;
import com.etv.util.SharedPerUtil;
import com.etv.util.system.LanguageChangeUtil;
import com.etv.util.system.SystemManagerUtil;

import com.ys.etv.R;
import com.ys.model.dialog.EditTextDialog;
import com.ys.model.dialog.MyToastView;
import com.ys.model.dialog.OridinryDialog;
import com.ys.model.dialog.RadioListDialog;
import com.ys.model.dialog.WaitDialogUtil;
import com.ys.model.entity.RedioEntity;
import com.ys.model.listener.EditTextDialogListener;
import com.ys.model.listener.OridinryDialogClick;
import com.ys.model.listener.RadioChooiceListener;
import com.ys.model.util.KeyBoardUtil;
import com.ys.model.view.MyToggleButton;

import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.List;

public class ServerConnectFragment extends Fragment implements View.OnClickListener {

    Button btn_server_address;
    private TextView tv_download_link;
    Button bt_save_line;
    Spinner spinner;

    private BroadcastReceiver receiverServer = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(AppInfo.UDP_SERVER_SEND_IP_PORT)) {
                //终端UDP检索服务器，服务器下发指令，这里通知界面
                handler.sendEmptyMessage(MESSAGE_UPDATE_VIEW);
                updateAutoLineView();
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        initServerReceiver();
    }

    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        View view = View.inflate(getActivity(), R.layout.fragment_server_connect, null);
        initView(view);
        initListener();
        return view;
    }

    WaitDialogUtil waitDialogUtil;
    Button btn_modfy_port;
    LinearLayout lin_net_view;
    Button btn_username;
    Button btn_line_by_hand;
    TextView tv_line_type;
    MyToggleButton toggle_switch_line;
    LinearLayout lin_socket_type;

    private void initView(View view) {
        AppInfo.startCheckTaskTag = false;
        waitDialogUtil = new WaitDialogUtil(getActivity());
        lin_socket_type = (LinearLayout) view.findViewById(R.id.lin_socket_type);
        toggle_switch_line = (MyToggleButton) view.findViewById(R.id.toggle_switch_line);
        tv_line_type = (TextView) view.findViewById(R.id.tv_line_type);
        btn_line_by_hand = (Button) view.findViewById(R.id.btn_line_by_hand);
        btn_line_by_hand.setOnClickListener(this);
        lin_net_view = (LinearLayout) view.findViewById(R.id.lin_net_view);
        btn_server_address = (Button) view.findViewById(R.id.btn_server_address);
        btn_server_address.setOnClickListener(this);
        btn_server_address.setText(SharedPerUtil.getWebHostIpAddress());
        tv_download_link = (TextView) view.findViewById(R.id.tv_download_link);
        tv_download_link.setText(AppInfo.BASE_PATH());
        bt_save_line = (Button) view.findViewById(R.id.bt_save_line);
        bt_save_line.setOnClickListener(this);
        btn_username = (Button) view.findViewById(R.id.btn_username);
        btn_username.setText(SharedPerManager.getUserName());
        btn_username.setOnClickListener(this);
        btn_modfy_port = (Button) view.findViewById(R.id.btn_modfy_port);
        btn_modfy_port.setText(ApiInfo.getWebPort());
        btn_modfy_port.setOnClickListener(this);
//        spinner = view.findViewById(R.id.spinner);
//        String ipaddress = SharedPerUtil.getWebHostIpAddress();
//        if (ipaddress.equals(IP_DEFAULT_URL_SOCKET)){
//            spinner.setSelection(0);
//        }else {
//            spinner.setSelection(1);
//        }
        updateAutoLineView();
    }

    private void initListener() {
        toggle_switch_line.setOnToggleListener(new MyToggleButton.ToggleClickListener() {

            public void click(View view, boolean isClick) {
                if (isClick) {
                    SharedPerManager.setSocketType(AppConfig.SOCKEY_TYPE_SOCKET);
                } else {
                    SharedPerManager.setSocketType(AppConfig.SOCKEY_TYPE_WEBSOCKET);
                }
                updateAutoLineView();
            }
        });
    }

    private void showRebootDialog(String content) {
        OridinryDialog oridinryDialog = new OridinryDialog(getActivity());
        oridinryDialog.setCancelable(false);
        oridinryDialog.show(content, false, false);
        handler.postDelayed(()-> SystemManagerUtil.rebootApp(getActivity()), 2000);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_username:  //修改用户名
                showInPutUsernameDialog();
                break;
            case R.id.btn_server_address:  //输入IP
                showInPutIpDialog();
                break;
            case R.id.btn_line_by_hand: //手动连接局域网
                searchIpHostLocalNet();
                break;
            case R.id.btn_modfy_port:
                showEditDialog();
                break;
            case R.id.bt_save_line:
                lineWebHostWeb();
                break;
//            case R.id.spinner:
//                break;
        }
    }


    private void searchIpHostLocalNet() {
        if (!NetWorkUtils.isNetworkConnected(getActivity())) {
            showToast(getActivity().getString(R.string.net_error));
            return;
        }
        OridinryDialog oridinDialog = new OridinryDialog(getActivity());
        oridinDialog.setOnDialogClickListener(new OridinryDialogClick() {
            @Override
            public void sure() {
                waitDialogUtil.show(getActivity().getString(R.string.querying), 3000);
                String ipaddress = CodeUtil.getIpAddress(getActivity(), "");
                String sendIp = ipaddress.substring(0, ipaddress.lastIndexOf(".") + 1) + 255;
                String sendJson = "{\"type\":\"searchServer\",\"ipaddress\":\"" + ipaddress + "\"}";
                EtvService.getInstance().sendUdpMessage(sendJson, sendIp);
            }

            @Override
            public void noSure() {

            }
        });
        oridinDialog.show(getActivity().getString(R.string.line_auto_tips),
                getActivity().getString(R.string.connect),
                getActivity().getString(R.string.cancel));
    }

    private static final int MESSAGE_UPDATE_VIEW = 456;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handler.removeMessages(msg.what);
            switch (msg.what) {
                case MESSAGE_UPDATE_VIEW:         //更新界面
                    waitDialogUtil.dismiss();
                    updateAutoLineView();
                    showToast(getActivity().getString(R.string.query_success));
                    lineWebHostWeb();
                    break;
            }
        }
    };

    private void updateAutoLineView() {
        String username = SharedPerManager.getUserName();
        String ipaddress = SharedPerUtil.getWebHostIpAddress();
        Log.e("TAG", "updateAutoLineView666: "+ipaddress );
        String port = SharedPerUtil.getWebHostPort();
        btn_username.setText(username);
        btn_server_address.setText(ipaddress);
        btn_modfy_port.setText(port);
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
            tv_line_type.setText("WebSocket");
            toggle_switch_line.setIsChoice(false);
            btn_line_by_hand.setVisibility(View.VISIBLE);
        } else {
            tv_line_type.setText("Socket");
            toggle_switch_line.setIsChoice(true);
            btn_line_by_hand.setVisibility(View.GONE);
        }
    }

    /***
     * 连接服务器
     */
    private void lineToSocketWeb() {
        Intent intent = new Intent();
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
            intent.setClass(getActivity(), TcpService.class);
        } else {
            intent.setClass(getActivity(), TcpSocketService.class);
        }
        getActivity().startService(intent);
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
            TcpService.getInstance().dealDisOnlineDev("手动点击连接，先断开，后重连", false);
        } else {
            SocketUtil.getInstance().sendRegisterMacToServer("注销登录状态", true);
//            TcpSocketService.getInstance().dealDisOnlineDev("手动点击连接，先断开，后重连", false);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                registerDevToWeb();
            }
        }, 1000);
    }

    private void registerDevToWeb() {
        if (SharedPerUtil.SOCKEY_TYPE() == AppConfig.SOCKEY_TYPE_WEBSOCKET) {
            registerDevWebSocket();
        } else {
            registerDevTcpSocket();
        }
    }

    private void registerDevTcpSocket() {
        TcpSocketService.getInstance().registerDev(getActivity(), getUsername(), new RegisterDevListener() {

            @Override
            public void registerDevState(boolean isSuccess, String errorrDesc, int code) {
                MyLog.message("=====注册成功连接中：" + isSuccess + " /errorrDesc= " + errorrDesc);
                if (isSuccess) {  //注册success
                    showToast(getActivity().getString(R.string.regsucc_line));
                    TcpSocketService.getInstance().dissOrReconnect();
                } else { //注册失败
                    showToast(LanguageChangeUtil.getLanguageFromResurceWithPosition(getActivity(), R.string.regfail_line, errorrDesc));
                }
            }

        });
    }

    /***
     * WebSocket 注册模式
     */
    private void registerDevWebSocket() {
        TcpService.getInstance().registerDev(getActivity(), getUsername(), new RegisterDevListener() {

            @Override
            public void registerDevState(boolean isSuccess, String errorrDesc, int code) {
                if (isSuccess) {  //注册success
                    MyLog.message("=====注册成功连接中：");
                    showToast(getActivity().getString(R.string.regsucc_line));
                    TcpService.getInstance().dissOrReconnect();
                } else { //注册失败
                    showToast(LanguageChangeUtil.getLanguageFromResurceWithPosition(getActivity(), R.string.regfail_line, errorrDesc));
                    MyLog.message("=====主界面注册失败：" + errorrDesc, true);
                }
            }

        });
    }

    public void showToast(final String toast) {
        MyToastView.getInstance().Toast(getActivity(), toast);
    }

    public void onDestroy() {
        super.onDestroy();
        if (receiverServer != null) {
            getActivity().unregisterReceiver(receiverServer);
        }
    }

    private void showEditDialog() {
        String webPort = SharedPerUtil.getWebHostPort();
        EditTextDialog editDialog = new EditTextDialog(getActivity());
        editDialog.setOnDialogClickListener(new EditTextDialogListener() {
            @Override
            public void clickHiddleView() {

            }

            @Override
            public void commit(String content) {
                if (TextUtils.isEmpty(content)) {
                    showToast(getActivity().getString(R.string.please_insert));
                    return;
                }
                if (content.trim().length() > 50) {
                    showToast(getActivity().getString(R.string.insert_less));
                    return;
                }
                try {
                    int portNum = Integer.parseInt(content);
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(getActivity().getString(R.string.input_success_info));
                    return;
                }
                SharedPerManager.setWebPort(content);
                btn_modfy_port.setText(SharedPerUtil.getWebHostPort());
            }
        });
        editDialog.show(getActivity().getString(R.string.modify_port), webPort, getActivity().getString(R.string.modify));
    }

    public String getUsername() {
        String username = btn_username.getText().toString().trim();
        username.replace(" ", "");
        return username;
    }

    private void initServerReceiver() {
        IntentFilter fileter = new IntentFilter();
        fileter.addAction(AppInfo.UDP_SERVER_SEND_IP_PORT);
        getActivity().registerReceiver(receiverServer, fileter);
    }

    /**
     * 连接服务器操作
     */
    private void lineWebHostWeb() {
        KeyBoardUtil.hiddleBord(bt_save_line);
        String ipAddress = btn_server_address.getText().toString().trim();
//        String ipAddress = spinner.getSelectedItem().toString();
        Log.e("TAG", "lineWebHostWeb: "+ipAddress );
        if (ipAddress.contains(" ")) { //去掉空格
            ipAddress = ipAddress.replace(" ", "");
        }
        ipAddress = ipAddress.trim();
        String userName = getUsername();
        if (userName.contains(" ")) { //去掉空格
            userName = userName.replace(" ", "");
        }
        userName = userName.trim();
        String port = btn_modfy_port.getText().toString().trim();
        SharedPerManager.setWebHost(ipAddress);
        SharedPerManager.setUserName(userName, "准备连接服务器，这里保存一次");
        SharedPerManager.setWebPort(port);
        int workModel = SharedPerManager.getWorkModel();
        if (workModel != AppInfo.WORK_MODEL_NET) {
            showToast(getActivity().getString(R.string.single_errror));
            return;
        }
        if (!NetWorkUtils.isNetworkConnected(getActivity())) {
            showToast(getActivity().getString(R.string.net_error));
            return;
        }
        if (TextUtils.isEmpty(userName) || userName.length() < 2) {
            showToast(getActivity().getString(R.string.insert_legitimate));
            return;
        }

        if (checkIfNeedRebootDev()) {
            showRebootDialog(getRebootContent());
            return;
        }

        SettingSysActivity.isServerConClick = true;
        showToast(getActivity().getString(R.string.savesucc_line));
        waitDialogUtil.show(getActivity().getString(R.string.linging), 8000);
        lineToSocketWeb();
    }

    private boolean checkIfNeedRebootDev() {
        String host = SharedPerManager.getWebHost();
        if (!host.startsWith("yun.won-giant.com")) {
            toggle_switch_line.checkStatus(false);
            return AppLinkSer.getSocketType() != AppConfig.SOCKEY_TYPE_WEBSOCKET;
        }
        toggle_switch_line.checkStatus(true);
        return AppLinkSer.getSocketType() != AppConfig.SOCKEY_TYPE_SOCKET;
    }

    private String getRebootContent(){
        String host = SharedPerManager.getWebHost();
        String restart = getString(R.string.str_reboot_ing) + "，";
        if (!host.startsWith("yun.won-giant.com")) {
            return restart + getString(R.string.switch_local_server);
        }
        return restart + getString(R.string.switch_yun_server);
    }

    //===========祖传代码========================================================================================================================================================================================
    private void showInPutUsernameDialog() {

        EditTextDialog editTextDialog = new EditTextDialog(getActivity());
        editTextDialog.setOnDialogClickListener(new EditTextDialogListener() {
            @Override
            public void clickHiddleView() {

            }

            @Override
            public void commit(String content) {
                if (TextUtils.isEmpty(content)) {
                    showToast(getActivity().getString(R.string.please_insert));
                    return;
                }
                if (content.trim().length() < 2) {
                    showToast(getActivity().getString(R.string.insert_less));
                    return;
                }
                SharedPerManager.setUserName(content, "手动修改用户名");
                updateAutoLineView();
            }
        });
        editTextDialog.show(getActivity().getString(R.string.username),
                SharedPerManager.getUserName(),
                getActivity().getString(R.string.submit));
    }

    /**
     * 输入IP得弹窗
     */
    private void showInPutIpDialog() {
        EditTextDialog editTextDialog = new EditTextDialog(getActivity());
        editTextDialog.setOnDialogClickListener(new EditTextDialogListener() {
            @Override
            public void clickHiddleView() {

            }

            @Override
            public void commit(String content) {
                if (TextUtils.isEmpty(content)) {
                    showToast(getActivity().getString(R.string.please_insert));
                    return;
                }
                if (content.trim().length() < 5) {
                    showToast(getActivity().getString(R.string.insert_less));
                    return;
                }
                SharedPerManager.setWebHost(content);
                updateAutoLineView();
            }
        });
        editTextDialog.show(getActivity().getString(R.string.input_ip),
                SharedPerUtil.getWebHostIpAddress(),
                getActivity().getString(R.string.submit));
    }


}
