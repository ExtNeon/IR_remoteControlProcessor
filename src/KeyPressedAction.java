import utils.ConsoleUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;

/**
 * Created by Кирилл on 26.03.2018.
 */
class KeyPressedAction {
    static final String HELP_ACTION_TYPES = "1 - эмулировать нажатие кнопки на клавиатуре\n" +
            "2 - эмулировать нажатие кнопки мыши\n" +
            "3 - сдвинуть курсор в определённую сторону\n" +
            "enter - отменить действие";
    private String keyCode;
    private int actionId;
    private ArrayList<Integer> params = new ArrayList<>();

    KeyPressedAction(String keyCode, String actionId) {
        if (actionId.length() == 0) {
            throw new CancellationException();
        }
        this.keyCode = keyCode;
        this.actionId = Integer.valueOf(actionId);
        switch (this.actionId) {
            case 1:
                params.add(translateUserEnteredKeyToKeyCode(ConsoleUtils.getEnteredString("Введите название необходимой клавиши на английском. Пример: enter, shift, up, k, 1...\n" +
                        "Для отмены просто нажмите enter...\n" +
                        "_> ")));
                if (params.get(0) == 0) {
                    throw new CancellationException();
                }
                break;
            default:
                throw new CancellationException();
        }
    }

    private static int translateUserEnteredKeyToKeyCode(String enteredKeyName) {
        ArrayList<String> keyNames = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            if (enteredKeyName.toUpperCase().equals(KeyEvent.getKeyText(i).toUpperCase())) {
                return i;
            }
        }
        return 0;
    }

    void runAction() {
        if (params.size() == 0) {
            throw new CancellationException();
        }
        try {
            Robot robot = new Robot();
            switch (actionId) {
                case 1:
                    robot.keyPress(params.get(0));
                    robot.keyRelease(params.get(0));
                    break;
            }
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    String getKeyCode() {
        return keyCode;
    }
}
