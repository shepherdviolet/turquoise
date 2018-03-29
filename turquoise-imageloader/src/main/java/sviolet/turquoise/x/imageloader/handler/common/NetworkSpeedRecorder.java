package sviolet.turquoise.x.imageloader.handler.common;

import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Record network speed of servers
 */
class NetworkSpeedRecorder {

    private Map<String, Record> recordMap = new ConcurrentHashMap<>();

    public void record(String url, double speed){
        Record record = getRecord(url, speed);
        if (record == null) {
            return;
        }
        synchronized (record) {
            record.averageSpeed = record.averageSpeed * 0.8d + speed * 0.2d;
        }
    }

    public double getSpeed(String url, double defaultValue){
        Record record = getRecord(url, defaultValue);
        if (record == null) {
            return defaultValue;
        }
        return record.averageSpeed;
    }

    private Record getRecord(String url, double speed) {
        String host = null;
        try {
            URL urlObj = new URL(url);
            host = urlObj.getHost();
        } catch (Throwable ignore) {
        }
        if (host == null){
            return null;
        }
        Record record = recordMap.get(host);
        if (record == null) {
            synchronized (recordMap) {
                record = recordMap.get(host);
                if (record == null) {
                    record = new Record();
                    record.averageSpeed = speed;
                    recordMap.put(host, record);
                }
            }
        }
        return record;
    }

    private static class Record {
        private volatile double averageSpeed;
    }

}
