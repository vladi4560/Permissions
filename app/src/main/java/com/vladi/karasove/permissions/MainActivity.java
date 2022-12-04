package com.vladi.karasove.permissions;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private MaterialButton requestCamera;
    private final int REQUEST_CODE_PERMISSION_CAMERA = 2;


    private MaterialButton requestContacts;
    private final int REQUEST_CODE_PERMISSION_CONTACTS = 3;
    private static final int MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE = 103;
    private boolean foundContact = false;

    private SensorManager sensorManager;
    private boolean isPhoneFlat = false;

    private AppCompatTextView welcome;
    private MaterialButton enter;
    private TextInputLayout userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        initViews();
    }


    private void findViews() {
        requestContacts = findViewById(R.id.mainActivity_BTN_contacts);
        requestCamera = findViewById(R.id.mainActivity_BTN_camera);
        enter = findViewById(R.id.mainActivity_BTN_enter);
        welcome = findViewById(R.id.mainActivity_TXT_welcome);
        userName = findViewById(R.id.main_EDT_userName);
    }

    private void initViews() {
        requestContacts.setOnClickListener(view -> requestContacts());
        requestCamera.setOnClickListener(view -> requestCamera());
        enter.setOnClickListener(view -> checkConditions());
        sensorInit();
    }

    private void sensorInit() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener((SensorEventListener) this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void checkConditions() {
        if (!isPhoneCharging()) {
            Toast.makeText(getApplicationContext(), "All the conditions must be valid", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "All the conditions must be valid", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isPhoneFlat) {
            Toast.makeText(getApplicationContext(), "All the conditions must be valid", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isContactExists()) {
            Toast.makeText(getApplicationContext(), "All the conditions must be valid", Toast.LENGTH_LONG).show();
            return;
        }
        if (!isCameraAvailable()) {
            Toast.makeText(getApplicationContext(), "All the conditions must be valid", Toast.LENGTH_LONG).show();
            return;
        }
        if (userName.getEditText().getText().toString().isEmpty()) {
            Toast.makeText(getApplicationContext(), "Enter Your Name", Toast.LENGTH_LONG).show();
            return;
        }
        //  openActivity();
        welcome.setText("Welcome " + userName.getEditText().getText().toString());
        welcome.setVisibility(View.VISIBLE);
    }

    private void openActivity(Class activity) {
        Intent intent = new Intent(this, activity);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        startActivity(intent);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float ax, ay, az;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            ax = event.values[0];
            ay = event.values[1];
            az = event.values[2];
            float norm_Of_g = (float) Math.sqrt(ax * ax + ay * ay + az * az);
            az = (az / norm_Of_g);
            int inclination = (int) Math.round(Math.toDegrees(Math.acos(az)));
            if (inclination < 10 || inclination > 175) {
                // device is flat
                isPhoneFlat = true;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private boolean isWifiEnabled() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifi.isWifiEnabled();
    }

    private boolean isPhoneCharging() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        return isCharging;
    }

    private void requestContacts() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE_PERMISSION_CONTACTS);
    }

    private boolean isCameraAvailable() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCamera() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION_CAMERA);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_CONTACTS: {
                Log.d("pttt", "REQUEST_CODE_PERMISSION_CONTACTS");
                boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
                if (result) {
                    return;
                }
                requestPermissionWithRationaleCheck();
                return;
            }
            case REQUEST_CODE_PERMISSION_CAMERA: {
                Log.d("pttt", "REQUEST_CODE_PERMISSION_WIFI");
                boolean result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
                if (result) {
                    return;
                }
                requestPermissionWithRationaleCheck();
                return;
            }

        }
    }


    private void requestPermissionWithRationaleCheck() {
        String message = "an approved needed for entering to the application";
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
            AlertDialog alertDialog =
                    new AlertDialog.Builder(this)
                            .setMessage(message)
                            .setPositiveButton(getString(android.R.string.ok),
                                    (dialog, which) -> {
                                        requestContacts();
                                        dialog.cancel();
                                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // disabled functions due to denied permissions
                                }
                            })
                            .show();
            alertDialog.setCanceledOnTouchOutside(true);
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            AlertDialog alertDialog =
                    new AlertDialog.Builder(this)
                            .setMessage(message)
                            .setPositiveButton(getString(android.R.string.ok),
                                    (dialog, which) -> {
                                        requestContacts();
                                        dialog.cancel();
                                    })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // disabled functions due to denied permissions
                                }
                            })
                            .show();
            alertDialog.setCanceledOnTouchOutside(true);
        } else {
            Log.d("pttt", "shouldShowRequestPermissionRationale = false");
            openPermissionSettingDialog();
        }
    }

    private void openPermissionSettingDialog() {
        String message = "for granting permission go to the settings of the application.";
        AlertDialog alertDialog =
                new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setPositiveButton(getString(android.R.string.ok),
                                (dialog, which) -> {
                                    openSettingsManually();
                                    dialog.cancel();
                                }).show();
        alertDialog.setCanceledOnTouchOutside(true);
    }

    private void openSettingsManually() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, MANUALLY_CONTACTS_PERMISSION_REQUEST_CODE);
    }

    private boolean isContactExists() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            foundContact = searchContact("+972 54-208-9220");
        return foundContact;
    }

    private boolean searchContact(String number) {
        Cursor cursor = getContentResolver().query(
                android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID, null,
                null);
        ArrayList<String> contactNumbers = new ArrayList<String>();
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            contactNumbers.add(cursor.getString(0));
            Log.d("pttt", "number:" + cursor.getString(0));
        }
        if (contactNumbers.contains(number)) {
            return true;
        }
        return false;
    }


}
