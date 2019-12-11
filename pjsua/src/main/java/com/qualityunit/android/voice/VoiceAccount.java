package com.qualityunit.android.voice;

import android.util.Log;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.AccountConfig;
import org.pjsip.pjsua2.AuthCredInfo;
import org.pjsip.pjsua2.AuthCredInfoVector;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnRegStateParam;
import org.pjsip.pjsua2.pjsip_status_code;

public class VoiceAccount extends Account {

    private final String TAG = VoiceAccount.class.getSimpleName();
    private Callbacks callbacks;
    private boolean isRegistered;

    public VoiceAccount(Callbacks callbacks) {
        super();
        this.callbacks = callbacks;
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        pjsip_status_code code = prm.getCode();
        int expiration = prm.getExpiration();
        try {
            int returnCode = code.swigValue();
            if (returnCode / 100 == 2 && expiration != 0) {
                if (!isRegistered) {
                    Log.d(TAG, "#### onRegState: SUCCESS");
                    callbacks.onSipRegistrationSuccess();
                    isRegistered = true;
                }
            } else {
                isRegistered = false;
                String errMsg = "SERVICE: Error while SIP registration: Asterisk returned code '" + returnCode + "'";
                Log.d(TAG, "#### onRegState: FAILURE");
                callbacks.onSipRegistrationFailure(errMsg);
            }
        } catch (Exception e) {
            callbacks.onSipRegistrationFailure(e.getMessage());
        }
    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        int callId = prm.getCallId();
        Log.d(TAG, "#### onIncomingCall: " + callId);
        callbacks.onIncomingVoiceCall(callId);
    }

    public static AccountConfig createAccountConfig(String sipHost, String sipUser, String sipPassword) {
        // create URIs
        String sipRegisterUri = "sip:" + sipHost;
        String sipAccountUri = "sip:" + sipUser + "@" + sipHost;
        // set voiceAccount config
        AccountConfig sipAccountConfig = new AccountConfig();
        sipAccountConfig.getRegConfig().setRegistrarUri(sipRegisterUri);
        sipAccountConfig.getRegConfig().setRegisterOnAdd(true);
        sipAccountConfig.setIdUri(sipAccountUri);
        AuthCredInfoVector creds = sipAccountConfig.getSipConfig().getAuthCreds();
        creds.clear();
        creds.add(new AuthCredInfo("digest", "*", sipUser, 0, sipPassword));
        sipAccountConfig.getSipConfig().setAuthCreds(creds);
        sipAccountConfig.getNatConfig().setIceEnabled(true);
        sipAccountConfig.getVideoConfig().setAutoTransmitOutgoing(false);
        sipAccountConfig.getVideoConfig().setAutoShowIncoming(false);
        return sipAccountConfig;
    }

    public void register(String sipHost, String sipUser, String sipPassword) throws Exception {
        Log.d(TAG, "#### register");
        create(createAccountConfig(sipHost, sipUser, sipPassword), true);
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public interface Callbacks {
        void onSipRegistrationSuccess();
        void onSipRegistrationFailure(String errorMessage);
        void onIncomingVoiceCall(int callId);
    }

}
