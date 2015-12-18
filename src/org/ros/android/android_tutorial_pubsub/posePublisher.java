package org.ros.android.android_tutorial_pubsub;

/**
 * Created by einar on 2015-09-30.
 */
import android.util.Log;

import org.ros.message.Time;
import org.ros.node.ConnectedNode;
import org.ros.node.topic.Publisher;

import geometry_msgs.Point;
import geometry_msgs.PoseStamped;
import geometry_msgs.Quaternion;


public class posePublisher {
    private PoseStamped pose;
    private Publisher<PoseStamped> publisher;

    public posePublisher(ConnectedNode connectedNode){
        Log.d("publisher","Initializing publisher");
        pose = connectedNode.getTopicMessageFactory().newFromType(PoseStamped._TYPE);
        publisher = connectedNode.newPublisher("/tango_pose", PoseStamped._TYPE);
        Log.d("publisher","Finished initializing publisher");
    }

    public void setQuat(double x, double y, double z, double w) {
        Log.d("publisher","Updating quaternion");
        Quaternion q = pose.getPose().getOrientation();
        q.setW(w);
        q.setX(x);
        q.setY(y);
        q.setZ(z);
        pose.getPose().setOrientation(q);
        Log.d("publisher", "Finished updating quaternion");
    }

    public void setPoint(double x, double y, double z) {
        Log.d("publisher","Updating point");
        Point p = pose.getPose().getPosition();
        p.setX(x);
        p.setY(y);
        p.setZ(z);
        pose.getPose().setPosition(p);
        Log.d("publisher", "Finished updating point");
    }

    public void publishPose(){
        Log.d("publisher","Publishing pose");
        long lt = System.currentTimeMillis();
        Time t = new Time((int) (lt / 1e3), (int) ((lt % 1e3) * 1e6));
        pose.getHeader().setStamp(t);
        pose.getHeader().setFrameId("/map");
        publisher.publish(pose);
        Log.d("publisher", "Finished publishing pose");
    }

}
