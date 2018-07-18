import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class AddMapsHandler implements EventHandler<ActionEvent> {
    private Stage stage;
    private VBox layout;
    private List<String> imgExt; // list of allowed file extensions
    private ArrayList<MapSelector> mapSelectors;

    /**
     * Constructor for AddMapsHandler - keep a list of all MapSelectors
     * @param s stage
     */
    public AddMapsHandler(Stage s){
        stage = s;
        this.layout = new VBox();
        mapSelectors = new ArrayList<MapSelector>();
        imgExt = new ArrayList<String>();
        imgExt.add("*.jpg");
        imgExt.add("*.png");
        //imgExt.add("*.gif");
    }

    /**
     * Add a MapSelector to the list and update the layout
     * @param e event
     */
    @Override
    public void handle(ActionEvent e){
        Button mapSelector = new Button("Choose map file");
        Text fileName = new Text();
        // set handler for button
        FileChooserHandler mapFileHandler = new FileChooserHandler(stage, imgExt, fileName);
        mapSelector.setOnAction(mapFileHandler);

        // create floor combo box
        ComboBox<Booth.Floor> floorComboBox = new ComboBox<Booth.Floor>();
        floorComboBox.getItems().addAll(
                Booth.Floor.ARENA,
                Booth.Floor.CONCOURSE,
                Booth.Floor.MEZZANINE
        );
        floorComboBox.setValue(Booth.Floor.ARENA);
        // create map selector
        mapSelectors.add(new MapSelector(floorComboBox, mapFileHandler));
        // create layout
        HBox horizLayout = new HBox(floorComboBox, mapSelector, fileName);
        layout.getChildren().add(layout.getChildren().size(), horizLayout);
    }

    /**
     * Get layout for all map selectors
     * @return vbox layout
     */
    public VBox getLayout(){
        return layout;
    }

    /**
     * Get all map selectors
     * @return list of map selectors
     */
    public ArrayList<MapSelector> getMapSelectors(){
        return mapSelectors;
    }
}

class MapSelector {
    private ComboBox<Booth.Floor> floorComboBox;
    private FileChooserHandler mapFileHandler;

    /**
     * Constructor - pair the floor combo box with a FileChooserHandler
     * @param cb floor combo box
     * @param fh map file chooser
     */
    public MapSelector(ComboBox<Booth.Floor> cb, FileChooserHandler fh){
        floorComboBox = cb;
        mapFileHandler = fh;
    }

    /**
     * Get floor combo box
     * @return floorComboBox
     */
    public ComboBox<Booth.Floor> getFloorComboBox(){
        return floorComboBox;
    }

    /**
     * Get map chooser file handler
     * @return mapFileHandler
     */
    public FileChooserHandler getMapFileHandler(){
        return mapFileHandler;
    }

}