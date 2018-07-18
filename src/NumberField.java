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

    /**
     * Constructor - pair a Label with a TextField that only takes numbers
     * @param labelText text for label
     * @param initValue initial value
     * @param required whether this textField value is required
     */
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

    /**
     * Get input value or null if input is null or an invalid number
     * @return getInputValue
     */
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

    /**
     * Get if field is required
     * @return required
     */
    public boolean getRequired(){
        return required;
    }

    /**
     * Get label text
     * @return text
     */
    public String getLabelText(){
        return label.getText();
    }

    /**
     * Get layout for the number field
     * @return
     */
    public HBox getLayout(){
        return new HBox(label, field);
    }

    /**
     * Get all input values for a given list of NumberFields
     * @param fieldArr list of NumberFields
     * @param values list of values
     * @param log Text object to log invalid entries
     * @return true if input is acceptable (all required values are filled), false otherwise
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