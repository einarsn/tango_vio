package org.ros.android.android_tutorial_pubsub;

/**
 * Created by einar on 2015-09-30.
 */

import android.util.Log;
import org.ros.node.Node;
import org.ros.node.NodeMain;
import org.ros.concurrent.CancellableLoop;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;

// Project tango imports
import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import com.google.atap.tangoservice.TangoCoordinateFramePair;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class TangoVioNode implements NodeMain {
    posePublisher pose_pub;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SECS_TO_MILLISECS = 1000;
    private Tango mTango;
    private TangoConfig mConfig;

    private float mPreviousTimeStamp;
    private int mPreviousPoseStatus;
    private int count;
    private float mDeltaTime;
    private boolean mIsAutoRecovery;

    private boolean mIsProcessing = false;
    private TangoPoseData mPose;
    private static final int UPDATE_INTERVAL_MS = 100;
    public static Object sharedLock = new Object();

    public TangoVioNode(Tango inTango, TangoConfig inConfig){
        mTango = inTango;
        mConfig = inConfig;
    }

    @Override
    public GraphName getDefaultNodeName() {
        Log.d("tangov_vio_node", "getDefaultNodename");
        return GraphName.of("Tango_Vio_Publisher");
    }

    @Override
    public void onStart(ConnectedNode node) {
        Log.d("tango_vio_node","-------------connecting to Tango");
        mTango.connect(mConfig);
        Log.d("tango_vio_node", "--------------finished connecting to Tango");
        Log.d("tango_vio_node", "got into onStart()");
        pose_pub = new posePublisher(node);
        Log.d("tango_vio_node", "initialized publisher");
        setTangoListeners();
        /*
        node.executeCancellableLoop(new CancellableLoop() {
            @Override
            protected void loop() throws InterruptedException {
                if(mPose == null){
                    return;
                }
                else {
                    updateTranslation(mPose.translation);
                    updateRoataion(mPose.rotation);
                    Log.d("tango_vio_node", mPose.toString());
                    pose_pub.publishPose();
                }
                Thread.sleep(5);
            }
        });*/
    }

    @Override
    public void onShutdown(Node node) {
        Log.d("tango_vio_node", "Entered Shutdown");
    }

    @Override
    public void onShutdownComplete(Node node) {
        Log.d("tango_vio_node", "Shutdown complete");
    }

    @Override
    public void onError(Node node, Throwable throwable) {
        Log.d("tango_vio_node", "Error");
    }

    public void updateTranslation(double[] state) {
        pose_pub.setPoint(state[0], state[1], state[2]);
    }

    public void updateRoataion(double[] state) {
        pose_pub.setQuat(state[0], state[1], state[2], state[3]);
    }

    private void setTangoListeners() {
        // Lock configuration and connect to Tango
        // Select coordinate frame pair
        final ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();
        framePairs.add(new TangoCoordinateFramePair(
                TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                TangoPoseData.COORDINATE_FRAME_DEVICE));
        // Listen for new Tango data
        mTango.connectListener(framePairs, new OnTangoUpdateListener() {

            @Override
            public void onPoseAvailable(final TangoPoseData pose) {
                //Make sure to have atomic access to Tango Pose Data so that
                //render loop doesn't interfere while Pose call back is updating
                // the data.
                synchronized (sharedLock) {
                    mPose = pose;
                    updateTranslation(mPose.translation);
                    updateRoataion(mPose.rotation);
                    Log.d("tango_vio_node", mPose.toString());
                    pose_pub.publishPose();
                    mDeltaTime = (float) (pose.timestamp - mPreviousTimeStamp) * SECS_TO_MILLISECS;
                    mPreviousTimeStamp = (float) pose.timestamp;
                    // Log whenever Motion Tracking enters an invalid state
                    if (!mIsAutoRecovery && (pose.statusCode == TangoPoseData.POSE_INVALID)) {
                        Log.w(TAG, "Invalid State");
                    }
                    if (mPreviousPoseStatus != pose.statusCode) {
                        count = 0;
                    }
                    count++;
                    mPreviousPoseStatus = pose.statusCode;
                }
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData arg0) {
                // We are not using TangoXyzIjData for this application
            }

            @Override
            public void onTangoEvent(final TangoEvent event) {
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                // We are not using onFrameAvailable for this application
            }
        });
    }

}
