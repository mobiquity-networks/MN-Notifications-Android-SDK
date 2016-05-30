# Android Mobiquity Networks SDK v2 Sample App #

## Release Notes ##
* **0.0.1** 
    * V2 Sample App


## Setup ##
Import project into Android Studio.  Run on a real device (no simulators).

Currently this project has hardcoded credentials for both the AAR file on S3, and API key/secret for Mobiquity servers.  To update the API key/secret simple edit the mobiquity.properties file in the assets directory.  Below you will find the readme information for the SDK set and usage.


# Android Mobiquity Networks SDK V2 #

## Release Notes ##
* **0.0.29** 
    * V2 Release Candidate


## Setup ##
This SDK requires use of the Android Studio development environment.

Currently the SDK AAR file is stored in the Amazon S3 cloud.  This requires that we setup a new repository in the project build.gradle file:

```
...
allprojects {
    repositories {
       maven {
          url "s3://mobiquity-android/releases"
          credentials(AwsCredentials) {
             accessKey "AKIAJTNYS6A62H26OEHQ"
             secretKey "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"
          }
       }
       ...
    }
}
...
```

Then, in the the app module build.gradle we need to add this SDK as a dependency:
```
dependencies {
    ...
    compile 'com.mobiquitynetworks:mobiquitynetworkssdk:x.x.x'
    ...
}
```

The SDK is now part of your application and ready to be used.

## Integration ##

The easiest integration to the SDK comes from extending the MNApplication class and registering it in your Manifest file as your application class.  This will automatically take care of starting the SDK when the app starts.

```
#!java
public class MainApplication extends MNApplication {
}
```

In your application manifest:
```
#!xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="mobiquitynetworks.com.mobiquitynetworkssampleapp">

   ...

    <application
       ...
       android:name=".MainApplication">

       ...

    </application>
</manifest>
```

## Alternative Integration ##

If for some reason you're unable to extend the MNApplication class (for example you're already extending MultidexApplication), you can add the following to the **Application** onCreate method:

```
#!java

    @Override
    public void onCreate() {
        super.onCreate();

        ...

        MNManager.attachToApplication(this);
    }
```

## **Important!** - Targeting Android API 23 or higher? ##
If your application is targeting Android API 23 or higher you must request some permissions at runtime.  To enable beacon scanning we need the permission ACCESS_FINE_LOCATION:

```
#!java
// You must implement the ActivityCompat.OnRequestPermissionsResultCallback
public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ...

        // We must check whether we need to request the users location.  If targetting API >= 23 we need to request at runtime.
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    ...

    // You Must override this method and manually start the service.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    MNManager.getInstance(getApplicationContext()).startService();
                }
            }
        }
    }
}

```

## Migrating From V1 ##
If you have already integrated a previous version of the Mobiquity Networks SDK into your app, you will need to make some modifications.  Most of these modification centre around features such as demographics, custom information, and user tracking.

### User Tracking ###
In previous versions of the SDK there was a concept of a "user" that included a username and a provider.  This has been removed in favour of tracking users through their AAID (Android Advertising ID).  You are no longer required to retrieve an email address from your users.  The SDK will retrieve the AAID from their phone automatically.

### Demographics and Custom Vars ###
In previous versions you had to extend the SetupSDKReceiver and override onSetupRequest to create the TrackingDemographics object and pass it into the ProximityManager.  This has been removed in favour of a more streamlined approach.  You no longer receive a broadcast to create and insert your demographics or custom var information.  It can now be added at anytime through the MNManager class.  Information about how it is done is available below.  

### ProximityManager vs MNManager/ProximityApplication vs MNApplication ###
While previously you would use the ProximityManager to interact with the SDK, it has now been replaced with the MNManager which provides similar functionality.  Similarly the ProximityApplication class that you would extend has now been replaced with the MNApplication class.

## Advanced Features ##

### Demographics ###
You can add demographic information that is used for ad filtering purposes:

