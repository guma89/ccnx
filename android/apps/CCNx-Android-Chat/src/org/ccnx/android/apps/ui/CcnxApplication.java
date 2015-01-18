package org.ccnx.android.apps.ui;

import android.app.Application;
import android.content.Context;

public class CcnxApplication extends Application {
	private static Context context;

    public void onCreate(){
        super.onCreate();
        CcnxApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return CcnxApplication.context;
    }

}
