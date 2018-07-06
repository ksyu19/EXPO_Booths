import java.util.*;

public class Booth implements Comparable<Booth>{

    public static enum Floor{
        ARENA, CONCOURSE, MEZZANINE
    }

    public static enum Day{
        FIRST, SECOND, BOTH
    }

    private static HashMap<String, Booth> firstDayBooths = new HashMap<String, Booth>(); //map number to booth
    private static HashMap<String, Booth> secondDayBooths = new HashMap<String, Booth>(); //map number to booth
    private static HashMap<String, Booth> boothPixels = new HashMap<String, Booth>(); //map number to booth
    private String number;
    private Floor floor;
    private Day day;
    private int xCoord;
    private int yCoord;
    private String companyName;
    private Booth otherBooth; // if a company has two booths, use this variable to connect them

    public Booth(){
        number = "";
        floor = null;
        day = null;
        xCoord = 0;
        yCoord = 0;
        companyName = "";
        otherBooth = null;
    }

    public Booth(String num, Floor f, Day d, int x, int y, String name, Booth other){
        number = num;
        floor = f;
        day = d;
        xCoord = x;
        yCoord = y;
        companyName = name;
        otherBooth = other;
        addToMap(this, number, day);
    }

    private static void addToMap(Booth b, String number, Day day){
        if(day == null){
            if(b.xCoord != 0 && b.yCoord != 0){
                boothPixels.put(number, b);
            }
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
            default:{
                if(b.xCoord != 0 && b.yCoord != 0){
                    boothPixels.put(number, b);
                }
            } break;
        }
    }

    public static void setFloorAndLoc(String num, Booth.Floor floor, int x, int y){
        Booth fb = firstDayBooths.get(num);
        Booth sb = secondDayBooths.get(num);
        if(fb != null || sb != null){ // if already in the first or second day maps
            if(fb != null) {
                fb.setFloor(floor);
                fb.setXCoord(x);
                fb.setYCoord(y);
            }
            if(sb != null){
                sb.setFloor(floor);
                sb.setXCoord(x);
                sb.setYCoord(y);
            }
        }
        else {
            new Booth(num, floor, null, x, y, num, null); // otherwise add to boothPixels map
        }
    }

    public static Booth setCompanyAndDay(String num, String name, Booth.Day day){
        Booth bp = boothPixels.get(num);
        Booth b;
        if(bp != null){ // if already in the pixel map
            b = new Booth(num, bp.getFloor(), day, bp.getXCoord(), bp.getYCoord(), name, null);
        }
        else {
            b = new Booth(num, null, day, 0, 0, name, null); // otherwise add to day maps without floor & pixel info
        }
        return b;
    }

    public String getNumber(){
        return number;
    }

    public Floor getFloor() {
        return floor;
    }

    public Day getDay(){
        return day;
    }

    public int getXCoord() {
        return xCoord;
    }

    public int getYCoord() {
        return yCoord;
    }

    public String getCompanyName() {
        return companyName;
    }

    public Booth getOtherBooth() {
        return otherBooth;
    }

    public static HashMap<String, Booth> getFirstDayBooths(){
        return firstDayBooths;
    }
    public static HashMap<String, Booth> getSecondDayBooths(){
        return secondDayBooths;
    }
    public static HashMap<String, Booth> getBoothPixels(){
        return boothPixels;
    }

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

    public void setFloor(Floor floor) {
        this.floor = floor;
    }

    public void setDay(Day day) {
        this.day = day;
    }

    public void setXCoord(int xCoord) {
        this.xCoord = xCoord;
    }

    public void setYCoord(int yCoord) {
        this.yCoord = yCoord;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setOtherBooth(Booth otherBooth) {
        otherBooth.otherBooth = this;
        this.otherBooth = otherBooth;
    }

    @Override
    public String toString(){
        String s = "Number: " + number + "\t\tFloor: " + floor + "\tDay: " + day + "\tX: " + xCoord + "\tY:" + yCoord;
        s +=  "\nCompany Name: " + companyName;
        if(otherBooth != null){
            s += "\tOther Booth: " + otherBooth.number;
        }
        return s;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booth booth = (Booth) o;
        return Objects.equals(number, booth.number) && day == booth.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, day);
    }

    @Override
    public int compareTo(Booth b2){
        return this.number.compareTo(b2.number);
    }
}