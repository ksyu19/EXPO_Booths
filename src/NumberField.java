import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class NumberField {
    private Label label;
    private TextField field;
    private boolean required;
    public NumberField(String labelText, Integer initValue, boolean required){
        if(required){
            labelText = labelText + " * ";
        }
        this.label = new Label(labelText);
        this.label.setPrefWidth(150);
        if(initValue == null){
            this.field = new TextField();
        }
        else {
            this.field = new TextField(String.valueOf(initValue));
        }
        field.textProperty().addListener(new NumberListener(field));
        field.setPrefWidth(30);
        this.required = required;
    }
    public Integer getInputValue(){
        Integer value;
        try{
            value = Integer.valueOf(field.getText());
        }
        catch(NumberFormatException e) {
            value = null;
        }
        return value;
    }
    public boolean getRequired(){
        return required;
    }
    public String getLabelText(){
        return label.getText();
    }
    public HBox getLayout(){
        return new HBox(label, field);
    }

    /**
     *
     * @param fieldArr
     * @param values
     * @param log
     * @return
     */
    public static boolean getInputValues(ArrayList<NumberField> fieldArr, Integer[] values, Text log){
        boolean valid = true;
        for(int i = 0; i < fieldArr.size(); i ++){
            NumberField field = fieldArr.get(i);
            Integer val = field.getInputValue();
            if(val == null && field.getRequired()){
                log.setText(log.getText() + "\nPlease enter a valid number for " + field.getLabelText());
                valid = false;
            }
            else{
                values[i] = val;
            }
        }
        return valid;
    }
}

/**
 * NumberListener changes the value of a TextField if it is not valid.
 */
class NumberListener implements ChangeListener<String> {
    private final TextField textField;

    /**
     * Constructor
     * @param textField the textField to point to
     */
    public NumberListener(TextField textField) {
        this.textField = textField;
    }

    /**
     * Clears the TextField if it is not a valid number
     * @param obsVal the ObservableValue which value changed
     * @param oldVal the old value
     * @param newVal the new value
     */
    @Override
    public void changed(ObservableValue<? extends String> obsVal, String oldVal, String newVal){
        try{
            Integer.parseInt(newVal);
        }
        catch(NumberFormatException e){
            Platform.runLater(() -> {   // have to use Platform.runLater because can't clear the object while the handler is using it
                textField.clear();
            });
        }
    }
}// NumberListener class