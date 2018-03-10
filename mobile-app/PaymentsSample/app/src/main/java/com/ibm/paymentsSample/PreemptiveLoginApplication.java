package com.ibm.paymentsSample;

import android.app.Application;

import com.worklight.wlclient.api.WLClient;

/**
 * Created by bob on 01/03/18.
 */

public class PreemptiveLoginApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize the MobileFirst SDK. This needs to happen just once.
        WLClient.createInstance(this);
        //Initialize the challenge handler
        LoginChallengeHandler.createAndRegister();
    }
}
