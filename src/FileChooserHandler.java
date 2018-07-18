import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class FileChooserHandler implements EventHandler<ActionEvent> {
    private Stage stage;
    private FileChooser fileChooser;
    private File selectedFile;
    private Text filename;

    /**
     * Constructor - pair a FileChooser with File and Text objects
     * @param s stage
     * @param extensions list of allowed file extensions
     * @param fname file name
     */
    public FileChooserHandler(Stage s, List<String> extensions, Text fname){
        fileChooser = new FileChooser();
        fileChooser.setTitle("Choose File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Extension", extensions));

        // Set to user directory or go to default if cannot access
        String userDirectoryString = System.getProperty("user.dir");
        File userDirectory = new File(userDirectoryString);
        if(!userDirectory.canRead()) {
            userDirectory = new File("c:/");
        }
        fileChooser.setInitialDirectory(userDirectory);
        selectedFile = null;
        stage = s;
        filename = fname;
    }

    /**
     * Open file chooser dialog and set Text with filename
     * @param e event
     */
    @Override
    public void handle(ActionEvent e){
        selectedFile = fileChooser.showOpenDialog(stage);
        if(selectedFile != null) {
            filename.setText(selectedFile.getName());
        }
    }

    /**
     * Get selected file
     * @return selectedFile
     */
    public File getSelectedFile(){
        return selectedFile;
    }
}