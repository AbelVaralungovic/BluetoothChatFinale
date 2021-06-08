package com.example.bluetoothchatfinale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Set;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView appBarStatus;
    private TextView appBarName;
    Menu optionsMenu;

    //private TextView status;
    //private Button btnConnect;
    private ListView listViewChat;
    private Dialog dialog;
    private EditText editText;
    private ArrayAdapter<String> chatAdapter;
    private ArrayList<String> chatMessages;

    //private boolean onOff = true;

    //Custom ArrayAdapter
    public static ArrayList<ChatMessage> arrayList = new ArrayList<ChatMessage>();
    ChatArrayAdapter adapter;


    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_OBJECT = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_OBJECT = "device_name";

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;

    private ChatController chatController;
    private BluetoothDevice connectingDevice;
    private ArrayAdapter<String> discoveredDevicesAdapter;


    private BluetoothAdapter bluetoothAdapter ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Checks if bluetooth is supported on the device
        bluetoothSupport();
        //Initialize custom appbat
        appbarInit();
        //




        findViewsByIds();







        //todo
       /*  chatMessages = new ArrayList<>();
         chatAdapter = new ArrayAdapter<>(this, R.layout.simple_list_item_custom, chatMessages);
         listView.setAdapter(chatAdapter);*/

        adapter = new ChatArrayAdapter(getApplicationContext(),R.layout.item_send);
        listViewChat.setAdapter(adapter);



    }

    private void appbarInit() {
        toolbar = (Toolbar) findViewById(R.id.myAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void bluetoothSupport() {


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            finish();
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        optionsMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.btn_connect_bluetooth:
                showPrinterPickDialog();
                 MenuItem disconnect = optionsMenu.findItem(R.id.btn_connect_disconnect).setVisible(true);
                break;
            case R.id.btn_connect_disconnect:
                chatController.stop();
                disconnect = optionsMenu.findItem(R.id.btn_connect_disconnect).setVisible(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatController.STATE_CONNECTED:
                            setStatus("Connected to: ");
                            appBarName.setText(connectingDevice.getName());


                            break;
                        case ChatController.STATE_CONNECTING:
                            setStatus("Connecting...");
                            appBarName.setText("");
                            break;
                        case ChatController.STATE_LISTEN:

                        case ChatController.STATE_NONE:
                            setStatus("Not connected");
                            appBarName.setText("");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);

                    adapter.add(new ChatMessage(false, writeMessage));
                    adapter.notifyDataSetChanged();

                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);

                    adapter.add(new ChatMessage(true, readMessage));
                    adapter.notifyDataSetChanged();

                    break;
                case MESSAGE_DEVICE_OBJECT:
                    connectingDevice = msg.getData().getParcelable(DEVICE_OBJECT);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectingDevice.getName(),
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString("toast"),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    private void showPrinterPickDialog() {
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.layout_bluetooth);
        dialog.setTitle("Bluetooth Devices");

        //Starts bluetooth discovery
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothAdapter.startDiscovery();

        //Initializing bluetooth adapters
        ArrayAdapter<String> pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        //Locate ListViews and attached the adapters


        ListView listView = (ListView) dialog.findViewById(R.id.pairedDeviceList);


        ListView listView2 = (ListView) dialog.findViewById(R.id.discoveredDeviceList);



        listView.setAdapter(pairedDevicesAdapter);
        listView2.setAdapter(discoveredDevicesAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter


        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            pairedDevicesAdapter.add(getString(R.string.none_paired));
        }

        //listView2.setOnItemClickListener();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }

        });

        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                bluetoothAdapter.cancelDiscovery();
                String info = ((TextView) view).getText().toString();
                String address = info.substring(info.length() - 17);

                connectToDevice(address);
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.show();
    }

    private void setStatus(String s) {
        appBarStatus.setText(s);
    }


    private void connectToDevice(String deviceAddress) {
        bluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        chatController.connect(device);
    }



    private void findViewsByIds() {
        //Initialization of appBar TextViews
        appBarStatus =(TextView) toolbar.findViewById(R.id.appBarStatus);
        appBarName =(TextView) toolbar.findViewById(R.id.appBarName);
        //Initialization of main chat ListView,EditChat, and sendButton
        listViewChat = (ListView) findViewById(R.id.list);
        editText = (EditText) findViewById(R.id.et_text);
        ImageButton btnSend = findViewById(R.id.btn_send);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Please input some texts", Toast.LENGTH_SHORT).show();
                } else {
                    //TODO: here
                    sendMessage(editText.getText().toString());
                    editText.setText("");
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BLUETOOTH:
                if (resultCode == Activity.RESULT_OK) {
                    chatController = new ChatController(this, handler);
                } else {
                    Toast.makeText(this, "Bluetooth still disabled, turn off application!", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    private void sendMessage(String message) {
        if (chatController.getState() != ChatController.STATE_CONNECTED) {
            Toast.makeText(this, "Connection was lost!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatController.write(send);
        }
    }

    @Override
    public void onStart() {
        super.onStart();


        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        } else {
            chatController = new ChatController(this, handler);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chatController != null) {
            if (chatController.getState() == ChatController.STATE_NONE) {
                chatController.start();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatController != null)
            chatController.stop();
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    discoveredDevicesAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (discoveredDevicesAdapter.getCount() == 0) {
                    discoveredDevicesAdapter.add(getString(R.string.none_found));
                }
            }
        }

    };
}