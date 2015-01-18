package org.ccnx.android.apps.ui.callback;

import org.ccnx.android.apps.ui.CcnxApplication;
import org.ccnx.ccnx.CcnxComunicationWorker;
import org.ccnx.database.SportsmanDatabaseHandler;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.ScrollView;
import android.widget.TextView;

public class CallbackGuiUpdater implements ChatCallback {
	private static final String UPDATE_STATE = "UPDATE PLEASE :)";
	private CcnxComunicationWorker worker;
	private MsgHandler msgHandler = new MsgHandler();
	private SportsmanDatabaseHandler db;
	
	
	protected CallbackGuiUpdater(String username, String namespace, String remotehost, String remotePort) {
		db = new SportsmanDatabaseHandler(CcnxApplication.getAppContext(), null, null, 1);
		prepareAndStartCcnxWorker(username, namespace, remotehost, remotePort);
	}
	
	private void prepareAndStartCcnxWorker(String username, String namespace, String remotehost, String remotePort) {
		worker = new CcnxComunicationWorker(CcnxApplication.getAppContext(), this, db);
		worker.start(username, namespace, remotehost, remotePort);
	}
	
	@Override
	public void recv(String message) {
		Intent intent = new Intent();
		intent.setAction("com.unitedcoders.android.broadcasttest.SHOWTOAST");
		CcnxApplication.getAppContext().sendBroadcast(intent); 		
	}
	
	public void send() {
		worker.send(UPDATE_STATE);
	}

	@Override
	public void ccnxServices(boolean ok) {
		if (ok) {
			msgHandler.post(new Runnable() {
				@Override
				public void run() {
				}
			});
		}
	}
    
	class MsgHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			String text = (String) msg.obj;
			msg.obj = null;
			msg = null;
		}
	}
}
