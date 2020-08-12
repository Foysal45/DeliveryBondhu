package com.ajkerdeal.app.essential.printer.connectivity.aidl;

/**
 * Created by Anup on 2018/6/07.
 */

public interface PrinterCallback {
    String getResult();
    void onReturnString(String result);
}
