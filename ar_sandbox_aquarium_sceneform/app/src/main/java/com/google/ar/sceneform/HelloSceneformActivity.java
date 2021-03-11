/*
 * Copyright 2018 Google LLC. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.ar.sceneform.samples.hellosceneform;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.CameraMainActivity;
import com.google.ar.sceneform.assets.RenderableSource;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class HelloSceneformActivity extends AppCompatActivity {
  private static final String TAG = HelloSceneformActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;

  //Link for models
  private static final String MODELLINK = "https://raw.githubusercontent.com/jayleenli/AR-Sandbox-Aquarium-gltf-obj-dump/main/";
  private static final String MODELFILES[] = { "fish1.gltf" };

  private ArFragment arFragment;
  private ArrayList<ModelRenderable> modelRenderables = new ArrayList<>();

  @RequiresApi(api = VERSION_CODES.O)
  @Override
  @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
  // CompletableFuture requires api level 24
  // FutureReturnValueIgnored is not valid
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_ux);
    arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);

    if (!checkIsSupportedDeviceOrFinish(this)) {
      return;
    }

    //Open and Close menu
    Button closeOpenButton = findViewById(R.id.close_open_button);
    LinearLayout objectMenu = findViewById(R.id.object_menu);
    closeOpenButton.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (closeOpenButton.getText() == "▲") {
                  closeOpenButton.setText("▼");
                  objectMenu.setVisibility(View.VISIBLE);
                } else {
                  closeOpenButton.setText("▲");
                  objectMenu.setVisibility(View.GONE);
                }
              }
            });

    //Open the Camera for object creation
    Button createButton = findViewById(R.id.create_button);
    createButton.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                openCameraActivity();
              }
            });
    
    // When you build a Renderable, Sceneform loads its resources in the background while returning
    // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().

    for (int i = 0; i < MODELFILES.length; i++ ) {

      ModelRenderable.builder()
              .setSource(this, RenderableSource.builder()
                      .setSource(this, Uri.parse(MODELLINK + MODELFILES[i]), RenderableSource.SourceType.GLTF2)
                      .setScale(1.0f)
                      .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                      .build()).setRegistryId(MODELFILES[i])
              .build()
              .thenAccept(renderable -> {
                //newRenderable = renderable;
                modelRenderables.add(renderable);
              }
              )
              .exceptionally(
                      throwable -> {
                        Toast toast =
                                Toast.makeText(this, "Unable to load renderable", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return null;
                      });

    }

    arFragment.setOnTapArPlaneListener(
        (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
          if (modelRenderables.get(0) == null) {
            Log.i("null","unull");
            return;
          }

          Log.i("item", "placed item");
          // Create the Anchor.
          Anchor anchor = hitResult.createAnchor();
          AnchorNode anchorNode = new AnchorNode(anchor);
          anchorNode.setParent(arFragment.getArSceneView().getScene());

          // Create the transformable andy and add it to the anchor.
          TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
          andy.setParent(anchorNode);
          andy.setRenderable(modelRenderables.get(0));
          andy.select();
        });
  }

  //Open Camera Activity
  public void openCameraActivity() {
    Intent intent = new Intent(this, CameraMainActivity.class);
    startActivity(intent);
  }

  /**
   * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
   * on this device.
   *
   * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
   *
   * <p>Finishes the activity if Sceneform can not run
   */
  public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
    if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
      Log.e(TAG, "Sceneform requires Android N or later");
      Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
      activity.finish();
      return false;
    }
    String openGlVersionString =
        ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
            .getDeviceConfigurationInfo()
            .getGlEsVersion();
    if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
      Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
      Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
          .show();
      activity.finish();
      return false;
    }
    return true;
  }
}
