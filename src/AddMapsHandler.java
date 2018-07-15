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
    private List<String> imgExt;
    private ArrayList<MapSelectors> mapSelectors;
    public AddMapsHandler(Stage s){
        stage = s;
        this.layout = new VBox();
        mapSelectors = new ArrayList<MapSelectors>();
        imgExt = new ArrayList<String>();
        imgExt.add("*.jpg");
        imgExt.add("*.png");
        //imgExt.add("*.gif");
    }
    @Override
    public void handle(ActionEvent e){
        Button mapSelector = new Button("Choose map file");
        Text fileName = new Text();
        FileChooserHandler mapFileHandler = new FileChooserHandler(stage, imgExt, fileName);
        // set handlers for buttons
        mapSelector.setOnAction(mapFileHandler);
        // add to pane
        ComboBox<Booth.Floor> floorComboBox = new ComboBox<Booth.Floor>();
        floorComboBox.getItems().addAll(
                Booth.Floor.ARENA,
                Booth.Floor.CONCOURSE,
                Booth.Floor.MEZZANINE
        );
        floorComboBox.setValue(Booth.Floor.ARENA);
        mapSelectors.add(new MapSelectors(floorComboBox, mapFileHandler));
        HBox horizLayout = new HBox(floorComboBox, mapSelector, fileName);
        layout.getChildren().add(layout.getChildren().size(), horizLayout);
    }
    public VBox getLayout(){
        return layout;
    }
    public ArrayList<MapSelectors> getMapSelectors(){
        return mapSelectors;
    }
}
class MapSelectors{
    ComboBox<Booth.Floor> floorComboBox;
    FileChooserHandler mapFileHandler;
    public MapSelectors(ComboBox<Booth.Floor> cb, FileChooserHandler fh){
        floorComboBox = cb;
        mapFileHandler = fh;
    }
    public ComboBox<Booth.Floor> getFloorComboBox(){
        return floorComboBox;
    }
    public FileChooserHandler getMapFileHandler(){
        return mapFileHandler;
    }
}