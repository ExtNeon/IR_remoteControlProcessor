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

    private boolean connectedToIR_receiver = false;
    private boolean waitForClick = false;
    private String clickResult = "";
    private SerialPort serialPort;
    private ArrayList<ButtonPressedEventListenerRecord> buttonPressedEventListenerRecords;

    public void serialEvent(SerialPortEvent event) {
        if(event.isRXCHAR() && event.getEventValue() > 1){
            try {
                String receivedStr = convertByteArrayToANSIStr(serialPort.readBytes(event.getEventValue()));
                switch (receivedStr) {
                    case "DEVICE_ACTIVE":
                        serialPort.writeString("CONNECTED\n");
                        connectedToIR_receiver = true;
                        //printToConsole("Устройство готово к работе.");
                        break;
                    default:
                        if (waitForClick) {
                            clickResult = receivedStr;
                            waitForClick = false;
                            serialPort.writeString("BEEP\n");
                        } else {
                            for (ButtonPressedEventListenerRecord currentRecord : buttonPressedEventListenerRecords) {
                                if (currentRecord.getButtonCode().equals(receivedStr)) {
                                    serialPort.writeString("BEEP\n");
                                    currentRecord.getEventListener().ButtonPressed(currentRecord.getButtonCode());
                                }
                            }
                        }
                }
            }
            catch (SerialPortException e) {
                System.out.println(e);
            }
        }
    }

    public IR_moduleConnection(String portName) {
        serialPort = new SerialPort(portName);
        buttonPressedEventListenerRecords = new ArrayList<ButtonPressedEventListenerRecord>();
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(this);
        } catch (SerialPortException ignored) {

        }
    }

    public void attachButtonEventListener(ButtonPressedEventListener listener, String buttonCode) {
        buttonPressedEventListenerRecords.add(new ButtonPressedEventListenerRecord(listener, buttonCode));
    }

    public void detachButtonEventListener(ButtonPressedEventListener listener) {
        for (ButtonPressedEventListenerRecord currentRecord : buttonPressedEventListenerRecords) {
            if (currentRecord.getEventListener() == listener) {
                buttonPressedEventListenerRecords.remove(currentRecord);
                break;
            }
        }
    }

    public boolean isWaitForClick() {
        return waitForClick;
    }

    public void setWaitForClick(boolean waitForClick) {
        this.waitForClick = waitForClick;
    }

    public String getClickResult() {
        return clickResult;
    }

    public boolean isConnectedToIR_receiver() {
        return connectedToIR_receiver;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    private static String convertByteArrayToANSIStr(byte[] buf) {
        StringBuilder builder = new StringBuilder();
        for (byte readedByte : buf) {
            builder.append((char) readedByte);
        }
        return builder.toString();
    }

    /**
     * Данный метод выводит строку в консоль, не мешая вводу пользователя (теоретически)
     * @param text Текст, что будет выведен в консоль
     */
    private static void printToConsole(String text) {
        System.out.print("\r" + text + "\n_> ");
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     * <p>
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     */
    @Override
    public void close() {
        try {
            serialPort.closePort();
        } catch (SerialPortException ignored) {

        }
    }
}
