package com.qualityunit.android.sip;

import org.pjsip.pjsua2.Account;
import org.pjsip.pjsua2.OnIncomingCallParam;
import org.pjsip.pjsua2.OnInstantMessageParam;
import org.pjsip.pjsua2.OnRegStateParam;

public class SipAccount extends Account {
    private SipCore sipCore;

    public SipAccount(SipCore sipCore) {
        super();
        this.sipCore = sipCore;
    }

    @Override
    public void onRegState(OnRegStateParam prm) {
        sipCore.observer.notifyRegState(prm.getCode(), prm.getReason(), prm.getExpiration());
    }

    @Override
    public void onIncomingCall(OnIncomingCallParam prm) {
        int callId = prm.getCallId();
        System.out.println("======== Incoming call - call id: " + callId +  " ======== ");
        SipCall call = new SipCall(this, callId, sipCore);
        sipCore.observer.notifyIncomingCall(call);
    }

    @Override
    public void onInstantMessage(OnInstantMessageParam prm) {
       // nothing
    }
}
