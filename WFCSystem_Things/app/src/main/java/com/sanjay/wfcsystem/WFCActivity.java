package com.sanjay.wfcsystem;

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

public class WFCActivity extends AppCompatActivity {
    private Gpio mWater0GPIO;
    private Gpio mWater25GPIO;
    private Gpio mWater50GPIO;
    private Gpio mWater75GPIO;
    private Gpio mWater100GPIO;
    private Gpio mPumpGPIO;
    private static final String WFCSystem_URL = "https://wfc-system.firebaseio.com/";
    private DatabaseReference databaseRef;
    private WFCSystem wfcSystem;
    private static final String TAG = "WFCActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         wfcSystem = new WFCSystem();
         initGPOPins();
         initFirebase();
    }

    private void initGPOPins(){
        PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
        try {
            mWater0GPIO = peripheralManagerService.openGpio(GPIOKEYS.WATER_LEVEL_0);
            mWater0GPIO.setDirection(Gpio.DIRECTION_IN);
            mWater0GPIO.setEdgeTriggerType(Gpio.EDGE_FALLING);

            mWater25GPIO = peripheralManagerService.openGpio(GPIOKEYS.WATER_LEVEL_25);
            mWater25GPIO.setDirection(Gpio.DIRECTION_IN);
            mWater25GPIO.setEdgeTriggerType(Gpio.EDGE_FALLING);

            mWater50GPIO = peripheralManagerService.openGpio(GPIOKEYS.WATER_LEVEL_50);
            mWater50GPIO.setDirection(Gpio.DIRECTION_IN);
            mWater50GPIO.setEdgeTriggerType(Gpio.EDGE_FALLING);

            mWater75GPIO = peripheralManagerService.openGpio(GPIOKEYS.WATER_LEVEL_75);
            mWater75GPIO.setDirection(Gpio.DIRECTION_IN);
            mWater75GPIO.setEdgeTriggerType(Gpio.EDGE_FALLING);

            mWater100GPIO = peripheralManagerService.openGpio(GPIOKEYS.WATER_LEVEL_100);
            mWater100GPIO.setDirection(Gpio.DIRECTION_IN);
            mWater100GPIO.setEdgeTriggerType(Gpio.EDGE_FALLING);

            mPumpGPIO = peripheralManagerService.openGpio(GPIOKEYS.PUMP_SWITCH_GPIO);
            mPumpGPIO.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mWater0GPIO.registerGpioCallback(mCallback);
            mWater25GPIO.registerGpioCallback(mCallback);
            mWater50GPIO.registerGpioCallback(mCallback);
            mWater75GPIO.registerGpioCallback(mCallback);
            mWater100GPIO.registerGpioCallback(mCallback);
            mPumpGPIO.registerGpioCallback(mCallback);
        }catch (IOException e){
            Log.e(TAG, "Error on PeripheralIO API", e);
        }


    }
    private void initFirebase() {
        databaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl(WFCSystem_URL);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wfcSystem = dataSnapshot.getValue(WFCSystem.class);
                Log.v(TAG," Database: PumpOn: "+wfcSystem.isPumpOn()+", AutoOn: "+wfcSystem.isAutoOn()+", Water Level: "+wfcSystem.getWaterLevel());
                try {
                    if(!wfcSystem.isAutoOn()) {
                        mPumpGPIO.setValue(wfcSystem.isPumpOn());
                    }
                } catch( IOException e ) {
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            Log.i(TAG, "GPIO changed, button pressed"+gpio.getName());
            try {
                switch (gpio.getName()){
                    case GPIOKEYS.WATER_LEVEL_0:
                        if(wfcSystem.isAutoOn()) {
                            mPumpGPIO.setValue(true);
                            wfcSystem.setPumpOn(true);
                        }
                        wfcSystem.setWaterLevel(0);
                        break;
                    case GPIOKEYS.WATER_LEVEL_25:
                        wfcSystem.setWaterLevel(25);
                        break;
                    case GPIOKEYS.WATER_LEVEL_50:
                        wfcSystem.setWaterLevel(50);
                        break;
                    case GPIOKEYS.WATER_LEVEL_75:
                        wfcSystem.setWaterLevel(75);
                        break;
                    case GPIOKEYS.WATER_LEVEL_100:
                        mPumpGPIO.setValue(false);
                        wfcSystem.setPumpOn(false);
                        wfcSystem.setWaterLevel(100);
                        break;
                    case GPIOKEYS.PUMP_SWITCH_GPIO:
                        mPumpGPIO.setValue(!mPumpGPIO.getValue());
                        break;
                    default:
                }
                databaseRef.setValue(wfcSystem);
            }catch (IOException e){
                e.printStackTrace();
            }
            // Step 5. Return true to keep callback active.
            return true;
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mWater0GPIO != null){
                mWater0GPIO.unregisterGpioCallback(mCallback);
                mWater0GPIO.close();
            }
            if (mWater25GPIO != null){
                mWater25GPIO.unregisterGpioCallback(mCallback);
                mWater25GPIO.close();
            }
            if (mWater50GPIO != null){
                mWater50GPIO.unregisterGpioCallback(mCallback);
                mWater50GPIO.close();
            }
            if (mWater75GPIO != null){
                mWater75GPIO.unregisterGpioCallback(mCallback);
                mWater75GPIO.close();
            }
            if (mWater100GPIO != null){
                mWater100GPIO.unregisterGpioCallback(mCallback);
                mWater100GPIO.close();
            }
            if (mPumpGPIO != null){
                mPumpGPIO.unregisterGpioCallback(mCallback);
                mPumpGPIO.close();
            }
        }catch (IOException e){
            Log.e(TAG, "Error on PeripheralIO API", e);
        }

    }
}
