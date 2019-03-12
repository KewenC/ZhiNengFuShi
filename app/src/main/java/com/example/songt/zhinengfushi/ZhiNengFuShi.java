package com.example.songt.zhinengfushi;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ZhiNengFuShi extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private ArrayList<String> devices;//配对设备集合
    private ArrayList<BluetoothDevice> BluetoothDevices;//配对设备集合
    private ArrayList<String> mArrayAdapter;//设备集合
    private Set<BluetoothDevice> bondedDevices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhi_neng_fu_shi);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 收索接收函数需要注册
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        if (bluetoothAdapter != null) {
//            if (!bluetoothAdapter.isEnabled()) {
//                //开启蓝牙
//                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(intent, REQUEST_ENABLE_BT);
//            } else {
//                pairedDevice();
//            }
//        } else {
//            Log.e("TAGF", "Device does not support Bluetooth");
//        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_zhi_neng_fu_shi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_open) {
            if (bluetoothAdapter != null) {
                if (!bluetoothAdapter.isEnabled()) {
                    //开启蓝牙
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                } else {
                    Toast.makeText(this,"蓝牙已打开！",Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this,"此设备不支持蓝牙！",Toast.LENGTH_SHORT).show();
                Log.e("TAGF", "此设备不支持蓝牙！");
            }
            return true;
        } else if (id == R.id.action_find_paired_device){
            devices = new ArrayList<String>();
            bondedDevices = bluetoothAdapter.getBondedDevices();
            String tmp = "";
            for (BluetoothDevice device : bondedDevices) {
                devices.add(device.getName() + " - " + device.getAddress());
                tmp = tmp +(device.getName() + " - " + device.getAddress()) + "\n";
                Log.e("TAGF","配对设备："+(device.getName() + "-" + device.getAddress()));
            }
            Toast.makeText(this,"配对设备：\n"+tmp,Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_find_device){
            bluetoothAdapter.startDiscovery();
            Intent settintIntent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(settintIntent);
            return true;
        } else if (id == R.id.action_connect_device){
            String[] devs = new String[devices.size()];
            for (int i=0;i<devs.length;i++){
                devs[i] = devices.get(i);
            }

            AlertDialog.Builder mBuilder = new AlertDialog.Builder(ZhiNengFuShi.this);
            mBuilder.setTitle("设备选取：");
            mBuilder.setItems(devs,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Iterator<BluetoothDevice> it = bondedDevices.iterator();
                            int i=0;
                            while (it.hasNext()){
                                if (i == which){
                                    Log.e("TAGF","select = "+which);
                                    ConnectThread connectThread = new ConnectThread(it.next());
                                    connectThread.run();
                                    break;
                                }
                                i++;
                            }

                        }
                    });
            mBuilder.create().show();


            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT){
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(this,"蓝牙打开失败！",Toast.LENGTH_SHORT).show();
                Log.e("TAGF", "蓝牙打开失败！");
            } else {
                Toast.makeText(this,"蓝牙打开成功！",Toast.LENGTH_SHORT).show();
                Log.e("TAGF", "蓝牙打开成功！");
            }
        }
    }

//    /**
//     * 查询配对设备
//     */
//    public void pairedDevice(){
//        devices = new ArrayList<String>();
//        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
//        for (BluetoothDevice device : bondedDevices) {
//            devices.add(device.getName() + "-" + device.getAddress());
//            Log.e("TAGF","配对设备："+(device.getName() + "-" + device.getAddress()));
//        }
//        findUnPairedDevice();
//    }

    /**
     * 搜索接收函数
     */
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();// When discovery finds a device
            Log.e("TAGF", "When discovery finds a device");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device =  intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show  in a ListView
                mArrayAdapter = new ArrayList<String>();
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                Log.e("TAGF", "配对成功设备："+(device.getName() + "-" + device.getAddress()));
            }
        }
    };

//    /**
//     * 开始收索未配对的设备
//     */
//    public void findUnPairedDevice(){
//        bluetoothAdapter.startDiscovery();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }


    /**
     * 内部类，如果是服务器端，需要建立监听，注意监听的是某个服务的UUID，服务器监听类如下：
     */
    public class ConnectThread extends Thread {
        private final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
        private final BluetoothSocket socket;
        private final BluetoothDevice device;
        private ConnectedThread connectedThread;

        public ConnectThread(BluetoothDevice device) {
            this.device = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.socket = tmp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                socket.connect();
                connectedThread = new ConnectedThread(socket);
                connectedThread.start();
            } catch (IOException e) {
                try {
                    socket.close();
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
                return;
            }
            //manageConnectedSocket(socket);
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 内部类，客户端与服务器端建立连接成功后，需要ConnectedThread类接收发送数据：
     */
    public class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            this.socket = socket;
            InputStream input = null;
            OutputStream output = null;

            try {
                input = socket.getInputStream();
                output = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.inputStream = input;
            this.outputStream = output;
        }

        public void run() {
            byte[] buff = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buff);
                    String str = new String(buff, "ISO-8859-1");
                    str = str.substring(0, bytes);

                    Log.e("TAGF", "收到的信息："+str);
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
