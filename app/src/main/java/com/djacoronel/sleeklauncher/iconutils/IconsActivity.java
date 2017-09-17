package com.djacoronel.sleeklauncher.iconutils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.djacoronel.sleeklauncher.R;

import java.util.HashMap;
import java.util.List;

public class IconsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icons);

        this.setTitle(getIntent().getStringExtra("iconpack"));


        loadIconsGrid();
    }

    String getIconPackPackageName(String iconPack){
        IconPackManager icManager = new IconPackManager(this);
        HashMap<String, String> iconPacks = icManager.getAvailableIconPacks();
        return iconPacks.get(iconPack);
    }

    public void loadIconsGrid() {
        IconPackManager icManager = new IconPackManager(this);
        String icPackageName = getIconPackPackageName(getIntent().getStringExtra("iconpack"));
        List<String> icons = icManager.getAllIcons(icPackageName);

        RecyclerView grid = (RecyclerView) findViewById(R.id.icon_grid);
        IconPickerAdapter adapter = new IconPickerAdapter(icons, icPackageName, this);
        grid.setLayoutManager(new GridLayoutManager(this, 4));
        grid.setAdapter(adapter);
    }

    public void setIconAsResult(String customIcon) {
        String packageName = getIconPackPackageName(getIntent().getStringExtra("iconpack"));
        Intent returnIntent = new Intent();
        returnIntent.putExtra("customicon", customIcon + "/" + packageName);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
