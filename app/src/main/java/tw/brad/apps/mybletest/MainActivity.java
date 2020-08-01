package tw.brad.apps.mybletest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    private void init(){
        mClient = new BluetoothClient(this);
    }
    public void scanDevices(View view) {
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
                Log.v("bradlog", name + " => " + mac);
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
}