import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.Closeable;
import java.util.ArrayList;

/**
 * Created by Кирилл on 25.03.2018.
 */
class IR_moduleConnection implements SerialPortEventListener, Closeable {

    private boolean isConnected = false;
    private boolean waitForClickMode = false;
    private String clickResult = "";
    private SerialPort serialPort;
    private ArrayList<ButtonPressedEventListener> buttonPressedEventListeners;
    private volatile long disableReceivingEndTime = 0;

    IR_moduleConnection(String portName, ArrayList<ButtonPressedEventListener> buttonPressedEventListeners) {
        serialPort = new SerialPort(portName);
        this.buttonPressedEventListeners = buttonPressedEventListeners;
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(this);
        } catch (SerialPortException ignored) {

        }
    }

    IR_moduleConnection(String portName) {
        this(portName, new ArrayList<>());
    }

    private static String convertByteArrayToANSIStr(byte[] buf) {
        StringBuilder builder = new StringBuilder();
        for (byte readedByte : buf) {
            builder.append((char) readedByte);
        }
        return builder.toString();
    }

    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR() && event.getEventValue() > 1) {
            try {
                String receivedStr = convertByteArrayToANSIStr(serialPort.readBytes(event.getEventValue()));
                if (System.currentTimeMillis() > disableReceivingEndTime) {
                    switch (receivedStr) {
                        case "DEVICE_ACTIVE":
                            serialPort.writeString("CONNECTED\n");
                            isConnected = true;
                            break;
                        default:
                            if (waitForClickMode) {
                                clickResult = receivedStr;
                                waitForClickMode = false;
                                beep();
                            } else {
                                for (ButtonPressedEventListener selectedListener : buttonPressedEventListeners) {
                                    selectedListener.buttonPressed(receivedStr, this);
                                }
                            }
                    }
                }
            } catch (SerialPortException ignored) {
            }
        }
    }

    public void pauseReceivingFor(long millis) {
        disableReceivingEndTime = System.currentTimeMillis() + millis;
    }

    public void resumeReceiving() {
        disableReceivingEndTime = 0;
    }

    void attachButtonEventListener(ButtonPressedEventListener listener) {
        detachButtonEventListener(listener); // Знаю, скорости не добавит. Но это избавит от клонирования записей
        buttonPressedEventListeners.add(listener);
    }

    void detachButtonEventListener(ButtonPressedEventListener listener) {
        for (ButtonPressedEventListener selectedListener : buttonPressedEventListeners) {
            if (selectedListener == listener) {
                buttonPressedEventListeners.remove(selectedListener);
                break;
            }
        }
    }

    boolean isWaitingForClick() {
        return waitForClickMode;
    }

    void turnToWaitToClickMode() {
        this.waitForClickMode = true;
    }

    String getClickResult() {
        return clickResult;
    }

    boolean isConnected() {
        return isConnected;
    }

    SerialPort getSerialPort() {
        return serialPort;
    }

    void beep() {
        try {
            serialPort.writeString("BEEP\n");
        } catch (SerialPortException ignored) {
        }
    }

    @Override
    public void close() {
        try {
            serialPort.closePort();
        } catch (SerialPortException ignored) {

        }
    }
}
