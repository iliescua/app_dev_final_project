/*
 * CoordData
 * Author: Andrew Iliescu, Sam Jansen
 * Date: 5/15/21
 * This creates a CoordData object that has the attributes we wish
 * to store to the Realm DB in order to easily
 * pass information between multiple Activities
 */
package edu.msoe.flashboard;

import androidx.annotation.NonNull;

import io.realm.RealmObject;

public class CoordData extends RealmObject {
    private double longitude;
    private double latitude;
    private double altitude;
    private double bearing;
    private double speed;

    private double accelX;
    private double accelY;
    private double accelZ;

    private String timeStamp;

    public CoordData() {
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getAccelX() {
        return accelX;
    }

    public void setAccelX(double accelX) {
        this.accelX = accelX;
    }

    public double getAccelY() {
        return accelY;
    }

    public void setAccelY(double accelY) {
        this.accelY = accelY;
    }

    public double getAccelZ() {
        return accelZ;
    }

    public void setAccelZ(double accelZ) {
        this.accelZ = accelZ;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @NonNull
    @Override
    public String toString() {
        return "Lat: " + getLatitude() + " Long: " + getLongitude() + " Alt: " + getAltitude();
    }
}