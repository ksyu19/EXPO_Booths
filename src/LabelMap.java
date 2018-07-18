import javax.imageio.ImageIO;
import java.awt.*;
//import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

public class LabelMap {

    /**
     * Read in .csv file containing booth pixel information and add booths to the boothPixels HashMap
     * Pre: none of the values can be null, and indices must be greater than or equal to 0
     * @param filepath path to .csv file
     * @param skipRows number of rows in the .csv to skip (for column headers)
     * @param floorCol index for column containing booth floor ("Arena", "Mezzanine", "Concourse" - not case-sensitive). Column indices start at 0
     * @param boothCol index for column containing booth number
     * @param xCol index for column containing x pixel coordinate
     * @param yCol index for column containing y pixel coordinate
     * @throws IOException file not found or not readable
     */
    public static void readInBoothPixels(String filepath, int skipRows, int floorCol, int boothCol, int xCol, int yCol)throws IOException{
        final BufferedReader br = new BufferedReader(new FileReader(filepath));
        String line = "";
        for(int i = 0; i < skipRows; i++){
            br.readLine(); // skip header rows
        }
        while ((line = br.readLine()) != null) {
            if(line.replaceAll(",", "").equals("")){ // skip empty rows
                continue;
            }
            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // split on the comma only if that comma has zero, or an even number of quotes ahead of it (comma not part of data)
            Booth.Floor f = (data.length > floorCol) ? Booth.convertStringToFloor(data[floorCol]) : null;
            String boothNum = (data.length > boothCol) ? data[boothCol].trim() : "";
            int x, y;
            try {
                x = (data.length > xCol) ? Integer.valueOf(data[xCol].trim()) : 0;
                y = (data.length > yCol) ? Integer.valueOf(data[yCol].trim()) : 0;
            }
            catch(NumberFormatException e){
                x = 0;
                y = 0;
            }
            new Booth(boothNum, f, x, y);
        }
    }

    /**
     * Read in .csv file containing company booth information and add booths to firstDayBooths and secondDayBooths HashMaps
     * Pre: only firstCol, secondCol, or bothCol can be null. All other values cannot be null, and indices must be greater than or equal to 0
     * @param filepath path to .csv file
     * @param skipRows number of rows in the .csv to skip (for column headers).
     * @param nameCol index for column containing company name.
     * @param firstCol index for column containing first day only attendance information ("YES" or "NO" - not case sensitive).
     * @param secondCol index for column containing second day only attendance information ("YES" or "NO" - not case sensitive).
     * @param bothCol index for column containing both day attendance information ("YES" or "NO" - not case sensitive).
     * @param boothCol index for column containing booth number. If company has two booths, booths should be separated with semicolons.
     * @throws IOException file not found or not readable
     */
    public static void readInCompanyData(String filepath, int skipRows, int nameCol, Integer firstCol, Integer secondCol, Integer bothCol, int boothCol) throws IOException{
        final BufferedReader br = new BufferedReader(new FileReader(filepath));
        String line = "";
        for(int i = 0; i < skipRows; i++){
            br.readLine(); // skip header rows
        }
        while ((line = br.readLine()) != null) {
            if(line.replaceAll(",", "").equals("")){ // skip empty rows
                continue;
            }
            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // split on the comma only if that comma has zero, or an even number of quotes ahead of it
            // get booth day
            Booth.Day day = null;
            if(firstCol == null && secondCol == null && bothCol == null){ // no
                day = Booth.Day.FIRST;
            }
            else {
                if (firstCol != null && (data.length > firstCol) ? data[firstCol].toUpperCase().trim().equals("YES") : false) {
                    day = Booth.Day.FIRST;
                } else if (secondCol != null && (data.length > secondCol) ? data[secondCol].toUpperCase().trim().equals("YES") : false) {
                    day = Booth.Day.SECOND;
                } else if (bothCol != null && (data.length > bothCol) ? data[bothCol].toUpperCase().trim().equals("YES") : false) {
                    day = Booth.Day.BOTH;
                }
            }
            String[] boothNums = (data.length > boothCol) ? data[boothCol].split(";") : new String[]{""};
            String name = (data.length > nameCol) ? data[nameCol] : boothNums[0].trim();
            Booth b = new Booth(boothNums[0].trim(), name, day, line);
            if(boothNums.length > 1) { // if company has more than one booth
                Booth b2 = new Booth(boothNums[1].trim(), name, day, line);
                b.setOtherBooth(b2);
            }
        }
    }

