import java.util.*;

public class Booth implements Comparable<Booth>{
    // Booth floor
    public static enum Floor {
        ARENA, CONCOURSE, MEZZANINE
    }
    // Fair day (first only, second only, or both days)
    public static enum Day{
        FIRST, SECOND, BOTH
    }

    private static HashMap<String, Booth> firstDayBooths = new HashMap<String, Booth>(); // map for company booths for the first day
    private static HashMap<String, Booth> secondDayBooths = new HashMap<String, Booth>(); // map for company booths for the second day
    private static HashMap<String, Booth> boothPixels = new HashMap<String, Booth>(); // map for booth pixel data

    private String number; // booth number (or name, since some booth numbers have letters in them)
    private Floor floor;
    private Day day;
    private int xCoord;
    private int yCoord;
    private String companyName;
    private Booth otherBooth; // if a company has two booths, use this variable to connect the two booths
    private boolean drawn; // already drawn, use if only labeling once per company
    private String companyData; // booth companyData in excel file, use if you want to print a specific company's data

    /**
     * Constructor for booths that only contain pixel data and will be stored in the boothPixel HashMap. If already in the map, old data will be replaced
     * @param num booth number
     * @param f booth floor
     * @param x x pixel coordinate
     * @param y y pixel coordinate
     */
    public Booth(String num, Floor f, int x, int y){
        this(num, f, null, x, y, num, null, null);
        boothPixels.put(num, this); // add to boothPixels map

        // if already in the first or second day maps, set x and y loc for the booths in those maps
        Booth fb = firstDayBooths.get(num);
        Booth sb = secondDayBooths.get(num);
        if(fb != null) {
            fb.floor = f;
            fb.xCoord = x;
            fb.yCoord = y;
        }
        if(sb != null){
            sb.floor = f;
            sb.xCoord = x;
            sb.yCoord = y;
        }
    }

    /**
     * Constructor for booths that contain company and day information, and will be stored in the day HashMaps
     * @param num booth number
     * @param name company name
     * @param day fair day
     * @param data full company data
     */
    public Booth(String num, String name, Booth.Day day, String data){
        this(num, null, day, 0, 0, name, null, data);
        addToMap(this, num, day);

        // if already in the pixel map, set floor, x pixel, and y pixel
        Booth bp = boothPixels.get(num);
        if(bp != null){
            this.floor = bp.floor;
            this.xCoord = bp.xCoord;
            this.yCoord = bp.yCoord;
        }
    }

    /**
     * Booth Constructor
     * @param num booth number
     * @param f booth floor
     * @param d fair day
     * @param x x pixel coordinate
     * @param y y pixel coordinate
     * @param name company name
     * @param other other booth, if exists
     * @param data full company data
     */
    private Booth(String num, Floor f, Day d, int x, int y, String name, Booth other, String data){
        number = num;
        floor = f;
        day = d;
        xCoord = x;
        yCoord = y;
        companyName = name;
        otherBooth = other;
        drawn = false;
        companyData = data;
    }

    /**
     * Add booth with company info to appropriate day map
     * @param b booth
     * @param number booth number
     * @param day fair day
     */
    private static void addToMap(Booth b, String number, Day day){
        if(day == null){
            return;
        }
        switch(day){
            case FIRST: firstDayBooths.put(number, b); break;
            case SECOND: secondDayBooths.put(number, b); break;
            case BOTH:{
                firstDayBooths.put(number, b);
                secondDayBooths.put(number, b);
                break;
            }
            default: break;
        }
    }

    /**
     * Convert string to Floor, not case sensitive
     * @param s string to convert
     * @return Floor
     */
    public static Floor convertStringToFloor(String s){
        if(s == null){
            return null;
        }
        s = s.toUpperCase().trim();
        switch(s){
            case "ARENA": return Floor.ARENA;
            case "CONCOURSE": return Floor.CONCOURSE;
            case "MEZZANINE": return Floor.MEZZANINE;
            default: return null;
        }
    }

