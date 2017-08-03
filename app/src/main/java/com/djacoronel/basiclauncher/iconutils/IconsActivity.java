package com.djacoronel.basiclauncher.iconutils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.djacoronel.basiclauncher.R;

import java.util.List;

public class IconsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icons);

        this.setTitle(getIntent().getStringExtra("iconpackname"));

        loadIconsGrid();
    }

    public void loadIconsGrid() {
        IconPackManager icManager = new IconPackManager(this);
        List<String> icons = icManager.getAllIcons(getIntent().getStringExtra("iconpack"));
        String iconPack = getIntent().getStringExtra("iconpack");

        RecyclerView grid = (RecyclerView) findViewById(R.id.icon_grid);
        IconPickerAdapter adapter = new IconPickerAdapter(icons, iconPack, this);
        grid.setLayoutManager(new GridLayoutManager(this, 4));
        grid.setAdapter(adapter);
    }

    public void pickIcon(String customIcon) {
        String packageName = getIntent().getStringExtra("iconpack");

        Intent returnIntent = new Intent();
        returnIntent.putExtra("customicon", customIcon + "/" + packageName);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
