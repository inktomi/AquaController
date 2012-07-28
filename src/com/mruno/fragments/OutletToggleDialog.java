package com.mruno.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.actionbarsherlock.app.SherlockDialogFragment;
import com.mruno.R;
import com.mruno.model.ConnectionData;
import com.mruno.model.OutletState;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class OutletToggleDialog extends SherlockDialogFragment {
    private static final String TAG = "OUTLET_TOGGLE_DIALOG";
    private ConnectionData mConnectionData;
    private OutletState mOutletState;
    private DashboardFragment mHost;

    public OutletToggleDialog(DashboardFragment host, ConnectionData connectionData, OutletState outletState) {
        this.mHost = host;
        this.mConnectionData = connectionData;
        this.mOutletState = outletState;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(mOutletState.name)
                .setMessage("Change the status of " + mOutletState.name)
                .setNegativeButton(R.string.dashboard_status_off, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mOutletState.value = 1;
                        new StatusUpdater().execute(mOutletState);
                    }
                })
                .setNeutralButton(R.string.dashboard_status_auto, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mOutletState.value = 0;
                        new StatusUpdater().execute(mOutletState);
                    }
                })
                .setPositiveButton(R.string.dashboard_status_on, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mOutletState.value = 2;
                        new StatusUpdater().execute(mOutletState);
                    }
                })
                .create();
    }

    private class StatusUpdater extends AsyncTask<OutletState, Void, Boolean> {
        @Override
        protected Boolean doInBackground(OutletState... outletStates) {
            OutletState state = outletStates[0];

            URL url = null;
            try {
                url = new URL("http", mConnectionData.host, new Integer(mConnectionData.port), "/cgi-bin/status.cgi");
            } catch (MalformedURLException e) {
                Log.wtf(TAG, "Controller URL was bad", e);
            }

            // If the url was bad, bail out
            if( null == url ){
                return false;
            }

            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                Log.e(TAG, "Failed to open connection to controller.", e);
            }

            // If the connection didn't happen, bail
            if( null == connection ){
                return false;
            }

            // Set up the body
            String charset = "utf-8";
            String query = null;
            try {
                query = String.format("%s=%d&Update=Update",
                        URLEncoder.encode(state.getPostName(), charset),
                        state.value);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to encode new values for outlet", e);
            }

            // If query didn't happen, bail
            if( null == query ){
                return null;
            }

            // Set the options for a POST
            connection.setDoOutput(true); // Triggers POST.
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            OutputStream output = null;
            try {
                output = connection.getOutputStream();
                output.write(query.getBytes(charset));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to encode new values for outlet", e);
            } catch (IOException e) {
                Log.e(TAG, "Failed to open connection to controller.", e);
            } finally {
                if (output != null) try { output.close(); } catch (IOException ignore) {}
            }

            // check response?
            try {
                if( !connection.getResponseMessage().equals("Ok") ){
                    return false;
                }
            } catch (IOException e) {
                Log.e(TAG, "Failed to get response message", e);
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if( !success ){
                // Try again after a bit.
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.w(TAG, "Running the update again because it did not complete!");
                        new StatusUpdater().execute(mOutletState);
                    }
                }, 1000);
            } else {
                mHost.loadData();
            }
        }
    }
}
