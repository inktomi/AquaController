package com.mruno.fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.mruno.HomeActivity;
import com.mruno.R;
import com.mruno.loaders.HistoryLoader;
import com.mruno.model.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;
import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class HistoryFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Datalog> {
    private static final String TAG = "DASHBOARD_FRAGMENT";

    private ListView mGraphList;
    private ConnectionData mConnectionData;

    private HashMap<String, List<ProbeRecord>> mHistoryData;

    private float mDefaultTextSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConnectionData = (ConnectionData) getArguments().getSerializable(HomeActivity.CONNECTION_DATA);

        if( null != savedInstanceState && savedInstanceState.containsKey("history") ){
            mHistoryData = (HashMap<String, List<ProbeRecord>>) savedInstanceState.getSerializable("history");
        } else {
            mHistoryData = new HashMap<String, List<ProbeRecord>>(3);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("history", mHistoryData);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void onResume() {
        super.onResume();

        // Create a button, and get the default text size off it
        mDefaultTextSize = new Button(getSherlockActivity()).getTextSize();

        if( null == mHistoryData || mHistoryData.isEmpty() ){
            getSherlockActivity().getSupportLoaderManager().initLoader(0, null, this);
        }

        // Set up the onclick listener
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Get the element from the adapter, if set
                if( null == getListAdapter() ){
                    return;
                }

                String probeName = (String) getListAdapter().getItem(position);

                // Get the history values for that probe
                List<ProbeRecord> records = mHistoryData.get(probeName);

                // If we have records, setup the graph
                if( null != records && records.size() > 0 ){
                    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                    XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();

                    TimeSeries series = null;
                    for( ProbeRecord record : records ){
                        if( null == series ){
                            series = new TimeSeries(record.name);
                        }

                        series.add(record.date, record.value);
                    }

                    dataset.addSeries(series);

                    // Configure the rendering.
                    renderer.setAxisTitleTextSize(mDefaultTextSize);
                    renderer.setChartTitleTextSize(mDefaultTextSize);
                    renderer.setLabelsTextSize(mDefaultTextSize);
                    renderer.setLegendTextSize(mDefaultTextSize);
                    renderer.setPointSize(5f);
                    renderer.setMargins(new int[]{
                            (int) mDefaultTextSize,
                            (int) mDefaultTextSize,
                            (int) mDefaultTextSize*2,
                            (int) mDefaultTextSize});

                    XYSeriesRenderer seriesRenderer = new XYSeriesRenderer();
                    seriesRenderer.setColor(getResources().getColor(R.color.light_green));
                    seriesRenderer.setPointStyle(PointStyle.POINT);
                    seriesRenderer.setDisplayChartValues(false);

                    renderer.addSeriesRenderer(seriesRenderer);

                    renderer.setAxesColor(Color.DKGRAY);
                    renderer.setLabelsColor(Color.LTGRAY);

                    startActivity(ChartFactory.getTimeChartIntent(getSherlockActivity(), dataset, renderer, "MM/dd HH"));
                } else {
                    // TODO: Warn the user we didn't have any data to show?
                }
            }
        });
    }

    private void setupGraphList() {
        // Get all our keys to a list
        List<String> probes = new ArrayList<String>(mHistoryData.keySet().size());
        for( String probe : mHistoryData.keySet() ){
            probes.add(probe);
        }

        // Make it easier to read.
        Collections.sort(probes);

        setListAdapter(new ArrayAdapter<String>(getSherlockActivity(), R.layout.history_element, R.id.history_row_tag, probes));
    }

    @Override
    public Loader<Datalog> onCreateLoader(int i, Bundle bundle) {
        return new HistoryLoader(getSherlockActivity(), mConnectionData);
    }

    @Override
    public void onLoadFinished(Loader<Datalog> datalogLoader, Datalog datalog) {
        if( null != datalog){
            Log.v(TAG, "Got a log with records in it! Now to parse out..");

            for( Record r : datalog.records ){
                for( Probe p : r.probes ){
                    // Is this probe already in the map?
                    if( mHistoryData.containsKey(p.name) ){
                        // Add it to the list
                        mHistoryData.get(p.name).add(new ProbeRecord(r.date, p.name, p.value));
                    } else {
                        // Create the list, register it, add the probe record
                        mHistoryData.put(p.name, new ArrayList<ProbeRecord>(datalog.records.size()));
                        mHistoryData.get(p.name).add(new ProbeRecord(r.date, p.name, p.value));
                    }
                }
            }

            setupGraphList();
        } else {
            // Wait half a second, and try again
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSherlockActivity().getSupportLoaderManager().initLoader(0, null, HistoryFragment.this);
                }
            }, 500);
        }
    }

    @Override
    public void onLoaderReset(Loader<Datalog> datalogLoader) {}
}
