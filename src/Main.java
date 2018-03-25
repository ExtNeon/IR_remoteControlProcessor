import jssc.SerialPortList;

import java.util.Scanner;

/**
 * Created by Кирилл on 25.03.2018.
 */
public class Main implements ButtonPressedEventListener {

    final private static long MAX_CONNECTION_WAIT_TIMEOUT = 3000;
    final private static String HELP_STR = "attach key - привязать определённую клавишу к действию";

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Список доступных портов:");
        printAvailablePorts();
        IR_moduleConnection ir_module = null;
        System.out.println("Поиск и подключение...");
        while (ir_module == null) {
            if (SerialPortList.getPortNames().length > 0) {
                for (String selectedPort : SerialPortList.getPortNames()) {
                    long connection_startedTime = System.currentTimeMillis();
                    ir_module = new IR_moduleConnection(selectedPort);
                    while (System.currentTimeMillis() - connection_startedTime < MAX_CONNECTION_WAIT_TIMEOUT
                            && !ir_module.isConnectedToIR_receiver()) {
                        Thread.sleep(100); //Ждём до тех пор, пока не будет осуществлено подключение, либо пока не выйдет время.
                    }
                    if (ir_module.isConnectedToIR_receiver()) {
                        System.out.println("Устройство найдено и готово к работе!\nПорт: " + ir_module.getSerialPort().getPortName());
                        break;
                    } else {
                        ir_module.close();
                        ir_module = null;
                    }
                }
            }
        }
        System.out.println("Доступные команды:\n" + HELP_STR);
        while (ir_module != null) {
            if (getEnteredString("\r_> ").equals("attach key")) {
                boolean sucessfulAttached;
                do {
                    System.out.println("Нажмите клавишу на пульте дистанционного управления...");
                    ir_module.setWaitForClick(true);
                    while (ir_module.isWaitForClick()) {
                        Thread.sleep(300); //Ждём до тех пор, пока юзер не нажмёт на кнопку на пульте
                    }
                    String buttonGettedCode = ir_module.getClickResult();
                    System.out.println("Код клавиши получен: " + buttonGettedCode + "\n" +
                            "Повторите нажатие, чтобы удостовериться в правильности установки...");
                    Thread.sleep(500); //Ждём, чтобы не получить этот же сигнад
                    ir_module.setWaitForClick(true);
                    while (ir_module.isWaitForClick()) {
                        Thread.sleep(300); //Ждём до тех пор, пока юзер не нажмёт на кнопку на пульте
                    }
                    sucessfulAttached = ir_module.getClickResult().equals(buttonGettedCode);
                    if (!sucessfulAttached) {
                        System.out.println("Ошибка! Полученный код клавиши отличается от первоначального. Повторите попытку.");
                    }
                } while (!sucessfulAttached);
                System.out.println("Данные корректны!");
            }
        }
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

    private static String getEnteredString(String message) {
        System.out.print(message);
        return new Scanner(System.in).nextLine();
    }

    private static int getEnteredIntegerNumber(String message) {
        System.out.print(message);
        return new Scanner(System.in).nextInt();
    }

    @Override
    public void ButtonPressed(String buttonCode) {

    }
}
