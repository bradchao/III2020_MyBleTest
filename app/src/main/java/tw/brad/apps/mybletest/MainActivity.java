package tw.brad.apps.mybletest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

public class MainActivity extends AppCompatActivity {
    private TextView mesg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mesg = findViewById(R.id.mesg);

        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED){
            init();
        }else{
            requestPermissions(
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION},
                    8);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 8){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                init();
            }else{
                finish();
            }
        }
    }
    private BluetoothClient mClient;
    private SearchResult arix1;
    private boolean isBTOpen;
    private final BluetoothStateListener mBluetoothStateListener = new BluetoothStateListener() {
        @Override
        public void onBluetoothStateChanged(boolean openOrClosed) {
            isBTOpen = openOrClosed;
            Log.v("bradlog", "bt:" + isBTOpen);
        }
    };

    private boolean isInitOpenBT;
    private void init(){
        mClient = new BluetoothClient(this);
        mClient.registerBluetoothStateListener(mBluetoothStateListener);
        if (!mClient.isBluetoothOpened()){
            mClient.openBluetooth();
        }else{
            isInitOpenBT = true;
        }
    }
    public void scanDevices(View view) {
        if (!isBTOpen) return;;
        SearchRequest request = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                .build();
        mClient.search(request, new SearchResponse() {
            @Override
            public void onSearchStarted() {
                Log.v("bradlog", "scan...");
            }

            @Override
            public void onDeviceFounded(SearchResult device) {
                String name = device.getName();
                String mac = device.getAddress();
                if (name.equals("ARIX1")){
                    arix1 = device;
                    mClient.stopSearch();
                    mesg.append("Arix: Got It!\n");
                }
            }

            @Override
            public void onSearchStopped() {
                Log.v("bradlog", "scan stop");
            }

            @Override
            public void onSearchCanceled() {
                Log.v("bradlog", "scan cancel");
            }
        });

    }

    public void connectDevices(View view) {
        if (arix1 == null) return;

    }

    @Override
    public void finish() {
        if (mBluetoothStateListener!= null){
            mClient.unregisterBluetoothStateListener(mBluetoothStateListener);
        }
        if (!isInitOpenBT) mClient.closeBluetooth();

        super.finish();
    }
}