```
#!java
        Demographics demographics = new Demographics();
        demographics.setEducation(Demographics.Education.COLLEGE);
        demographics.setEthnicity(Demographics.Ethnicity.CAUSASIAN);
        demographics.setGender(Demographics.Gender.MALE);
        demographics.setMaritalStatus(Demographics.MaritalStatus.MARRIED);

        MNManager.getInstance(getApplicationContext()).updateDemographics(demographics);
```
You can also retrieve the demographics currently set to update them:

```
#!java
        Demographics demographics = MNManager.getInstance(getApplicationContext()).getDemographics();
```


### Custom Vars ###
You can add custom vars:

```
#!java
        Map<String, String> customVars = new HashMap<String, String>();
        customVars.put("custom1", "foo");
        customVars.put("custom2", "bar");

        MNManager.getInstance(getApplicationContext()).updateCustomVars(customVars);
```
You can also retrieve the custom vars currently set to update them:

```
#!java
        Map<String, String> customVars = MNManager.getInstance(getApplicationContext()).getCustomVarsMap();
```


### Custom Tags ##
You can add custom tags:

```
#!java
        List<String> tags = new ArrayList<String>();
        tags.add("horror");
        tags.add("comedy");
        tags.add("drama");

        MNManager.getInstance(getApplication()).updateTags(tags);
```
You can also retrieve your custom tags to update them:

```
#!java
        List<String> tags = MNManager.getInstance(getApplicationContext()).getTags();
```


### Deeplinking Your App ###
If you want to be able to open you app by clicking a notification, you need to register the associated deeplink scheme and create an Activity or Service that will handle the deeplink.

Register the deeplink scheme in your Manifest:

```
#!xml
        <activity
            android:name=".DeepLinkActivity"
            android:label="@string/title_activity_deep_link"
            android:theme="@style/AppTheme.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />

                <data
                    android:host="deeplink"
                    android:scheme="mobiquitySampleApp" />
            </intent-filter>

        </activity>
```

Create the associated Activity:

```
#!java
public class DeepLinkActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_link);
        final Intent intent = getIntent();
        if( intent == null || intent.getData() == null ){
            finish();
            return;
        }

        //get deeplink uri
        Uri uri = intent.getData();
        openDeeplink(uri);
    }

    private void openDeeplink(Uri deepLink) {
        String path = deepLink.getPath();

        // Pass extra parameters on the URI
        String campaignId = deepLink.getQueryParameter("campaignId");

        // Handle the path specific portion of the URI
        if("/home".equals(path)) {
            // Open home screen
            startActivity(new Intent(HomeActivity.class));
        } else if ("/foo".equals(path)) {
            // Open foo screen
            startActivity(new Intent(FooActivity.class));
        }
        // etc...
    }
}

```

### Bluetooth Broadcast ###
The SDK will send a broadcast whenever the Bluetooth is turned off.  This is an optional broadcast your app can receive if it wishes to be notified by the SDK when the Bluetooth is turned off and thus the SDK will not function.  Below you will find a sample implementation that will ask the user to enable Bluetooth while specifying which app is requesting it and why.

