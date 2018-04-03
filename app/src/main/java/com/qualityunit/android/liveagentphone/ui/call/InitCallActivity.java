package com.qualityunit.android.liveagentphone.ui.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.qualityunit.android.liveagentphone.Const;
import com.qualityunit.android.liveagentphone.R;
import com.qualityunit.android.liveagentphone.service.CallingCommands;
import com.qualityunit.android.liveagentphone.service.CallingException;

/**
 * Created by rasto on 18.10.16.
 */

public class InitCallActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    public static final String INTENT_FILTER_INIT_CALL = "com.qualityunit.android.liveagentphone.INITCALL";
    private FloatingActionButton fabMakeCall;
    private FloatingActionButton fabCancelCall;
    private Toolbar toolbar;
    private String contactName;
    private String dialString;
    private String remoteNumber;
    private PowerManager.WakeLock wakeLock;

    private boolean isReceiverRegistered;
    private BroadcastReceiver initCallReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (Const.Push.PUSH_TYPE_CANCEL_INIT_CALL.equals(bundle.getString("type"))) {
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.init_call_activity);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getSimpleName());
            wakeLock.acquire(10*60*1000L /*10 minutes*/);
        }
        registerReceiver();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        fabMakeCall = (FloatingActionButton) findViewById(R.id.fab_makeCall);
        if (fabMakeCall != null) {
            fabMakeCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        finish();
                        CallingCommands.makeCall(getApplicationContext(), dialString, "", TextUtils.isEmpty(contactName) ? remoteNumber : contactName);
                    } catch (CallingException e) {
                        Toast.makeText(InitCallActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        fabCancelCall = (FloatingActionButton) findViewById(R.id.fab_cancelCall);
        if (fabCancelCall != null) {
            fabCancelCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
        onBackStackChanged();
        fillUi();
    }

    private void fillUi() {
        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            return;
        }
        dialString = bundle.getString("dialString");
        contactName = bundle.getString("contactName");
        remoteNumber = bundle.getString("number");
        TextView tvPrimary = (TextView) findViewById(R.id.tv_primary);
        TextView tvSecondary = (TextView) findViewById(R.id.tv_secondary);
        if (!TextUtils.isEmpty(contactName)) {
            tvPrimary.setText(contactName);
            tvSecondary.setText(remoteNumber);
            tvSecondary.setVisibility(View.VISIBLE);
        } else {
            tvSecondary.setVisibility(View.GONE);
            tvPrimary.setText(remoteNumber);
        }
    }

    @Override
    public void onBackStackChanged() {
        if (getSupportActionBar() != null && toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        fillUi();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver();
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            isReceiverRegistered = true;
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(initCallReceiver, new IntentFilter(INTENT_FILTER_INIT_CALL));
        }
    }

    private void unregisterReceiver() {
        if (isReceiverRegistered) {
            isReceiverRegistered = false;
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(initCallReceiver);
        }
    }

}
