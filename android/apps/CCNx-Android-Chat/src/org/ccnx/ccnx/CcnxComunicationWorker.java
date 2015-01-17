package org.ccnx.ccnx;

import java.io.IOException;

import org.ccnx.android.apps.ui.ChatCallback;
import org.ccnx.android.ccnlib.CCNxConfiguration;
import org.ccnx.android.ccnlib.CCNxServiceCallback;
import org.ccnx.android.ccnlib.CCNxServiceControl;
import org.ccnx.android.ccnlib.CCNxServiceStatus.SERVICE_STATUS;
import org.ccnx.android.ccnlib.CcndWrapper.CCND_OPTIONS;
import org.ccnx.android.ccnlib.RepoWrapper.REPO_OPTIONS;
import org.ccnx.ccn.apps.ccnchat.CCNChatNet;
import org.ccnx.ccn.apps.ccnchat.CCNChatNet.CCNChatCallback;
import org.ccnx.ccn.config.ConfigurationException;
import org.ccnx.ccn.profiles.ccnd.CCNDaemonException;
import org.ccnx.ccn.profiles.ccnd.SimpleFaceControl;
import android.content.Context;
import android.util.Log;

public class CcnxComunicationWorker implements Runnable, CCNxServiceCallback, CCNChatCallback {
	protected final static String TAG="ChatWorker";
	protected CCNChatNet _chat;
	protected final ChatCallback chatCallback;
	protected final Context context;
	protected CCNxServiceControl ccnxService;
	protected final Thread thd;
	protected boolean running = false;
	protected boolean finished = true;
	protected String remotehost = null;
	protected String remoteport = "9695";

	public CcnxComunicationWorker(Context ctx, ChatCallback callback) {
		context = ctx;
		thd = new Thread(this, "ChatWorker");
		chatCallback = callback;
		CCNxConfiguration.config(ctx, false);
	}

	public synchronized void start(String username, String namespace, String remotehost, String remoteport) {
		if( false == running ) {
			try {
				this.remotehost = remotehost;
				this.remoteport = remoteport;
				_chat = new CCNChatNet(this, namespace);
				running = true;
				finished = false;
				thd.start();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void stop() {
		if( !finished ) {
			finished = true;
			try {
				_chat.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
			ccnxService.disconnect();
		}
	}

	public synchronized void shutdown() {
		if( !finished ) {
			finished = true;
			try {
				_chat.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				ccnxService.stopAll();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized boolean send(String text) {
		Log.d(TAG, "send text = " + text);

		try {
			_chat.sendMessage(text);
		} catch(Exception e) {
			return false;
		}

		return true;
	}

	@Override
	public void run() {
		service_run();
	}

	protected void service_run() {
		// Startup CCNx in a blocking call
		if( !initializeCCNx() ) {
			Log.e(TAG, "Could not start CCNx services!");
		} else {
			Log.i(TAG,"Starting ccnChatNet.listen() loop");
			// Now do the Chat event loop
			try {
				_chat.listen();
			} catch (ConfigurationException e) {
				System.err.println("Configuration exception running ccnChat: "
						+ e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("IOException handling chat messages: "
						+ e.getMessage());
				e.printStackTrace();
			} catch(Exception e) {
				System.err.println("Exception handling chat messages: "
						+ e.getMessage());
				e.printStackTrace();	
			}
		}
		Log.i(TAG, "service_run() exits");
	}

	private boolean initializeCCNx() {
		ccnxService = new CCNxServiceControl(context);
		ccnxService.registerCallback(this);
		ccnxService.setCcndOption(CCND_OPTIONS.CCND_DEBUG, "1");
		ccnxService.setRepoOption(REPO_OPTIONS.REPO_DEBUG, "WARNING");
		return ccnxService.startAll();
	}

	/**
	 * Called from CCNxServiceControl
	 */
	@Override
	public void newCCNxStatus(SERVICE_STATUS st) {
		// NOw pass on the status to the app
		if( null != chatCallback ) {
			switch(st) {
			case START_ALL_DONE:
				try {
					// If we specified a remote host, use it not multicast
					if( null != remotehost && remotehost.length() > 0 ) {
						SimpleFaceControl.getInstance().connectTcp(remotehost, Integer.parseInt(remoteport));
					} else {
						SimpleFaceControl.getInstance().openMulicastInterface();
					}
					chatCallback.ccnxServices(true);
				} catch (CCNDaemonException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					chatCallback.ccnxServices(false);
				}

				break;
			case START_ALL_ERROR:
				chatCallback.ccnxServices(false);
				break;
			}
		}	
	}

	/**
	 * called from ccnChatNet when there's a new message.
	 * Pass it on to the UI.
	 */
	@Override
	public void recvMessage(String message) {
		Log.d(TAG, "recv text = " + message);
		chatCallback.recv(message);
	}
}
