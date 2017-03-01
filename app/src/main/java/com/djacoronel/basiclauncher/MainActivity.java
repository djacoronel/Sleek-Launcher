package com.djacoronel.basiclauncher;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import org.malcdevelop.cyclicview.CyclicFragmentAdapter;
import org.malcdevelop.cyclicview.CyclicView;

public class MainActivity extends AppCompatActivity {

    CyclicView viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = (CyclicView) findViewById(R.id.pager);
        viewPager.setAdapter(
                new CyclicFragmentAdapter(this, getSupportFragmentManager()) {
                    @Override
                    protected Fragment createFragment(int i) {
                        if (i == 0)
                            return new TabFragment1();
                        else if (i == 1)
                            return new TabFragment2();
                        else
                            return new TabFragment3();

                    }

                    @Override
                    public int getItemsCount() {
                        return 3;
                    }
                }
        );
        viewPager.setCurrentPosition(1);


        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

    }

    @Override
    public void onBackPressed() {
        viewPager.setCurrentPosition(1);
    }
}
