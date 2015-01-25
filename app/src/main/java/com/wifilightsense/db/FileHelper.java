package com.wifilightsense.db;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wifilightsense.pojos.LightReadings;
import com.wifilightsense.util.CommonUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class FileHelper {
    @NonNull
    private final File file;
    @Nullable
    private static FileHelper fileHelper = null;

    @NonNull
    public static FileHelper getFileHelperObj() {
        if (fileHelper == null)
            fileHelper = new FileHelper();
        return fileHelper;
    }

    private FileHelper() {
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File dir = new File(root.getAbsolutePath());
        if (!dir.exists() && !dir.mkdirs()) {
            Log.e(CommonUtil.TAG, "Can't make directories");
        }
        if (!dir.isDirectory()) throw new RuntimeException("root isn't a directory");

        file = new File(dir, "WifiLightSense" + CommonUtil.getDeviceId() + ".txt");
        writeReadings("Device ID \t\t\t\t\t\t\t\t\t\t\t\t LUX Reading \t Timestamp");
    }

    public void saveReadingsToFile(@NonNull List<LightReadings> lr) {
        StringBuilder row = new StringBuilder();

        for (LightReadings l : lr) {
            row.append(CommonUtil.getDeviceId());
            row.append("\t");
            row.append(l.getLux());
            row.append("\t");
            row.append(CommonUtil.dateFormat.format(l.getTimestamp()));
            row.append("\n");
            //Log.i(CommonUtil.TAG, "Record : "+row.toString() );
        }

        writeReadings(row.toString());
        //Log.i(CommonUtil.TAG,row.toString() );
        //CommonUtil.getCommonObj(context).writeReadings(row.toString());

    }

    void writeReadings(String data) {
        try {
            Log.i(CommonUtil.TAG, "Writing data to file " + file.getAbsolutePath());
            //file = new File(dir, "WifiLightSense.txt");
            FileOutputStream f = new FileOutputStream(file, true);
            PrintWriter pw = new PrintWriter(f);
            pw.println(data);
            pw.flush();
            pw.close();
            f.close();
        } catch (FileNotFoundException e) {
            Log.e(CommonUtil.TAG,
                    "******* File not found. Did you"
                            + " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//	private void readRaw(){
//		txtTesting.append("\nData read from res/raw/textfile.txt:");
//	    InputStream is = this.getResources().openRawResource(R.raw.textfile);
//	    InputStreamReader isr = new InputStreamReader(is);
//	    BufferedReader br = new BufferedReader(isr, 8192);    // 2nd arg is buffer size
//
//	    // More efficient (less readable) implementation of above is the composite expression
//	    /*BufferedReader br = new BufferedReader(new InputStreamReader(
//	            this.getResources().openRawResource(R.raw.textfile)), 8192);*/
//
//	    try {
//	        String test;    
//	        while (true){               
//	            test = br.readLine();   
//	            // readLine() returns null if no more lines in the file
//	            if(test == null) break;
//	            txtTesting.append("\n"+"    "+test);
//	        }
//	        isr.close();
//	        is.close();
//	        br.close();
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }
//	    txtTesting.append("\n\nThat is all");
//	}
}
