package sviolet.turquoise.x.imageloader.handler.common;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Record network speed of servers
 */
class NetworkSpeedRecorder {

    private static final String SHARED_PREF_NAME = "tiloader-ns";
    private static final int MAX_RECORD_NUM = 100;

    private Map<String, Record> recordMap = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    private SharedPreferences sharedPreferences;

    public NetworkSpeedRecorder(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }

        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    private void init(){
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (!initialized) {
                try {
                    Map<String, ?> dataMap = sharedPreferences.getAll();
                    if (dataMap == null) {
                        return;
                    }
                    //clean if too much records
                    if (dataMap.size() > MAX_RECORD_NUM) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        for (Map.Entry<String, ?> entry : dataMap.entrySet()) {
                            editor.remove(entry.getKey());
                        }
                        editor.apply();
                        return;
                    }
                    for (Map.Entry<String, ?> entry : dataMap.entrySet()) {
                        try {
                            double speed = Double.parseDouble(String.valueOf(entry.getValue()));
                            recordMap.put(entry.getKey(), new Record(speed));
                        } catch (Throwable ignore) {
                        }
                    }
                    initialized = true;
                } catch (Throwable ignore){
                }
            }
        }
    }

    public void record(String host, double speed){
        init();
        if (host == null){
            return;
        }
        Record record = getRecord(host, speed);
        try {
            synchronized (record) {
                record.current = record.current * 0.8d + speed * 0.2d;
                /*
                    Because the number of concurrent is less, We use a simple implementation for the time being.
                 */
                sharedPreferences.edit()
                        .putString(host, String.valueOf(speed))
                        .apply();
            }
        } catch (Throwable ignore){
            //ignore exceptions
        }
    }

    public double getSpeed(String host, double defaultValue){
        init();
        if (host == null){
            return defaultValue;
        }
        double speed = getRecord(host, defaultValue).current;
        return speed >= 1d ? speed : 1d;
    }

    private Record getRecord(String host, double speed) {
        Record record = recordMap.get(host);
        if (record == null) {
            synchronized (recordMap) {
                record = recordMap.get(host);
                if (record == null) {
                    record = new Record(speed);
                    recordMap.put(host, record);
                }
            }
        }
        return record;
    }

    private static class Record {

        private volatile double current;

        private Record(double current) {
            this.current = current;
        }

    }

}
