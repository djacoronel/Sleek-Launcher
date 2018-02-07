package com.djacoronel.sleeklauncher.settings;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.djacoronel.sleeklauncher.R;

public class BackgroundSettingsActivity extends Activity {
    ImageView preview;
    int argb[] = {0, 0, 0, 0};
    int blur = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_settings);

        setupTabs();
        setupApplyButton();

        SeekBar alphaBar = (SeekBar) findViewById(R.id.transparencyBar);
        SeekBar redBar = (SeekBar) findViewById(R.id.redBar);
        SeekBar greenBar = (SeekBar) findViewById(R.id.greenBar);
        SeekBar blueBar = (SeekBar) findViewById(R.id.blueBar);
        SeekBar blurBar = (SeekBar) findViewById(R.id.blurBar);

        setupBackgroundPreview();
        setupArgbSeekBar(alphaBar, 0);
        setupArgbSeekBar(redBar, 1);
        setupArgbSeekBar(greenBar, 2);
        setupArgbSeekBar(blueBar, 3);
        setupBlurSeekBar(blurBar);
    }

    public void setupTabs() {
        TabHost host = (TabHost) findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("Tab One");
        spec.setContent(R.id.tab1);
        spec.setIndicator("Color");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("Tab Two");
        spec.setContent(R.id.tab2);
        spec.setIndicator("Blur");
        host.addTab(spec);
    }

    void setupApplyButton(){
        TextView apply = (TextView) findViewById(R.id.apply);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePref();
                finish();
            }
        });
    }

    public void setupBackgroundPreview() {
        preview = (ImageView) findViewById(R.id.preview);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Drawable wallpaperDrawable = WallpaperManager.getInstance(this).getDrawable();
        Bitmap wallpaperBitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
        preview.setImageDrawable(new BitmapDrawable(getResources(), wallpaperBitmap));
    }

    public void setupArgbSeekBar(SeekBar seekBar, final int argbIndex) {
        seekBar.setMax(255);
        seekBar.setProgress(1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                argb[argbIndex] = progress;
                preview.setColorFilter(Color.argb(argb[0], argb[1], argb[2], argb[3]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void setupBlurSeekBar(SeekBar blurBar) {
        blurBar.setMax(75);
        blurBar.setProgress(1);
        blurBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                applyBlur(progress);
                blur = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void applyBlur(int progress) {
        Drawable wallpaperDrawable = WallpaperManager.getInstance(this).getDrawable();
        Bitmap blurredBitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();

        int numberOfFullBlur = progress / 25;
        int remainingBlur = progress % 25;

        for (int i = 0; i < numberOfFullBlur; i++) {
            blurredBitmap = BlurBuilder.blur(this, blurredBitmap, 25);
        }

        if (remainingBlur != 0) {
            blurredBitmap = BlurBuilder.blur(this, blurredBitmap, remainingBlur);
        }

        preview.setImageDrawable(new BitmapDrawable(getResources(), blurredBitmap));
    }

    public void savePref() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();

        String bgPref = argb[0] + "," + argb[1] + "," + argb[2] + "," + argb[3] + "," + blur;

        editor.putString("bgPref", bgPref);
        editor.apply();
    }
}