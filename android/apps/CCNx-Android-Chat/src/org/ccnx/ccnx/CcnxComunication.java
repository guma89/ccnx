package org.ccnx.ccnx;

import org.ccnx.android.apps.ui.interfaces.Comunication;
import org.ccnx.android.apps.ui.interfaces.transferObjects.SportData;
import org.ccnx.ccnx.interfaces.NotificationListener;

public class CcnxComunication implements Comunication {
    private NotificationListener listener;

    @Override
    public void send(SportData data) {

    }

    @Override
    public SportData receive() {
        return null;
    }

    public void notifyAboutUpdates() {
        listener.updateState();
    }
}