    /**
     * Booth toString
     * @return string
     */
    @Override
    public String toString(){
        String s = "Number: " + number + "\t\tFloor: " + floor + "\tDay: " + day + "\tX: " + xCoord + "\tY:" + yCoord;
        s +=  "\nCompany Name: " + companyName;
        if(otherBooth != null){
            s += "\tOther Booth: " + otherBooth.number;
        }
        return s;
    }

    /**
     * Print all booths for the first and second days
     */
    public static void printAllBooths(){
        ArrayList<Booth> sortedBooths = new ArrayList<Booth>(firstDayBooths.values());
        Collections.sort(sortedBooths);
        System.out.println("FIRST AND BOTH DAY BOOTHS\n\n");
        for(Booth b: sortedBooths){
            System.out.println(b + "\n");
        }

        sortedBooths = new ArrayList<Booth>(secondDayBooths.values());
        Collections.sort(sortedBooths);
        System.out.println("\n\nSECOND AND BOTH DAY BOOTHS\n\n");
        for(Booth b: sortedBooths){
            System.out.println(b + "\n");
        }

    }

    /**
     * Booths equal each other if the number and day are the same
     * @param o booth to compare to
     * @return true if number and day are the same, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booth booth = (Booth) o;
        return Objects.equals(number, booth.number) && day == booth.day;
    }

    /**
     * Hashcode for number and day
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return Objects.hash(number, day);
    }

    /**
     * Compare to based on booth number
     * @param b2 booth to compare to
     * @return string compareTo result
     */
    @Override
    public int compareTo(Booth b2){
        return this.number.compareTo(b2.number);
    }

    //================================================================================
    // Accessors
    //================================================================================

    /**
     * Get fair day
     * @return day
     */
    public Day getDay(){
        return day;
    }

    /**
     * Get x pixel coordinate
     * @return xCoord
     */
    public int getX() {
        return xCoord;
    }

    /**
     * Get y pixel coordinate
     * @return yCoord
     */
    public int getY() {
        return yCoord;
    }

    /**
     * Get company name
     * @return companyName
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Get other booth
     * @return otherBooth
     */
    public Booth getOtherBooth() {
        return otherBooth;
    }

    /**
     * Get drawn
     * @return return drawn
     */
    public boolean getDrawn() { return drawn;}

    /**
     * Get all company data
     * @return companyData
     */
    public String getCompanyData() {return companyData;}

    /**
     * Get booths for a given floor and day
     * @param f floor
     * @param d day. if no day is specified, will return booth pixel data instead
     * @return set of booths
     */
    public static Set<Booth> getBoothsOnFloorAndDay(Floor f, Day d){
        // first day returns first and both
        // second day returns second and both
        Set<Booth> selectedBooths = new HashSet<Booth>();
        if(d == null){
            return null;
        }
        switch(d){
            case FIRST:
                for(Booth b: firstDayBooths.values()){
                    if((b.floor == f)){
                        selectedBooths.add(b);
                    }
                }
                break;
            case SECOND:
                for(Booth b: secondDayBooths.values()){
                    if((b.floor == f)){
                        selectedBooths.add(b);
                    }
                }
                break;
            default:
                for(Booth b: boothPixels.values()){
                    if((b.floor == f)){
                        selectedBooths.add(b);
                    }
                }
                break;
        }
        return selectedBooths;
    }

    //================================================================================
    // Mutators
    //================================================================================

    /**
     * Set drawn
     * @param d drawn
     */
    public void setDrawn(boolean d){
        drawn = d;
    }

    /**
     * Set other booth for this booth and the other booth
     * @param otherBooth other booth to connect this booth with
     */
    public void setOtherBooth(Booth otherBooth) {
        otherBooth.otherBooth = this;
        this.otherBooth = otherBooth;
    }

}