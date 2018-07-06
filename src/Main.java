import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.regex.*;
public class Main {
    /*
    TODO: color by double booths? or rotate and center text
    TODO: comment code
    TODO: user input
    TODO: make more robust
     */

    public static void main(String[] args) throws Exception {
        readInBoothPixels(1,0, 1,2,3);
        readInBoothData(1,0, 1,2,3,4);
        //readInBoothPixels(1,0, 1,2,3);
        Booth.printAllBooths();
        File inputF = new File("2017_Booth_outline_arena_1.jpg");
        File outputF = new File("test.png");
        labelImage(inputF, outputF, Booth.Floor.ARENA, Booth.Day.FIRST);

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
                Booth.setFloorAndLoc(boothNum, f, x, y);
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
                if(line.replaceAll(",", "").equals("")){
                    continue;
                }
                String[] data = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1); // split on the comma only if that comma has zero, or an even number of quotes ahead of it
                Booth.Day day = null;
                if((data.length > firstCol) ? data[firstCol].equals("YES") : false){
                    day = Booth.Day.FIRST;
                }
                else if((data.length > secondCol) ? data[secondCol].equals("YES") : false){
                    day = Booth.Day.SECOND;
                }
                else if((data.length > bothCol) ? data[bothCol].equals("YES") : false){
                    day = Booth.Day.BOTH;
                }
                String[] boothNums = (data.length > boothCol) ? data[boothCol].split(";") : new String[]{""};
                String name = (data.length > nameCol) ? data[nameCol] : boothNums[0];
                Booth b = Booth.setCompanyAndDay(boothNums[0], name, day);
                if(boothNums.length > 1) {
                    Booth b2 = Booth.setCompanyAndDay(boothNums[1], name, day);
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

    //TODO: fix this one for first and second day split
    public static void labelImage(File inputF, File outputF, Booth.Floor floor, Booth.Day day){
        try {
            final BufferedImage image = ImageIO.read(inputF);
            Graphics g = image.getGraphics();
            g.setFont(g.getFont().deriveFont(14f));
            g.setColor(Color.BLUE);
            String unwantedWords = getUnwantedWords();
            Set<Booth> booths = Booth.getBoothsOnFloorAndDay(floor, day);
            for (Booth b : booths) {
                int letterHeight = g.getFont().getSize();
                String [] name = cleanString(b.getCompanyName(), unwantedWords);
                for(int i = 0; i < ((name.length > 3) ? 3 : name.length); i++){
                    String s = name[i];
                    if(b.getDay() == Booth.Day.BOTH){
                        g.setColor(Color.RED);
                        g.drawString(s, b.getXCoord(), b.getYCoord() + (i + 1) * letterHeight);
                        g.setColor(Color.BLUE);
                    }
                    else {
                        g.drawString(s, b.getXCoord(), b.getYCoord() + (i + 1) * letterHeight);
                    }
                }
            }
            g.dispose();
            ImageIO.write(image, "png", outputF);
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
