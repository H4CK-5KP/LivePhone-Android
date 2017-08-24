package com.qualityunit.android.liveagentphone.ui.about;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qualityunit.android.liveagentphone.R;
import com.qualityunit.android.liveagentphone.util.Tools;

/**
 * Created by rasto on 21.8.17.
 */

public class AboutFragment extends Fragment {

    public static final String TAG = AboutFragment.class.getSimpleName();
    private AboutActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (AboutActivity) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        activity.setTitle(R.string.about);
        ((TextView)view.findViewById(R.id.tv_version)).setText(Tools.getVersionName());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.about_context_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_licenses:
                activity.switchFragments(new LicensesFragment(), LicensesFragment.TAG);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
