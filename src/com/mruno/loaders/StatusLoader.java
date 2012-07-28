package com.mruno.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import com.mruno.model.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.basic.DateConverter;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class StatusLoader extends AsyncTaskLoader<ControllerStatus> {
    private String TAG = "STATUS_LOADER";
    private ConnectionData data;

    public StatusLoader(Context context, ConnectionData data) {
        super(context);
        this.data = data;
    }

    @Override
    public ControllerStatus loadInBackground() {
        ControllerStatus controllerStatus = null;

        URL url = null;
        try {
            url = new URL("http", data.host, new Integer(data.port), "/cgi-bin/status.xml");
        } catch (MalformedURLException e) {
            Log.wtf(TAG, "Controller URL was bad", e);
        }

        // If the url was bad, bail out
        if( null == url ){
            return null;
        }

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            Log.e(TAG, "Failed to open connection to controller.", e);
        }

        // If we could not open the connection, bail out
        if( null == urlConnection ){
            return null;
        }

        Log.d(TAG, "About to connect to " + urlConnection.getURL().toString());

        try {
            XStream xstream = new XStream();

            // Add a date converter
            String[] dateFormats = new String[] { "MM/dd/yyyy HH:mm:ss" };
            xstream.registerConverter(new DateConverter("MM/dd/yyyy HH:mm:ss", dateFormats));

            // Set up our aliases
            xstream.alias("status", ControllerStatus.class);
            xstream.alias("probe", Probe.class);
            xstream.alias("power", Power.class);
            xstream.alias("outlet", Outlet.class);

            controllerStatus = (ControllerStatus) xstream.fromXML(urlConnection.getInputStream());
        } catch (ConnectException e){
            Log.w(TAG, "Controller threw connection exception, trying again", e);
        } catch (IOException e) {
            Log.e(TAG, "Could not connect to controller", e);
        } finally {
            urlConnection.disconnect();
        }

        return controllerStatus;
    }
}