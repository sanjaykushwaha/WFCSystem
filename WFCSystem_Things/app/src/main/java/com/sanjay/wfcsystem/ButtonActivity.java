package com.sanjay.wfcsystem;

import android.app.Activity;
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

public class ButtonActivity extends Activity {
    private static final String TAG = "ButtonActivity";
//    private static final String WFCSystem_URL = "https://wfc-system.firebaseio.com/";
//    private DatabaseReference databaseRef;
//    private WFCSystem wfcSystem;
    private static final String BUTTON_PIN_NAME = "GPIO5_IO02"; // GPIO port wired to the button
    private static final String LED_PIN_NAME = "GPIO2_IO03"; // GPIO port wired to the LED
    private Gpio mButtonGpio;
    private Gpio mLedGpio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initGPOPins();
     //   initFirebase();
    }

    private void initGPOPins(){
        PeripheralManagerService service = new PeripheralManagerService();
        try {
            // Step 1. Create GPIO connection.
            mButtonGpio = service.openGpio(BUTTON_PIN_NAME);
            // Step 2. Configure as an input.
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            // Step 3. Enable edge trigger events.
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            // Step 4. Register an event callback.
            mButtonGpio.registerGpioCallback(mCallback);

            mLedGpio = service.openGpio(LED_PIN_NAME);
            // Step 2. Configure as an output.
            mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        } catch (IOException e) {
            Log.e(TAG, "Error on PeripheralIO API", e);
        }
    }
//    private void initFirebase() {
//        databaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl(WFCSystem_URL);
//        databaseRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                wfcSystem = dataSnapshot.getValue(WFCSystem.class);
//                Log.v("Sanjay"," Database: PumpOn: "+wfcSystem.isPumpOn()+", AutoOn: "+wfcSystem.isAutoOn()+", Water Label: "+wfcSystem.getWaterLabel());
////                try {
////                    fanStateSignal.setValue(smartFanStates.isFanOn());
////                } catch( IOException e ) {
////                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//            }
//        });
//    }


    // Step 4. Register an event callback.
    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            Log.i(TAG, "GPIO changed, button pressed"+gpio.getName());
            try {
                if(!gpio.getValue()){
                    mLedGpio.setValue(true);
                }else {
                    mLedGpio.setValue(false);
                }
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

        // Step 6. Close the resource
        if (mButtonGpio != null) {
            mButtonGpio.unregisterGpioCallback(mCallback);
            try {
                mButtonGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }

        // Step 6. Close the resource
        if (mLedGpio != null) {
            try {
                mLedGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error on PeripheralIO API", e);
            }
        }
    }
}

