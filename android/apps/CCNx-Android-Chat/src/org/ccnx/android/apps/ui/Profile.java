package org.ccnx.android.apps.ui;

import org.ccnx.ccnx.CcnxComunicationWorker;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

public class Profile extends Activity implements ChatCallback {
	
	protected static final String PREF_NAMESPACE = "namespace";
	protected static final String PREF_HANDLE = "handle";
	protected static final String PREF_REMOTEHOST = "remotehost";
	protected static final String PREF_REMOTEPORT = "remoteport";
	private final static int EXIT_MENU = 1;
	private final static int SHUTDOWN_MENU = 2;
	protected CcnxComunicationWorker worker;
	protected TextView tv = null;
	protected ScrollView sv = null;
	private MsgHandler msgHandler = new MsgHandler();
	private FieldCleaner fieldCleaner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profil);
		fieldCleaner = new FieldCleaner(this);

		preparePlanButton();
		prepareResetButton();
		prepareSendButton();

		String namespace = getIntent().getStringExtra(PREF_NAMESPACE);
		String username = getIntent().getStringExtra(PREF_HANDLE);
		String remotehost = getIntent().getStringExtra(PREF_REMOTEHOST);
		String remoteport = getIntent().getStringExtra(PREF_REMOTEPORT);

		worker = new CcnxComunicationWorker(this, this);
		worker.start(username, namespace, remotehost, remoteport);
	}

	private void preparePlanButton() {
		Button plan = (Button) findViewById(R.id.btnPlan);
		plan.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent myIntent = new Intent(view.getContext(), Plan.class);
				startActivityForResult(myIntent, 0);
			}
		});
	}

	private void prepareResetButton() {
		Button btn = (Button) findViewById(R.id.btnReset);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fieldCleaner.clearTextFields(R.id.edImie, R.id.edNazwisko,
						R.id.edWzrost, R.id.edWaga);
				fieldCleaner.clearRadioButton(R.id.radioSex);
			}
		});
	}

	private void prepareSendButton() {
		Button sendBtn = (Button) findViewById(R.id.btnWyslij);
		sendBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				worker.send("cos");
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		worker.stop();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_profil, menu);
		menu.add(0, EXIT_MENU, 1, "Exit");
		menu.add(0, SHUTDOWN_MENU, 1, "Exit & Shutdown");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case EXIT_MENU:
			finish();
			return true;
		case SHUTDOWN_MENU:
			worker.shutdown();
			finish();
			return true;
		case R.id.action_settings:
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void recv(String message) {
		Message msg = Message.obtain();
		msg.obj = message;
		msgHandler.sendMessage(msg);
	}

	@Override
	public void ccnxServices(boolean ok) {
		if (ok) {
			recv("CCN Services now ready -- let's chat!\n");
			msgHandler.post(new Runnable() {
				@Override
				public void run() {
				}
			});
		} else {
			recv("CCN Service error, cannot chat!\n");
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
