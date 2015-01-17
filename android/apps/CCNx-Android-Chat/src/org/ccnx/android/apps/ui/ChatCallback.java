package org.ccnx.android.apps.ui;

public interface ChatCallback {

	public void recv(String message);

	public void ccnxServices(boolean ok);
}
