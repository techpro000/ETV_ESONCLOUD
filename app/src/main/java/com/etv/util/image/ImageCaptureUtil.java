package com.etv.util.image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.View;

import com.etv.config.AppInfo;
import com.etv.runnable.BitmapWriteLocalRunnable;
import com.etv.runnable.WriteBitmapToLocalListener;
import com.etv.util.MyLog;

/***
 * 此雷仅仅用于 M11 截屏使用
 */
public class ImageCaptureUtil {


    /***
     * M11-竖屏截图截图
     */
    public static void takeScreenShotM11(Activity activity) {
        try {
            MyLog.guardian("takeScreenShot===000====");
            View view = activity.getWindow().getDecorView();
            view.setDrawingCacheEnabled(true);
            view.buildDrawingCache();
            Bitmap bitmap = view.getDrawingCache();
            Rect frame = new Rect();
            activity.getWindow().getDecorView()
                    .getWindowVisibleDisplayFrame(frame);
            int width = activity.getWindowManager().getDefaultDisplay()
                    .getWidth();
            int height = activity.getWindowManager().getDefaultDisplay()
                    .getHeight();
            MyLog.guardian("takeScreenShot===111===" + width + " / " + height);
            Bitmap bitmapSave = Bitmap.createBitmap(bitmap, 0, 0, width,
                    height);
            view.destroyDrawingCache();
            bitmapWriteToLocalImagePath(activity, bitmapSave);
            MyLog.guardian("takeScreenShot===111");
        } catch (Exception e) {
            MyLog.guardian("takeScreenShot===222==" + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void bitmapWriteToLocalImagePath(Activity activity, Bitmap bitmapSave) {
        String catpturePathCache = AppInfo.CAPTURE_MAIN;
        if (bitmapSave == null) {
            MyLog.guardian("takeScreenShot===333==bitmapSave==null");
            return;
        }
        if (TextUtils.isEmpty(catpturePathCache)) {
            MyLog.guardian("takeScreenShot===333==保存地址为==null");
            return;
        }
        BitmapWriteLocalRunnable runnable = new BitmapWriteLocalRunnable(bitmapSave, catpturePathCache, new WriteBitmapToLocalListener() {
            @Override
            public void writeStatues(boolean isSuccess, String path) {
                MyLog.guardian("takeScreenShot===333==" + isSuccess + " / " + path);
                if (!isSuccess) {
                    return;
                }
                Intent intent1 = new Intent();
                intent1.setAction(AppInfo.SEND_IMAGE_CAPTURE_SUCCESS);
                intent1.putExtra("tag", AppInfo.TAG_UPDATE);
                activity.sendBroadcast(intent1);
            }
        });
        Thread thread = new Thread(runnable);
        thread.start();
    }


}
