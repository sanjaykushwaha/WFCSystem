package com.sanjay.wfcsystem;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
public class WFCActivity extends AppCompatActivity implements DistanceListener{
    private final int HEIGHT_OF_SENSOR_FROM_BOTTOM = 60;// Height of sensor position from tank bottom(in cm)
    private final int HEIGHT_OF_CONTAINER = 15;
    static final float ALPHA = 0.15f;
    private Gpio mPumpGPIO; //GPIO for Water moter
    private static final String WFCSystem_URL = "https://wfc-system.firebaseio.com/";
    private DatabaseReference databaseRef;
    private WFCSystem wfcSystem;
    private static final String TAG = "WFCActivity";
    private UltrasonicSensorDriver ultraSonicSensor;
    private int oldDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         ultraSonicSensor = new UltrasonicSensorDriver(GPIOKEYS.TRIGGER_PIN, GPIOKEYS.ECHO_PIN, this);
         initGPOPins();
         initFirebase();
    }

    /**
     * initialize GPIO pins
     */
    private void initGPOPins(){
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        try {
            // Create GPIO connection.
            mPumpGPIO = peripheralManagerService.openGpio(GPIOKEYS.PUMP_SWITCH_GPIO);

            // Configure as an output with default LOW (false) value.
            mPumpGPIO.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

        }catch (IOException e){
            Log.e(TAG, "Error on PeripheralIO API", e);
        }


    }

    /**
     * Initialize the use of Firebase db
     */
    private void initFirebase() {
        databaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl(WFCSystem_URL);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wfcSystem = dataSnapshot.getValue(WFCSystem.class);
                if(wfcSystem != null) {
                    Log.v(TAG, " Database: PumpOn: " + wfcSystem.isPumpOn() + ", AutoOn: " + wfcSystem.isAutoOn() + ", Water Level: " + wfcSystem.getWaterLevel());
                    try {
                        if (!wfcSystem.isAutoOn()) {
                            mPumpGPIO.setValue(wfcSystem.isPumpOn());
                        }
                    } catch (IOException e) {
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onDistanceChange(double distanceInCm) {
        int distance =  Integer.parseInt(String.format("%.0f", distanceInCm));
        Log.d("Distance", distanceInCm + " cm");
        if(oldDistance != distance){
            int diff = Math.abs(oldDistance - distance);
            if(diff>2) {
                oldDistance = distance;
                int precentage = ((HEIGHT_OF_SENSOR_FROM_BOTTOM - distance) / HEIGHT_OF_CONTAINER) * 100;
                wfcSystem.setWaterLevel(precentage);
                databaseRef.setValue(wfcSystem);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mPumpGPIO != null){
                mPumpGPIO.close();
            }
            if(ultraSonicSensor != null){
                ultraSonicSensor.close();
            }
        }catch (IOException e){
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

    }
}
