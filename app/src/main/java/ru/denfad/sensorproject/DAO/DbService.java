package ru.denfad.sensorproject.DAO;

import android.content.Context;

import java.util.List;

import ru.denfad.sensorproject.DAO.model.Zone;

public class DbService {

    private DbWorker dbWorker;

    public DbService(Context context) {
        dbWorker = new DbWorker(context);
    }

    public Zone getZoneById(int id) {
        return dbWorker.selectZone(id);
    }

    public List<Zone> getAllZones() {
        return dbWorker.selectAllZones();
    }

    public void addZone(Zone zone) {
        dbWorker.insertZone(zone.getX(), zone.getY(), zone.getTemperature());
    }

    public Zone findZoneByXandY(float x, float y) {
        for (Zone z : getAllZones()) {
            if (z.getX() == x & z.getY() == y) {
                return z;
            }
        }
        return null;
    }
}
