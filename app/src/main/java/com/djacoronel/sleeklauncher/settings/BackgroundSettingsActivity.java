package com.djacoronel.sleeklauncher.settings;

import android.app.Activity;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TabHost;

import com.djacoronel.sleeklauncher.R;

import jp.wasabeef.blurry.Blurry;

public class BackgroundSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_settings);
        TabHost host = (TabHost)findViewById(R.id.tabHost);
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

        SeekBar transparencyBar = (SeekBar) findViewById(R.id.transparencyBar);
        SeekBar redBar = (SeekBar) findViewById(R.id.redBar);
        SeekBar greenBar = (SeekBar) findViewById(R.id.greenBar);
        SeekBar blueBar = (SeekBar) findViewById(R.id.blueBar);

        SeekBar blurBar = (SeekBar) findViewById(R.id.blurBar);
        SeekBar brightnessBar = (SeekBar) findViewById(R.id.brightnessBar);

        final ImageView preview = (ImageView) findViewById(R.id.preview);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        final int argb[] = {0,0,0,0};
        final int blurBrightness[] = {0,0};

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
        final Drawable wallpaperDrawable = wallpaperManager.getDrawable();

        setupSeekBar(transparencyBar);
        setupSeekBar(redBar);
        setupSeekBar(greenBar);
        setupSeekBar(blueBar);
        blurBar.setMax(75);
        blueBar.setProgress(1);

        transparencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                argb[0] = progress;
                preview.setColorFilter(Color.argb(argb[0], argb[1], argb[2], argb[3]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        redBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                argb[1] = progress;
                preview.setColorFilter(Color.argb(argb[0], argb[1], argb[2], argb[3]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        greenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                argb[2] = progress;
                preview.setColorFilter(Color.argb(argb[0], argb[1], argb[2], argb[3]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        blueBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                argb[3] = progress;
                preview.setColorFilter(Color.argb(argb[0], argb[1], argb[2], argb[3]));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        blurBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            Bitmap blurredBitmap;
            int progress;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    this.progress = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(progress == 0){
                    blurredBitmap = ((BitmapDrawable)wallpaperDrawable).getBitmap();
                }
                else if(progress<=25){
                    blurredBitmap = BlurBuilder.blur(
                            BackgroundSettingsActivity.this,
                            ((BitmapDrawable)wallpaperDrawable).getBitmap(),
                            progress);
                }else if (progress<=50 && progress>25){
                    blurredBitmap = BlurBuilder.blur(
                            BackgroundSettingsActivity.this,
                            ((BitmapDrawable)wallpaperDrawable).getBitmap(),
                            25);
                    blurredBitmap = BlurBuilder.blur(
                            BackgroundSettingsActivity.this,
                            blurredBitmap,
                            progress-25);
                }else{
                    blurredBitmap = BlurBuilder.blur(
                            BackgroundSettingsActivity.this,
                            ((BitmapDrawable)wallpaperDrawable).getBitmap(),
                            25);
                    blurredBitmap = BlurBuilder.blur(
                            BackgroundSettingsActivity.this,
                            blurredBitmap,
                            25);
                    blurredBitmap = BlurBuilder.blur(
                            BackgroundSettingsActivity.this,
                            blurredBitmap,
                            progress-50);
                }
                preview.setImageDrawable(new BitmapDrawable(getResources(), blurredBitmap));
            }
        });


    }

    public void setupSeekBar(SeekBar seekBar){
        seekBar.setMax(255);
        seekBar.setProgress(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "Save")
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }
}
