package com.google.ar.core.examples.java.arsandboxaquarium;

import android.Manifest;
import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.ar.core.examples.java.arsandboxaquarium.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class CameraActivity extends Activity {
  private static final int CAMERA_REQUEST = 1888;
  private ImageView imageView;
  private static final int MY_CAMERA_PERMISSION_CODE = 100;
  /**
   * Whether or not the system UI should be auto-hidden after
   * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
   */
  private static final boolean AUTO_HIDE = true;

  /**
   * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
   * user interaction before hiding the system UI.
   */
  private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

  /**
   * Some older devices needs a small delay between UI widget updates
   * and a change of the status and navigation bar.
   */
  private static final int UI_ANIMATION_DELAY = 300;
  private final Handler mHideHandler = new Handler();
  //private View mContentView;
  private final Runnable mHidePart2Runnable = new Runnable() {
    @SuppressLint("InlinedApi")
    @Override
    public void run() {
      // Delayed removal of status and navigation bar

      // Note that some of these constants are new as of API 16 (Jelly Bean)
      // and API 19 (KitKat). It is safe to use them, as they are inlined
      // at compile-time and do nothing on earlier devices.
//      mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
//              | View.SYSTEM_UI_FLAG_FULLSCREEN
//              | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//              | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//              | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//              | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
  };
  //private View mControlsView;
  private final Runnable mShowPart2Runnable = new Runnable() {
    @Override
    public void run() {
      // Delayed display of UI elements
//      ActionBar actionBar = getSupportActionBar();
//      if (actionBar != null) {
//        actionBar.show();
//      }
      //mControlsView.setVisibility(View.VISIBLE);
    }
  };
  private boolean mVisible;
  private final Runnable mHideRunnable = new Runnable() {
    @Override
    public void run() {
      hide();
    }
  };
  /**
   * Touch listener to use for in-layout UI controls to delay hiding the
   * system UI. This is to prevent the jarring behavior of controls going away
   * while interacting with activity UI.
   */
  private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      switch (motionEvent.getAction()) {
        case MotionEvent.ACTION_DOWN:
          if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
          }
          break;
        case MotionEvent.ACTION_UP:
          view.performClick();
          break;
        default:
          break;
      }
      return false;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {

    mVisible = true;


    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    this.imageView = (ImageView)this.findViewById(R.id.imageView1);
    ImageButton photoButton = (ImageButton) this.findViewById(R.id.take_photo);
    photoButton.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
          requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
        else
        {
          Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
          startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
      }
    });
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);

    // Trigger the initial hide() shortly after the activity has been
    // created, to briefly hint to the user that UI controls
    // are available.
    delayedHide(100);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
  {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == MY_CAMERA_PERMISSION_CODE)
    {
      if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
      {
        Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
      }
      else
      {
        Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
    {
      Bitmap photo = (Bitmap) data.getExtras().get("data");
      imageView.setImageBitmap(photo);
    }
  }

  private void hide() {
    // Hide UI first
    //mControlsView.setVisibility(View.GONE);
    //mVisible = false;

    // Schedule a runnable to remove the status and navigation bar after a delay
    mHideHandler.removeCallbacks(mShowPart2Runnable);
    mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
  }

  private void show() {
    // Show the system bar
//    mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
//    mVisible = true;

    // Schedule a runnable to display UI elements after a delay
    mHideHandler.removeCallbacks(mHidePart2Runnable);
    mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
  }

  /**
   * Schedules a call to hide() in delay milliseconds, canceling any
   * previously scheduled calls.
   */
  private void delayedHide(int delayMillis) {
    mHideHandler.removeCallbacks(mHideRunnable);
    mHideHandler.postDelayed(mHideRunnable, delayMillis);
  }
}