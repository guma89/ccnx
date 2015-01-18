package org.ccnx.ccnx;

/*
 * CCNx Android Chat
 *
 * Copyright (C) 2010 Palo Alto Research Center, Inc.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation. 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details. You should have received
 * a copy of the GNU Lesser General Public License along with this library;
 * if not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301 USA.
 */

import java.io.IOException;
import java.util.Arrays;

import org.ccnx.android.apps.ui.callback.ChatCallback;
import org.ccnx.android.apps.ui.interfaces.transferObjects.SportData;
import org.ccnx.android.ccnlib.CCNxConfiguration;
import org.ccnx.android.ccnlib.CCNxServiceCallback;
import org.ccnx.android.ccnlib.CCNxServiceControl;
import org.ccnx.android.ccnlib.CCNxServiceStatus.SERVICE_STATUS;
import org.ccnx.android.ccnlib.CcndWrapper.CCND_OPTIONS;
import org.ccnx.android.ccnlib.RepoWrapper.REPO_OPTIONS;
import org.ccnx.ccn.apps.ccnchat.CCNChatNet;
import org.ccnx.ccn.apps.ccnchat.CCNChatNet.CCNChatCallback;
import org.ccnx.ccn.profiles.ccnd.CCNDaemonException;
import org.ccnx.ccn.profiles.ccnd.SimpleFaceControl;
import org.ccnx.database.SportsmanDatabaseHandler;

import android.content.Context;
import android.util.Log;

public class CcnxComunicationWorker implements Runnable, CCNxServiceCallback,
		CCNChatCallback {
	protected CCNChatNet chatNet;
	protected final ChatCallback chatCallback;
	protected final Context context;
	protected CCNxServiceControl ccnxService;
	protected final Thread thd;
	protected boolean running = false;
	protected boolean finished = true;
	protected String remotehost = null;
	protected String remoteport = "9695";
	private SportsmanDatabaseHandler db;

	public CcnxComunicationWorker(Context ctx, ChatCallback callback,
			SportsmanDatabaseHandler db) {
		context = ctx;
		thd = new Thread(this, "ChatWorker");
		chatCallback = callback;
		CCNxConfiguration.config(ctx, false);
		this.db = db;
	}

	public synchronized void start(String username, String namespace,
			String remotehost, String remoteport) {
		if (false == running) {
			try {
				this.remotehost = remotehost;
				this.remoteport = remoteport;
				chatNet = new CCNChatNet(this, namespace);
				running = true;
				finished = false;
				thd.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void stop() {
		if (!finished) {
			finished = true;
			try {
				chatNet.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ccnxService.disconnect();
		}
	}

	public synchronized void shutdown() {
		if (!finished) {
			finished = true;
			try {
				chatNet.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				ccnxService.stopAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized boolean send(String text) {
		try {
			SportData data = createObjectToSend();
			String toSend = DataSerializer.serializeObject(data);
			chatNet.sendMessage(toSend);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	private SportData createObjectToSend() {
		return new SportData(db.getLatestProfile(), Arrays.asList(db
				.getLastTraining()));
	}

	@Override
	public void recvMessage(String message) {
		SportData data = unserializeData(message);
		if (data != null) {
			db.addProfile(data.getSportsman());
			db.addTrainings(data.getPlan());
			chatCallback.recv(message);
		}
	}

	private SportData unserializeData(String message) {
		try {
			SportData data = (SportData) DataSerializer
					.unserializeObject(message);
			return data;
		} catch (Exception ex) {
			Log.d("ccnx", "Wrong incomming object");
		}
		return null;
	}

	@Override
	public void run() {
		service_run();
	}

	protected void service_run() {
		if (initializeCCNx()) {
			try {
				chatNet.listen();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean initializeCCNx() {
		ccnxService = new CCNxServiceControl(context);
		ccnxService.registerCallback(this);
		ccnxService.setCcndOption(CCND_OPTIONS.CCND_DEBUG, "1");
		ccnxService.setRepoOption(REPO_OPTIONS.REPO_DEBUG, "WARNING");
		return ccnxService.startAll();
	}

	@Override
	public void newCCNxStatus(SERVICE_STATUS serviceStatus) {
		if (chatCallback != null) {
			switch (serviceStatus) {
			case START_ALL_DONE:
				connectTcp();
				break;
			case START_ALL_ERROR:
				chatCallback.ccnxServices(false);
				break;
			}
		}
	}

	private void connectTcp() {
		try {
			if (checkThatRemoteHostExists()) {
				SimpleFaceControl.getInstance().connectTcp(remotehost,
						Integer.parseInt(remoteport));
			} else {
				SimpleFaceControl.getInstance().openMulicastInterface();
			}
			chatCallback.ccnxServices(true);
		} catch (CCNDaemonException e) {
			e.printStackTrace();
			chatCallback.ccnxServices(false);
		}
	}

	private boolean checkThatRemoteHostExists() {
		return remotehost != null & !remotehost.isEmpty();
	}

}
