package mobiquitynetworks.com.mobiquitynetworkssampleapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.mobiquitynetworks.MNManager;
import com.mobiquitynetworks.model.commonpayload.Demographics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int FINE_LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // We must check whether we need to request the users location.  If targetting API >= 23 we need to request at runtime.
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Example way to set demographics:
        Demographics demographics = new Demographics();
        demographics.setEducation(Demographics.Education.COLLEGE);
        MNManager.getInstance(getApplicationContext()).updateDemographics(demographics);

        // Example way to set custom tags:
        List<String> tags = new ArrayList<String>();
        tags.add("horror");
        MNManager.getInstance(getApplicationContext()).updateTags(tags);

        // Example way to set custom vars:
        Map<String, String> customVars = new HashMap<String, String>();
        customVars.put("key1", "value1");
        MNManager.getInstance(getApplicationContext()).updateCustomVars(customVars);
    }


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
