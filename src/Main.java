import java.io.*;
public class Main {
    /*
    TODO: comment code
    TODO: user input
    TODO: make more robust
     */
    public static void main(String[] args) throws Exception {
        //readInBoothPixels(1,0, 1,2,3);
        LabelMap.readInBoothData(1,0, 1,2,3,4);
        LabelMap.readInBoothPixels(1,0, 1,2,3);
        //Booth.printAllBooths();
        //File inputF = new File("2017_Booth_outline_arena_1.jpg");
        //File inputF = new File("2017_Booth_outline_concourse_3.jpg");
        File inputF = new File("2017_Booth_outline_mezzanine_4.jpg");
        File outputF = new File("test.png");
        LabelMap.labelImage(inputF, outputF, Booth.Floor.MEZZANINE, Booth.Day.FIRST);
    }

}
