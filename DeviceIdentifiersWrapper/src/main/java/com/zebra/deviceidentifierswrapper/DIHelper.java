package com.zebra.deviceidentifierswrapper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Base64;

public class DIHelper {

    // Placeholder for custom certificate
    // Otherwise, the app will use the first certificate found with the method:
    // final Signature[] arrSignatures = packageInfo.signingInfo.getApkContentsSigners();
    // TODO: Put your custom certificate in the apkCertificate member for MX AccessMgr registering (only if necessary and if you know what you are doing)
    public static Signature apkCertificate = null;

    // This method will return the serial number in the string passed through the onSuccess method
    public static void getSerialNumber(Context context, IDIResultCallbacks callbackInterface)
    {
        registerCurrentApplication(context, Uri.parse("content://oem_info/oem.zebra.secure/build_serial"), new IDIResultCallbacks() {
            @Override
            public void onSuccess(String message) {
                String test = message;
            }

            @Override
            public void onError(String message) {
                String test = message;

            }

            @Override
            public void onDebugStatus(String message) {
                String test = message;

            }
        });
        RetrieveOEMInfo(context, Uri.parse("content://oem_info/oem.zebra.secure/build_serial"), callbackInterface);
    }

    // This method will return the imei number in the string passed through the onSuccess method
    public static void getIMEINumber(Context context, IDIResultCallbacks callbackInterface)
    {
        RetrieveOEMInfo(context, Uri.parse("content://oem_info/wan/imei"), callbackInterface);
    }

    private static void registerCurrentApplication(Context context, Uri serviceIdentifier, IDIResultCallbacks callbackInterface)
    {
        String profileName = "AccessMgr-1";
        String profileData = "";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES);
            String path = context.getApplicationInfo().sourceDir;
            final String strName = packageInfo.applicationInfo.loadLabel(context.getPackageManager()).toString();
            final String strVendor = packageInfo.packageName;
            Signature sig = apkCertificate;

            // Let's check if we have a custom certificate
            if(sig == null)
            {
                // Nope, we will get the first apk signing certificate that we find
                // You can copy/paste this snippet if you want to provide your own
                // certificate
                // TODO: use the following code snippet to extract your custom certificate if necessary
                final Signature[] arrSignatures = packageInfo.signingInfo.getApkContentsSigners();
                if(arrSignatures == null || arrSignatures.length == 0)
                {
                    if(callbackInterface != null)
                    {
                        callbackInterface.onError("Error : Package has no signing certificates... how's that possible ?");
                        return;
                    }
                }
                sig = arrSignatures[0];
            }

                /*
                 * Get the X.509 certificate.
                 */
                final byte[] rawCert = sig.toByteArray();

                // Get the certificate as a base64 string
                String encoded = Base64.getEncoder().encodeToString(rawCert);

                profileData =
                        "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                                "<characteristic type=\"Profile\">" +
                                "<parm name=\"ProfileName\" value=\"" + profileName + "\"/>" +
                                "<characteristic type=\"AccessMgr\" version=\"9.2\">" +
                                "<parm name=\"OperationMode\" value=\"1\" />" +
                                "<parm name=\"ServiceAccessAction\" value=\"4\" />" +
                                "<parm name=\"ServiceIdentifier\" value=\"" + serviceIdentifier + "\" />" +
                                "<parm name=\"CallerPackageName\" value=\"" + context.getPackageName().toString() + "\" />" +
                                "<parm name=\"CallerSignature\" value=\"" + encoded + "\" />" +
                                "</characteristic>"+
                                "</characteristic>";
                DIProfileManagerCommand profileManagerCommand = new DIProfileManagerCommand(context);
                profileManagerCommand.execute(profileData, profileName, callbackInterface);
            //}
        } catch (Exception e) {
            e.printStackTrace();
            if(callbackInterface != null)
            {
                callbackInterface.onError("Error on profile: " + profileName + "\nError:" + e.getLocalizedMessage() + "\nProfileData:" + profileData);
            }
        }
    }

    private static void RetrieveOEMInfo(final Context context, final Uri uri, final IDIResultCallbacks callbackInterface) {
        //  For clarity, this code calls ContentResolver.query() on the UI thread but production code should perform queries asynchronously.
        //  See https://developer.android.com/guide/topics/providers/content-provider-basics.html for more information
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null || cursor.getCount() < 1)
        {
            if(callbackInterface != null)
            {
                callbackInterface.onDebugStatus("App not registered to call OEM Service:" + uri.toString()+"\nRegistering current application using profile manger, this may take a couple of seconds...");
            }
            // Let's register the application
            registerCurrentApplication(context, uri, new IDIResultCallbacks() {
                @Override
                public void onSuccess(String message) {
                    // The app has been registered
                    // Let's try again to get the identifier
                    Cursor cursor2 = context.getContentResolver().query(uri, null, null, null, null);
                    if (cursor2 == null || cursor2.getCount() < 1) {
                        if(callbackInterface != null)
                        {
                            callbackInterface.onError("Fail to register the app for OEM Service call:" + uri + "\nIt's time to debug this app ;)");
                            return;
                        }
                    }
                    getURIValue(cursor2, uri, callbackInterface);
                    return;
                }

                @Override
                public void onError(String message) {
                    if(callbackInterface != null)
                    {
                        callbackInterface.onError(message);
                        return;
                    }
                }

                @Override
                public void onDebugStatus(String message) {
                    if(callbackInterface != null)
                    {
                        callbackInterface.onDebugStatus(message);
                    }
                }
            });
        }
        else
        {
            // We have the right to call this service, and we obtained some data to parse...
            getURIValue(cursor, uri, callbackInterface);
        }
    }

    private static void getURIValue(Cursor cursor, Uri uri, IDIResultCallbacks resultCallbacks)
    {
        while (cursor.moveToNext()) {
            if (cursor.getColumnCount() == 0)
            {
                //  No data in the cursor.  I have seen this happen on non-WAN devices
                String errorMsg = "Error: " + uri + " does not exist on this device";
                resultCallbacks.onDebugStatus(errorMsg);
            }
            else{
                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    try {
                        String data = cursor.getString(cursor.getColumnIndex(cursor.getColumnName(i)));
                        resultCallbacks.onSuccess(data);
                        cursor.close();
                        return;
                    }
                    catch (Exception e)
                    {
                        resultCallbacks.onDebugStatus(e.getLocalizedMessage());
                    }
                }
            }
        }
        cursor.close();
        resultCallbacks.onError("Data not found in Uri:" + uri);
    }
}
