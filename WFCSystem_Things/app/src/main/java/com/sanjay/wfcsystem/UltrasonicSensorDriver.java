package com.sanjay.wfcsystem;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Driver class for Ultrasonic distance sensor - HC-SR04.
 * Used background threads to send pulses to trigger pin and get the echo pulses.
 *
 * @author Keval {https://github.com/kevalpatel2106}
 */

public final class UltrasonicSensorDriver implements AutoCloseable {
    private static final int INTERVAL_BETWEEN_TRIGGERS = 500;   //Interval between two subsequent pulses

    private final DistanceListener mListener;

    private Gpio mEchoPin;                  //GPIO for echo
    private Gpio mTrigger;                  //GPIO for trigger
    private Handler mTriggerHandler;        //Handler for trigger.

    /**
     * Runnable to send trigger pulses.
     */
    private Runnable mTriggerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                sendTriggerPulse();
                mTriggerHandler.postDelayed(mTriggerRunnable, INTERVAL_BETWEEN_TRIGGERS);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Callback for {@link #mEchoPin}. This callback will be called on both edges.
     */
    private GpioCallback mEchoCallback = new GpioCallback() {
        private long mPulseStartTime;

        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                if (gpio.getValue()) {
                    mPulseStartTime = System.nanoTime();
                } else {
                    //Calculate distance.
                    //From data-sheet (https://cdn.sparkfun.com/datasheets/Sensors/Proximity/HCSR04.pdf)
                    double distance = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - mPulseStartTime) / 58.23; //cm

                    //Notify callback
                    if (mListener != null) mListener.onDistanceChange(distance);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        public void onGpioError(Gpio gpio, int error) {
            super.onGpioError(gpio, error);
        }
    };

    /**
     * Public constructor.
     *
     * @param triggerPin Name of the trigger pin
     * @param echoPin    Name of the echo pin
     * @param listener   {@link DistanceListener} to get callbacks when distance changes.
     */
    public UltrasonicSensorDriver(@NonNull String triggerPin, @NonNull String echoPin, DistanceListener listener) {
        PeripheralManagerService service = new PeripheralManagerService();

        try {
            setTriggerPin(service, triggerPin);
            setEchoPin(service, echoPin);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Invalid pin name.");
        }

        mListener = listener;
        if (mListener == null)
            throw new IllegalArgumentException("DistanceListener cannot be null.");

        //Start sending pulses
        HandlerThread triggerHandlerThread = new HandlerThread("TriggerHandlerThread");
        triggerHandlerThread.start();
        mTriggerHandler = new Handler(triggerHandlerThread.getLooper());
        mTriggerHandler.post(mTriggerRunnable);
    }

    /**
     * Set the trigger pin
     *
     * @param service    {@link PeripheralManagerService}.
     * @param triggerPin Name of the trigger pin.
     * @throws IOException If pin initialization fails.
     */
    private void setTriggerPin(PeripheralManagerService service, String triggerPin) throws IOException {
        mTrigger = service.openGpio(triggerPin);
        mTrigger.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
    }

    /**
     * Set the echo pin
     *
     * @param service {@link PeripheralManagerService}.
     * @param echoPin Name of the echo pin.
     * @throws IOException If pin initialization fails.
     */
    private void setEchoPin(PeripheralManagerService service, String echoPin) throws IOException {
        mEchoPin = service.openGpio(echoPin);
        mEchoPin.setDirection(Gpio.DIRECTION_IN);
        mEchoPin.setEdgeTriggerType(Gpio.EDGE_BOTH);
        mEchoPin.setActiveType(Gpio.ACTIVE_HIGH);

        // Prepare handler for GPIO callback
        HandlerThread handlerThread = new HandlerThread("EchoCallbackHandlerThread");
        handlerThread.start();
        mEchoPin.registerGpioCallback(mEchoCallback, new Handler(handlerThread.getLooper()));
    }

    /**
     * Fire trigger pulse for 10 micro seconds.
     */
    private void sendTriggerPulse() throws IOException, InterruptedException {
        //Resetting trigger
        mTrigger.setValue(false);
        Thread.sleep(0, 2000);

        //Set trigger pin for 10 micro seconds.
        mTrigger.setValue(true);
        Thread.sleep(0, 10000);

        // Reset the trigger after 10 micro seconds.
        mTrigger.setValue(false);
    }

    /**
     * Closing the resources
     * @throws Exception
     */
    @Override
    public void close() throws IOException {
            mEchoPin.unregisterGpioCallback(mEchoCallback);
            mEchoPin.close();
            mTrigger.close();
    }
}
