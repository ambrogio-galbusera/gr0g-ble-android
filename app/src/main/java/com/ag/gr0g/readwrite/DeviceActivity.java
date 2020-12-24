package com.ag.gr0g.readwrite;

import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.ag.gr0g.util.BleUtil;
import com.ag.gr0g.util.BleUuid;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class DeviceActivity extends Activity implements View.OnClickListener {
	private static final String TAG = "BLEDevice";

	public static final String EXTRA_BLUETOOTH_DEVICE = "BT_DEVICE";
	private BluetoothAdapter mBTAdapter;
	private BluetoothDevice mDevice;
	private BluetoothGatt mConnGatt;
	private int mStatus;

	private Button mLightRelease;
	private EditText mLight;
	private Switch mSwLight;
	private EditText mTemperature;
	private EditText mTemperatureSetpoint;
	private EditText mHumidity;
	private EditText mHumiditySetpoint;
	private Timer mTimer;

	class UpdateDataTask extends TimerTask {

		int mIndex = 0;

		private void Read(View v)
		{
			if ((v.getTag() != null)
					&& (v.getTag() instanceof BluetoothGattCharacteristic)) {
				BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v.getTag();
				if (mConnGatt.readCharacteristic(ch)) {
					//setProgressBarIndeterminateVisibility(true);
				}
			}
		}

		public void run() {
			//calculate the new position of myBall
			if (mStatus == BluetoothProfile.STATE_CONNECTED) {
				if (mIndex == 0)
					Read(mLight);
				else if (mIndex == 1)
					Read(mTemperature);
				else if (mIndex == 2)
					Read(mTemperatureSetpoint);
				else if (mIndex == 3)
					Read(mHumidity);
				else if (mIndex == 4)
					Read(mHumiditySetpoint);

				mIndex = (mIndex + 1) % 5;
			}
		}
	}

	private final BluetoothGattCallback mGattcallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status,
				int newState) {
			if (newState == BluetoothProfile.STATE_CONNECTED) {
				mStatus = newState;
				mConnGatt.discoverServices();
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				mStatus = newState;

				mConnGatt = mDevice.connectGatt(null, false, mGattcallback, BluetoothDevice.TRANSPORT_LE);
				mStatus = BluetoothProfile.STATE_CONNECTING;

				runOnUiThread(new Runnable() {
					public void run() {
					};
				});
			}
		};

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			for (BluetoothGattService service : gatt.getServices()) {
				if ((service == null) || (service.getUuid() == null)) {
					continue;
				}
				if (BleUuid.SERVICE_STATUS.equalsIgnoreCase(service
						.getUuid().toString())) {

					mTemperature.setTag(service
							.getCharacteristic(UUID
									.fromString(BleUuid.CHAR_TEMPERATURE)));
					mTemperatureSetpoint.setTag(service
							.getCharacteristic(UUID
									.fromString(BleUuid.CHAR_TEMPERATURE_SETPOINT)));
					mHumidity.setTag(service
							.getCharacteristic(UUID
									.fromString(BleUuid.CHAR_HUMIDITY)));
					mHumiditySetpoint.setTag(service
							.getCharacteristic(UUID
									.fromString(BleUuid.CHAR_HUMIDITY_SETPOINT)));
					mLight.setTag(service
							.getCharacteristic(UUID
									.fromString(BleUuid.CHAR_LIGHT)));
					mSwLight.setTag(service
							.getCharacteristic(UUID
									.fromString(BleUuid.CHAR_LIGHT_CONTROL)));

					mLightRelease.setTag(service
							.getCharacteristic(UUID
									.fromString(BleUuid.CHAR_LIGHT_CONTROL)));
				}
			}

			runOnUiThread(new Runnable() {
				public void run() {
					setProgressBarIndeterminateVisibility(false);
				};
			});
		};

		private void reverse(byte[] array) {
			if (array == null) {
				return;
			}
			int i = 0;
			int j = array.length - 1;
			byte tmp;
			while (j > i) {
				tmp = array[j];
				array[j] = array[i];
				array[i] = tmp;
				j--;
				i++;
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (BleUuid.CHAR_TEMPERATURE
					.equalsIgnoreCase(characteristic.getUuid().toString())) {
					final byte[] bvalue = characteristic.getValue();
					reverse(bvalue);
					double value = ByteBuffer.wrap(bvalue).getDouble();;

					runOnUiThread(new Runnable() {
						public void run() {
							DecimalFormat df = new DecimalFormat("#.#");
							mTemperature.setText(df.format(value));
							setProgressBarIndeterminateVisibility(false);
						};
					});
				} else if (BleUuid.CHAR_TEMPERATURE_SETPOINT
						.equalsIgnoreCase(characteristic.getUuid().toString())) {
					final byte[] bvalue = characteristic.getValue();
					reverse(bvalue);
					double value = ByteBuffer.wrap(bvalue).getDouble();;

					runOnUiThread(new Runnable() {
						public void run() {
							DecimalFormat df = new DecimalFormat("#.#");
							mTemperatureSetpoint.setText(df.format(value));
							setProgressBarIndeterminateVisibility(false);
						};
					});
				} else if (BleUuid.CHAR_HUMIDITY
						.equalsIgnoreCase(characteristic.getUuid().toString())) {
					final byte[] bvalue = characteristic.getValue();
					reverse(bvalue);
					double value = ByteBuffer.wrap(bvalue).getDouble();;

					runOnUiThread(new Runnable() {
						public void run() {
							DecimalFormat df = new DecimalFormat("#.#");
							mHumidity.setText(df.format(value));
							setProgressBarIndeterminateVisibility(false);
						};
					});
				} else if (BleUuid.CHAR_HUMIDITY_SETPOINT
						.equalsIgnoreCase(characteristic.getUuid().toString())) {
					final byte[] bvalue = characteristic.getValue();
					reverse(bvalue);
					double value = ByteBuffer.wrap(bvalue).getDouble();;

					runOnUiThread(new Runnable() {
						public void run() {
							DecimalFormat df = new DecimalFormat("#.#");
							mHumiditySetpoint.setText(df.format(value));
							setProgressBarIndeterminateVisibility(false);
						};
					});
				} else if (BleUuid.CHAR_LIGHT
						.equalsIgnoreCase(characteristic.getUuid().toString())) {
					final byte[] bvalue = characteristic.getValue();
					reverse(bvalue);
					double value = ByteBuffer.wrap(bvalue).getDouble();

					runOnUiThread(new Runnable() {
						public void run() {
							DecimalFormat df = new DecimalFormat("#.#");
							mLight.setText(df.format(value));
							setProgressBarIndeterminateVisibility(false);
						};
					});
				}

			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt,
				BluetoothGattCharacteristic characteristic, int status) {

			runOnUiThread(new Runnable() {
				public void run() {
					setProgressBarIndeterminateVisibility(false);
				};
			});
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_device);

		// state
		mStatus = BluetoothProfile.STATE_DISCONNECTED;

		mTemperature = (EditText) findViewById(R.id.txtTemperature);
		mTemperatureSetpoint = (EditText) findViewById(R.id.txtTemperatureSetpoint);
		mTemperatureSetpoint.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mTemperatureSetpoint.getText().length() == 0)
					return;

				if ((mTemperatureSetpoint.getTag() != null)
						&& (mTemperatureSetpoint.getTag() instanceof BluetoothGattCharacteristic)) {
					BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) mTemperatureSetpoint
							.getTag();
					ch.setValue(new byte[] { (byte) Integer.parseInt(mTemperatureSetpoint.getText().toString()) });
					if (mConnGatt.writeCharacteristic(ch)) {
						setProgressBarIndeterminateVisibility(true);
					}
				}
			}
		});

		mHumidity = (EditText) findViewById(R.id.txtHumidity);
		mHumiditySetpoint = (EditText) findViewById(R.id.txtHumiditySetpoint);
		mHumiditySetpoint.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mHumiditySetpoint.getText().length() == 0)
					return;

				if ((mHumiditySetpoint.getTag() != null)
						&& (mHumiditySetpoint.getTag() instanceof BluetoothGattCharacteristic)) {
					BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) mHumiditySetpoint
							.getTag();
					ch.setValue(new byte[] { (byte) Integer.parseInt(mHumiditySetpoint.getText().toString()) });
					if (mConnGatt.writeCharacteristic(ch)) {
						setProgressBarIndeterminateVisibility(true);
					}
				}
			}
		});

		mLight = (EditText) findViewById(R.id.txtLight);
		mSwLight = (Switch) findViewById(R.id.swLight);
		mSwLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				View v = (View) buttonView;
				if ((v.getTag() != null)
						&& (v.getTag() instanceof BluetoothGattCharacteristic)) {
					BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v
							.getTag();
					ch.setValue(new byte[] { (byte) (isChecked? '1' : '0'), (byte)0 });
					if (mConnGatt.writeCharacteristic(ch)) {
						setProgressBarIndeterminateVisibility(true);
					}
				}
			}
		});

		mLightRelease = (Button) findViewById(R.id.cmdLightRelease);
		mLightRelease.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		init();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mConnGatt != null) {
			if ((mStatus != BluetoothProfile.STATE_DISCONNECTING)
					&& (mStatus != BluetoothProfile.STATE_DISCONNECTED)) {
				mConnGatt.disconnect();
			}
			mConnGatt.close();
			mConnGatt = null;
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.cmdLightRelease) {
			if ((v.getTag() != null)
					&& (v.getTag() instanceof BluetoothGattCharacteristic)) {
				BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) v
						.getTag();
				ch.setValue(new byte[]{(byte) '2', (byte) 0});
				if (mConnGatt.writeCharacteristic(ch)) {
					setProgressBarIndeterminateVisibility(true);
				}
			}
		}
	}

	private void init() {
		// BLE check
		if (!BleUtil.isBLESupported(this)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

		// BT check
		BluetoothManager manager = BleUtil.getManager(this);
		if (manager != null) {
			mBTAdapter = manager.getAdapter();
		}
		if (mBTAdapter == null) {
			Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT)
					.show();
			finish();
			return;
		}

		// check BluetoothDevice
		if (mDevice == null) {
			mDevice = getBTDeviceExtra();
			if (mDevice == null) {
				finish();
				return;
			}
		}

		// connect to Gatt
		if ((mConnGatt == null)
				&& (mStatus == BluetoothProfile.STATE_DISCONNECTED)) {
			// try to connect
			mConnGatt = mDevice.connectGatt(this, false, mGattcallback);
			mStatus = BluetoothProfile.STATE_CONNECTING;
		} else {
			if (mConnGatt != null) {
				// re-connect and re-discover Services
				mConnGatt.connect();
				mConnGatt.discoverServices();
			} else {
				Log.e(TAG, "state error");
				finish();
				return;
			}
		}

		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new UpdateDataTask(), 0, 2000);

		setProgressBarIndeterminateVisibility(true);
	}

	private BluetoothDevice getBTDeviceExtra() {
		Intent intent = getIntent();
		if (intent == null) {
			return null;
		}

		Bundle extras = intent.getExtras();
		if (extras == null) {
			return null;
		}

		return extras.getParcelable(EXTRA_BLUETOOTH_DEVICE);
	}

}
