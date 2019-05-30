package com.palarz.mike.rxsticky.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.palarz.mike.rxsticky.R;

import io.reactivex.subjects.BehaviorSubject;

public class StickyFragment extends Fragment {

    private Button mStickyButton;
    private BroadcastReceiver mReceiver;
    private IntentFilter mFilter;

    private BehaviorSubject<String> mSubject;

    public static final String TAG = StickyFragment.class.getSimpleName();

    public static StickyFragment newInstance() {
        StickyFragment fragment = new StickyFragment();
        return fragment;
    }

    public StickyFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSubject = BehaviorSubject.create();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mSubject.onNext("Intent action: " + intent.getAction());
                if (intent.getAction().equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                    mSubject.onNext("Airplane mode on: " + intent.getBooleanExtra("state", false));
                }
            }
        };

        mFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        mFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        getActivity().registerReceiver(mReceiver, mFilter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sticky, container, false);
        mStickyButton = rootView.findViewById(R.id.sticky_button);
        mStickyButton.setOnClickListener( (View view) -> mSubject.subscribe(string -> Log.i(TAG, string)));

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unregisterReceiver(mReceiver);
    }
}
