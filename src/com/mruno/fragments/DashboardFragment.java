package com.mruno.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.mruno.HomeActivity;
import com.mruno.R;
import com.mruno.loaders.StatusLoader;
import com.mruno.model.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<ControllerStatus> {
    private static final String TAG = "DASHBOARD_FRAGMENT";
    private ArrayList<Outlet> mOutletStatus = new ArrayList<Outlet>();

    private ConnectionData data;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        data = (ConnectionData) getArguments().getSerializable(HomeActivity.CONNECTION_DATA);

        setHasOptionsMenu(true);

        if( null != savedInstanceState && savedInstanceState.containsKey("status") ){
            mOutletStatus = (ArrayList<Outlet>) savedInstanceState.getSerializable("status");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("status", mOutletStatus);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.dash_action_bar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                loadData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if( null == mOutletStatus || mOutletStatus.isEmpty() ){
            loadData();
        }

        // Set up on click listener
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Outlet outlet = (Outlet) getListAdapter().getItem(position);

                OutletState state = new OutletState();
                state.name = outlet.name;

                OutletToggleDialog dialog = new OutletToggleDialog(DashboardFragment.this, data, state);
                dialog.show(getFragmentManager(), "dialog");
            }
        });
    }

    @SuppressWarnings({"unchecked"})
    public void loadData() {
        if( null != getListAdapter() ){
            mOutletStatus.clear();
            ((DashboardAdapter) getListAdapter()).notifyDataSetChanged();
        }

        getSherlockActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<ControllerStatus> onCreateLoader(int i, Bundle bundle) {
        return new StatusLoader(getSherlockActivity(), data);
    }

    @Override
    public void onLoadFinished(Loader<ControllerStatus> controllerStatusLoader, ControllerStatus controllerStatus) {
        if( null != controllerStatus){
            mOutletStatus.clear();
            mOutletStatus.addAll(controllerStatus.outlets);
            setListAdapter(new DashboardAdapter(getSherlockActivity(), R.layout.dashboard_element, mOutletStatus));
        } else {
            // Wait half a second, and try again
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSherlockActivity().getSupportLoaderManager().initLoader(0, null, DashboardFragment.this);
                }
            }, 500);
        }
    }

    @Override
    public void onLoaderReset(Loader<ControllerStatus> controllerStatusLoader) {}

    private class DashboardAdapter extends ArrayAdapter<Outlet> {

        public DashboardAdapter(Context context, int resource, List<Outlet> objects) {
            super(context, resource, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if( null == convertView ){
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.dashboard_element, parent, false);
            }

            TextView name = (TextView) convertView.findViewById(R.id.dashboard_tag);
            TextView status = (TextView) convertView.findViewById(R.id.dashboard_status);

            name.setText(getItem(position).name);

            String statusText = "";
            if( getItem(position).state.contains("A") ){
                statusText = "Automatic";
                // Add in on or off
                if( getItem(position).state.contains("ON") ){
                    statusText = statusText + " on";
                } else {
                    statusText = statusText + " off";
                }

                status.setTextColor(getResources().getColor(R.color.light_green));
            } else if(getItem(position).state.equals("ON")){
                statusText = "Manual On";
                status.setTextColor(getResources().getColor(R.color.light_blue));
            } else if(getItem(position).state.equals("OFF")){
                statusText = "Manual Off";
                status.setTextColor(getResources().getColor(R.color.light_red));
            }

            status.setText(statusText);

            return convertView;
        }
    }

}