```
#!java

public class BluetoothReceiver extends BroadcastReceiver {
    public final static int DISMISS = 0;
    public final static int ENABLE = 1;
    public static int notificationId = createNotificationId();
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent buttonDismissIntent = new Intent(context, ButtonReceiver.class);
        buttonDismissIntent.putExtra("notification.id", notificationId);
        buttonDismissIntent.putExtra("button.clicked", DISMISS);
        PendingIntent buttonDismissPendingIntent = PendingIntent.getBroadcast(context, 0, buttonDismissIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent buttonEnableIntent = new Intent(context, ButtonReceiver.class);
        buttonEnableIntent.putExtra("notification.id", notificationId);
        buttonEnableIntent.putExtra("button.clicked", ENABLE);
        PendingIntent buttonEnablePendingIntent = PendingIntent.getBroadcast(context, 0, buttonEnableIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Create notification
        Notification.Action cancelAction = new Notification.Action.Builder(R.drawable.ic_media_pause, "Dismiss", buttonDismissPendingIntent).build();
        Notification.Action confirmAction = new Notification.Action.Builder(R.drawable.ic_media_play, "Configure", buttonEnablePendingIntent).build();

        Notification notification = new Notification.Builder(context)
                .setContentTitle("DemoApp needs Bluetooth.")
                .setContentText("Enable bluetooth to unlock offers!")
                .setSmallIcon(R.drawable.cast_ic_notification_2)
                .setContentIntent(buttonEnablePendingIntent)
                .setAutoCancel(true)
                .addAction(cancelAction)
                .addAction(confirmAction)
                .build();

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, notification);
    }

    private static int createNotificationId() {
        int max = 200;
        int min = 100;
        Random r = new Random();
        int id = r.nextInt(max - min) + min;
        return id;
    }

    public static class ButtonReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            int buttonCode = intent.getIntExtra("button.clicked", ENABLE);
            if (buttonCode == ENABLE) {
                Intent i = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }

            int notificationId = intent.getIntExtra("notification.id", 0);
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationId);
        }
    }
}

```

The broadcast receivers must also be defined in the manifest:

```
#!xml

        <receiver
            android:name=".BluetoothReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="com.mobiquitynetworks.action.bluetoothEnable" />
            </intent-filter>
        </receiver>

        <receiver
            android:name=".BluetoothReceiver$ButtonReceiver"
            android:exported="false" />
```

### Google Play Service Broadcast ###
The SDK will send a broadcast message whenever a GooglePlayServicesRepairableException.  It will contain the required status code so that the app can request the user to take whatever action is required to fix the issue with Google Play Services.  Below you will find a sample implementation of the receiver.


```
#!java

public class GooglePlayServicesReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            int code = intent.getIntExtra(MNManager.INTENT_EXTRA_CONNECTION_CODE, -1);
            GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
            PendingIntent pendingIntent = googleAPI.getErrorResolutionPendingIntent(context, code, 123);

            if(pendingIntent != null) {
                try {
                    pendingIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

Always register the receiver in the manifest:

```
#!xml

        <receiver
            android:name=".GooglePlayServicesReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="com.mobiquitynetworks.action.googlePlayServiceUpdate" />
            </intent-filter>
        </receiver>
```

### Venue and POI Broadcast ###
The SDK will broadcast the venue and POI location information retrieved anytime a beacon enter event is processed along with the information for the beacon that triggered the event.  Below is a sample implementation of this receiver.

```
#!java

public class LocationInformationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent != null) {
            BeaconLocationSignal beaconLocationSignal = intent.getParcelableExtra(MNManager.INTENT_EXTRA_BEACON_LOCATION_SIGNAL);

            if (beaconLocationSignal != null) {
               // Handle any beacon information you wish to use
               BeaconLocationSignal.Beacon beacon = beaconLocationSignal.getBeacon();

               if(beaconLocationSignal.getBeacon() != null) {
                  // The Beacon object may contain beacon region information as well as beacon proximity information
               }

                // Handle any Venue or POI information you wish to use
                Location.Venue venue = beaconLocationSignal.getVenue();
                List<Location.NearPOI> pois = beaconLocationSignal.getNearPOIs();

                if (venue != null) {

                }

                if(pois != null && pois.size() > 0) {

                }
            }
        }
    }
}
```

Always remember to register the receiver in the manifest:

```
#!xml

        <receiver
            android:name=".LocationInformationReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="com.mobiquitynetworks.action.location" />
            </intent-filter>
        </receiver>
```



### Mobiquity Properties File ###
The following are the valid keys/values for the mobiquity.properties file.  All but the appkey and secret are optional.


```
appkey=<Required App Key from Mobiquity Networks>
secret=<Required Secret from Mobiquity Networks>
environment=production/sandbox/qa/dev (defaults to production)
role=final/test/dev (defaults to final)
iconname=<whatever the icon you'd like to use in the notifications is called, defaults to app icon>
debug=true/false (setting this to true will allow the SDK to log messages, default is false)
```