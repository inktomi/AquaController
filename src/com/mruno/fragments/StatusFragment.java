package com.mruno.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.mruno.HomeActivity;
import com.mruno.R;
import com.mruno.loaders.StatusLoader;
import com.mruno.model.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatusFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<ControllerStatus> {
    private static final String TAG = "STATUS_FRAGMENT";

    private DecimalFormat mFormat = new DecimalFormat("#.##");

    private ConnectionData mConnectionData;
    private ArrayList<Probe> mProbeStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConnectionData = (ConnectionData) getArguments().getSerializable(HomeActivity.CONNECTION_DATA);

        if( null != savedInstanceState && savedInstanceState.containsKey("probeStatus") ){
            mProbeStatus = (ArrayList<Probe>) savedInstanceState.getSerializable("probeStatus");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("probeStatus", mProbeStatus);
    }

    @Override
    public void onResume() {
        super.onResume();

        getSherlockActivity().getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<ControllerStatus> onCreateLoader(int i, Bundle bundle) {
        return new StatusLoader(getSherlockActivity(), mConnectionData);
    }

    @Override
    public void onLoadFinished(Loader<ControllerStatus> controllerStatusLoader, ControllerStatus controllerStatus) {
        if( null != controllerStatus){
            if( null == mProbeStatus ){
                mProbeStatus = new ArrayList<Probe>(controllerStatus.probes.size());
            } else {
                mProbeStatus.clear();
            }

            mProbeStatus.addAll(controllerStatus.probes);
            setListAdapter(new ProbeStatusAdapter(getSherlockActivity(), R.layout.history_element, mProbeStatus));
        } else {
            // Wait half a second, and try again
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSherlockActivity().getSupportLoaderManager().initLoader(0, null, StatusFragment.this);
                }
            }, 500);
        }
    }

    @Override
    public void onLoaderReset(Loader<ControllerStatus> controllerStatusLoader) {}

    private class ProbeStatusAdapter extends ArrayAdapter<Probe> {

        public ProbeStatusAdapter(Context context, int resource, List<Probe> objects) {
            super(context, resource, 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if( null == convertView ){
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = layoutInflater.inflate(R.layout.status_element, parent, false);
            }

            TextView name = (TextView) convertView.findViewById(R.id.probe_tag);
            TextView status = (TextView) convertView.findViewById(R.id.probe_status);
            name.setText(getItem(position).name);
            status.setText(mFormat.format(getItem(position).value));

            return convertView;
        }
    }
}
