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
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;
import static com.inuker.bluetooth.library.Constants.STATUS_CONNECTED;
import static com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED;

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

    private String cUUID = "00002a19-0000-1000-8000-00805f9b34fb";
    private String sUUID = "0000180f-0000-1000-8000-00805f9b34fb";
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
    private final BleConnectStatusListener mBleConnectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(String mac, int status) {
            if (status == STATUS_CONNECTED) {
                Log.v("bradlog", "connected");
                mesg.append("Connected\n");
                isConnecting = true;
                setNotify();
            } else if (status == STATUS_DISCONNECTED) {
                Log.v("bradlog", "disconnected");
                isConnecting = false;
                mesg.append("disconnected\n");
            }
        }
    };

    private boolean isInitOpenBT;
    private boolean isConnecting;
    private void init(){
        Log.v("bradlog", "init");
        mClient = new BluetoothClient(this);
        mClient.registerBluetoothStateListener(mBluetoothStateListener);
        if (!mClient.isBluetoothOpened()){
            mClient.openBluetooth();
        }else{
            isInitOpenBT = true;
            isBTOpen = true;
        }
    }
    public void scanDevices(View view) {
        if (!isBTOpen) return;
        Log.v("bradlog", "search");
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
                Log.v("bradlog", name +":" + mac);
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
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();

        String mac = arix1.getAddress();
        mClient.registerConnectStatusListener(mac, mBleConnectStatusListener);
        mClient.connect(mac, options, new BleConnectResponse() {
            @Override
            public void onResponse(int code, BleGattProfile data) {
                Log.v("bradlog", "connect response: "+ code);
            }
        });
    }

    private void setNotify(){
        mClient.notify(arix1.getAddress(), UUID.fromString(sUUID),
                UUID.fromString(cUUID), new BleNotifyResponse() {
            @Override
            public void onNotify(UUID service, UUID character, byte[] value) {
                mesg.append(" ==> " + value[0] + "\n");
            }

            @Override
            public void onResponse(int code) {
                if (code == REQUEST_SUCCESS) {

                }
            }
        });
    }

    @Override
    public void finish() {
        if (mBluetoothStateListener!= null){
            mClient.unregisterBluetoothStateListener(mBluetoothStateListener);
        }
        if (!isInitOpenBT) mClient.closeBluetooth();

        super.finish();
    }

    public void disconnectDevices(View view) {
        if (arix1!= null && isConnecting) {
            mClient.disconnect(arix1.getAddress());
        }
    }
}