package com.ajkerdeal.app.essential.printer.command;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.IntRange;

import com.ajkerdeal.app.essential.printer.connectivity.aidl.AidlUtil;
import com.ajkerdeal.app.essential.printer.connectivity.bluetooth.BluetoothUtil;
import com.ajkerdeal.app.essential.printer.connectivity.utils.ESCUtil;

import timber.log.Timber;

public class PrinterCommand {

    private final String TAG = "PrinterCommand";
    private boolean isBluetooth = false;
    private String charsetName = "utf-8";
    private String printerLanguage = "English";

    private static PrinterCommand instance = null;
    private boolean isBold;
    private boolean isUnderline;
    private float fontSize = 22f;
    private AlignmentType alignmentType = AlignmentType.LEFT;
    private int lineGap = 1;
    private int contrast = 12;

    private PrinterCommand(Context mContext) {

        //SharedPreferencesManager mPreferencesManager = new SharedPreferencesManager(mContext, NameSpace.PREFERENCE_DB_PRINTER_SETTINGS);
        //isBluetooth = mPreferencesManager.getBoolean(NameSpace.PREFERENCE_KEY_IS_BlUETOOTH);
        //charsetName = mPreferencesManager.getString(NameSpace.PREFERENCE_KEY_PRINTER_ENCODING);
        //printerLanguage = mPreferencesManager.getString(NameSpace.PREFERENCE_KEY_PRINTER_LANGUAGE);
        Timber.tag(TAG).d("is Bluetooth service: %b", isBluetooth);
        if (isBluetooth){
            /*BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                //Toast.makeText(mContext, "Bluetooth is not available", Toast.LENGTH_LONG).show();
                Log.d(TAG, "PrinterCommand: Bluetooth is not available");
                return;
            }
            if (!mBluetoothAdapter.isEnabled()) {
                // If BT is not on, request that it be enabled.
                Toast.makeText(mContext, "Please turn on Bluetooth", Toast.LENGTH_LONG).show();
                //Intent enableIntent = new Intent();
                //enableIntent.setAction(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            } else {
                // Otherwise, setup the Bluetooth Manager
                BluetoothUtil.connectBlueTooth(mContext);
            }*/
        }else {
            // Aidl service
            AidlUtil.getInstance().connectPrinterService(mContext);
            if (AidlUtil.getInstance().isConnect()){
                Log.d(TAG, "PrinterCommand: "+"AIDL is connected");
                AidlUtil.getInstance().initPrinter();
            }else {
                Log.d(TAG, "PrinterCommand: "+"AIDL is disconnected" );
            }
        }

    }

    public static PrinterCommand getInstance(Context context){
        if (instance == null){
            synchronized (PrinterCommand.class){
                if (instance == null){
                    instance = new PrinterCommand(context);
                    instance.setContrast(0);//130%
                }
            }
        }
        return instance;
    }

    public void setBold(boolean shouldBold){
        isBold = shouldBold;
    }

    public void setIsUnderline(boolean shouldUnderline){
        isUnderline = shouldUnderline;
    }

    public void setFontSize(float size){
        fontSize = size;
    }

    public void setAlignmentType(AlignmentType type){
        alignmentType = type;
        switch (type){
            case LEFT:
                if (isBluetooth) {
                    BluetoothUtil.sendData(ESCUtil.alignLeft());
                } else {
                    AidlUtil.getInstance().alignLeft();
                }
                break;
            case CENTER:
                if (isBluetooth) {
                    BluetoothUtil.sendData(ESCUtil.alignCenter());
                } else {
                    AidlUtil.getInstance().alignCenter();
                }
                break;
            case RIGHT:
                if (isBluetooth) {
                    BluetoothUtil.sendData(ESCUtil.alignRight());
                } else {
                    AidlUtil.getInstance().alignRight();
                }
                break;
        }
    }

    public void setLineGap(int emptyLineGap){
        lineGap = emptyLineGap;
    }

    public void printText(String content){
        AidlUtil.getInstance().printText(content,charsetName,fontSize,lineGap);
    }
    public void printText(String content, float size){
        AidlUtil.getInstance().printText(content,charsetName,size,lineGap);
    }
    public void printText(String content,int lineGap){
        AidlUtil.getInstance().printText(content,charsetName,fontSize,lineGap);
    }
    public void printText(String content, float size, int lineGap){
        AidlUtil.getInstance().printText(content,charsetName,size,lineGap);
    }

    public void setContrast(@IntRange(from=0, to=12) int contrast){
        AidlUtil.getInstance().setDarkness(contrast);
    }

    /**
     * Print one-dimensional barcode
     * @param data: barcode data
     * @param encoding: barcode type
     * 0 -- UPC-A,
     * 1 -- UPC-E,
     * 2 -- JAN13 (EAN13),
     * 3 -- JAN8 (EAN8),
     * 4 -- CODE39,
     * 5 -- ITF,
     * 6 -- CODABAR,
     * 7 -- CODE93,
     * 8 -- CODE128
     *
     * @param height: barcode height,
     * values 1 to 255, default 162
     *
     * @param width: barcode width,
     * value 2 to 6, default 2
     *
     * @param textposition: Text position
     * 0--Do not print text,
     * 1--Text is above the barcode,
     * 2--Text is below the barcode,
     * 3--Barcode is printed above and below
     */
    public void printBarCode(String data, @IntRange(from=0, to=8) int encoding, @IntRange(from=1, to=255) int height, @IntRange(from=2, to=6) int width, @IntRange(from=0, to=3) int textposition){
        AidlUtil.getInstance().printBarCode(data,encoding,height,width,textposition);
    }

    public void printBitmap(Bitmap bitmap, @IntRange(from=0, to=2) int type){
        AidlUtil.getInstance().printBitmapCustom(bitmap,type);
    }

    public void printBitmap(Bitmap bitmap){
        AidlUtil.getInstance().printBitmap(bitmap);
    }

    public void printEmptyLine(int line){
        AidlUtil.getInstance().printEmptyLine(line);
    }
}
