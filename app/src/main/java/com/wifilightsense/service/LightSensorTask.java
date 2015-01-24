/**
 *
 */
package com.wifilightsense.service;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.wifilightsense.db.FileHelper;
import com.wifilightsense.pojos.LightReadings;
import com.wifilightsense.util.CommonUtil;
import com.wifilightsense.util.LightManager;

import java.util.List;

/**
 * @author FAISAL
 */
class LightSensorTask extends AsyncTask<String, Void, String> {


    private final Context context;

    public LightSensorTask(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    protected String doInBackground(String... params) {
        Log.i(CommonUtil.TAG, "doInBackground called .....");
        saveReadingsInDB(new LightManager(context).getLightReadings());
        PeriodicBroadcastReceiver.releaseLock();
        return "Executed";
    }

    private void saveReadingsInDB(@NonNull List<LightReadings> readings) {
        Log.i(CommonUtil.TAG, "saveReadingsInDB called .....");
        FileHelper.getFileHelperObj().saveReadingsToFile(readings);
        //getDBInstance().insertIntoLightTable(readings);

    }

}
