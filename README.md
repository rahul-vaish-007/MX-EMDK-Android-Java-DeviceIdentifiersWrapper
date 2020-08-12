# EMDK-DeviceIdentifiers-Sample

How to access device identifiers such as serial number and IMEI on Zebra devices running Android 10

Android 10 limited access to device identifiers for all apps running on the platform regardless of their target API level.  As explained in the docs for [Android 10 privacy changes](https://developer.android.com/about/versions/10/privacy/changes) this includes the serial number, IMEI and some other identifiable information.

**Zebra mobile computers running Android 10 are able to access both the serial number and IMEI** however applications need to be **explicitly granted the ability** to do so and use a proprietary API.

To access the serial number and IMEI file on Zebra Android devices running Android 10 or higher, first declare a new permission in your AndroidManifest.xml

```xml
<uses-permission android:name="com.zebra.provider.READ"/>
<uses-permission android:name="com.symbol.emdk.permission.EMDK" />
```

Then add the uses-library element to your application 

```xml
    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <uses-library android:name="com.symbol.emdk" />
        <activity android:name=".MainActivity">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
```

Finally, add EMDK dependency to your application build.graddle file:
```text
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation 'com.android.support:appcompat-v7:28.0.0'
    testImplementation 'junit:junit:4.13'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
    compileOnly 'com.symbol:emdk:+'
}
```

Snippet code to use to retrieve the Serial Number of the device:

```java
     private void getSerialNumber()
     {
         DIHelper.getSerialNumber(this, new IDIResultCallbacks() {
             @Override
             public void onSuccess(String message) {
                 // The message contains the serial number
                 String mySerialNumber = message
             }

             @Override
             public void onError(String message) {
                // An error occured
             }

             @Override
             public void onDebugStatus(String message) {
                // You can use this method to get verbose information
                // about what's happening behing the curtain             
             }
         });
     }
```

Snippet code to use to retrieve the Serial Number of the device:

```java
    private void getIMEINumber()
    {
        DIHelper.getIMEINumber(this, new IDIResultCallbacks() {
            @Override
            public void onSuccess(String message) {
                // We've got an EMEI number
                String myIMEI = message;
            }

            @Override
            public void onError(String message) {
                // An error occured
            }

            @Override
            public void onDebugStatus(String message) {
                // You can use this method to get verbose information
                // about what's happening behing the curtain
            }
        });
    }
```
