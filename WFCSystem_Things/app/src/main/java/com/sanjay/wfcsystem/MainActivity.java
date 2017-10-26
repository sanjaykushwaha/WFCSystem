package com.sanjay.wfcsystem;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String WFCSystem_URL = "https://wfc-system.firebaseio.com/";
    private DatabaseReference databaseRef;
    private WFCSystem wfcSystem;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl(WFCSystem_URL);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wfcSystem = dataSnapshot.getValue(WFCSystem.class);
                Log.v("Sanjay"," Database: PumpOn: "+wfcSystem.isPumpOn()+", AutoOn: "+wfcSystem.isAutoOn());
//                try {
//                    fanStateSignal.setValue(smartFanStates.isFanOn());
//                } catch( IOException e ) {
//                }
                wfcSystem.setAutoOn(false);
                databaseRef.setValue(wfcSystem);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}
