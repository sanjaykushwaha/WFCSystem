package com.sanjay.wfcsystem;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String WFCSystem_URL = "https://wfc-system.firebaseio.com/";
    private DatabaseReference databaseRef;
    private WFCSystem wfcSystem;
    private Button btnMoter;
    private ProgressBar pbWaterLable;
    private Switch switchAutoMode;
    private LinearLayout llParent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        llParent = (LinearLayout)findViewById(R.id.ll_parent);
        btnMoter = (Button)findViewById(R.id.btn_moter);
        pbWaterLable = (ProgressBar)findViewById(R.id.pbWaterLabel);
        switchAutoMode = (Switch)findViewById(R.id.switchAutoMode);
        switchAutoMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                btnMoter.setVisibility(!b?View.VISIBLE:View.GONE);
                wfcSystem.setAutoOn(b);
                databaseRef.setValue(wfcSystem);
            }
        });
        btnMoter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wfcSystem.setPumpOn(!wfcSystem.isPumpOn());
                databaseRef.setValue(wfcSystem);
            }
        });
        databaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl(WFCSystem_URL);
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                llParent.setVisibility(View.VISIBLE);
                wfcSystem = dataSnapshot.getValue(WFCSystem.class);
                btnMoter.setText(wfcSystem.isPumpOn()?"Switch OFF":"Switch ON");
                switchAutoMode.setChecked(wfcSystem.isAutoOn());
                pbWaterLable.setProgress(wfcSystem.getWaterLevel());
              //  pushNotification("Hello WFC System");
                Log.v("Sanjay"," Database: PumpOn: "+wfcSystem.isPumpOn()+", AutoOn: "+wfcSystem.isAutoOn()+", Water Level: "+wfcSystem.getWaterLevel());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

//    private void pushNotification(String message){
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setContentTitle("WFC System")
//                        .setContentText(message);
//
//// Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this, MainActivity.class);
//
//// The stack builder object will contain an artificial back stack for the
//// started Activity.
//// This ensures that navigating backward from the Activity leads out of
//// your app to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//// Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(MainActivity.class);
//// Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//// mNotificationId is a unique integer your app uses to identify the
//// notification. For example, to cancel the notification, you can pass its ID
//// number to NotificationManager.cancel().
//        mNotificationManager.notify(1, mBuilder.build());
//    }
}
