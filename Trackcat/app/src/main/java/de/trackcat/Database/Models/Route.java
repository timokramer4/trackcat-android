package de.trackcat.Database.Models;

/**
 * Model to define an route object
 */
public class Route {
    /*
     + private model attributes
     + modifications via getter and setter
     */
    private int id;
    private int type;
    private String name;
    private long date;
    private long time;
    private long rideTime;
    private long timeStamp;
    private double distance;
    private boolean isTemp;
    private String locations;
    /**
     * Empty constructor, modifications via getter and setter
     */
    public Route() {
    }

    /**
     * Constructor to save route information from database read.
     *
     * @param id        of type integer
     * @param name      of type string
     * @param time      of type long
     * @param rideTime  of type long
     * @param distance  of type double
     * @param type  of type int
     * @param date  of type long
     * @param timeStamp  of type long
     * @param isTemp  of type int
     * @param locations  of type string
     */
    public Route(int id, String name, long time, long rideTime, double distance,
                 int type, long date, long timeStamp, int isTemp, String locations) {
        this.id = id;

        this.name = name;
        this.time = time;
        this.rideTime = rideTime;
        this.distance = distance;
        this.timeStamp= timeStamp;
        this.date = date;
        this.type = type;
        this.setTempDB(isTemp);
        this.locations = locations;
    }

    /**
     * Constructor to save route information from database read.
     *
     * @param id        of type integer
     * @param name      of type string
     * @param time      of type long
     * @param rideTime  of type long
     * @param distance  of type double
     * @param type  of type int
     * @param date  of type long
     * @param timeStamp  of type long
     * @param isTemp  of type int
     */
    public Route(int id, String name, long time, long rideTime, double distance,
                 int type, long date, long timeStamp, int isTemp) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.rideTime = rideTime;
        this.distance = distance;
        this.timeStamp= timeStamp;
        this.date = date;
        this.type = type;
        this.setTempDB(isTemp);
    }

    /**
     * Getter for the id.
     *
     * @return value of type integer
     */
    public int getId() {
        return this.id;
    }

    /**
     * Setter for the id.
     *
     * @param id of type integer
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter for the name of the route.
     *
     * @return value of type string
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the name of the route.
     *
     * @param name value of type string
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for the time.
     *
     * @return value of type long
     */
    public long getTime() {
        return time;
    }

    /**
     * Setter for the time.
     *
     * @param time of type long
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Getter for the distance.
     *
     * @return value of type double
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Setter for the distance.
     *
     * @param distance of type double
     */
    public void setDistance(double distance) {
        this.distance = distance;
    }


    /**
     * Getter for the ride time.
     *
     * @return value of type long
     */
    public long getRideTime() {
        return rideTime;
    }

    /**
     * Setter for the ride time.
     *
     * @param rideTime of type long
     */
    public void setRideTime(long rideTime) {
        this.rideTime = rideTime;
    }

    /**
     * Getter for the date.
     *
     * @return value of type long (millis since Jan. 1, 1970)
     */
    public long getDate() {
        return date;
    }

    /**
     * Setter for the date.
     *
     * @param date of type long
     */
    public void setDate(long date) {
        this.date = date;
    }

    /**
     * Getter for the type.
     *
     * @return value of type integer
     */
    public int getType() {
        return type;
    }

    /**
     * Setter for the type.
     *
     * @param type of type integer
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Getter for the timeStamp.
     *
     * @return value of type long
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Setter for the timeStamp.
     *
     * @param timeStamp of type long
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Getter for temp flag.
     *
     * @return value of type boolean
     *
     * <p>
     * Returns true if the route is temp or false if it isn't.
     * </p>
     */
    public boolean isTemp() {
        return isTemp;
    }

    /**
     * Setter for temp flag.
     *
     * @param isTemp of type integer
     *
     *                   <p>
     *                   Hand over true to define that the route is temp or false to define that it isn't.
     *                   </p>
     */
    public void setTemp(boolean isTemp) {
        this.isTemp = isTemp;
    }

    /**
     * Getter to define if route is temp or not for database storage purposes.
     *
     * @return value of type integer
     *
     * <p>
     * Integer value is necessary due to SQLite Database constraint.
     * SQLite does not implement boolean values natively as true or false but only as integer.
     * </p>
     * <p>
     * Returns "1" if the route is temp or "0" if it isn't.
     * </p>
     */
    public int isTempDB() {
        if (isTemp) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Getter to define if route is temp or not for database storage purposes.
     *
     * @param isTemp value of type integer
     *
     *                   <p>
     *                   Integer value is necessary due to SQLite Database constraint.
     *                   SQLite does not implement boolean values natively as true or false but only as integer.
     *                   </p>
     *                   <p>
     *                   Hand over "1" to define that the route is temp or "0" to define that it isn't.
     *                   </p>
     */
    public void setTempDB(int isTemp) {
        this.isTemp = isTemp == 1;
    }

    /**
     * Getter for the locations of the route.
     *
     * @return value of type Text
     */
    public String getLocations() {
        return locations;
    }

    /**
     * Setter for the locations of the route.
     *
     * @param locations value of type string
     */
    public void setLocations(String locations) {
        this.locations = locations;
    }
}