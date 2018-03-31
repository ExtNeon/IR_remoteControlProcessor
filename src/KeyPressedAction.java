import utils.ConsoleUtils;
import utils.iniSettings.INISettingsField;
import utils.iniSettings.INISettingsSection;
import utils.iniSettings.exceptions.AlreadyExistsException;
import utils.iniSettings.exceptions.NotFoundException;

import java.awt.*;
import java.awt.event.InputEvent;
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
            "4 - эмулировать вращение колеса прокрутки мыши\n" +
            "5 - эмулировать нажатие комбинации клавиш\n" +
            "enter - отменить действие";
    private String keyCode;
    private int actionId;
    private ArrayList<Integer> params = new ArrayList<>();
    private long minimalIntervalBetweenNextPress = 0;
    private long lastPressedTime = System.currentTimeMillis();

    KeyPressedAction(String keyCode, String actionId) throws CancellationException {
        if (actionId.length() == 0) {
            throw new CancellationException();
        }
        this.keyCode = keyCode;
        this.actionId = Integer.valueOf(actionId);
        switch (this.actionId) {
            case 1:
                String enteredStr = "";
                do {
                    enteredStr = ConsoleUtils.getEnteredString("Введите название необходимой клавиши на английском. Пример: enter, shift, up, k, 1...\nДля получения списка всех возможных клавиш, введите list keys\n" +
                            "Для отмены просто нажмите enter...\n" +
                            "_> ");
                    if (enteredStr.equals("list keys")) {
                        System.out.println(getAllKeyNames());
                    }
                } while (enteredStr.equals("list keys"));
                params.add(translateUserEnteredKeyToKeyCode(enteredStr));
                if (params.get(0) == 0) {
                    throw new CancellationException();
                }
                break;
            case 2:
                params.add(ConsoleUtils.getEnteredIntegerNumber("Выберите, какая именно клавиша мыши будет нажата: \n" +
                        "1 - Левая\n" +
                        "2 - Правая\n" +
                        "3 - обе\n" +
                        "Для отмены введите 0...\n" +
                        "_> "));
                if (params.get(0) == 0 || params.get(0) > 3) {
                    throw new CancellationException();
                }
                minimalIntervalBetweenNextPress = 300;
                break;
            case 3:
                params.add(ConsoleUtils.getEnteredIntegerNumber("Введите количество пикселей по горизонтали, на которое будет сдвинута мышь (вправо): "));
                params.add(ConsoleUtils.getEnteredIntegerNumber("Введите количество пикселей по вертикали, на которое будет сдвинута мышь (вниз): "));
                break;
            case 4:
                params.add(ConsoleUtils.getEnteredIntegerNumber("Введите количество шагов, на которое будет совершена прокрутка (вниз): "));
                break;
            case 5:
                params.add(ConsoleUtils.getEnteredIntegerNumber("Введите количество клавиш, которые будут нажаты одновременно: "));
                if (params.get(0) <= 0) {
                    throw new CancellationException();
                }
                for (int i = 0; i < params.get(0); i++) {
                    do {
                        enteredStr = ConsoleUtils.getEnteredString("Введите название " + (i + 1) + "-ой клавиши на английском. Пример: enter, shift, up, k, 1...\nДля получения списка всех возможных клавиш, введите list keys\n" +
                                "Для отмены просто нажмите enter...\n" +
                                "_> ");
                        if (enteredStr.equals("list keys")) {
                            System.out.println(getAllKeyNames());
                        }
                    } while (enteredStr.equals("list keys"));
                    int code = translateUserEnteredKeyToKeyCode(enteredStr);
                    if (code == 0) {
                        throw new CancellationException();
                    } else {
                        params.add(code);
                    }
                }
                minimalIntervalBetweenNextPress = 300;
                break;
            default:
                throw new CancellationException();
        }
    }

    public KeyPressedAction(INISettingsSection settingsRecord) throws InvalidSettingsRecordException {
        this.keyCode = settingsRecord.getSectionName();
        try {
            this.actionId = Integer.valueOf(settingsRecord.getFieldByKey("actionId").getValue());
            this.minimalIntervalBetweenNextPress = Integer.valueOf(settingsRecord.getFieldByKey("minPressInterval").getValue());
            for (int i = 0; i < Integer.valueOf(settingsRecord.getFieldByKey("paramsCount").getValue()); i++) {
                params.add(Integer.valueOf(settingsRecord.getFieldByKey("param_" + i).getValue()));
            }
        } catch (NotFoundException e) {
            throw new InvalidSettingsRecordException();
        }
    }

    private static int translateUserEnteredKeyToKeyCode(String enteredKeyName) {
        ArrayList<String> keyNames = new ArrayList<>();
        for (int i = 0; i < 1024; i++) {
            if (enteredKeyName.toUpperCase().equals(KeyEvent.getKeyText(i).toUpperCase())) {
                return i;
            }
        }
        return 0;
    }

    private static String getAllKeyNames() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1024; i++) {
            if (!KeyEvent.getKeyText(i).contains("Unknown keyCode:")) {
                builder.append(KeyEvent.getKeyText(i));
                builder.append('\n');
            }
        }
        return builder.toString();
    }

    void runAction() throws CancellationException {
        if (params.size() == 0) {
            throw new CancellationException();
        }
        lastPressedTime = System.currentTimeMillis();
        try {
            Robot robot = new Robot();
            switch (actionId) {
                case 1:
                    robot.keyPress(params.get(0));
                    robot.keyRelease(params.get(0));
                    break;
                case 2:
                    int mouseKeyMask = params.get(0) == 1 || params.get(0) != 2 ? InputEvent.BUTTON1_DOWN_MASK : InputEvent.BUTTON3_DOWN_MASK;
                    if (params.get(0) == 3) {
                        mouseKeyMask = mouseKeyMask | InputEvent.BUTTON3_DOWN_MASK;
                    }
                    robot.mousePress(mouseKeyMask);
                    robot.mouseRelease(mouseKeyMask);
                    break;
                case 3:
                    if (params.size() < 2) {
                        throw new CancellationException();
                    }
                    int newXPos = (int) MouseInfo.getPointerInfo().getLocation().getX() + params.get(0);
                    int newYPos = (int) MouseInfo.getPointerInfo().getLocation().getY() + params.get(1);
                    robot.mouseMove(newXPos, newYPos);
                    break;
                case 4:
                    if (params.size() < 1) {
                        throw new CancellationException();
                    }
                    robot.mouseWheel(params.get(0));
                    break;
                case 5:
                    if (params.size() < 1) {
                        throw new CancellationException();
                    }
                    for (int i = 1; i <= params.get(0); i++) {
                        robot.keyPress(params.get(i));
                    }
                    for (int i = 1; i <= params.get(0); i++) {
                        robot.keyRelease(params.get(i));
                    }
                    break;
            }
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public long getMinimalIntervalBetweenNextPress() {
        return minimalIntervalBetweenNextPress;
    }

    public void setMinimalIntervalBetweenNextPress(long minimalIntervalBetweenNextPress) {
        this.minimalIntervalBetweenNextPress = minimalIntervalBetweenNextPress;
    }

    public long getLastPressedTime() {
        return lastPressedTime;
    }

    public INISettingsSection getSettingsSection() {
        INISettingsSection newSection = new INISettingsSection(keyCode);
        try {
            newSection.addField(new INISettingsField("actionId", "" + actionId));
            newSection.addField(new INISettingsField("minPressInterval", "" + minimalIntervalBetweenNextPress));
            newSection.addField(new INISettingsField("paramsCount", "" + params.size()));
            for (int i = 0; i < params.size(); i++) {
                newSection.addField(new INISettingsField("param_" + i, "" + params.get(i)));
            }
        } catch (AlreadyExistsException e) {
            e.printStackTrace();
        }
        return newSection;
    }

    String getKeyCode() {
        return keyCode;
    }
}
