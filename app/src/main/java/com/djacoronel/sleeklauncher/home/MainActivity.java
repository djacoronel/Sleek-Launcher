package com.djacoronel.sleeklauncher.home;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.djacoronel.sleeklauncher.R;
import com.djacoronel.sleeklauncher.data.model.AppDetail;
import com.djacoronel.sleeklauncher.data.model.IconPrefs;
import com.djacoronel.sleeklauncher.data.room.IconPrefsDao;
import com.djacoronel.sleeklauncher.iconutils.IconPackManager;
import com.djacoronel.sleeklauncher.iconutils.IconsActivity;
import com.djacoronel.sleeklauncher.settings.SettingsActivity;
import com.djacoronel.sleeklauncher.settings.BlurBuilder;

import dagger.android.AndroidInjection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;

public class MainActivity extends Activity {

  private List<AppDetail> apps;
  GridAdapter adapter;

  @Inject
  IconPrefsDao iconPrefsDao;
  @Inject
  SharedPreferences preferences;

  int selectedAppPosition;
  ImageView image;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AndroidInjection.inject(this);
    setContentView(R.layout.activity_main);

    loadBackground();
    setupBackgroundRefreshing();
    loadApps();
    loadAppGrid();
    setupGridRefreshing();

    // This makes status bar and navigation bar transparent
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
  }

  void loadBackground() {
    String bgPref = preferences.getString("bgPref", "0,0,0,0,0");
    String argbBlur[] = bgPref.split(",");
    int argb[] = {Integer.parseInt(argbBlur[0]),
        Integer.parseInt(argbBlur[1]),
        Integer.parseInt(argbBlur[2]),
        Integer.parseInt(argbBlur[3])};
    int blur = Integer.parseInt(argbBlur[4]);

    if (blur == 0) {
      loadBackgroundWithColorFilter(argb);
    } else {
      loadBackgroundWithBlur(argb, blur);
    }
  }

  void loadBackgroundWithColorFilter(int argb[]) {
    ImageView mainBg = findViewById(R.id.mainBackground);
    mainBg.setVisibility(View.GONE);
    getWindow().getDecorView().setBackgroundColor(Color.argb(argb[0], argb[1], argb[2], argb[3]));
  }

  void loadBackgroundWithBlur(int argb[], int blur) {
    ImageView mainBg = findViewById(R.id.mainBackground);
    Drawable wallpaperDrawable = WallpaperManager.getInstance(this).getDrawable();
    Bitmap blurredBitmap = cropWallpaper(((BitmapDrawable) wallpaperDrawable).getBitmap());

    int numberOfFullBlur = blur / 25;
    int remainingBlur = blur % 25;

    for (int i = 0; i < numberOfFullBlur; i++) {
      blurredBitmap = new BlurBuilder().blur(this, blurredBitmap, 25);
    }

    if (remainingBlur != 0) {
      blurredBitmap = new BlurBuilder().blur(this, blurredBitmap, remainingBlur);
    }

    mainBg.setVisibility(View.VISIBLE);
    mainBg.setColorFilter(Color.argb(argb[0], argb[1], argb[2], argb[3]));
    mainBg.setImageDrawable(new BitmapDrawable(getResources(), blurredBitmap));
    mainBg.setScaleType(ImageView.ScaleType.FIT_CENTER);
  }

  Bitmap cropWallpaper(Bitmap wallpaper) {
    DisplayMetrics displaymetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
    int height = displaymetrics.heightPixels;
    int width = displaymetrics.widthPixels;

    return Bitmap.createBitmap(wallpaper, 0, 0, width, height);
  }

  void setupBackgroundRefreshing() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
    BroadcastReceiver br = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        loadBackground();
      }
    };
    this.registerReceiver(br, intentFilter);
  }

  void loadApps() {
    PackageManager manager = this.getPackageManager();

    apps = new ArrayList<>();

    boolean showHidden = preferences.getBoolean("showHidden", false);

    List<ResolveInfo> availableActivities = getInstalledAppsInfo();

    for (ResolveInfo ri : availableActivities) {
      String name = ri.activityInfo.packageName;
      String label = (String) ri.loadLabel(manager);
      Drawable icon = ri.activityInfo.loadIcon(manager);

      IconPrefs iconPrefs = iconPrefsDao.getIconPrefs(name);

      icon = getAppIcon(name);

      if (iconPrefs != null) {
        if (!iconPrefs.getLabel().equals(IconPrefs.NO_CUSTOM_LABEL)) {
          label = iconPrefs.getLabel();
        }
        if (!iconPrefs.isHidden() || showHidden) {
          AppDetail app = new AppDetail(label, name, icon);
          apps.add(app);
        }
      } else {
        AppDetail app = new AppDetail(label, name, icon);
        apps.add(app);
      }
    }

    Collections.sort(apps, new Comparator<AppDetail>() {
      @Override
      public int compare(AppDetail a1, AppDetail a2) {
        return a1.getLabel().compareToIgnoreCase(a2.getLabel());
      }
    });
  }

  List<ResolveInfo> getInstalledAppsInfo() {
    Intent intent = new Intent(Intent.ACTION_MAIN, null);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    return getPackageManager().queryIntentActivities(intent, 0);
  }

  Drawable getAppIcon(String appName) {
    IconPackManager icManager = new IconPackManager(this);

    String selectedIconPack = preferences.getString("iconPack", "");
    HashMap<String, String> icPackComponents = icManager.load(selectedIconPack);

    IconPrefs iconPrefs = iconPrefsDao.getIconPrefs(appName);

    // get custom icon
    if (iconPrefs != null && !iconPrefs.getIconName().equals(IconPrefs.NO_CUSTOM_ICON)) {
      String iconInfo = iconPrefs.getIconName();
      String split[] = iconInfo.split("/");
      String iconDrawable = split[0];
      String iconPackage = split[1];
      return icManager.loadDrawable(iconDrawable, iconPackage);
    }
    // get themed icon if available
    else if (!selectedIconPack.equals("") && icPackComponents.get(appName) != null
        && icManager.loadDrawable(icPackComponents.get(appName), selectedIconPack)
        != null) {
      return icManager.loadDrawable(icPackComponents.get(appName), selectedIconPack);
    } else {
      try {
        return getPackageManager().getApplicationIcon(appName);
      } catch (PackageManager.NameNotFoundException e) {
        e.printStackTrace();
      }
      return null;
    }
  }

  public void loadAppGrid() {
    RecyclerView grid = findViewById(R.id.app_grid);
    adapter = new GridAdapter(apps, this);
    grid.setLayoutManager(new GridLayoutManager(this, 4));
    grid.setAdapter(adapter);
  }

  void setupGridRefreshing() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
    intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
    intentFilter.addDataScheme("package");
    BroadcastReceiver br = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        loadApps();
        loadAppGrid();
      }
    };
    this.registerReceiver(br, intentFilter);
  }

  public void launchApp(int position, View v) {
    PackageManager manager = getPackageManager();
    Intent intent = manager.getLaunchIntentForPackage(apps.get(position).getName());
    Bundle optsBundle = getMarshmallowOpeningAnimationOpts(v).toBundle();

    if (intent != null) {
      startActivity(intent, optsBundle);
    } else {
      Toast.makeText(MainActivity.this, "App cannot be found", Toast.LENGTH_SHORT).show();
      apps.remove(position);
      loadAppGrid();
    }
  }

  ActivityOptions getMarshmallowOpeningAnimationOpts(View v) {
    if (Build.VERSION.SDK_INT >= 23) {
      int left = 0, top = 0;
      int width = v.getMeasuredWidth(), height = v.getMeasuredHeight();
      return ActivityOptions.makeClipRevealAnimation(v, left, top, width, height);
    } else {
      // Below L, we use a scale up animation
      return ActivityOptions
          .makeScaleUpAnimation(v, 0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
    }
  }


  public void openSettings() {
    Intent I = new Intent(this, SettingsActivity.class);
    startActivityForResult(I, 1);
  }

  public void iconLongClick(final int position) {
    selectedAppPosition = position;
    AlertDialog.Builder longClickDialog = buildLongClickDialog();
    longClickDialog = setButtonNameAndActions(longClickDialog);
    longClickDialog = setDialogIcon(longClickDialog);
    longClickDialog.create().show();
  }

  AlertDialog.Builder buildLongClickDialog() {
    return new AlertDialog.Builder(this)
        .setTitle("Options")
        .setMessage("Touch the icon to apply theme.")
        .setCancelable(true)
        .setNegativeButton("Uninstall", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Uri packageUri = Uri.parse("package:" + apps.get(selectedAppPosition).getName());
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
            startActivity(uninstallIntent);
          }
        });
  }

  AlertDialog.Builder setButtonNameAndActions(AlertDialog.Builder dBuilder) {
    final boolean showHidden = preferences.getBoolean("showHidden", false);
    final String appName = apps.get(selectedAppPosition).getName();
    IconPrefs iconPrefs = iconPrefsDao.getIconPrefs(appName);

    if (iconPrefs == null) {
      iconPrefs = new IconPrefs(appName);
      iconPrefsDao.addIconPrefs(iconPrefs);
    }

    if (iconPrefs.isHidden()) {
      dBuilder.setPositiveButton("Unhide", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          IconPrefs iconPrefs = iconPrefsDao.getIconPrefs(appName);

          if (iconPrefs != null) {
            iconPrefs.setHidden(false);
            iconPrefsDao.updateIconPrefs(iconPrefs);

            Toast.makeText(MainActivity.this, "App removed from hidden!",
                Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(MainActivity.this, "App icon preference not found.",
                Toast.LENGTH_LONG).show();
          }
        }
      });
    } else {
      dBuilder.setPositiveButton("Hide", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          IconPrefs iconPrefs = iconPrefsDao.getIconPrefs(appName);

          if (iconPrefs != null) {
            iconPrefs.setHidden(true);
            iconPrefsDao.updateIconPrefs(iconPrefs);
          }

          if (!showHidden) {
            apps.remove(selectedAppPosition);
            adapter.notifyItemRemoved(selectedAppPosition);
          } else {
            Toast
                .makeText(MainActivity.this, "App marked as hidden, unshow marked apps in settings",
                    Toast.LENGTH_LONG).show();
          }
        }
      });
    }
    return dBuilder;
  }

  AlertDialog.Builder setDialogIcon(AlertDialog.Builder dBuilder) {
    image = new ImageView(this);
    Bitmap bitmap = ((BitmapDrawable) apps.get(selectedAppPosition).getIcon()).getBitmap();
    Drawable icon = new BitmapDrawable(getResources(),
        Bitmap.createScaledBitmap(bitmap, 200, 200, true));
    image.setImageDrawable(icon);
    image.setOnClickListener(
        new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            showIconPackList();
          }
        }
    );
    dBuilder.setView(image);
    return dBuilder;
  }

  public void showIconPackList() {
    IconPackManager icManager = new IconPackManager(this);
    final HashMap<String, String> iconPacks = icManager.getAvailableIconPacks();
    final String[] iconPackNames = iconPacks.keySet().toArray(new String[0]);

    AlertDialog icPackListDialog = buildIconPackList(iconPackNames);
    icPackListDialog.show();
  }

  AlertDialog buildIconPackList(final String[] iconPackNames) {
    return new AlertDialog.Builder(this)
        .setTitle("Icon Packs")
        .setItems(iconPackNames, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            if (iconPackNames[which].equals("Default")) {
              setDefaultIcon();
            } else {
              String selectedIconPack = iconPackNames[which];
              launchIconPickerGridForSelectedPack(selectedIconPack);
            }
          }
        }).create();
  }

  void setDefaultIcon() {
    AppDetail app = apps.get(selectedAppPosition);
    IconPrefs iconPrefs = iconPrefsDao.getIconPrefs(app.getName());

    if (iconPrefs != null) {
      iconPrefsDao.deleteIconPrefs(iconPrefs);
    }

    try {
      Drawable icon = getPackageManager().getApplicationIcon(app.getName());
      changeIconInGrid(icon);
      changeIconInDialog(icon);

    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
  }

  void launchIconPickerGridForSelectedPack(String selectedIconPack) {
    Intent intent = new Intent(getBaseContext(), IconsActivity.class);
    intent.putExtra("iconpack", selectedIconPack);

    startActivityForResult(intent, 2);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 1) {
      loadApps();
      loadAppGrid();
      loadBackground();
    } else if (requestCode == 2) {
      if (resultCode == RESULT_OK) {
        String customIcon = data.getStringExtra("customicon");
        changeIcon(customIcon);
      }
    }
  }

  public void changeIcon(String customIcon) {
    String iconProp[] = customIcon.split("/");
    Drawable icon = new IconPackManager(this).loadDrawable(iconProp[0], iconProp[1]);

    changeIconInGrid(icon);
    changeIconInDialog(icon);
    changeIconInDatabase(customIcon);
  }

  void changeIconInGrid(Drawable icon) {
    apps.get(selectedAppPosition).setIcon(icon);
    adapter.notifyItemChanged(selectedAppPosition);
  }

  void changeIconInDialog(Drawable icon) {
    Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
    icon = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 200, 200, true));
    image.setImageDrawable(icon);
  }

  void changeIconInDatabase(String customIcon) {
    String appName = apps.get(selectedAppPosition).getName();
    IconPrefs iconPrefs = iconPrefsDao.getIconPrefs(appName);

    if (iconPrefs == null) {
      iconPrefs = new IconPrefs(appName);
    }

    iconPrefs.setIconName(customIcon);
    iconPrefsDao.addIconPrefs(iconPrefs);
  }

  public void changeLabel() {
    //TODO: Implement custom label on app grid
  }

  @Override
  public void onBackPressed() {
  }
}
