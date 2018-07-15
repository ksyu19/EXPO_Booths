import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

public class LabelMap {
    public static void readInBoothPixels(String filename, Integer skipRows, Integer floorCol, Integer boothCol, Integer xCol, Integer yCol)throws IOException{
        final BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = "";
        for(int i = 0; i < skipRows; i++){
            br.readLine(); // skip header rows
        }
        while ((line = br.readLine()) != null) {
            if(line.replaceAll(",", "").equals("")){
                continue;
            }
            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // split on the comma only if that comma has zero, or an even number of quotes ahead of it
            Booth.Floor f = (data.length > floorCol) ? Booth.convertStringToFloor(data[floorCol]) : null;
            String boothNum = (data.length > boothCol) ? data[boothCol] : "";
            int x, y;
            try {
                x = (data.length > xCol) ? Integer.valueOf(data[xCol]) : 0;
                y = (data.length > yCol) ? Integer.valueOf(data[yCol]) : 0;
            }
            catch(NumberFormatException e){
                x = 0;
                y = 0;
            }
            new Booth(boothNum, f, x, y);
        }
    }

    public static void readInCompanyData(String filename, Integer skipRows, Integer nameCol, Integer firstCol, Integer secondCol, Integer bothCol, Integer boothCol) throws IOException{
        // skipRows = number of rows to skip
        final BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = "";
        for(int i = 0; i < skipRows; i++){
            br.readLine(); // skip header rows
        }
        while ((line = br.readLine()) != null) {
            if(line.replaceAll(",", "").equals("")){
                continue;
            }
            String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // split on the comma only if that comma has zero, or an even number of quotes ahead of it
            Booth.Day day = null;
            if(firstCol == null && secondCol == null && bothCol == null){
                day = Booth.Day.FIRST;
            }
            else {
                if (firstCol != null && (data.length > firstCol) ? data[firstCol].equals("YES") : false) {
                    day = Booth.Day.FIRST;
                } else if (secondCol != null && (data.length > secondCol) ? data[secondCol].equals("YES") : false) {
                    day = Booth.Day.SECOND;
                } else if (bothCol != null && (data.length > bothCol) ? data[bothCol].equals("YES") : false) {
                    day = Booth.Day.BOTH;
                }
            }
            String[] boothNums = (data.length > boothCol) ? data[boothCol].split(";") : new String[]{""};
            String name = (data.length > nameCol) ? data[nameCol] : boothNums[0].trim();
            Booth b = new Booth(boothNums[0].trim(), name, day, line);
            if(boothNums.length > 1) {
                Booth b2 = new Booth(boothNums[1].trim(), name, day, line);
                b.setOtherBooth(b2);
            }
        }
    }

    public static void labelImage(File inputF, File outputF, Booth.Floor floor, Booth.Day day){
        try {
            final BufferedImage image = ImageIO.read(inputF);
            Graphics2D g = (Graphics2D)image.getGraphics();
            g.setFont(g.getFont().deriveFont(12f));
            final Color ONE_DAY = new Color(0,102,0); // GREEN
            final Color BOTH_DAYS = Color.BLUE;
            final Color DOUBLE = Color.RED;
            Set<Booth> booths = Booth.getBoothsOnFloorAndDay(floor, day);
            int letterHeight = g.getFontMetrics().getAscent();
            Color col;
            // create legend
            int legendX = 1000;
            int legendY = 60;
            g.setColor(Color.BLACK);
            g.drawString("Legend", legendX, legendY + letterHeight + 10);
            g.setColor(ONE_DAY);
            g.drawString(day + " DAY ONLY", legendX, legendY + 2 * (letterHeight + 10));
            g.setColor(BOTH_DAYS);
            g.drawString("BOTH DAYS", legendX, legendY + 3 * (letterHeight+ 10));
            g.setColor(DOUBLE);
            g.drawString("DOUBLE BOOTHS", legendX, legendY + 4 * (letterHeight + 10));
            for (Booth b : booths) {
                /*if(b.getDrawn()){ // already drawn
                    continue;
                } */ // for double booths
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
                    labelDoubleBooth(g, b, name, letterHeight);
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
                        g.fillRect(b.getX(), b.getY() + i * letterHeight, g.getFontMetrics().stringWidth(s), letterHeight); // white background for text
                        g.setColor(col);
                        g.drawString(s, b.getX(), b.getY() + (i+1) * letterHeight);
                    }
                    if(b.getOtherBooth()!=null) {
                        g.setColor(DOUBLE);
                        g.drawLine(b.getX()-2, b.getY(), b.getOtherBooth().getX()-2, b.getOtherBooth().getY());
                    }
               // } // end not double booths
            }
            g.dispose();
            ImageIO.write(image, "png", outputF);
        }
        catch(IOException e){
            System.out.println(e);
        }
    }
    /*
    public static void labelDoubleBooth(Graphics2D g, Booth b, String[] name, int letterHeight){
        AffineTransform origAt = g.getTransform();
        AffineTransform newAt = new AffineTransform();
        Booth b1 = getStartBooth(b);
        double angle = getAngleInRadians(b1);
        newAt.setToRotation(angle);
        g.setTransform(newAt);
        double newX, newY = 0;
        int[] centeredPoints = getCenteredPoints(g, b1, b1.getOtherBooth(), getLongestString(name), letterHeight);
        int oldX = centeredPoints[0];
        int oldY = centeredPoints[1];
        for (int i = 0; i < name.length; i++) {
            String s = name[i];
            newX = (oldY) * Math.sin(angle) + (oldX)* Math.cos(angle);
            newY = (oldY) * Math.cos(angle) - (oldX)* Math.sin(angle) + (i+1)*letterHeight;
            g.drawString(s, (int) newX, (int) newY);
        }
        g.setTransform(origAt);
        b.setDrawn(true);
        b.getOtherBooth().setDrawn(true);
    }
    public static int[] getCenteredPoints (Graphics2D g, Booth b1, Booth b2, String longest, int letterHeight){
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
    public static double getDistance(int x1, int y1, int x2, int y2){
        return Math.sqrt(Math.pow(y2 - y1, 2) + Math.pow(x2 - x1, 2));
    }
    public static String getLongestString(String[] name){
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

    public static Booth getStartBooth(Booth b){
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
    public static double getAngleInRadians(Booth b1){
        Booth b2 = b1.getOtherBooth();
        return Math.atan2(b2.getY() - b1.getY(), b2.getX() - b1.getX());
    }*/
    public static String getUnwantedWords(){
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
    public static String[] cleanString(String s){
        String str = s.split(",")[0]; // take only the part of the name before the first comma
        str = str.replaceAll("\\.|\"|\\&","").trim(); // remove periods and quotation marks and ampersands
        Pattern stopWords = Pattern.compile("\\b(?:" + getUnwantedWords() + ")\\b\\s*", Pattern.CASE_INSENSITIVE);
        str = stopWords.matcher(str).replaceAll(""); // remove unwanted words
        String[] name = str.split("\\s+|\\-|/"); // split on spaces(one or more), dashes, and forward slashes
        for(int i = 0; i < name.length; i++){
            name[i] = name[i].substring(0,(name[i].length() > 8 ? 8 : name[i].length())); // only keep first 8 letters of each word
        }
        return Arrays.copyOfRange(name, 0, (name.length > 3 ? 3 : name.length)); // only keep up to first 3 words
    }
}
