package se.tommyolofsson.ble_htp_test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements BluetoothAdapter.LeScanCallback {
    private static final String TAG = "MainActivity";
    private static final int REQ_ENABLE_BT = 1;

    private TextView tempText;

    private BluetoothAdapter mBTAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tempText = (TextView) findViewById(R.id.temp);

        setupSensors();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onAvtivityResult(int req, int res, Intent data) {
        if (req == REQ_ENABLE_BT) {
            startScan();
        }
    }

    private void setupSensors() {
        final BluetoothManager blm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBTAdapter = blm.getAdapter();
        if (mBTAdapter == null) {
            tempText.setText("BT not available.");
        } else {
            if (!mBTAdapter.isEnabled()) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBT, REQ_ENABLE_BT);
            } else {
                startScan();
            }
        }
    }

    private void startScan() {
        mBTAdapter.startLeScan(this);
    }

    private String byteArrayToString(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length; i++)
            sb.append(String.format("0x%02x ", a[i]));
        return sb.toString().trim();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.w(TAG, String.format("Found %s device: %s, %s: %s",
                device.getType(), device.getName(), device.getAddress(),
                byteArrayToString(scanRecord)));

        // TODO: Extract.
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
                    double t = tfrac / 128.0;
                    tempText.setText(String.format("Current temperature: %f", t));
                }
            } else if (type == 0x08) {
            } else if (type == 0x02) {
            }
            pos += len + 1;
        }
    }
}
