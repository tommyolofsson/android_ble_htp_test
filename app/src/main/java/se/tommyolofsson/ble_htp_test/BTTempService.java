package se.tommyolofsson.ble_htp_test;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class BTTempService extends Service implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "BTTempService";

    public static final String BROADCAST_SINGLE = "se.tommyolofsson.ble_htp_test.BTTempService.BROADCAST_SINGLE";
    public static final String BROADCAST_SINGLE_VAL = "se.tommyolofsson.ble_htp_test.BTTempService.BROADCAST_SINGLE.VAL";

    private BluetoothAdapter mBTAdapter;
    private double temp;

    @Override
    public void onCreate() {
        final BluetoothManager blm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = blm.getAdapter();
        if (mBTAdapter == null) {
            Log.w(TAG, "BT not available.");
        } else {
            if (!mBTAdapter.isEnabled()) {
                Log.w(TAG, "BT not enabled.");
            } else {
                mBTAdapter.startLeScan(this);
            }
        }
    }

    @Override
    public void onDestroy() {
        mBTAdapter.stopLeScan(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        Log.d(TAG, "Starting service.");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.d(TAG, String.format("Found %s device: %s, %s: %s",
                device.getType(), device.getName(), device.getAddress(),
                Util.byteArrayToString(scanRecord)));

        int pos = 0;
        while (true) {
            int len = scanRecord[pos] & 0xff;
            if (len <= 0)
                break;
            int type = scanRecord[pos+1] & 0xff;
            Log.w(TAG, String.format("Len: %d, Type: %02x", len, type));
            if (type == 0xff) {
                if (((scanRecord[pos + 2] & 0xff) == 0x40)) {
                    // Format:
                    // 0: Length (6)
                    // 1: Type (0xff)
                    // 2: 0x40 -- Temperature reading packet id?
                    // 3-4: -- Varies btw. sensors. Constant in time(?)
                    // 5-6: Temperature as MSB int in 1/128 deg.
                    int tfrac = ((scanRecord[pos+5] & 0xff) << 8) | ((scanRecord[pos+6] & 0xff) << 0);
                    temp = tfrac / 128.0;

                    Intent bc = new Intent(BROADCAST_SINGLE)
                            .putExtra(BROADCAST_SINGLE_VAL, temp);
                    //LocalBroadcastManager.getInstance(this).sendBroadcast(bc);
                    sendBroadcast(bc);
                }
            } else if (type == 0x08) {
            } else if (type == 0x02) {
            }
            pos += len + 1;
        }
    }
}
