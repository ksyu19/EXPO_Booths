import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Start - JavaFX main function
     * @param primaryStage initial stage
     */
    @Override
    public void start(Stage primaryStage) {
        GridPane pane = new GridPane();
        Scene scene = new Scene(pane, 1000, 500);

        primaryStage.setTitle("Label Career Fair Booth Maps");
        primaryStage.setScene(scene);

        Text instructions = new Text("1) Select .csv files. Enter the number of rows to skip (for column headers) and column indices (starting at 0)." +
                                    "\n\t* = required fields. Fields that are not required can be left blank." +
                                    "\n2) Add booth map files and select building floors." +
                                    "\n\tConvert .pdf to .jpg online if needed (ex. https://pdf2jpg.net/).");

        // booth file selectors
        List<String> ext = Arrays.asList("*.csv");

        Button companyFileButton = new Button("Select Company Info File (.csv)");
        Text companyFileName = new Text();
        FileChooserHandler companyFileHandler = new FileChooserHandler(primaryStage, ext, companyFileName);
        companyFileButton.setOnAction(companyFileHandler);
        HBox companyFileLayout = new HBox(companyFileButton, companyFileName);
        companyFileLayout.setSpacing(10);

        Button pixelFileButton = new Button("Select Booth Pixel File (.csv)");
        Text pixelFileName = new Text();
        FileChooserHandler pixelFileHandler = new FileChooserHandler(primaryStage, ext, pixelFileName);
        pixelFileButton.setOnAction(pixelFileHandler);
        HBox pixelFileLayout = new HBox(pixelFileButton, pixelFileName);
        pixelFileLayout.setSpacing(10);

        // column inputs
        ArrayList<NumberField> companyInputs = new ArrayList<NumberField>();
        VBox companyInputsLayout = new VBox();
        fillCompanyInputs(companyInputs, companyInputsLayout);

        ArrayList<NumberField> pixelInputs = new ArrayList<NumberField>();
        VBox pixelInputsLayout = new VBox();
        fillPixelInputs(pixelInputs, pixelInputsLayout);

        // map file selectors
        Button addMapButton = new Button("Add Map Files (.jpg, .png)");
        AddMapsHandler mapsHandler = new AddMapsHandler(primaryStage);
        addMapButton.setOnAction(mapsHandler);

        // day selector
        ComboBox<Booth.Day> dayComboBox = new ComboBox<Booth.Day>();
        dayComboBox.getItems().addAll(
                Booth.Day.FIRST,
                Booth.Day.SECOND
        );
        dayComboBox.setValue(Booth.Day.FIRST);
        HBox dayLayout = new HBox(dayComboBox, new Text("Choose the day of the fair to map. If the fair only occurs on one day, select FIRST."));
        dayLayout.setSpacing(10);

        // label maps handler
        Text log = new Text();
        Button label = new Button("Label and save as \"FLOOR_DAY_labeled.png\"");
        LabelMapsHandler labelHandler = new LabelMapsHandler(companyFileHandler, companyInputs, pixelFileHandler, pixelInputs, mapsHandler, dayComboBox, log);
        label.setOnAction(labelHandler);

        // layouts
        HBox inputs = new HBox(new VBox(companyFileLayout, companyInputsLayout), new VBox(pixelFileLayout, pixelInputsLayout), new VBox(addMapButton, mapsHandler.getLayout()));
        inputs.setSpacing(30);
        VBox vbox = new VBox(instructions, inputs, dayLayout, label, log);
        vbox.setSpacing(10);
        pane.getChildren().add(vbox);
        pane.setPadding(new Insets(10, 10, 10, 10));

        primaryStage.show();
    }//main

    /**
     * Set up the column inputs for the company file.
     * @param companyInputs ColumnInputs will be added to this ArrayList
     * @param companyInputsLayout NumberField layouts will be added to this VBox
     */
    private void fillCompanyInputs(ArrayList<NumberField> companyInputs, VBox companyInputsLayout){
        NumberField skipRowsBooths = new NumberField("Number of Rows to Skip:", 1, true);
        NumberField nameCol = new NumberField("Company Name Column:", 0, true);
        NumberField firstCol = new NumberField("First Day Column:", 1, false);
        NumberField secondCol = new NumberField("Second Day Column:", 2, false);
        NumberField bothCol = new NumberField("Both Day Column:", 3, false);
        NumberField boothColBooths = new NumberField("Booth Num Column:", 4, true);

        companyInputs.add(skipRowsBooths);
        companyInputs.add(nameCol);
        companyInputs.add(firstCol);
        companyInputs.add(secondCol);
        companyInputs.add(bothCol);
        companyInputs.add(boothColBooths);

        for(int i = 0; i < companyInputs.size(); i++){
            companyInputsLayout.getChildren().add(i, companyInputs.get(i).getLayout());
        }
    }

    /**
     * Set up the column inputs for the pixel file.
     * @param pixelInputs ColumnInputs will be added to this ArrayList
     * @param pixelInputsLayout NumberField layouts will be added to this VBox
     */
    private void fillPixelInputs(ArrayList<NumberField> pixelInputs, VBox pixelInputsLayout){
        NumberField skipRowsPixels = new NumberField("Number of Rows to Skip:", 1, true);
        NumberField floorCol = new NumberField("Floor Column:", 0, true);
        NumberField boothColPixels = new NumberField("Booth Num Column:", 1, true);
        NumberField xCol = new NumberField("X Pixel Column:", 2, true);
        NumberField yCol = new NumberField("Y Pixel Column:", 3, true);

        pixelInputs.add(skipRowsPixels);
        pixelInputs.add(floorCol);
        pixelInputs.add(boothColPixels);
        pixelInputs.add(xCol);
        pixelInputs.add(yCol);

        for(int i = 0; i < pixelInputs.size(); i++){
            pixelInputsLayout.getChildren().add(i, pixelInputs.get(i).getLayout());
        }
    }
}

