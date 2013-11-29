package co.mscsea.imageviewer;


import co.mscsea.imageviewer.MainActivity;

import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.mediacontrol.Smc;
import com.samsung.android.sdk.mediacontrol.SmcDevice;
import com.samsung.android.sdk.mediacontrol.SmcDeviceFinder;
import com.samsung.android.sdk.mediacontrol.SmcImageViewer;
import com.samsung.android.sdk.mediacontrol.SmcItem;
import com.samsung.android.sdk.mediacontrol.SmcDeviceFinder.DeviceListener;
import com.samsung.android.sdk.mediacontrol.SmcDeviceFinder.StatusListener;

import java.util.List;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	Smc smc;
	TextView devices;
	Button show;
	SmcDeviceFinder deviceFinder;
	SmcDevice target;
	SmcItem photoItem;
	List<SmcDevice> deviceList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (deviceFinder != null) {
			deviceFinder.stop();

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("img", "onResume");
		devices = (TextView) findViewById(R.id.devices);
		show = (Button) findViewById(R.id.button1);
		show.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				photoItem = new SmcItem(
						new SmcItem.WebContent(
								Uri.parse("http://www.samsung.com/sg/consumer-images/"
										+ "product/smartphone/2013/GT-I9505ZKAXSP/"
										+ "features/GT-I9505ZKAXSP-16-0.jpg"),
								MimeTypeMap.getSingleton()
										.getMimeTypeFromExtension("jpg")));
				if (target != null) {
					((SmcImageViewer) target).show(photoItem);
				} else {
					Log.d("IMG", "No target device");
				}

			}
		});

		show.setEnabled(false);

		smc = new Smc();
		try {
			smc.initialize(getBaseContext());
		} catch (SsdkUnsupportedException e) {
			Toast.makeText(this, "Not supported", Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		deviceFinder = new SmcDeviceFinder(this);
		deviceFinder.setStatusListener(finderListener);
		deviceFinder.start();

	}

	private StatusListener finderListener = new StatusListener() {

		@Override
		public void onStopped(SmcDeviceFinder deviceFinder) {
			if (MainActivity.this.deviceFinder == deviceFinder) {
				deviceFinder.setStatusListener(null);
				MainActivity.this.deviceFinder = null;
			}
		}

		@Override
		public void onStarted(SmcDeviceFinder deviceFinder, int error) {
			if (error == Smc.SUCCESS) {
				MainActivity.this.deviceFinder = deviceFinder;
				MainActivity.this.searchDevices();
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private DeviceListener deviceListener = new DeviceListener() {

		@Override
		public void onDeviceRemoved(SmcDeviceFinder deviceFinder,
				SmcDevice device, int error) {
			deviceList = deviceFinder.getDeviceList(SmcDevice.TYPE_IMAGEVIEWER);
			devices.setText("Found " + deviceList.size() + "devices.");
			if (deviceList.size() > 0) {
				target = deviceList.get(0);
				devices.append("Connecting to " + target.getName().toString());
				show.setEnabled(true);
			} else {
				show.setEnabled(false);
			}
		}

		@Override
		public void onDeviceAdded(SmcDeviceFinder deviceFinder, SmcDevice device) {

			deviceList = deviceFinder.getDeviceList(SmcDevice.TYPE_IMAGEVIEWER);
			devices.setText("Found " + deviceList.size() + "devices.");
			if (deviceList.size() > 0) {
				target = deviceList.get(0);
				devices.append("Connecting to " + target.getName().toString());
				show.setEnabled(true);
			} else {
				show.setEnabled(false);
			}
		}
	};

	protected void searchDevices() {

		Log.d("IMG", "Searching for devices");
		deviceList = deviceFinder.getDeviceList(SmcDevice.TYPE_IMAGEVIEWER);
		deviceFinder.setDeviceListener(SmcDevice.TYPE_IMAGEVIEWER,
				deviceListener);
		deviceFinder.rescan();
	}

}
