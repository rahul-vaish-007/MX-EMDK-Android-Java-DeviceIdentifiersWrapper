package com.zebra.deviceidentifierswrapper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.AsyncTask;

import java.util.Base64;

class UnregisterAppTask extends AsyncTask<Object, Void, Boolean> {
    @Override
    protected Boolean doInBackground(Object... objects) {
        Context context = (Context) objects[0];
        Uri uri = (Uri) objects[1];
        IDIResultCallbacks idiResultCallbacks = (IDIResultCallbacks) objects[2];
        unregisterCurrentApplication(context, uri, idiResultCallbacks);
        return true;
    }

    private static void unregisterCurrentApplication(Context context,
                                                     Uri serviceIdentifier,
                                                     IDIResultCallbacks callbackInterface) {
        String profileName = "AccessMgr-1";
        String profileData = "";
        try {
            String callerSignature = getCallerSignature(context);
            if (callerSignature == null) {
                if (callbackInterface != null) {
                    callbackInterface.onError("Error : Package has no signing certificates... " +
                            "how's that possible ?");
                }
                return;
            }

            profileData =
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
                            "<characteristic type=\"Profile\">" +
                            "<parm name=\"ProfileName\" value=\"" + profileName + "\"/>" +
                            "<characteristic type=\"AccessMgr\" version=\"9.2\">" +
                            "<parm name=\"OperationMode\" value=\"1\" />" +
                            "<parm name=\"ServiceAccessAction\" value=\"5\" />" +
                            "<parm name=\"ServiceIdentifier\" value=\"" + serviceIdentifier + "\" />" +
                            "<parm name=\"CallerPackageName\" value=\"" + context.getPackageName() + "\" />" +
                            "<parm name=\"CallerSignature\" value=\"" + callerSignature + "\" />" +
                            "</characteristic>" +
                            "</characteristic>";
            DIProfileManagerCommand profileManagerCommand = new DIProfileManagerCommand(context);
            profileManagerCommand.execute(profileData, profileName, callbackInterface);
            //}
        } catch (Exception e) {
            e.printStackTrace();
            if (callbackInterface != null) {
                callbackInterface.onError("Error on profile: " + profileName + "\nError:"
                        + e.getLocalizedMessage() + "\nProfileData:" + profileData);
            }
        }
    }

    private static String getCallerSignature(Context context)
            throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager()
                .getPackageInfo(
                        context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES
                );
        String path = context.getApplicationInfo().sourceDir;
        final String strName = packageInfo.applicationInfo
                .loadLabel(context.getPackageManager()).toString();
        final String strVendor = packageInfo.packageName;
        Signature sig = DIHelper.apkCertificate;

        // Let's check if we have a custom certificate
        if (sig == null) {
            // Nope, we will get the first apk signing certificate that we find
            // You can copy/paste this snippet if you want to provide your own
            // certificate
            // TODO: use the following code snippet to extract your custom certificate if necessary
            Signature[] arrSignatures = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                arrSignatures = packageInfo.signingInfo.getApkContentsSigners();
            }
            if (arrSignatures == null || arrSignatures.length == 0) {
                return null;
            }
            sig = arrSignatures[0];
        }

        /*
         * Get the X.509 certificate.
         */
        final byte[] rawCert = sig.toByteArray();

        // Get the certificate as a base64 string
        String encodedCert = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            encodedCert = Base64.getEncoder().encodeToString(rawCert);
        }
        return encodedCert;
    }
}
