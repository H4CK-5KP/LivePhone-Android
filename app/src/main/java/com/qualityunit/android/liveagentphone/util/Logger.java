package com.qualityunit.android.liveagentphone.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

/**
 * Created by rasto on 11.02.16.
 */
public class Logger {

    private static final String TAG = Logger.class.getSimpleName();
    private static Logger instance;
    public static String logFileName = "logfile.log";

    private static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public static void e(String TAG, Throwable e) {
        getInstance().logError(TAG, null, e, null);
    }

    public static void e(String TAG, Throwable e, Bundle additionalInfo) {
        getInstance().logError(TAG, null, e, additionalInfo);
    }

    public static void e(String TAG, String errMsg) {
        getInstance().logError(TAG, errMsg, null, null);
    }

    public static void e(String TAG, String errMsg, Bundle additionalInfo) {
        getInstance().logError(TAG, errMsg, null, additionalInfo);
    }

    public static void e(String TAG, String errMsg, Throwable e, Bundle additionalInfo) {
        getInstance().logError(TAG, errMsg, e, additionalInfo);
    }

    public static void e(String TAG, String errMsg, Throwable e) {
        getInstance().logError(TAG, errMsg, e, null);
    }

    public void logError(String TAG, String errMsg, Throwable e, Bundle additionalInfo) {
        if (errMsg == null) {
            errMsg = "";
        }
        if (additionalInfo != null) {
            errMsg = errMsg + "\n" + additionalInfo.toString();
        }
        if(e != null) {
            Log.e(TAG, errMsg, e);
        }
    }

    public static void logToFile(Context context, String text) {
        Log.d(TAG, text);
        File logFile = new File(context.getExternalFilesDir(null), logFileName);
        try {
            if (!logFile.exists()) {
                if (logFile.createNewFile()) {
                    text = Tools.getDeviceName() + System.lineSeparator() + System.lineSeparator() + new Date().toString() + ": " + text; // initial line
                }
            }
            FileOutputStream fos = new FileOutputStream(logFile, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.append(new Date().toString() + ": " + text + System.lineSeparator());
            osw.flush();
            osw.close();
            fos.close();
        } catch (IOException e) {
            e(TAG, "Error while 'logToFile': " + logFileName, e);
        }

    }

    public static void sendLogFile(Context context) {
        File logFile = new File(context.getExternalFilesDir(null), logFileName);
        File file = new File(logFile.getAbsolutePath());
        Uri path = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent .setType("vnd.android.cursor.dir/email");
        String to[] = {"support@qualityunit.com"};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent .putExtra(Intent.EXTRA_STREAM, path);
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "LivePhone log file");
        context.startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }

}
