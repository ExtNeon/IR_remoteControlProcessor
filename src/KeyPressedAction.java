import utils.ConsoleUtils;
import utils.iniSettings.INISettingsRecord;
import utils.iniSettings.INISettingsSection;
import utils.iniSettings.exceptions.AlreadyExistsException;
import utils.iniSettings.exceptions.NotFoundException;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;

/**
 * Класс, представляющий определённую клавишу. Позволяет привязать действие и исполнить его.
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

    /**
     * Конструктор. Опрашивает пользователя в соответствии с id действия, и заполняет параметры действия на основании полученных данных.
     *
     * @param keyCode  Код клавиши на ПДУ, которому соответствует действие
     * @param actionId Код действия, выбранный пользователем.
     * @throws CancellationException В случае намеренной отмены дальнейшего ввода пользователем.
     */
    KeyPressedAction(String keyCode, String actionId) throws CancellationException {
        if (actionId.length() == 0) {
            throw new CancellationException();
        }
        this.keyCode = keyCode;
        try {
            this.actionId = Integer.valueOf(actionId);
        } catch (Exception ignored) {
            throw new CancellationException();
        }
        switch (this.actionId) {
            case 1:
                set_oneKeyPress();
                break;
            case 2:
                set_mouseKeyPress();
                break;
            case 3:
                set_mouseMove();
                break;
            case 4:
                set_mouseWheelTurn();
                break;
            case 5:
                set_keyComboPress();
                break;
            default:
                throw new CancellationException();
        }
    }

    /**
     * Конструктор, принимающий в качестве параметра секцию INI - файла, представленную в виде объекта INISettingsSection.
     * Обратный данному метод, позволяющий сгенерировать секцию - @code{getSettingsSection()}
     *
     * @param settingsSection Секция, из которой будут импортированы параметры.
     * @throws InvalidSettingsRecordException В случае, если секция не содержит определённых записей, необходимых для успешного импорта.
     */
    KeyPressedAction(INISettingsSection settingsSection) throws InvalidSettingsRecordException {
        this.keyCode = settingsSection.getSectionName();
        try {
            this.actionId = Integer.valueOf(settingsSection.getFieldByKey("actionId").getValue());
            this.minimalIntervalBetweenNextPress = Integer.valueOf(settingsSection.getFieldByKey("minPressInterval").getValue());
            for (int i = 0; i < Integer.valueOf(settingsSection.getFieldByKey("paramsCount").getValue()); i++) {
                params.add(Integer.valueOf(settingsSection.getFieldByKey("param_" + i).getValue()));
            }
        } catch (NotFoundException e) {
            throw new InvalidSettingsRecordException();
        }
    }

    /**
     * Преобразует введённое пользователем название клавиши в её код, причём независимо от регистра.
     * @param enteredKeyName Название клавиши, код которой нужно узнать.
     * @return Код клавиши с заданным названием.
     */
    private static int translateUserEnteredKeyToKeyCode(String enteredKeyName) {
        for (int i = 0; i < 1024; i++) {
            if (enteredKeyName.toUpperCase().equals(KeyEvent.getKeyText(i).toUpperCase())) {
                return i;
            }
        }
        return 0;
    }

    /**
     * @return Список названий всех доступных клавиш. Каждое название располагается на новой строке.
     */
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

    /**
     * Устанавливает параметры действия "Нажать комбинацию клавиш" в соответствие с введёнными пользователем данными.
     *
     * @throws CancellationException В случае, если пользователь прервал ввод некорректными данными.
     */
    private void set_keyComboPress() throws CancellationException {
        String enteredStr;
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
    }

    /**
     * Устанавливает параметры действия "Прокрутить колесо мыши" в соответствие с введёнными пользователем данными.
     */
    private void set_mouseWheelTurn() {
        params.add(ConsoleUtils.getEnteredIntegerNumber("Введите количество шагов, на которое будет совершена прокрутка (вниз): "));
    }

    /**
     * Устанавливает параметры действия "Переместить курсор" в соответствие с введёнными пользователем данными.
     */
    private void set_mouseMove() {
        params.add(ConsoleUtils.getEnteredIntegerNumber("Введите количество пикселей по горизонтали, на которое будет сдвинута мышь (вправо): "));
        params.add(ConsoleUtils.getEnteredIntegerNumber("Введите количество пикселей по вертикали, на которое будет сдвинута мышь (вниз): "));
    }

    /**
     * Устанавливает параметры действия "Нажать клавишу мыши" в соответствие с введёнными пользователем данными.
     *
     * @throws CancellationException В случае, если пользователь прервал ввод некорректными данными.
     */
    private void set_mouseKeyPress() throws CancellationException {
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
    }

    /**
     * Устанавливает параметры действия "Нажать клавишу" в соответствие с введёнными пользователем данными.
     *
     * @throws CancellationException В случае, если пользователь прервал ввод некорректными данными.
     */
    private void set_oneKeyPress() throws CancellationException {
        String enteredStr;
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
        String minPressInterval = ConsoleUtils.getEnteredString("Введите минимальный интервал для данной клавиши (миллисекунд), в течение которого её нельзя будет нажать повторно.\nДанная опция необходима для тех клавиш, периодическое частое срабатывание которых нежелательно.\n" +
                "Для отмены просто нажмите enter...\n" +
                "_> ");
        if (minPressInterval.equals("\n")) {
            throw new CancellationException();
        }
        try {
            minimalIntervalBetweenNextPress = Integer.valueOf(minPressInterval);
        } catch (Exception ignored) {
            throw new CancellationException();
        }
    }

    /**
     * Выполняет действие, привязанное к данной клавише ранее.
     * @throws CancellationException В случае, если параметры действия некорректны.
     */
    void runAction() throws CancellationException {
        if (params.size() == 0) {
            throw new CancellationException();
        }
        lastPressedTime = System.currentTimeMillis();
        try {
            Robot robot = new Robot();
            switch (actionId) {
                case 1:
                    runAction_oneKeyPress(robot);
                    break;
                case 2:
                    runAction_mouseKeyPress(robot);
                    break;
                case 3:
                    runAction_mouseMove(robot);
                    break;
                case 4:
                    runAction_mouseWheelTurn(robot);
                    break;
                case 5:
                    runAction_keyComboPress(robot);
                    break;
            }
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    /**
     * Виртуально нажимает комбинацию клавиш, и тут же её отпускает. Комбинация клавиш должна быть записана в параметрах к действию.
     *
     * @param robot Экземпляр класса Robot, с помощью которого будет осуществляться дуйствие в системе.
     * @throws CancellationException В случае, если параметры к действию некорректны.
     */
    private void runAction_keyComboPress(Robot robot) throws CancellationException {
        if (params.size() < 1) {
            throw new CancellationException();
        }
        for (int i = 1; i <= params.get(0); i++) {
            robot.keyPress(params.get(i));
        }
        for (int i = 1; i <= params.get(0); i++) {
            robot.keyRelease(params.get(i));
        }
    }

    /**
     * Виртуально поворачивает колесо мыши вверх или вниз, исходя из параметров к действию.
     *
     * @param robot Экземпляр класса Robot, с помощью которого будет осуществляться дуйствие в системе.
     * @throws CancellationException В случае, если параметры к действию некорректны.
     */
    private void runAction_mouseWheelTurn(Robot robot) throws CancellationException {
        if (params.size() < 1) {
            throw new CancellationException();
        }
        robot.mouseWheel(params.get(0));
    }

    /**
     * Перемещает курсор мыши на определённое количество пикселей по вертикали и по горизонтали, исходя из параметров к действию.
     *
     * @param robot Экземпляр класса Robot, с помощью которого будет осуществляться дуйствие в системе.
     * @throws CancellationException В случае, если параметры к действию некорректны.
     */
    private void runAction_mouseMove(Robot robot) throws CancellationException {
        if (params.size() < 2) {
            throw new CancellationException();
        }
        int newXPos = (int) MouseInfo.getPointerInfo().getLocation().getX() + params.get(0);
        int newYPos = (int) MouseInfo.getPointerInfo().getLocation().getY() + params.get(1);
        robot.mouseMove(newXPos, newYPos);
    }

    /**
     * Виртуально нажимает и тут же отпускает определённую клавишу мыши, исходя из параметров к действию.
     *
     * @param robot Экземпляр класса Robot, с помощью которого будет осуществляться дуйствие в системе.
     */
    private void runAction_mouseKeyPress(Robot robot) {
        int mouseKeyMask = params.get(0) == 1 || params.get(0) != 2 ? InputEvent.BUTTON1_DOWN_MASK : InputEvent.BUTTON3_DOWN_MASK;
        if (params.get(0) == 3) {
            mouseKeyMask = mouseKeyMask | InputEvent.BUTTON3_DOWN_MASK;
        }
        robot.mousePress(mouseKeyMask);
        robot.mouseRelease(mouseKeyMask);
    }

    /**
     * Виртуально нажимает и тут же отпускает клавишу, записанную в параметрах к действию
     *
     * @param robot Экземпляр класса Robot, с помощью которого будет осуществляться дуйствие в системе.
     */
    private void runAction_oneKeyPress(Robot robot) {
        robot.keyPress(params.get(0));
        robot.keyRelease(params.get(0));
    }

    /**
     * @return Установленный минимальный интервал между повторными нажатиями данной клавиши в миллисекундах.
     */
    long getMinimalIntervalBetweenNextPress() {
        return minimalIntervalBetweenNextPress;
    }

    /**
     * Метод позволяет установить минимальный интервал между повторными нажатиями данной клавиши в миллисекундах.
     * @param minimalIntervalBetweenNextPress Необходимый минимальный интервал в миллисекундах.
     */
    public void setMinimalIntervalBetweenNextPress(long minimalIntervalBetweenNextPress) {
        this.minimalIntervalBetweenNextPress = minimalIntervalBetweenNextPress;
    }

    /**
     * @return Время последнего нажатия данной клавиши
     */
    long getLastPressedTime() {
        return lastPressedTime;
    }

    /**
     * Возвращает объект типа INISettingsSection, представляющего собой секцию INI. Нужен для сохранения настроек в файл INI.
     * @return Секция INI - файла в виде экземпляра INISettingsSection.
     */
    INISettingsSection getSettingsSection() {
        INISettingsSection newSection = new INISettingsSection(keyCode);
        try {
            newSection.addField(new INISettingsRecord("actionId", "" + actionId));
            newSection.addField(new INISettingsRecord("minPressInterval", "" + minimalIntervalBetweenNextPress));
            newSection.addField(new INISettingsRecord("paramsCount", "" + params.size()));
            for (int i = 0; i < params.size(); i++) {
                newSection.addField(new INISettingsRecord("param_" + i, "" + params.get(i)));
            }
        } catch (AlreadyExistsException e) {
            e.printStackTrace();
        }
        return newSection;
    }

    /**
     * @return Возвращает код клавиши, к которому привязано это действие
     */
    String getKeyCode() {
        return keyCode;
    }
}