class LabelMapsHandler implements EventHandler<ActionEvent>{
    private FileChooserHandler companyFileHandler;
    private ArrayList<NumberField> companyInputs;
    private FileChooserHandler pixelFileHandler;
    private ArrayList<NumberField> pixelInputs;
    private AddMapsHandler addMapsHandler;
    private ComboBox<Booth.Day> dayComboBox;
    private Text log;

    /**
     * Constructor - make sure label handler has access to user input values
     * @param companyFileHandler FileChooserHandler for company booth .csv
     * @param companyInputs column inputs for company booth .csv
     * @param pixelFileHandler FileChooserHandler for booth pixels .csv
     * @param pixelInputs column inputs for booth pixels .csv
     * @param mapHandler AddMapsHandler
     * @param dayComboBox day selector
     * @param log Text log for any issues
     */
    public LabelMapsHandler(FileChooserHandler companyFileHandler, ArrayList<NumberField> companyInputs, FileChooserHandler pixelFileHandler, ArrayList<NumberField> pixelInputs, AddMapsHandler mapHandler, ComboBox<Booth.Day> dayComboBox, Text log){
        this.companyFileHandler = companyFileHandler;
        this.companyInputs = companyInputs;
        this.pixelFileHandler = pixelFileHandler;
        this.pixelInputs = pixelInputs;
        this.addMapsHandler = mapHandler;
        this.dayComboBox = dayComboBox;
        this.log = log;
    }

    /**
     * Get user input values, read in booth data, and label map
     * @param e event
     */
    @Override
    public void handle(ActionEvent e){
       ArrayList<MapSelector> mapSelectors = addMapsHandler.getMapSelectors();
        if(companyFileHandler.getSelectedFile() == null || pixelFileHandler.getSelectedFile() == null || mapSelectors.size() == 0){
            log.setText(log.getText() + "\n\nMust choose both .csv files and at least one map file.");
        }
        else {
            try {
                log.setText("\nLabeling...");
                Integer[] pixelCols = new Integer[pixelInputs.size()];
                Integer[] boothCols = new Integer[companyInputs.size()];
                boolean valid = NumberField.getInputValues(pixelInputs, pixelCols, log) && NumberField.getInputValues(companyInputs, boothCols, log);
                if(valid){
                    LabelMap.readInBoothPixels(pixelFileHandler.getSelectedFile().getCanonicalPath(), pixelCols[0], pixelCols[1], pixelCols[2], pixelCols[3], pixelCols[4]);
                    LabelMap.readInCompanyData(companyFileHandler.getSelectedFile().getCanonicalPath(), boothCols[0], boothCols[1], boothCols[2], boothCols[3], boothCols[4], boothCols[5]);
                    for(MapSelector ms: mapSelectors){
                        Booth.Floor f = ms.getFloorComboBox().getValue();
                        File mapFile = ms.getMapFileHandler().getSelectedFile();
                        if(mapFile != null) {
                            File outputFile = new File(f + "_" + dayComboBox.getValue() + "_labeled.png");
                            LabelMap.labelImage(mapFile, outputFile, f, dayComboBox.getValue());
                        }
                    }
                    log.setText("\nDone.");
                }
            }//try
            catch (IOException except) {
                System.out.println(except.getMessage());
            }
        }
    }
}