    /**
     * Label and save map for a given floor and day
     * @param mapFile input map image file (.png or .jpg)
     * @param outputFile labeled output map image file (.png)
     * @param floor booth floor
     * @param day fair day
     * @throws IOException file not found or not readable/writable
     */
    public static void labelImage(File mapFile, File outputFile, Booth.Floor floor, Booth.Day day) throws IOException{
        final Color ONE_DAY = new Color(0,102,0); // GREEN
        final Color BOTH_DAYS = Color.BLUE;
        final Color DOUBLE = Color.RED;

        final BufferedImage image = ImageIO.read(mapFile);
        Graphics2D g = (Graphics2D)image.getGraphics();
        g.setFont(g.getFont().deriveFont(12f));
        int textHeight = g.getFontMetrics().getAscent();

        addLegend(g, day, ONE_DAY, BOTH_DAYS, DOUBLE);

        Set<Booth> booths = Booth.getBoothsOnFloorAndDay(floor, day);
        Color col;
        for (Booth b : booths) {
            /*if(b.getDrawn()){ // already drawn
                continue;
            }  // for double booths */
            String [] name = cleanString(b.getCompanyName());
            switch(b.getDay()){
                case BOTH:
                    g.setColor(col = BOTH_DAYS);
                    break;
                case FIRST:
                case SECOND:
                default:
                    g.setColor(col = ONE_DAY);
                    break;
            }
            /*if(b.getOtherBooth() != null){
                labelDoubleBooth(g, b, name, textHeight);
            } // rotate and center double booths?
            else { */
                switch(floor){
                    case ARENA: break;
                    case CONCOURSE:
                    case MEZZANINE:
                    default:
                        name = new String[]{String.join(" ", name)}; // put on one line if not arena
                        break;
                }
                for(int i = 0; i < name.length; i ++) {
                    String s = name[i];
                    g.setColor(Color.WHITE);
                    g.fillRect(b.getX(), b.getY() + i * textHeight, g.getFontMetrics().stringWidth(s), textHeight); // white background for text
                    g.setColor(col);
                    g.drawString(s, b.getX(), b.getY() + (i+1) * textHeight);
                }
                if(b.getOtherBooth()!=null) {
                    g.setColor(DOUBLE);
                    drawLineBetweenDoubleBooths(g, b, b.getOtherBooth());
                }
           // } // end single booths
        }
        g.dispose();
        ImageIO.write(image, "png", outputFile);
    }

    /**
     * Add a legend in the top right corner of the map.
     * @param g Graphics2D object
     * @param day fair day
     * @param ONE_DAY color for companies attending one day only
     * @param BOTH_DAYS color for companies attending both days
     * @param DOUBLE color for companies with two booths
     */
    public static void addLegend(Graphics2D g, Booth.Day day, Color ONE_DAY, Color BOTH_DAYS, Color DOUBLE){
        int textHeight = g.getFontMetrics().getAscent();
        int legendX = 1000; // may need to adjust start x and y values depending on map
        int legendY = 60;
        g.setColor(Color.BLACK);
        g.drawString("Legend", legendX, legendY + textHeight + 10);
        g.setColor(ONE_DAY);
        g.drawString(day + " DAY ONLY", legendX, legendY + 2 * (textHeight + 10));
        g.setColor(BOTH_DAYS);
        g.drawString("BOTH DAYS", legendX, legendY + 3 * (textHeight+ 10));
        g.setColor(DOUBLE);
        g.drawString("DOUBLE BOOTHS", legendX, legendY + 4 * (textHeight + 10));
    }

    /**
     * Draw a line between booths occupied by the same company
     * @param g Graphics2D object
     * @param b1 first booth (order doesn't matter)
     * @param b2 second booth
     */
    public static void drawLineBetweenDoubleBooths(Graphics2D g, Booth b1, Booth b2){
        g.drawLine(b1.getX()-2, b1.getY(), b2.getX()-2, b2.getY());
    }

    /**
     * Words to leave out of map labels
     * @return string of words to leave out
     */
    private static String getUnwantedWords(){
        String words = "";
        words+="the|and|of|for|a|";
        words+="corporation|corp|co|";
        words+="llc|llp|lp|";
        words+="incorporated|inc|";
        words+="group|";
        words+="company|companies|";
        words+="associates|";
        words+="partners";
        return words;
    }

