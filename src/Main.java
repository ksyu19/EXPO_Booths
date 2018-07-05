import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.regex.*;
public class Main {
    /*
    TODO: separate by floor,
    TODO: separate by day!!!, color by day
    TODO: color by double booths? or rotate and center text
    TODO: comment code
    TODO: user input
    TODO: make more robust (ex. clean csv input - remove empty lines, add exception handling for converting to Integer)
     */

    public static void main(String[] args) throws Exception {
        readInBoothPixels(1,0, 1,2,3);
        //readInBoothData(1,0, 1,2,3,4);
        Booth.printAllBooths();
        labelImage(Booth.getBooths());

        //cleanString("London Consulting Group", getUnwantedWords());
    }
    public static String getUnwantedWords(){
        String words = "";
        words+="the|";
        words+="corporation|corp|co|";
        words+="llc|llp|lp|";
        words+="incorporated|inc|";
        words+="group|";
        words+="company|";
        words+="associates|";
        words+="partners";
        return words;
    }

    public static void readInBoothPixels(int skipRows, int floorCol, int boothCol, int xCol, int yCol){
        // skipRows = number of rows to skip
        try {
            final BufferedReader brPixels = new BufferedReader(new FileReader("Booth_Pixels.csv"));
            String line = "";
            for(int i = 0; i < skipRows; i++){
                brPixels.readLine(); // skip header rows
            }
            while ((line = brPixels.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // split on the comma only if that comma has zero, or an even number of quotes ahead of it
                Booth.Floor f = Booth.convertStringToFloor(data[floorCol]);
                HashMap<String, Booth> booths = Booth.getBooths();
                setFloorAndLoc(booths, data[boothCol], f, Integer.valueOf(data[xCol]), Integer.valueOf(data[yCol]));
            }

        }
        catch(FileNotFoundException e){
            System.out.println(e);
        }
        catch(IOException e){
            System.out.println(e);
        }
    }

    public static void readInBoothData(int skipRows, int nameCol, int firstCol, int secondCol, int bothCol, int boothCol){
        // skipRows = number of rows to skip
        try {
            final BufferedReader brBooths = new BufferedReader(new FileReader("EXPO_Booths_Example.csv"));
            String line = "";
            for(int i = 0; i < skipRows; i++){
                brBooths.readLine(); // skip header rows
            }
            while ((line = brBooths.readLine()) != null) {
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // split on the comma only if that comma has zero, or an even number of quotes ahead of it
                Booth.Day day;
                if(data[firstCol].equals("YES")){
                    day = Booth.Day.FIRST;
                }
                else if(data[secondCol].equals("YES")){
                    day = Booth.Day.SECOND;
                }
                else{
                    day = Booth.Day.BOTH;
                }
                HashMap<String, Booth> booths = Booth.getBooths();
                String[] boothNums = data[boothCol].split(";");
                Booth b = setCompanyAndDay(booths, boothNums[0], data[nameCol], day);
                if(boothNums.length > 1) {
                    Booth b2 = setCompanyAndDay(booths, boothNums[1], data[nameCol], day);
                    b.setOtherBooth(b2);
                }
            }
        }
        catch(FileNotFoundException e){
            System.out.println(e);
        }
        catch(IOException e){
            System.out.println(e);
        }
    }
    public static Booth setFloorAndLoc(HashMap <String, Booth> booths, String num, Booth.Floor floor, int x, int y){
        Booth b = booths.get(num);
        if(b != null){
            b.setFloor(floor);
            b.setXCoord(x);
            b.setYCoord(y);
        }
        else {
            b = new Booth(num, floor, Booth.Day.FIRST, x, y, num, null);
        }
        return b;
    }
    public static Booth setCompanyAndDay(HashMap <String, Booth> booths, String num, String name, Booth.Day day){
        Booth b = booths.get(num);
        if(b != null){
            b.setCompanyName(name);
            b.setDay(day);
        }
        else {
            b = new Booth(num, Booth.Floor.ARENA, day, 0, 0, name, null);
        }
        return b;
    }
    public static void labelImage(HashMap<String, Booth> booths){
        try {
            final BufferedImage image = ImageIO.read(new File("2017_Booth_outline_arena_1.jpg"));
            Graphics g = image.getGraphics();
            g.setFont(g.getFont().deriveFont(14f));
            g.setColor(Color.BLUE);
            String unwantedWords = getUnwantedWords();
            for (Booth b : booths.values()) {
                int letterHeight = g.getFont().getSize();
                String [] name = cleanString(b.getCompanyName(), unwantedWords);
                for(int i = 0; i < ((name.length > 3) ? 3 : name.length); i++){
                    String s = name[i];
                    g.drawString(s, b.getXCoord(), b.getYCoord() + (i + 1) * letterHeight);
                }
            }
            g.dispose();
            ImageIO.write(image, "png", new File("test.png"));
        }
        catch(IOException e){
            System.out.println(e);
        }
    }
    public static String[] cleanString(String s, String unwantedWords){
        String str = s.split(",")[0]; // take only the part of the name before the first comma
        str = str.replaceAll("[\\.|\"]","").trim(); // remove periods and quotation marks
        Pattern stopWords = Pattern.compile("\\b(?:" + unwantedWords + ")\\b\\s*", Pattern.CASE_INSENSITIVE);
        str = stopWords.matcher(str).replaceAll(""); // remove unwanted words
        String [] name = str.split("[ |\\-|/]"); // split on spaces, dashes, and forward slashes
        return name;
    }
}

class Booth implements Comparable<Booth>{

    public static enum Floor{
        ARENA, CONCOURSE, MEZZANINE
    }

    public static enum Day{
        FIRST, SECOND, BOTH
    }

    public static HashMap<String, Booth> booths = new HashMap<String, Booth>(); //map number to booth
    private String number;
    private Floor floor;
    private Day day;
    private int xCoord;
    private int yCoord;
    private String companyName;
    private Booth otherBooth; // if a company has two booths, use this variable to connect them

    public Booth(){
        number = "";
        floor = Floor.ARENA;
        day = Day.FIRST;
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
        booths.put(number, this);
    }

    public static void cleanCompanyNames(){
        for(Booth b: booths.values()){
        }
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

    public static HashMap<String, Booth> getBooths(){
        return booths;
    }

    public static Floor convertStringToFloor(String s){
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
        ArrayList<Booth> sortedBooths = new ArrayList<Booth>(booths.values());
        Collections.sort(sortedBooths);
        for(Booth b: sortedBooths){
            System.out.println(b + "\n");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booth booth = (Booth) o;
        return Objects.equals(number, booth.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public int compareTo(Booth b2){
        return this.number.compareTo(b2.number);
    }
}