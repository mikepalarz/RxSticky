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
import android.widget.TextView;

import com.palarz.mike.rxsticky.R;

import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class StickyFragment extends Fragment {

    public static final String TAG = StickyFragment.class.getSimpleName();

    // Fragment views and UI-related variables
    private Button mSubscribeButton;
    private Button mClearButton;
    private TextView mBroadcastsTextview;
    private StringBuilder mStringBuilder;

    // Intent filters needed for broadcasts
    private final IntentFilter mBatteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    private final IntentFilter mAirplaneModeFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    private final IntentFilter mTimeFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
    private final IntentFilter mScreenOnFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
    private final IntentFilter mScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);

    // RxJava observables and Rx-related variables
    private Observable<String> mBatteryObservable;
    private Observable<String> mAirplaneModeObservable;
    private Observable<String> mTimeObservable;
    private Observable<String> mScreenOnObservable;
    private Observable<String> mScreenOffObservable;
    private Observable<String> mAllBroadcasts;
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private boolean mSubscriptionToggle;    // Toggles the subscription on/off

    public static StickyFragment newInstance() {
        StickyFragment fragment = new StickyFragment();
        return fragment;
    }

    public StickyFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBatteryObservable = broadcastObservable(mBatteryFilter);
        mAirplaneModeObservable = broadcastObservable(mAirplaneModeFilter);
        mTimeObservable = broadcastObservable(mTimeFilter);
        mScreenOnObservable = broadcastObservable(mScreenOnFilter);
        mScreenOffObservable = broadcastObservable(mScreenOffFilter);
        mAllBroadcasts = Observable
                .merge(Arrays.asList(mBatteryObservable, mAirplaneModeObservable, mTimeObservable, mScreenOnObservable, mScreenOffObservable))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread());

        mSubscriptionToggle = false;
        mStringBuilder = new StringBuilder();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sticky, container, false);
        mBroadcastsTextview = rootView.findViewById(R.id.sticky_textview);

        mSubscribeButton = rootView.findViewById(R.id.subscribe_button);
        mSubscribeButton.setOnClickListener((View view) -> {

            mSubscriptionToggle = !mSubscriptionToggle;

            if (mSubscriptionToggle) {
                mAllBroadcasts
                        .doOnDispose(() -> {
                            Log.i(TAG, "Unsubscribed from all broadcasts");
                            mBroadcastsTextview.setText(mStringBuilder);
                            // Clearing the StringBuilder
                            mStringBuilder.setLength(0);
                            // Re-enabling the clear broadcasts button
                            mClearButton.setEnabled(true);
                            mSubscribeButton.setText(R.string.fragment_sticky_subscribe_button_before_subscribe_text);
                        })
                        .subscribe(
                                // onNext()
                                string -> {
                                    Log.i(TAG, string);
                                    mStringBuilder.append(string + "\n");
                                    },
                                // onError()
                                (error) -> Log.e(TAG, "Error occurred: " + error),
                                // onComplete() should never be called since these broadcasts will always
                                // be sent. I.e., we have a never-ending stream of events.
                                () -> Log.i(TAG, "onComplete()"),
                                // onSubscribe()
                                disposable -> {
                                    Log.i(TAG, "Subscribed to all broadcasts");
                                    mBroadcastsTextview.setText(R.string.fragment_sticky_textview_logging_broadcasts);
                                    // We're listening to broadcasts, so let's not enable this button
                                    mClearButton.setEnabled(false);
                                    mSubscribeButton.setText(R.string.fragment_sticky_subscribe_button_during_subscription_text);
                                    mDisposable.add(disposable);
                                });
            } else {
                mDisposable.clear();
            }

        });

        mClearButton = rootView.findViewById(R.id.clear_broadcasts_button);
        mClearButton.setOnClickListener(view -> {
            mBroadcastsTextview.setText(null);
            mBroadcastsTextview.setHint(R.string.fragment_sticky_textview_hint);
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (!mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    private Observable<String> broadcastObservable(IntentFilter filter) {
        Observable<String> observable = Observable.create(observer -> {
            Log.i(TAG, Thread.currentThread().getName() + ": Creating observable");
            final BroadcastReceiver receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    observer.onNext("Action: " + action);

                    if (action.equals(mAirplaneModeFilter.getAction(0))) {
                        observer.onNext("Airplane mode on: " + intent.getBooleanExtra("state", false));
                    } else if (action.equals(mTimeFilter.getAction(0))) {
                        observer.onNext("And time goes on...");
                    }                 }
            };

            getActivity().registerReceiver(receiver, filter);
        });

        return observable.subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
