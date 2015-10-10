package com.example.wojciech.saper;

import com.example.wojciech.saper.util.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class BestScoresActivity extends Activity {
    private int position;
    private int bestScore;
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
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_best_scores);
        if(getIntent().getIntExtra("view",0) == 1){
            setRank();
            findViewById(R.id.best_score_edit_text).setVisibility(View.GONE);
            findViewById(R.id.ok).setVisibility(View.GONE);
        }else{
            findViewById(R.id.best_score_edit_text).setVisibility(View.VISIBLE);
            findViewById(R.id.ok).setVisibility(View.VISIBLE);
            findViewById(R.id.Rank1).setVisibility(View.GONE);
            findViewById(R.id.Rank2).setVisibility(View.GONE);
            findViewById(R.id.Rank3).setVisibility(View.GONE);
            findViewById(R.id.end).setVisibility(View.GONE);
            findViewById(R.id.start).setVisibility(View.GONE);
            position = getIntent().getIntExtra("Place",0);
            bestScore = getIntent().getIntExtra("Score",999);
        }
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.ok).setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }


    public void onMyClick(View view) {
        switch (view.getId()) {
            case R.id.start:
                Intent intent = new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.end:
                finish();
                break;
            case R.id.ok:
                enterNewRecord();
                setRank();
                findViewById(R.id.best_score_edit_text).setVisibility(View.GONE);
                findViewById(R.id.ok).setVisibility(View.GONE);
                findViewById(R.id.Rank1).setVisibility(View.VISIBLE);
                findViewById(R.id.Rank2).setVisibility(View.VISIBLE);
                findViewById(R.id.Rank3).setVisibility(View.VISIBLE);
                findViewById(R.id.end).setVisibility(View.VISIBLE);
                findViewById(R.id.start).setVisibility(View.VISIBLE);
                break;
        }
    }

    private void enterNewRecord() {
        String name = ((EditText) findViewById(R.id.best_score_edit_text)).getText().toString();
        if (name.isEmpty())
            name = "Player";
        String tempName = "";
        int previousResult;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ((TextView) findViewById(R.id.fullscreen_content)).setText(new String("Best\nScores " +position));
        switch (position) {
            case 1:
                //first step

                editor.putString("Player" + (position + 2), sharedPreferences.getString("Player" + (position + 1), "Player"));
                editor.putInt("Rank" + (position + 2), sharedPreferences.getInt("Rank" + (position + 1), 999));
                editor.commit();

                tempName = sharedPreferences.getString("Player" + position, "Player");
                previousResult = sharedPreferences.getInt("Rank" + position, 999);
                editor.putString("Player" + (position + 1), tempName);
                editor.putInt("Rank" + (position + 1), previousResult);
                editor.putString("Player" + position, name);
                editor.putInt("Rank" + position, bestScore);
                editor.commit();
                //second step

                break;
            case 2:
                tempName = sharedPreferences.getString("Player" + position, "Player");
                previousResult = sharedPreferences.getInt("Rank" + position, 999);
                editor.putString("Player" + (position + 1), tempName);
                editor.putInt("Rank" + (position + 1), previousResult);
                editor.putString("Player" + position, name);
                editor.putInt("Rank" + position, bestScore);
                editor.commit();
                break;
            case 3:
                editor.putString("Player" + position, name);
                editor.putInt("Rank" + position, bestScore);
                editor.commit();
                break;
        }
    }
    private void setRank(){
        TextView textView[] = new TextView[3];
        textView[0] = (TextView) findViewById(R.id.Rank1);
        textView[1] = (TextView) findViewById(R.id.Rank2);
        textView[2] = (TextView) findViewById(R.id.Rank3);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        for (int i = 0;i < 3;++i){
            String name = sharedPreferences.getString("Player" + (i + 1),"Player");
            int res = sharedPreferences.getInt("Rank"+(i+1), 999);
            textView[i].setVisibility(View.VISIBLE);
            textView[i].setText(new String((i+1) + ". " + name + " " + res));
        }

    }
}

