package org.ccnx.android.apps.ui.interfaces;

import org.ccnx.android.apps.ui.interfaces.transferObjects.SportData;


/**
 * Created by Piotrek on 2015-01-14.
 */
public interface Comunication {

    public void send(SportData data);

    public SportData receive();

}

