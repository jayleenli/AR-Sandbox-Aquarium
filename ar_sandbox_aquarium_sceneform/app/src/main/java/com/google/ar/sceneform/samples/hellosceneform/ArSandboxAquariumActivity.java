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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
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

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
public class ArSandboxAquariumActivity extends AppCompatActivity {
  private static final String TAG = ArSandboxAquariumActivity.class.getSimpleName();
  private static final double MIN_OPENGL_VERSION = 3.0;

  //Link for models
  private static final String MODELLINK = "https://raw.githubusercontent.com/jayleenli/AR-Sandbox-Aquarium-gltf-obj-dump/main/";
  private static final String MODELFILES[] = { "jellyfish.glb", "clownfish.glb", "fish.glb", "starfish.glb", "turtle.glb", "blue_tang.glb", "seaweed.glb"};

  private ArFragment arFragment;
  private ArrayList<ModelRenderable> modelRenderables = new ArrayList<>();
  private ArrayList<String> modelRenderablesNames = new ArrayList<>();
  private int viewStartIndex = 0;
  private int viewEndIndex = 2;
  private int squareBoxIds[] = {R.id.obj_img_0, R.id.obj_img_1, R.id.obj_img_2};
  private int stopLoadModelIndex;
  private int selectedModelIndex = 0;

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
    //Load files from github
    for (int i = 0; i < MODELFILES.length; i++ ) {
      Log.i("MODEL", "In  " + i);
        loadModel(MODELFILES[i]);
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

      Button prevPageButton = findViewById(R.id.back_button);
      prevPageButton.setOnClickListener(
              new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  //If can go more
                  if (viewEndIndex > 2) {
                    viewStartIndex -= 3;
                    viewEndIndex -= 3;
                    loadModelsIntoScreen();
                  }
                  //else do nothing
                }
              });

      Button nextPageButton = findViewById(R.id.next_button);
      nextPageButton.setOnClickListener(
              new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                  //If can go more
                  if (viewEndIndex < modelRenderables.size() - 1 ) {
                    viewStartIndex += 3;
                    viewEndIndex += 3;
                    loadModelsIntoScreen();
                  }
                  //else do nothing
                }
              });


    ColorDrawable white_cd = new ColorDrawable(Color.parseColor("#ffffff"));
    ColorDrawable gray_cd = new ColorDrawable(Color.parseColor("#dedede"));

    //Listeners for all the object stuff
    androidx.cardview.widget.CardView modelObjCard0 = findViewById(R.id.obj0);
    modelObjCard0.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                selectedModelIndex = viewStartIndex; // should always have something in it
                TextView textCard0 = (TextView) findViewById(R.id.obj_img_0);
                textCard0.setBackground(gray_cd);
                Log.i("item", "select item" + selectedModelIndex);

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                          @Override
                          public void run() {
                            textCard0.setBackground(white_cd);
                          }
                        },
                        250
                );
              }
            });


    androidx.cardview.widget.CardView modelObjCard1 = findViewById(R.id.obj1);

    modelObjCard1.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (stopLoadModelIndex > viewStartIndex) selectedModelIndex = viewStartIndex + 1;
                TextView textCard1 = (TextView) findViewById(R.id.obj_img_1);
                textCard1.setBackground(gray_cd);
                Log.i("item", "select item 2 " + selectedModelIndex);

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                          @Override
                          public void run() {
                            textCard1.setBackground(white_cd);
                          }
                        },
                        250
                );
              }
            });

    androidx.cardview.widget.CardView modelObjCard2 = findViewById(R.id.obj2);
    modelObjCard2.setOnClickListener(
            new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                if (stopLoadModelIndex > viewStartIndex+1) selectedModelIndex = stopLoadModelIndex;
                TextView textCard2 = (TextView) findViewById(R.id.obj_img_2);
                textCard2.setBackground(gray_cd);
                Log.i("item", "select item 3 " + selectedModelIndex);

                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                          @Override
                          public void run() {
                            textCard2.setBackground(white_cd);
                          }
                        },
                        250
                );
              }
            });


    arFragment.setOnTapArPlaneListener(
            (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
              if (modelRenderables.get(selectedModelIndex) == null) {
                Log.i("null", "unull");
                return;
              }

              Log.i("item", "placed item" + selectedModelIndex);
              // Create the Anchor.
              Anchor anchor = hitResult.createAnchor();
              AnchorNode anchorNode = new AnchorNode(anchor);
              anchorNode.setParent(arFragment.getArSceneView().getScene());

              // Create the transformable andy and add it to the anchor.
              TransformableNode andy = new TransformableNode(arFragment.getTransformationSystem());
              andy.setParent(anchorNode);
              andy.setRenderable(modelRenderables.get(selectedModelIndex));
              andy.select();
            });
  }

  public void loadModel(String filename) {
    CompletableFuture makeModel = ModelRenderable.builder()
            .setSource(this, RenderableSource.builder()
                    .setSource(this, Uri.parse(MODELLINK + filename), RenderableSource.SourceType.GLB)
                    .setScale(.75f)
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build()).setRegistryId(filename)
            .build()
            .thenAccept(renderable -> {
                      modelRenderables.add(renderable);

                      Log.i("MODEL", " FINISHED HERE" + filename);
                      String name = filename.substring(0, filename.indexOf("."));
                      modelRenderablesNames.add(name);
                      loadModelsIntoScreen();
                      //return renderable;
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
  //Open Camera Activity
  public void openCameraActivity() {
    Intent intent = new Intent(this, CameraMainActivity.class);
    startActivity(intent);
  }

  //Uses the global indexes, assumes they are correctly set
  public void loadModelsIntoScreen() {
    if (viewEndIndex - viewStartIndex != 2) { //2 because 1 off because start at index 0
      throw new Error("WRONG!!");
    }
    int index = 0;
    //Clear all text
    for(int i = viewStartIndex; i <= viewEndIndex; i++) {
      //Just loads the model names
      TextView obj = (TextView) this.findViewById(squareBoxIds[index]);
      obj.setText("");
      index++;
    }

    stopLoadModelIndex = (modelRenderables.size()-1 < viewEndIndex) ? modelRenderables.size() - 1 : viewEndIndex;

    Log.i("MODELS", "stoploadModelIndex" + stopLoadModelIndex);

    index = 0;
    for(int i = viewStartIndex; i <= stopLoadModelIndex; i++) {
      //Just loads the model names
      TextView obj = (TextView) this.findViewById(squareBoxIds[index]);
      obj.setText(modelRenderablesNames.get(i));
      Log.i("MODELS", "Loaded " + modelRenderablesNames.get(i) + " name into screen");
      index++;
    }
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
