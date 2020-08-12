package com.ajkerdeal.app.essential.printer.connectivity.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.ajkerdeal.app.essential.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothUtil {

    private final String TAG = "BluetoothUtil";

    //*********************************** Method 1 ***************************************//

    /*private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            CONNECTION_STATE_NONE,CONNECTION_STATE_LISTEN,CONNECTION_STATE_CONNECTING,CONNECTION_STATE_CONNECTED
    })
    public @interface ConnectionState { }
    public static final int CONNECTION_STATE_NONE = 0;
    public static final int CONNECTION_STATE_LISTEN = 1;
    public static final int CONNECTION_STATE_CONNECTING = 2;
    public static final int CONNECTION_STATE_CONNECTED = 3;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_CONNECTION_FAIL = 5;
    public static final int MESSAGE_PROGRESS = 6;

    //private static final String Innerprinter_Address = "00:11:22:33:44:55";
    //private static final String Rongta_Address = "00:0E:0E:13:BD:AB";
    private Context mContext;
    private static BluetoothSocket bluetoothSocket;
    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothDevice device;


    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;
    private static Handler mHandler;
    private static int mState;


    public BluetoothUtil(Context mContext, Handler mHandler) {
        this.mContext = mContext;
        this.mHandler = mHandler;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public synchronized void connect(BluetoothDevice device) {
        //Log.d(TAG, "connect to: " + device);
        if (mState == CONNECTION_STATE_CONNECTING && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        this.mConnectThread = new ConnectThread(device);
        this.mConnectThread.start();
        this.setState(CONNECTION_STATE_CONNECTING);
    }

    private synchronized void setState(@ConnectionState int state) {
        //Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        this.mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() { return mState; }

    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        //Log.d(TAG, "connected");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        Message msg = this.mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString("device_name", device.getName());
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        this.setState(CONNECTION_STATE_CONNECTED);
    }

    private void connectionFailed() {
        this.setState(CONNECTION_STATE_LISTEN);
        Message msg = this.mHandler.obtainMessage(MESSAGE_CONNECTION_FAIL);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect device");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        this.setState(CONNECTION_STATE_LISTEN);
        Message msg = this.mHandler.obtainMessage(MESSAGE_CONNECTION_FAIL);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Device connection was lost");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    public static boolean IsNoConnection() { return mState != 3; }

    public static void Begin() {
        WakeUpPrinter();
        InitPrinter();
    }

    private static boolean InitPrinter() {
        byte[] combyte = new byte[]{27, 64};
        if (mState != 3) {
            return false;
        } else {
            sendData(combyte);
            return true;
        }
    }

    private static void WakeUpPrinter() {
        byte[] b = new byte[3];

        try {
            sendData(b);
            Thread.sleep(100L);
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public synchronized void start() {
        //Log.d(TAG, "start");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (this.mAcceptThread == null) {
            this.mAcceptThread = new AcceptThread();
            this.mAcceptThread.start();
        }

        this.setState(CONNECTION_STATE_LISTEN);
    }

    public synchronized void stop() {
        //Log.d(TAG, "stop");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (this.mAcceptThread != null) {
            this.mAcceptThread.cancel();
            this.mAcceptThread = null;
        }

        this.setState(CONNECTION_STATE_NONE);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(TAG, PRINTER_UUID);
            } catch (IOException var4) {
                //Log.e(TAG, "listen() failed", var4);
            }
            this.mmServerSocket = tmp;
        }
        public void run() {
            //Log.d(TAG, "BEGIN mAcceptThread" + this);
            this.setName("AcceptThread");
            BluetoothSocket socket = null;

            while(mState != 3) {
                try {
                    socket = this.mmServerSocket.accept();
                } catch (IOException var6) {
                    //Log.e("BluetoothChatService", "accept() failed", var6);
                    break;
                }

                if (socket != null) {

                    synchronized(BluetoothUtil.this) {
                        switch(mState) {
                            case 0:
                            case 3:
                                try {
                                    socket.close();
                                } catch (IOException var4) {
                                    //Log.e(TAG, "Could not close unwanted socket", var4);
                                }
                                break;
                            case 1:
                            case 2:
                                connected(socket, socket.getRemoteDevice());
                        }
                    }
                }
            }

            //Log.i(TAG, "END mAcceptThread");
        }
        public void cancel() {
            //Log.d(TAG, "cancel " + this);
            try {
                this.mmServerSocket.close();
            } catch (IOException var2) {
                //Log.e(TAG, "close() of server failed", var2);
            }
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
            } catch (IOException var5) {
                //Log.e(TAG, "create() failed", var5);
            }
            this.mmSocket = tmp;
        }
        public void run() {
            //Log.i(TAG, "BEGIN mConnectThread");
            this.setName("ConnectThread");
            bluetoothAdapter.cancelDiscovery();

            try {
                this.mmSocket.connect();
            } catch (IOException var5) {
                connectionFailed();

                try {
                    this.mmSocket.close();
                } catch (IOException var3) {
                    //Log.e(TAG, "unable to close() socket during connection failure", var3);
                }
                start();
                return;
            }
            synchronized(BluetoothUtil.this) {
                mConnectThread = null;
            }
            connected(this.mmSocket, this.mmDevice);
        }
        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                //Log.e(TAG, "close() of connect socket failed", var2);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            //Log.d(TAG, "create ConnectedThread");
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException var6) {
                //Log.e(TAG, "temp sockets not created", var6);
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }
        public void run() {
            //Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            while(true) {
                try {
                    while(true) {
                        if (this.mmInStream.available() != 0) {
                            for(int i = 0; i < 3; ++i) {
                                buffer[i] = (byte)this.mmInStream.read();
                            }
                            //Log.i(TAG, "revBuffer[0]:" + buffer[0] + "  revBuffer[1]:" + buffer[1] + "  revBuffer[2]:" + buffer[2]);
                            mHandler.obtainMessage(MESSAGE_READ, 0, -1, buffer).sendToTarget();
                        }
                    }
                } catch (IOException var3) {
                    //Log.e(TAG, "disconnected", var3);
                    connectionLost();
                    return;
                }
            }
        }
        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException var3) {
                //Log.e(TAG, "Exception during write", var3);
            }
        }
        public void write(byte[] buffer, int dataLen) {
            try {
                for(int i = 0; i < dataLen; ++i) {
                    this.mmOutStream.write(buffer[i]);
                }
                mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException var4) {
                //Log.e(TAG, "Exception during write", var4);
            }
        }
        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                //Log.e(TAG, "close() of connect socket failed", var2);
            }
        }
    }


    *//**
     *Connect Bluetooth
     *
     * @param context
     * @return
     *//*
    public static boolean connectBlueTooth(Context context, String MAC_Address) {
        if (bluetoothSocket == null) {

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (bluetoothAdapter == null) {
                Toast.makeText(context,  R.string.toast_3, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!bluetoothAdapter.isEnabled()) {
                Toast.makeText(context, R.string.toast_4, Toast.LENGTH_SHORT).show();
                return false;
            }

            if ( (device = getDevice(bluetoothAdapter,MAC_Address) ) == null) {
                Toast.makeText(context, R.string.toast_5, Toast.LENGTH_SHORT).show();
                return false;
            }

            try {
                bluetoothSocket = getSocket(device);
            } catch (IOException e) {
                Toast.makeText(context, R.string.toast_6, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private static BluetoothDevice getDevice(BluetoothAdapter bluetoothAdapter, String MAC_Address) {
        BluetoothDevice innerprinter_device = null;
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getAddress().equals(MAC_Address)) {
                innerprinter_device = device;
                break;
            }
        }
        return innerprinter_device;
    }

    private static BluetoothSocket getSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket socket = null;
        socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
        socket.connect();
        return  socket;
    }

    *//*private static BluetoothAdapter getBTAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }*//*

    *//**
     * Disconnect Bluetooth
     *//*
    public static void disconnectBlueTooth() {
        if (bluetoothSocket != null) {
            try {
                OutputStream out = bluetoothSocket.getOutputStream();
                out.close();
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void reconnectSocket(){

        disconnectBlueTooth();
        try {
            bluetoothSocket = getSocket(device);
        } catch (IOException e) {
            //Toast.makeText(context, R.string.toast_6, Toast.LENGTH_SHORT).show();
        }

    }

    *//**
     * Use the Epson command for Bluetooth printing
     *
     * @param bytes
     * @throws IOException
     *//*
    public static void sendData(byte[] bytes) {
        *//*if (bluetoothSocket != null) {
            OutputStream out = null;
            try {
                out = bluetoothSocket.getOutputStream();
                out.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*//*

        if (mState == CONNECTION_STATE_CONNECTED) {
            ConnectedThread r = mConnectedThread;
            r.write(bytes);
            mHandler.obtainMessage(MESSAGE_PROGRESS, -1, -1).sendToTarget();
        }



    }*/


    //*********************************** Method 2 ***************************************//

    private static final UUID PRINTER_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static final String Innerprinter_Address = "00:11:22:33:44:55";
    private static final String Rongta_Address = "00:0E:0E:13:BD:AB";

    private static BluetoothSocket bluetoothSocket;

    private static BluetoothAdapter getBTAdapter() {
        return BluetoothAdapter.getDefaultAdapter();
    }

    private static BluetoothDevice getDevice(BluetoothAdapter bluetoothAdapter) {
        BluetoothDevice innerprinter_device = null;
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : devices) {
            if (device.getAddress().equals(Innerprinter_Address)) {
                innerprinter_device = device;
                break;
            }
        }
        return innerprinter_device;
    }

    private static BluetoothSocket getSocket(BluetoothDevice device) throws IOException {
        BluetoothSocket socket = null;
        socket = device.createRfcommSocketToServiceRecord(PRINTER_UUID);
        socket.connect();
        return  socket;
    }

    /**
     * 连接蓝牙
     *
     * @param context
     * @return
     */
    public static boolean connectBlueTooth(Context context) {
        if (bluetoothSocket == null) {
            if (getBTAdapter() == null) {
                Toast.makeText(context,  R.string.toast_3, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (!getBTAdapter().isEnabled()) {
                Toast.makeText(context, R.string.toast_4, Toast.LENGTH_SHORT).show();
                return false;
            }
            BluetoothDevice device;
            if ((device = getDevice(getBTAdapter())) == null) {
                Toast.makeText(context, R.string.toast_5, Toast.LENGTH_SHORT).show();
                return false;
            }

            try {
                bluetoothSocket = getSocket(device);
            } catch (IOException e) {
                Toast.makeText(context, R.string.toast_6, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    /**
     * 断开蓝牙
     */
    public static void disconnectBlueTooth(Context context) {
        if (bluetoothSocket != null) {
            try {
                OutputStream out = bluetoothSocket.getOutputStream();
                out.close();
                bluetoothSocket.close();
                bluetoothSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 蓝牙方式打印均使用epson指令
     *
     * @param bytes
     * @throws IOException
     */
    public static void sendData(byte[] bytes) {
        if (bluetoothSocket != null) {
            OutputStream out = null;
            try {
                out = bluetoothSocket.getOutputStream();
                out.write(bytes, 0, bytes.length);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("kaltin", "bluetoothSocketttt null");
        }
    }





}
