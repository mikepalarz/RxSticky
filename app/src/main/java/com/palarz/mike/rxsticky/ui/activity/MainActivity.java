package com.palarz.mike.rxsticky.ui.activity;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.palarz.mike.rxsticky.R;
import com.palarz.mike.rxsticky.ui.fragment.StickyFragment;

public class MainActivity extends FragmentActivity {

    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            mFragmentManager = getSupportFragmentManager();
            StickyFragment fragment = StickyFragment.newInstance();
            mFragmentManager.beginTransaction()
                            .add(R.id.fragment_container, fragment)
                            .commit();
        }
    }
}
