package se.tommyolofsson.ble_htp_test;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.*;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQ_ENABLE_BT = 1;

    private TextView tempText;
    private XYPlot plot;
    private BluetoothAdapter mBTAdapter;
    private BroadcastReceiver tempRecv;
    private ArrayList<Number> tempReadings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect resources.
        tempText = (TextView) findViewById(R.id.temp);
        plot = (XYPlot) findViewById(R.id.plot);

        tempReadings = new ArrayList<Number>();
        tempRecv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                double t = intent.getDoubleExtra(BTTempService.BROADCAST_SINGLE_VAL, -1.0);
                tempText.setText(String.format("%f", t));
                tempReadings.add(t);
                plotData();
            }
        };
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

    protected void onActivityResult(int req, int res, Intent data) {
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
        Intent startIntent = new Intent(this, BTTempService.class);
        startService(startIntent);
        IntentFilter ifilt = new IntentFilter();
        ifilt.addAction(BTTempService.BROADCAST_SINGLE);
        registerReceiver(tempRecv, ifilt);
    }

    private void plotData() {
        Log.d(TAG, "Updating plot");
        XYSeries series1 = new SimpleXYSeries(tempReadings,
            SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, "Sensor 1");
        LineAndPointFormatter series1Format = new LineAndPointFormatter();
        //series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_labels);

        plot.clear();
        plot.addSeries(series1, series1Format);
        plot.redraw();
    }
}
