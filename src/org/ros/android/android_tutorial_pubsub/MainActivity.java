/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.ros.android.android_tutorial_pubsub;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;

import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

// Tango imports

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.Tango.OnTangoUpdateListener;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * @author damonkohler@google.com (Damon Kohler)
 */
public class MainActivity extends RosActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
  private TangoVioNode tangoVioNode;
  private boolean mIsAutoRecovery;
    private Tango mTango;
    private TangoConfig mConfig;
  public MainActivity() {
    // The RosActivity constructor configures the notification title and ticker
    // messages.

    super("Pubsub Tutorial", "Pubsub Tutorial");
  }

  @SuppressWarnings("unchecked")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
      Intent intent = getIntent();
      mIsAutoRecovery = intent.getBooleanExtra(StartActivity.KEY_MOTIONTRACKING_AUTORECOVER,
              false);

      // Instantiate the Tango service
      mTango = new Tango(this);
      // Create a new Tango Configuration and enable the MotionTrackingActivity API
      mConfig = new TangoConfig();
      mConfig = mTango.getConfig(TangoConfig.CONFIG_TYPE_CURRENT);
      mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_MOTIONTRACKING, true);

      // The Auto-Recovery ToggleButton sets a boolean variable to determine
      // if the
      // Tango service should automatically attempt to recover when
      // / MotionTrackingActivity enters an invalid state.
      if (mIsAutoRecovery) {
          mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, true);
          Log.i(TAG, "Auto Reset On");
      } else {
          mConfig.putBoolean(TangoConfig.KEY_BOOLEAN_AUTORECOVERY, false);
          Log.i(TAG, "Auto Reset Off");
      }

  }

  @Override
  protected void init(NodeMainExecutor nodeMainExecutor) {
      Log.d("MainActivity","--------Entered NodeMainExecutor----------");
    tangoVioNode = new TangoVioNode(mTango,mConfig);
    NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
    nodeConfiguration.setMasterUri(getMasterUri());
    nodeMainExecutor.execute(tangoVioNode, nodeConfiguration);
      Log.d("MainActivity","------------Exiting NodeMainEcecutor----------");
    /*
    talker = new Talker();

    // At this point, the user has already been prompted to either enter the URI
    // of a master to use or to start a master locally.

    // The user can easily use the selected ROS Hostname in the master chooser
    // activity.
    NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(getRosHostname());
    nodeConfiguration.setMasterUri(getMasterUri());
    nodeMainExecutor.execute(talker, nodeConfiguration);
    // The RosTextView is also a NodeMain that must be executed in order to
    // start displaying incoming messages.
    nodeMainExecutor.execute(rosTextView, nodeConfiguration);
    */
  }
}
