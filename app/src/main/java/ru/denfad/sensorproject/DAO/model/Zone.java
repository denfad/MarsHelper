package ru.denfad.sensorproject.DAO.model;

public class Zone {
    private int zone_id;
    private int temperature;
    private float x, y;

    public Zone(int zone_id, int temperature, float x, float y) {
        this.zone_id = zone_id;
        this.temperature = temperature;
        this.x = x;
        this.y = y;
    }

    public Zone(int temperature, float x, float y) {
        this.temperature = temperature;
        this.x = x;
        this.y = y;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
