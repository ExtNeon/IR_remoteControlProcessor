import jssc.SerialPortList;
import utils.ConsoleUtils;
import utils.iniSettings.INISettings;
import utils.iniSettings.INISettingsSection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;

/**
 * Created by Кирилл on 25.03.2018.
 */
public class Main implements ButtonPressedEventListener {

    final private static String KEY_ACTIONS_SETTINGS_FILE = "actions.ini";
    final private static String MAIN_SETTINGS_FILE = "settings.ini";
    final private static long MAX_CONNECTION_WAIT_TIMEOUT = 3000;
    final private static String HELP_STR = "help - показать эту справку\n" +
            "enter - привязать определённую клавишу к действию\n" +
            "reconnect - закрыть текущее подключение, и заново запустить поиск устройств\n" +
            "toggle speaker - включить/выключить звуковой сигнал при нажатии кнопки\n" +
            "exit - выйти из приложения";
    private ArrayList<KeyPressedAction> keyPressedActions = new ArrayList<>();
    private boolean enableSignal = false;
    private String lastKeyPressed = "";

    private Main() throws InterruptedException {
        INISettings settings = new INISettings();
        try {
            settings.loadFromFile(KEY_ACTIONS_SETTINGS_FILE);
            for (INISettingsSection currentSection : settings.getSections()) {
                keyPressedActions.add(new KeyPressedAction(currentSection));
            }
        } catch (InvalidSettingsRecordException e) {
            System.err.println("Ошибка интерпретации файла настроек");
        } catch (IOException ignored) {
            System.err.println("Ошибка чтения файла настроек");
        }
        System.out.println("Список доступных портов:");
        printAvailablePorts();
        IR_moduleConnection ir_module = findAndConnectToTheModule();
        ir_module.attachButtonEventListener(this);
        System.out.println("Доступные команды:\n" + HELP_STR);
        while (ir_module != null) {
            switch (ConsoleUtils.getEnteredString("\r_> ")) {
                case "":
                    String buttonGettedCode = askForChoseKey(ir_module);
                    try {
                        addButtonPressedActionIntoList(new KeyPressedAction(buttonGettedCode, ConsoleUtils.getEnteredString("Выберите действие при нажатии на данную кнопку: \n" + KeyPressedAction.HELP_ACTION_TYPES + "\n_>")), keyPressedActions);
                        System.out.println("Действие успешно привязано к данной кнопке.");
                        settings.updateSection(keyPressedActions.get(keyPressedActions.size() - 1).getSettingsSection());
                        settings.saveToFile(KEY_ACTIONS_SETTINGS_FILE);
                    } catch (CancellationException ignored) {
                        System.out.println("Установка действия была отменена");
                    } catch (IOException e) {
                        System.err.println("Ошибка сохранения файла настроек");
                    }
                    break;
                case "exit":
                    ir_module.close();
                    ir_module = null;
                    break;
                case "reconnect":
                    ir_module.close();
                    ir_module = findAndConnectToTheModule();
                    ir_module.attachButtonEventListener(this);
                    break;
                case "toggle speaker":
                    enableSignal = !enableSignal;
                    System.out.println(enableSignal ? "Сигнал включен" : "Сигнал выключен");
                    break;
                case "help":
                    System.out.println("Доступные команды:\n" + HELP_STR);
                    break;
                default:
                    System.out.println("Неверная команда");
                    break;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new Main();
    }

    private static void addButtonPressedActionIntoList(KeyPressedAction action, ArrayList<KeyPressedAction> actionsList) {
        removeButtonPressedActionFromList(action, actionsList);
        actionsList.add(action);
    }

    private static void removeButtonPressedActionFromList(KeyPressedAction action, ArrayList<KeyPressedAction> actionsList) {
        for (KeyPressedAction selectedAction : actionsList) {
            if (action.getKeyCode().equals(selectedAction.getKeyCode())) {
                actionsList.remove(selectedAction);
                break;
            }
        }
    }

    private static IR_moduleConnection findAndConnectToTheModule() throws InterruptedException {
        IR_moduleConnection ir_moduleConnection = null;
        System.out.println("Поиск и подключение...");
        while (ir_moduleConnection == null) {
            if (SerialPortList.getPortNames().length > 0) {
                for (String selectedPort : SerialPortList.getPortNames()) {
                    long connection_startedTime = System.currentTimeMillis();
                    ir_moduleConnection = new IR_moduleConnection(selectedPort);
                    while (System.currentTimeMillis() - connection_startedTime < MAX_CONNECTION_WAIT_TIMEOUT
                            && !ir_moduleConnection.isConnected()) {
                        Thread.sleep(100); //Ждём до тех пор, пока не будет осуществлено подключение, либо пока не выйдет время.
                    }
                    if (ir_moduleConnection.isConnected()) {
                        System.out.println("Устройство найдено и готово к работе!\nПорт: " + ir_moduleConnection.getSerialPort().getPortName());
                        break;
                    } else {
                        ir_moduleConnection.close();
                        ir_moduleConnection = null;
                    }
                }
            }
        }
        return ir_moduleConnection;
    }

    private static String askForChoseKey(IR_moduleConnection ir_module) throws InterruptedException {
        boolean isValid;
        String buttonGettedCode;
        do {
            System.out.println("Нажмите клавишу на пульте дистанционного управления...");
            buttonGettedCode = getPressedKey(ir_module);
            System.out.println("Код клавиши получен: " + buttonGettedCode + "\n" +
                    "Повторите нажатие для подтверждения действия...");
            ir_module.pauseReceivingFor(300);
            isValid = getPressedKey(ir_module).equals(buttonGettedCode);
            if (!isValid) {
                System.out.println("Ошибка! Полученный код клавиши отличается от первоначального. Повторите попытку.");
            }
        } while (!isValid);
        System.out.println("Клавиша подтверждена");
        return buttonGettedCode;
    }

    private static String[] freeInput(IR_moduleConnection ir_module, int inputLength, int delayBetween) throws InterruptedException {
        String[] pressedButtons = new String[inputLength];
        for (int i = 0; i < inputLength; i++) {
            pressedButtons[i] = getPressedKey(ir_module);
            Thread.sleep(delayBetween);
        }
        return pressedButtons;
    }

    private static String getPressedKey(IR_moduleConnection ir_module) throws InterruptedException {
        ir_module.turnToWaitToClickMode();
        while (ir_module.isWaitingForClick()) {
            Thread.sleep(100); //Ждём до тех пор, пока юзер не нажмёт на кнопку на пульте
        }
        return ir_module.getClickResult();
    }

    private static void printAvailablePorts() {
        String[] portNames = SerialPortList.getPortNames();
        if (portNames.length > 0) {
            for (String currentPortName : portNames) {
                System.out.println(currentPortName);
            }
        } else {
            System.out.println("Нет доступных портов.");
        }
    }


    @Override
    public void buttonPressed(String buttonCode, IR_moduleConnection ir_module) {
        for (KeyPressedAction selectedAction : keyPressedActions) {
            if (selectedAction.getKeyCode().equals(buttonCode)) {
                if (!(lastKeyPressed.equals(buttonCode) && (System.currentTimeMillis() - selectedAction.getLastPressedTime()) < selectedAction.getMinimalIntervalBetweenNextPress())) {
                    if (enableSignal) {
                        ir_module.beep();
                    }
                    lastKeyPressed = buttonCode;
                    selectedAction.runAction();
                }
                break;
            }
        }
    }
}