    /**
     * Clean string for label and return an array of words remaining
     * @param s string to clean
     * @return array of cleaned words
     */
    private static String[] cleanString(String s){
        // take only the part of the name before the first comma
        String str = s.split(",")[0];
        // remove periods, quotation marks, and ampersands
        str = str.replaceAll("\\.|\"|\\&","").trim();
        // remove unwanted words
        Pattern stopWords = Pattern.compile("\\b(?:" + getUnwantedWords() + ")\\b\\s*", Pattern.CASE_INSENSITIVE);
        str = stopWords.matcher(str).replaceAll("");
        // split on spaces(one or more), dashes, and forward slashes
        String[] name = str.split("\\s+|\\-|/");
        for(int i = 0; i < name.length; i++){
            name[i] = name[i].substring(0,(name[i].length() > 8 ? 8 : name[i].length())); // only keep first 8 letters of each word
        }
        return Arrays.copyOfRange(name, 0, (name.length > 3 ? 3 : name.length)); // only keep up to first 3 words
    }

    // CODE FOR ROTATING TEXT WHEN COMPANIES HAVE TWO BOOTHS - not very pretty or standardized, so not using
    /*private static void labelDoubleBooth(Graphics2D g, Booth b, String[] name, int textHeight){
        AffineTransform origAt = g.getTransform();
        AffineTransform newAt = new AffineTransform();
        Booth b1 = getStartBooth(b);
        double angle = getAngleInRadians(b1);
        newAt.setToRotation(angle);
        g.setTransform(newAt);
        double newX, newY = 0; // new points after transformation
        int[] centeredPoints = getCenteredPoints(g, b1, b1.getOtherBooth(), getLongestString(name), textHeight);
        int oldX = centeredPoints[0];
        int oldY = centeredPoints[1];
        for (int i = 0; i < name.length; i++) {
            String s = name[i];
            newX = (oldY) * Math.sin(angle) + (oldX)* Math.cos(angle);
            newY = (oldY) * Math.cos(angle) - (oldX)* Math.sin(angle) + (i+1)*textHeight;
            g.drawString(s, (int) newX, (int) newY);
        }
        g.setTransform(origAt);
        b.setDrawn(true);
        b.getOtherBooth().setDrawn(true);
    }
    private static int[] getCenteredPoints (Graphics2D g, Booth b1, Booth b2, String longest, int textHeight){
        double totalDist = 2*getDistance(b1.getX(), b1.getY(), b2.getX(), b2.getY());
        double distFromStart = (totalDist - g.getFontMetrics().stringWidth(longest))/2;
        int newX, newY = 0;
        if(b1.getY() == b2.getY()){ // slope is 0
            newX = (int)(b1.getX() + distFromStart);
            newY = b1.getY();
        }
        else if(b1.getX() == b2.getX()){ // slope is undef
            newX = b1.getX();
            newY = (int)(b1.getY() + distFromStart);
        }
        else{
            double slope = ((double)(b2.getY() - b1.getY()))/(b2.getX() - b1.getX());
            double dx = (distFromStart / Math.sqrt(1 + Math.pow(slope, 2)));
            double dy = slope * dx;
            newX = (int)(b1.getX() + dx);
            newY = (int)(b1.getY() + dy);
        }
        return new int[]{newX, newY};
    }
    private static double getDistance(int x1, int y1, int x2, int y2){
        return Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
    }
    private static String getLongestString(String[] name){
        int len = 0;
        String longest = "";
        for(String s: name){
            if(s.length() > len){
                len = s.length();
                longest = s;
            }
        }
        return longest;
    }

    private static Booth getStartBooth(Booth b){
        Booth o = b.getOtherBooth();
        if(b.getX() < o.getX()){
            return b;
        }
        if(o.getX() < b.getX()){
            return o;
        }
        if(b.getY() > o.getY()){ // x's are equal - return the booth with the lower y
            return b;
        }
        return o;
    }
    private static double getAngleInRadians(Booth b1){
        Booth b2 = b1.getOtherBooth();
        return Math.atan2(b2.getY() - b1.getY(), b2.getX() - b1.getX());
    }*/
}
