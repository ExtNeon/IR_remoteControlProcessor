/**
 * Created by Кирилл on 25.03.2018.
 */
public class ButtonPressedEventListenerRecord {
    private ButtonPressedEventListener eventListener;
    private String buttonCode;

    public ButtonPressedEventListenerRecord(ButtonPressedEventListener eventListener, String buttonCode) {
        this.eventListener = eventListener;
        this.buttonCode = buttonCode;
    }

    public ButtonPressedEventListener getEventListener() {
        return eventListener;
    }

    public void setEventListener(ButtonPressedEventListener eventListener) {
        this.eventListener = eventListener;
    }

    public String getButtonCode() {
        return buttonCode;
    }

    public void setButtonCode(String buttonCode) {
        this.buttonCode = buttonCode;
    }
}
