/**
 *
 */
package com.wifilightsense.service;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.wifilightsense.db.FileHelper;
import com.wifilightsense.util.LightManager;

/**
 * @author FAISAL
 */
class LightSensorTask extends AsyncTask<Void, Void, Void> {


    private final Context context;

    public LightSensorTask(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    protected Void doInBackground(Void... params) {
        FileHelper.getFileHelperObj().saveReadingsToFile(new LightManager(context).generateLightReadingsList());
        PeriodicBroadcastReceiver.releaseLock();
        return null;
    }
}
