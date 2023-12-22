package com.zebra.deviceidentifierswrapper;

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.core.content.ContextCompat;

public class DIHelper {

    // Placeholder for custom certificate
    // Otherwise, the app will use the first certificate found with the method:
    // final Signature[] arrSignatures = packageInfo.signingInfo.getApkContentsSigners();
    // TODO: Put your custom certificate in the apkCertificate member for MX AccessMgr registering (only if necessary and if you know what you are doing)
    public static Signature apkCertificate = null;

    protected static String sIMEI = null;
    protected static String sSerialNumber = null;

    protected static String sWifiMacAddress = null;

    public static final long SEC_IN_MS = 1000;
    public static final long MIN_IN_MS = SEC_IN_MS * 60;
    public static long MAX_EMDK_TIMEOUT_IN_MS = 10 * MIN_IN_MS; // 10 minutes
    public static long WAIT_PERIOD_BEFORE_RETRY_EMDK_RETRIEVAL_IN_MS = 2 * SEC_IN_MS; // 2 seconds

    public static void resetCachedValues()
    {
        sIMEI = null;
        sSerialNumber = null;
    }

    // This method will return the serial number in the string passed through the onSuccess method
    public static void getSerialNumber(Context context, IDIResultCallbacks callbackInterface)
    {
        if(sSerialNumber != null)
        {
            if(callbackInterface != null)
            {
                callbackInterface.onDebugStatus("Serial number already in cache.");
            }
            callbackInterface.onSuccess(sSerialNumber);
            return;
        }
        if (android.os.Build.VERSION.SDK_INT < 29) {
            returnSerialUsingAndroidAPIs(context, callbackInterface);
        } else {
            returnSerialUsingZebraAPIs(context, callbackInterface);
        }
    }

    public static void getWifiMacAddress(Context context, IDIResultCallbacks callbackInterface) {
        if (sWifiMacAddress != null) {
            if (callbackInterface != null) {
                callbackInterface.onDebugStatus("Wifi Mac address already in cache.");
                callbackInterface.onSuccess(sWifiMacAddress);
            }
        } else {
            getWifiMacAddressUsingZebraAPIs(context, callbackInterface);
        }
    }

    @SuppressLint({"MissingPermission", "ObsoleteSdkInt", "HardwareIds"})
    private static void returnSerialUsingAndroidAPIs(Context context, IDIResultCallbacks callbackInterface) {
        if (android.os.Build.VERSION.SDK_INT < 26) {
            sSerialNumber = Build.SERIAL;
            callbackInterface.onSuccess(Build.SERIAL);
        } else {
            if (ContextCompat.checkSelfPermission(context, permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                sSerialNumber = Build.getSerial();
                callbackInterface.onSuccess(Build.getSerial());
            } else {
                callbackInterface.onError("Please grant READ_PHONE_STATE permission");
            }
        }
    }

    private static void returnSerialUsingZebraAPIs(Context context, final IDIResultCallbacks callbackInterface) {
        IDIResultCallbacks tempCallbackInterface = new IDIResultCallbacks() {
            @Override
            public void onSuccess(String message) {
                sSerialNumber = message;
                callbackInterface.onSuccess(message);
            }

            @Override
            public void onError(String message) {
                callbackInterface.onError(message);
            }

            @Override
            public void onDebugStatus(String message) {
                callbackInterface.onDebugStatus(message);
            }
        };

        new RetrieveOEMInfoTask()
            .execute(context, Uri.parse("content://oem_info/oem.zebra.secure/build_serial"),
                    tempCallbackInterface);
    }

    private static void getWifiMacAddressUsingZebraAPIs(Context context, final IDIResultCallbacks callbackInterface) {
        IDIResultCallbacks tempCallbackInterface = new IDIResultCallbacks() {
            @Override
            public void onSuccess(String message) {
                sWifiMacAddress = message;
                callbackInterface.onSuccess(message);
            }

            @Override
            public void onError(String message) {
                callbackInterface.onError(message);
            }

            @Override
            public void onDebugStatus(String message) {
                callbackInterface.onDebugStatus(message);
            }
        };

        new RetrieveOEMInfoTask()
                .execute(context, Uri.parse("content://oem_info/oem.zebra.secure/wifi_mac"),
                        tempCallbackInterface);
    }

    // This method will return the imei number in the string passed through the onSuccess method
    public static void getIMEINumber(Context context, IDIResultCallbacks callbackInterface)
    {
        if(sIMEI != null)
        {
            if(callbackInterface != null)
            {
                callbackInterface.onDebugStatus("IMEI number already in cache.");
            }
            callbackInterface.onSuccess(sIMEI);
            return;
        }
        if (android.os.Build.VERSION.SDK_INT < 29) {
            returnImeiUsingAndroidAPIs(context, callbackInterface);
        } else {
            returnImeiUsingZebraAPIs(context, callbackInterface);
        }
    }

    @SuppressLint({"MissingPermission", "ObsoleteSdkInt", "HardwareIds" })
    private static void returnImeiUsingAndroidAPIs(Context context, IDIResultCallbacks callbackInterface) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT < 26) {String imei = telephonyManager.getDeviceId();
            if (imei != null && !imei.isEmpty()) {
                sIMEI = imei;
                callbackInterface.onSuccess(imei);
            } else {
                callbackInterface.onError("Could not get IMEI number");
            }
        } else {
            if (ContextCompat.checkSelfPermission(context, permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                String imei = telephonyManager.getImei();
                if (imei != null && !imei.isEmpty()) {
                    sIMEI = imei;
                    callbackInterface.onSuccess(imei);
                } else {
                    callbackInterface.onError("Could not get IMEI number");
                }
            } else {
                callbackInterface.onError("Please grant READ_PHONE_STATE permission");
            }
        }
    }

    private static void returnImeiUsingZebraAPIs(Context context, final IDIResultCallbacks callbackInterface) {
        IDIResultCallbacks tempCallbackInterface = new IDIResultCallbacks() {
            @Override
            public void onSuccess(String message) {
                sIMEI = message;
                callbackInterface.onSuccess(message);
            }

            @Override
            public void onError(String message) {
                callbackInterface.onError(message);
            }

            @Override
            public void onDebugStatus(String message) {
                callbackInterface.onDebugStatus(message);
            }
        };

        new RetrieveOEMInfoTask().execute(context, Uri.parse("content://oem_info/wan/imei"),
            tempCallbackInterface);
    }
}
