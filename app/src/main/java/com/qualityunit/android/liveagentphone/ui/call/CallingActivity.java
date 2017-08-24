package com.qualityunit.android.liveagentphone.ui.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.qualityunit.android.liveagentphone.R;
import com.qualityunit.android.liveagentphone.service.CallingCommands;
import com.qualityunit.android.liveagentphone.service.CallingException;
import com.qualityunit.android.liveagentphone.service.CallingService;
import com.qualityunit.android.liveagentphone.ui.common.ToolbarActivity;
import com.qualityunit.android.liveagentphone.util.Logger;

/**
 * Created by rasto on 18.10.16.
 */

public class CallingActivity extends ToolbarActivity {

    private static final String TAG = CallingActivity.class.getSimpleName();
    private FloatingActionButton fabHangupCall;
    private boolean isCallEnded;
    private boolean isSipEventsReceiverRegistered;
    private boolean isRinging;
    private boolean isCallEstablished;
    private SipBroadcastReceiver sipBroadcastReceiver = new SipBroadcastReceiver();

    @Override
    protected boolean allwaysShowHomeAsUp() {
        return false;
    }

    @Override
    protected int getContentViewRes() {
        return R.layout.activity_calling;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerSipEventsReceiver();
        if (savedInstanceState == null) {
            addFragment(new CallingFragment(), CallingFragment.TAG);
        }
        fabHangupCall = (FloatingActionButton) findViewById(R.id.fab_hangCall);
        if (fabHangupCall != null) {
            fabHangupCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    declineCall();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        unregisterSipEventsReceiver();
        super.onDestroy();
    }

    @Override
    protected void beforeSetContentView() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        super.beforeSetContentView();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isRinging && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)){
            try {
                CallingCommands.silenceRinging(getApplicationContext());
                isRinging = false;
                return true;
            } catch (CallingException e) {
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        if (isCallEstablished) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                try {
                    System.out.println("@@@@ decreasing incall volume");
                    CallingCommands.adjustIncallVolume(getApplicationContext(), false);
                    return true;
                } catch (CallingException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                try {
                    System.out.println("@@@@ increasing incall volume");
                    CallingCommands.adjustIncallVolume(getApplicationContext(), true);
                    return true;
                } catch (CallingException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void declineCall() {
        try {
            if (isCallEnded) {
                finish();
            }
            CallingCommands.hangupCall(CallingActivity.this);
        } catch (CallingException e) {
            Toast.makeText(CallingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void receiveCall() {
        try {
            CallingCommands.receiveCall(CallingActivity.this);
        } catch (CallingException e) {
            Toast.makeText(CallingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public FloatingActionButton getFabHangupCall() {
        return fabHangupCall;
    }

    private void registerSipEventsReceiver() {
        if (!isSipEventsReceiverRegistered) {
            isSipEventsReceiverRegistered = true;
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(sipBroadcastReceiver, new IntentFilter(CallingService.INTENT_FILTER_CALLBACK));
        }
    }

    private void unregisterSipEventsReceiver() {
        if (isSipEventsReceiverRegistered) {
            isSipEventsReceiverRegistered = false;
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(sipBroadcastReceiver);
        }
    }

    private class SipBroadcastReceiver extends BroadcastReceiver {

        private final String TAG = CallingActivity.class.getSimpleName() + "." + CallingActivity.SipBroadcastReceiver.class.getSimpleName();

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Logger.e(TAG, "Intent cannot be null in 'onReceive' method!");
                return;
            }
            final String callback = intent.getStringExtra(CallingService.KEY_CALLBACK);
            switch (callback) {
                case CallingService.CALLBACKS.RINGING:
                    isRinging = true;
                    break;
                case CallingService.CALLBACKS.CALL_ESTABLISHED:
                    isRinging = false;
                    isCallEstablished = true;
                    break;
                case CallingService.CALLBACKS.HANGING_UP_CALL:
                    isCallEstablished = false;
                    break;
                case CallingService.CALLBACKS.CALL_ENDED:
                    isCallEstablished = false;
                    isCallEnded = true;
                    popToFragment(CallingFragment.TAG);
                    break;
                default:
                    Logger.e(TAG, "Unknown event type in 'onReceive' method!");
            }

        }

    }

}
