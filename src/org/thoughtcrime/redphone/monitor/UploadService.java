package org.thoughtcrime.redphone.monitor;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages background uploads of collected data stored on disk.
 */
public class UploadService extends Service {
  public static final String CLIENT_ID_PREF_KEY = "pref_client_logging_id";
  public static final String DATAFILE_KEY = "datafile";
  public static final String CALLID_KEY = "call_id";
  public final ExecutorService executor = Executors.newSingleThreadExecutor();

  @Override
  public int onStartCommand(Intent intent, int flags, final int startId) {
    final String datafile = intent.getStringExtra(DATAFILE_KEY);
    final String callId = intent.getStringExtra(CALLID_KEY);
    executor.submit(new Runnable() {
      @Override
      public void run() {
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(UploadService.this);
        String clientId = defaultSharedPreferences.getString(CLIENT_ID_PREF_KEY, null);
        if (clientId == null) {
          clientId = UUID.randomUUID().toString();
          defaultSharedPreferences.edit().putString(CLIENT_ID_PREF_KEY, clientId).commit();
        }

        Uploader uploader = new Uploader(clientId, callId, datafile);
        uploader.upload();
        UploadService.this.stopSelfResult(startId);
      }
    });
    return START_NOT_STICKY;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
