package com.ajkerdeal.app.essential.printer.connectivity.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class BluetoothPrintDriver {
    private static final String TAG = "BluetoothPrintDriver";
    private static final boolean D = true;
    private static final String NAME = "BluetoothPrintDriver";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private final Handler mHandler;
    private BluetoothPrintDriver.AcceptThread mAcceptThread;
    private BluetoothPrintDriver.ConnectThread mConnectThread;
    private static BluetoothPrintDriver.ConnectedThread mConnectedThread;
    private static int mState;
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int UPCA = 0;
    public static final int UPCE = 1;
    public static final int EAN13 = 2;
    public static final int EAN8 = 3;
    public static final int CODE39 = 4;
    public static final int ITF = 5;
    public static final int CODEBAR = 6;
    public static final int CODE93 = 7;
    public static final int Code128_B = 8;
    public static final int CODE11 = 9;
    public static final int MSI = 10;


    public BluetoothPrintDriver(Context context, Handler handler) {
        mState = 0;
        this.mHandler = handler;
    }

    public synchronized void connect(BluetoothDevice device) {
        //Log.d("BluetoothChatService", "connect to: " + device);
        if (mState == 2 && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        this.mConnectThread = new BluetoothPrintDriver.ConnectThread(device);
        this.mConnectThread.start();
        this.setState(2);
    }


    private synchronized void setState(int state) {
        //Log.d("BluetoothChatService", "setState() " + mState + " -> " + state);
        mState = state;
        this.mHandler.obtainMessage(1, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        Log.d("BluetoothChatService", "start");
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (this.mAcceptThread == null) {
            this.mAcceptThread = new BluetoothPrintDriver.AcceptThread();
            this.mAcceptThread.start();
        }

        this.setState(1);
    }



    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        //Log.d("BluetoothChatService", "connected");
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

        mConnectedThread = new BluetoothPrintDriver.ConnectedThread(socket);
        mConnectedThread.start();
        Message msg = this.mHandler.obtainMessage(4);
        Bundle bundle = new Bundle();
        bundle.putString("device_name", device.getName());
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        this.setState(3);
    }

    public synchronized void stop() {
        //Log.d("BluetoothChatService", "stop");
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

        this.setState(0);
    }

    public void write(byte[] out) {
        BluetoothPrintDriver.ConnectedThread r;
        synchronized(this) {
            if (mState != 3) {
                return;
            }

            r = mConnectedThread;
        }

        r.write(out);
    }

    public void write2(byte[] out) throws IOException {
        BluetoothPrintDriver.ConnectedThread r;
        synchronized(this) {
            if (mState != 3) {
                return;
            }

            r = mConnectedThread;
        }

        for(int i = 0; i < out.length; ++i) {
            r.mmOutStream.write(out[i]);
        }

    }

    public static void BT_Write(String dataString) {
        byte[] data = null;
        if (mState == 3) {
            BluetoothPrintDriver.ConnectedThread r = mConnectedThread;

            try {
                data = dataString.getBytes("GBK");
            } catch (UnsupportedEncodingException var4) {
                var4.printStackTrace();
            }

            r.write(data);
        }
    }

    public static void BT_Write(String dataString, boolean bGBK) {
        byte[] data = null;
        if (mState == 3) {
            BluetoothPrintDriver.ConnectedThread r = mConnectedThread;
            if (bGBK) {
                try {
                    data = dataString.getBytes("GBK");
                } catch (UnsupportedEncodingException var5) {
                    ;
                }
            } else {
                data = dataString.getBytes();
            }

            r.write(data);
        }
    }

    public static void BT_Write(byte[] out) {
        if (mState == 3) {
            BluetoothPrintDriver.ConnectedThread r = mConnectedThread;
            r.write(out);
        }
    }

    public static void BT_Write(byte[] out, int dataLen) {
        if (mState == 3) {
            BluetoothPrintDriver.ConnectedThread r = mConnectedThread;
            r.write(out, dataLen);
        }
    }

    private void connectionFailed() {
        this.setState(1);
        Message msg = this.mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Unable to connect device");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        this.setState(1);
        Message msg = this.mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString("toast", "Device connection was lost");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    public static boolean IsNoConnection() {
        return mState != 3;
    }

    public static boolean InitPrinter() {
        byte[] combyte = new byte[]{27, 64};
        if (mState != 3) {
            return false;
        } else {
            BT_Write(combyte);
            return true;
        }
    }

    public static void WakeUpPritner() {
        byte[] b = new byte[3];

        try {
            BT_Write(b);
            Thread.sleep(100L);
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public static void Begin() {
        WakeUpPritner();
        InitPrinter();
    }


    // ESC-POS Commands

    public static void LF() {
        byte[] cmd = new byte[]{13};
        BT_Write(cmd);
    }

    public static void CR() {
        byte[] cmd = new byte[]{10};
        BT_Write(cmd);
    }

    public static void SelftestPrint() {
        byte[] cmd = new byte[]{18, 84};
        BT_Write(cmd, 2);
    }

    public static void StatusInquiry() {
        byte[] cmd = new byte[]{0, 0, 16, 4, -2, 0, 0, 16, 4, -1};
        BT_Write(cmd, 10);
    }

    public static void SetRightSpacing(byte Distance) {
        byte[] cmd = new byte[]{27, 32, Distance};
        BT_Write(cmd);
    }

    public static void SetAbsolutePrintPosition(byte nL, byte nH) {
        byte[] cmd = new byte[]{27, 36, nL, nH};
        BT_Write(cmd);
    }

    public static void SetRelativePrintPosition(byte nL, byte nH) {
        byte[] cmd = new byte[]{27, 92, nL, nH};
        BT_Write(cmd);
    }

    public static void SetDefaultLineSpacing() {
        byte[] cmd = new byte[]{27, 50};
        BT_Write(cmd);
    }

    public static void SetLineSpacing(byte LineSpacing) {
        byte[] cmd = new byte[]{27, 51, LineSpacing};
        BT_Write(cmd);
    }

    public static void SetLeftStartSpacing(byte nL, byte nH) {
        byte[] cmd = new byte[]{29, 76, nL, nH};
        BT_Write(cmd);
    }

    public static void SetAreaWidth(byte nL, byte nH) {
        byte[] cmd = new byte[]{29, 87, nL, nH};
        BT_Write(cmd);
    }

    public static void SetCharacterPrintMode(byte CharacterPrintMode) {
        byte[] cmd = new byte[]{27, 33, CharacterPrintMode};
        BT_Write(cmd);
    }

    public static void SetUnderline(byte UnderlineEn) {
        byte[] cmd = new byte[]{27, 45, UnderlineEn};
        BT_Write(cmd);
    }

    public static void SetBold(byte BoldEn) {
        byte[] cmd = new byte[]{27, 69, BoldEn};
        BT_Write(cmd);
    }

    public static void SetCharacterFont(byte Font) {
        byte[] cmd = new byte[]{27, 77, Font};
        BT_Write(cmd);
    }

    public static void SetRotate(byte RotateEn) {
        byte[] cmd = new byte[]{27, 86, RotateEn};
        BT_Write(cmd);
    }

    public static void SetAlignMode(byte AlignMode) {
        byte[] cmd = new byte[]{27, 97, AlignMode};
        BT_Write(cmd);
    }

    public static void SetInvertPrint(byte InvertModeEn) {
        byte[] cmd = new byte[]{27, 123, InvertModeEn};
        BT_Write(cmd);
    }

    public static void SetFontEnlarge(byte FontEnlarge) {
        byte[] cmd = new byte[]{29, 33, FontEnlarge};
        BT_Write(cmd);
    }

    public static void SetBlackReversePrint(byte BlackReverseEn) {
        byte[] cmd = new byte[]{29, 66, BlackReverseEn};
        BT_Write(cmd);
    }

    public static void SetChineseCharacterMode(byte ChineseCharacterMode) {
        byte[] cmd = new byte[]{28, 33, ChineseCharacterMode};
        BT_Write(cmd);
    }

    public static void SelChineseCodepage() {
        byte[] cmd = new byte[]{28, 38};
        BT_Write(cmd);
    }

    public static void CancelChineseCodepage() {
        byte[] cmd = new byte[]{28, 46};
        BT_Write(cmd);
    }

    public static void SetChineseUnderline(byte ChineseUnderlineEn) {
        byte[] cmd = new byte[]{28, 45, ChineseUnderlineEn};
        BT_Write(cmd);
    }

    public static void OpenDrawer(byte DrawerNumber, byte PulseStartTime, byte PulseEndTime) {
        byte[] cmd = new byte[]{27, 112, DrawerNumber, PulseStartTime, PulseEndTime};
        BT_Write(cmd);
    }

    public static void CutPaper() {
        byte[] cmd = new byte[]{27, 105};
        BT_Write(cmd);
    }

    public static void PartialCutPaper() {
        byte[] cmd = new byte[]{27, 109};
        BT_Write(cmd);
    }

    public static void FeedAndCutPaper(byte CutMode) {
        byte[] cmd = new byte[]{29, 86, CutMode};
        BT_Write(cmd);
    }

    public static void FeedAndCutPaper(byte CutMode, byte FeedDistance) {
        byte[] cmd = new byte[]{29, 86, CutMode, FeedDistance};
        BT_Write(cmd);
    }

    public static void AddQRCodePrint() {
        byte[] cmd = new byte[]{29, 40, 107, 3, 0, 49, 67, 3, 29, 40, 107, 3, 0, 49, 69, 51, 29, 40, 107, 83, 0, 49, 80, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 29, 40, 107, 3, 0, 49, 81, 48, 29, 40, 107, 4, 0, 49, 65, 49, 0};
        BT_Write(cmd);
    }

    public static void AddCodePrint(int CodeType, String data) {
        switch(CodeType) {
            case 0:
                UPCA(data);
                break;
            case 1:
                UPCE(data);
                break;
            case 2:
                EAN13(data);
                break;
            case 3:
                EAN8(data);
                break;
            case 4:
                CODE39(data);
                break;
            case 5:
                ITF(data);
                break;
            case 6:
                CODEBAR(data);
                break;
            case 7:
                CODE93(data);
                break;
            case 8:
                Code128_B(data);
            case 9:
            case 10:
        }

    }

    public static void UPCA(String data) {
        int m = 0;
        int num = data.length();
        int mIndex = 0;
        byte[] cmd = new byte[1024];
        int var6 = mIndex + 1;
        cmd[mIndex] = 29;
        cmd[var6++] = 107;
        cmd[var6++] = (byte)m;

        int i;
        for(i = 0; i < num; ++i) {
            if (data.charAt(i) > '9' || data.charAt(i) < '0') {
                return;
            }
        }

        if (num <= 30) {
            for(i = 0; i < num; ++i) {
                cmd[var6++] = (byte)data.charAt(i);
            }

            BT_Write(cmd);
        }
    }

    public static void UPCE(String data) {
        int m = 1;
        int num = data.length();
        int mIndex = 0;
        byte[] cmd = new byte[1024];
        int var6 = mIndex + 1;
        cmd[mIndex] = 29;
        cmd[var6++] = 107;
        cmd[var6++] = (byte)m;

        int i;
        for(i = 0; i < num; ++i) {
            if (data.charAt(i) > '9' || data.charAt(i) < '0') {
                return;
            }
        }

        if (num <= 30) {
            for(i = 0; i < num; ++i) {
                cmd[var6++] = (byte)data.charAt(i);
            }

            BT_Write(cmd);
        }
    }

    public static void EAN13(String data) {
        int m = 2;
        int num = data.length();
        int mIndex = 0;
        byte[] cmd = new byte[1024];
        int var6 = mIndex + 1;
        cmd[mIndex] = 29;
        cmd[var6++] = 107;
        cmd[var6++] = (byte)m;

        int i;
        for(i = 0; i < num; ++i) {
            if (data.charAt(i) > '9' || data.charAt(i) < '0') {
                return;
            }
        }

        if (num <= 30) {
            for(i = 0; i < num; ++i) {
                cmd[var6++] = (byte)data.charAt(i);
            }

            BT_Write(cmd);
        }
    }

    public static void EAN8(String data) {
        int m = 3;
        int num = data.length();
        int mIndex = 0;
        byte[] cmd = new byte[1024];
        int var6 = mIndex + 1;
        cmd[mIndex] = 29;
        cmd[var6++] = 107;
        cmd[var6++] = (byte)m;

        int i;
        for(i = 0; i < num; ++i) {
            if (data.charAt(i) > '9' || data.charAt(i) < '0') {
                return;
            }
        }

        if (num <= 30) {
            for(i = 0; i < num; ++i) {
                cmd[var6++] = (byte)data.charAt(i);
            }

            BT_Write(cmd);
        }
    }

    public static void CODE39(String data) {
        int m = 4;
        int num = data.length();
        int mIndex = 0;
        byte[] cmd = new byte[1024];
        int var6 = mIndex + 1;
        cmd[mIndex] = 29;
        cmd[var6++] = 107;
        cmd[var6++] = (byte)m;

        int i;
        for(i = 0; i < num; ++i) {
            if (data.charAt(i) > 127 || data.charAt(i) < ' ') {
                return;
            }
        }

        if (num <= 30) {
            for(i = 0; i < num; ++i) {
                cmd[var6++] = (byte)data.charAt(i);
            }

            BT_Write(cmd);
        }
    }

    public static void ITF(String data) {
        int m = 5;
        int num = data.length();
        int mIndex = 0;
        byte[] cmd = new byte[1024];
        int var6 = mIndex + 1;
        cmd[mIndex] = 29;
        cmd[var6++] = 107;
        cmd[var6++] = (byte)m;

        int i;
        for(i = 0; i < num; ++i) {
            if (data.charAt(i) > '9' || data.charAt(i) < '0') {
                return;
            }
        }

        if (num <= 30) {
            for(i = 0; i < num; ++i) {
                cmd[var6++] = (byte)data.charAt(i);
            }

            BT_Write(cmd);
        }
    }

    public static void CODEBAR(String data) {
        int m = 6;
        int num = data.length();
        int mIndex = 0;
        byte[] cmd = new byte[1024];
        int var6 = mIndex + 1;
        cmd[mIndex] = 29;
        cmd[var6++] = 107;
        cmd[var6++] = (byte)m;

        int i;
        for(i = 0; i < num; ++i) {
            if (data.charAt(i) > 127 || data.charAt(i) < ' ') {
                return;
            }
        }

        if (num <= 30) {
            for(i = 0; i < num; ++i) {
                cmd[var6++] = (byte)data.charAt(i);
            }

            BT_Write(cmd);
        }
    }

    public static void CODE93(String data) {
        int m = 7;
        int num = data.length();
        int mIndex = 0;
        byte[] cmd = new byte[1024];
        int var6 = mIndex + 1;
        cmd[mIndex] = 29;
        cmd[var6++] = 107;
        cmd[var6++] = (byte)m;

        int i;
        for(i = 0; i < num; ++i) {
            if (data.charAt(i) > 127 || data.charAt(i) < ' ') {
                return;
            }
        }

        if (num <= 30) {
            for(i = 0; i < num; ++i) {
                cmd[var6++] = (byte)data.charAt(i);
            }

            BT_Write(cmd);
        }
    }

    public static void Code128_B(String data) {
        int m = 73;
        int num = data.length();
        int transNum = 0;
        int mIndex = 0;
        byte[] cmd = new byte[1024];
        int var10 = mIndex + 1;
        cmd[mIndex] = 29;
        cmd[var10++] = 107;
        cmd[var10++] = (byte)m;
        int Code128C = var10++;
        cmd[var10++] = 123;
        cmd[var10++] = 66;

        int checkcodeID;
        for(checkcodeID = 0; checkcodeID < num; ++checkcodeID) {
            if (data.charAt(checkcodeID) > 127 || data.charAt(checkcodeID) < ' ') {
                return;
            }
        }

        if (num <= 30) {
            for(checkcodeID = 0; checkcodeID < num; ++checkcodeID) {
                cmd[var10++] = (byte)data.charAt(checkcodeID);
                if (data.charAt(checkcodeID) == '{') {
                    cmd[var10++] = (byte)data.charAt(checkcodeID);
                    ++transNum;
                }
            }

            checkcodeID = 104;
            int n = 1;

            for(int i = 0; i < num; ++i) {
                checkcodeID += n++ * (data.charAt(i) - 32);
            }

            checkcodeID %= 103;
            if (checkcodeID >= 0 && checkcodeID <= 95) {
                cmd[var10++] = (byte)(checkcodeID + 32);
                cmd[Code128C] = (byte)(num + 3 + transNum);
            } else if (checkcodeID == 96) {
                cmd[var10++] = 123;
                cmd[var10++] = 51;
                cmd[Code128C] = (byte)(num + 4 + transNum);
            } else if (checkcodeID == 97) {
                cmd[var10++] = 123;
                cmd[var10++] = 50;
                cmd[Code128C] = (byte)(num + 4 + transNum);
            } else if (checkcodeID == 98) {
                cmd[var10++] = 123;
                cmd[var10++] = 83;
                cmd[Code128C] = (byte)(num + 4 + transNum);
            } else if (checkcodeID == 99) {
                cmd[var10++] = 123;
                cmd[var10++] = 67;
                cmd[Code128C] = (byte)(num + 4 + transNum);
            } else if (checkcodeID == 100) {
                cmd[var10++] = 123;
                cmd[var10++] = 52;
                cmd[Code128C] = (byte)(num + 4 + transNum);
            } else if (checkcodeID == 101) {
                cmd[var10++] = 123;
                cmd[var10++] = 65;
                cmd[Code128C] = (byte)(num + 4 + transNum);
            } else if (checkcodeID == 102) {
                cmd[var10++] = 123;
                cmd[var10++] = 49;
                cmd[Code128C] = (byte)(num + 4 + transNum);
            }

            BT_Write(cmd);
        }
    }

    public static void printString(String str) {
        try {
            BT_Write(str.getBytes("GBK"));
            BT_Write(new byte[]{10});
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public static void printParameterSet(byte[] buf) {
        BT_Write(buf);
    }

    public static void printByteData(byte[] buf) {
        BT_Write(buf);
        BT_Write(new byte[]{10});
    }

    public static void printImage() {
        byte[] bufTemp2 = new byte[]{27, 74, 24, 29, 118, 48, 0, 16, 0, -128, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -13, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 8, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 12, 0, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 14, 0, 63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 15, 0, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -13, -1, -16, 15, -128, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -31, -1, -16, 15, -64, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -64, -1, -16, 15, -32, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -128, 127, -16, 15, -16, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 63, -16, 15, -8, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 31, -16, 15, -8, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -128, 15, -16, 15, -16, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -64, 7, -16, 15, -32, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -32, 3, -16, 15, -64, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 1, -16, 15, -128, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -8, 0, -16, 15, 0, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -4, 0, 112, 14, 0, 63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, 0, 48, 12, 0, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 16, 8, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -128, 0, 0, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -64, 0, 0, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -32, 0, 0, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 0, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -8, 0, 0, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -4, 0, 0, 63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, 0, 0, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -128, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -64, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -64, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -128, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, 0, 0, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -4, 0, 0, 63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -8, 0, 0, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 0, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -32, 0, 0, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -64, 0, 0, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -128, 0, 0, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 16, 8, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -2, 0, 48, 12, 0, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -4, 0, 112, 14, 0, 63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -8, 0, -16, 15, 0, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 1, -16, 15, -128, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -32, 3, -16, 15, -64, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -64, 7, -16, 15, -32, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -128, 15, -16, 15, -16, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 31, -16, 15, -8, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 63, -16, 15, -4, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -128, 127, -16, 15, -8, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -64, -1, -16, 15, -16, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -31, -1, -16, 15, -32, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -13, -1, -16, 15, -64, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 15, -128, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 15, 0, 63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 14, 0, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 12, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 8, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 3, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 7, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 31, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 63, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, 127, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -16, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -13, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -9, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10};
        printByteData(bufTemp2);
    }



    // Threads

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = BluetoothPrintDriver.this.mAdapter.listenUsingRfcommWithServiceRecord("BluetoothPrintDriver", BluetoothPrintDriver.MY_UUID);
            } catch (IOException var4) {
                //Log.e("BluetoothChatService", "listen() failed", var4);
            }

            this.mmServerSocket = tmp;
        }

        public void run() {
            //Log.d("BluetoothChatService", "BEGIN mAcceptThread" + this);
            this.setName("AcceptThread");
            BluetoothSocket socket = null;

            while(BluetoothPrintDriver.mState != 3) {
                try {
                    socket = this.mmServerSocket.accept();
                } catch (IOException var6) {
                    //Log.e("BluetoothChatService", "accept() failed", var6);
                    break;
                }

                if (socket != null) {
                    BluetoothPrintDriver var2 = BluetoothPrintDriver.this;
                    synchronized(BluetoothPrintDriver.this) {
                        switch(BluetoothPrintDriver.mState) {
                            case 0:
                            case 3:
                                try {
                                    socket.close();
                                } catch (IOException var4) {
                                    //Log.e("BluetoothChatService", "Could not close unwanted socket", var4);
                                }
                                break;
                            case 1:
                            case 2:
                                BluetoothPrintDriver.this.connected(socket, socket.getRemoteDevice());
                        }
                    }
                }
            }

            //Log.i("BluetoothChatService", "END mAcceptThread");
        }

        public void cancel() {
            //Log.d("BluetoothChatService", "cancel " + this);

            try {
                this.mmServerSocket.close();
            } catch (IOException var2) {
                //Log.e("BluetoothChatService", "close() of server failed", var2);
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
                tmp = device.createRfcommSocketToServiceRecord(BluetoothPrintDriver.MY_UUID);
            } catch (IOException var5) {
                //Log.e("BluetoothChatService", "create() failed", var5);
            }

            this.mmSocket = tmp;
        }

        public void run() {
            //Log.i("BluetoothChatService", "BEGIN mConnectThread");
            this.setName("ConnectThread");
            BluetoothPrintDriver.this.mAdapter.cancelDiscovery();

            try {
                this.mmSocket.connect();
            } catch (IOException var5) {
                BluetoothPrintDriver.this.connectionFailed();

                try {
                    this.mmSocket.close();
                } catch (IOException var3) {
                    //Log.e("BluetoothChatService", "unable to close() socket during connection failure", var3);
                }

                BluetoothPrintDriver.this.start();
                return;
            }

            BluetoothPrintDriver var1 = BluetoothPrintDriver.this;
            synchronized(BluetoothPrintDriver.this) {
                BluetoothPrintDriver.this.mConnectThread = null;
            }

            BluetoothPrintDriver.this.connected(this.mmSocket, this.mmDevice);
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                //Log.e("BluetoothChatService", "close() of connect socket failed", var2);
            }

        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            //Log.d("BluetoothChatService", "create ConnectedThread");
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException var6) {
                //Log.e("BluetoothChatService", "temp sockets not created", var6);
            }

            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
        }

        public void run() {
            //Log.i("BluetoothChatService", "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];

            while(true) {
                try {
                    while(true) {
                        if (this.mmInStream.available() != 0) {
                            for(int i = 0; i < 3; ++i) {
                                buffer[i] = (byte)this.mmInStream.read();
                            }

                            //Log.i("BluetoothChatService", "revBuffer[0]:" + buffer[0] + "  revBuffer[1]:" + buffer[1] + "  revBuffer[2]:" + buffer[2]);
                            BluetoothPrintDriver.this.mHandler.obtainMessage(2, 0, -1, buffer).sendToTarget();
                        }
                    }
                } catch (IOException var3) {
                    //Log.e("BluetoothChatService", "disconnected", var3);
                    BluetoothPrintDriver.this.connectionLost();
                    return;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
                BluetoothPrintDriver.this.mHandler.obtainMessage(3, -1, -1, buffer).sendToTarget();
            } catch (IOException var3) {
                //Log.e("BluetoothChatService", "Exception during write", var3);
            }

        }

        public void write(byte[] buffer, int dataLen) {
            try {
                for(int i = 0; i < dataLen; ++i) {
                    this.mmOutStream.write(buffer[i]);
                }

                BluetoothPrintDriver.this.mHandler.obtainMessage(3, -1, -1, buffer).sendToTarget();
            } catch (IOException var4) {
                //Log.e("BluetoothChatService", "Exception during write", var4);
            }

        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException var2) {
                //Log.e("BluetoothChatService", "close() of connect socket failed", var2);
            }

        }
    }
}

