import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.Closeable;
import java.util.ArrayList;

/**
 * Данный класс представляет собой обработчик модуля инфракрасного приёмника для ИК - пультов.
 * Модуль инфракрасного приёмника представляет собой плату Arduino Nano c ИК - детектором и миниатюрным динамиком.
 * Микроконтроллер передаёт данные через UART (последовательный порт TTL - уровня), а они, в свою очередь, принимаются
 * установленной на плате микросхемой - конвертором, которая подключается к компьютеру с помощью USB.
 * В конечном итоге, в системе данные можно принять из виртуального эмулированного COM - порта.
 * Данный класс использует библиотеку JSSC для работы с последовательным портом. Это библиотека с открытым исходным кодом,
 * позволяющая организовать многопоточную асинхронную работу с COM - портами.
 * Класс принимает данные с модуля, позволяет подключить обработчики событий нажатия на кнопки, принимать одиночные нажатия,
 * чтобы организовать, например, ввод определённх ожидаемых клавиш.
 * Содержит методы, позволяющие полноценно работать с модулем, а также реализует интерфейс Closeable, поэтому его можно
 * использовать в конструкциях try с ресурсами.
 */
class IR_moduleConnection implements SerialPortEventListener, Closeable {

    /*
    ********Стандартные мелодии********
     */
    public MonofonicComposition melody_connected;
    public MonofonicComposition melody_standartSignal;
    public MonofonicComposition melody_doubleSignal;
    public MonofonicComposition melody_confirmationSignal;
    private boolean isConnected = false;
    private boolean waitForClickMode = false;
    private String clickResult = "";
    private SerialPort serialPort;
    private ArrayList<ButtonPressedEventListener> buttonPressedEventListeners;
    private volatile long disableReceivingEndTime = 0;

    /**
     * Конструктор. Открывает COM - порт на скорости 115200 бод, подключается и ждёт ответа от устройства.
     *
     * @param portName                    Название COM - порта, через который будет осуществлено подключение
     * @param buttonPressedEventListeners Список объектов, реализующих интерфейс с обработчиками нажатий на кнопки.
     */
    private IR_moduleConnection(String portName, ArrayList<ButtonPressedEventListener> buttonPressedEventListeners) {
        fillStantartMelodies();
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

    /**
     * Конструктор. Просто открывает COM - порт на скорости 115200 бод, подключается и ждёт ответа от устройства.
     * Создаёт пустой список обработчиков событий.
     * @param portName Название COM - порта, через который будет осуществлено подключение
     */
    IR_moduleConnection(String portName) {
        this(portName, new ArrayList<>());
    }

    /**
     * Метод конвертирует массив байтов в строку, представляя каждый байт, как символ.
     * @param buf входной массив байтов, который необходимо конвертировать
     * @return Строка, состоящая из символов, которые представляют каждый отдельный элемент.
     */
    private static String convertByteArrayToANSIStr(byte[] buf) {
        StringBuilder builder = new StringBuilder();
        for (byte readedByte : buf) {
            builder.append((char) readedByte);
        }
        return builder.toString();
    }

    /**
     * Это метод, который вызывается обработчиком соединения com - порта.
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR() && event.getEventValue() > 1) {
            try {
                String receivedStr = convertByteArrayToANSIStr(serialPort.readBytes(event.getEventValue()));
                if (System.currentTimeMillis() > disableReceivingEndTime) {
                    switch (receivedStr) {
                        case "DEVICE_ACTIVE":
                            playSignal(melody_connected);
                            isConnected = true;
                            break;
                        default:
                            if (waitForClickMode) {
                                clickResult = receivedStr;
                                waitForClickMode = false;
                                playSignal(melody_standartSignal);
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

    /**
     * Временно приостанавливает обработку нажатий на @code{millis} миллисекунд
     * @param millis Время, на которое будет приостановлена обработка данных с модуля.
     */
    void pauseReceivingFor(long millis) {
        disableReceivingEndTime = System.currentTimeMillis() + millis;
    }

    /**
     * Метод возобновляет обработку нажатий
     */
    public void resumeReceiving() {
        disableReceivingEndTime = 0;
    }

    /**
     * Метод добавляет объект, реализующий интерфейс @code{ButtonPressedEventListener}, представляющий собой обработчик событий
     * связанных с нажатием на кнопки, в список.
     * @param listener объект, реализующий интерфейс @code{ButtonPressedEventListener}
     */
    void attachButtonEventListener(ButtonPressedEventListener listener) {
        detachButtonEventListener(listener); // Знаю, скорости не добавит. Но это избавит от клонирования записей
        buttonPressedEventListeners.add(listener);
    }

    /**
     * Метод удаляет объект, реализующий интерфейс @code{ButtonPressedEventListener}, представляющий собой обработчик событий
     * связанных с нажатием на кнопки, из списка.
     * @param listener объект, реализующий интерфейс @code{ButtonPressedEventListener}
     */
    private void detachButtonEventListener(ButtonPressedEventListener listener) {
        for (ButtonPressedEventListener selectedListener : buttonPressedEventListeners) {
            if (selectedListener == listener) {
                buttonPressedEventListeners.remove(selectedListener);
                break;
            }
        }
    }

    /**
     * @return true, если обработчик находитсся в режиме ожидания одиночного нажатия клавиши и false в обратном случае.
     */
    boolean isWaitingForClick() {
        return waitForClickMode;
    }

    /**
     * Метод переводит обработчик в режим ожидания одиночного нажатия на клавишу.
     */
    void turnToWaitToClickMode() {
        this.waitForClickMode = true;
    }

    /**
     * @return результат нажатия на клавишу, проошедшего в режиме ожидания одиночного нажатия.
     */
    String getClickResult() {
        return clickResult;
    }

    /**
     * @return true, если модуль идентефицирован и подключён, false во всех остальных случаях.
     */
    boolean isConnected() {
        return isConnected;
    }

    /**
     * @return название COM - порта, через который работает подключение.
     */
    SerialPort getSerialPort() {
        return serialPort;
    }

    /**
     * Посылает на модуль мелодию для воспроизведения на PC SPEAKER. Используется для уведомительных целей.
     */
    void playSignal(MonofonicComposition composition) {
        try {
            serialPort.writeString("PLAY:" + composition + "\n");
        } catch (SerialPortException ignored) {}
    }

    /**
     * Заполняет стандартные контейнеры мелодий для их последующего воспроизведения через PC SPEAKER устройства.
     */
    void fillStantartMelodies() {
        melody_connected = new MonofonicComposition();
        melody_standartSignal = new MonofonicComposition(1700, 40);
        melody_doubleSignal = new MonofonicComposition();
        melody_confirmationSignal = new MonofonicComposition();

        melody_connected.getCompositionSheet().add(new MonofonicNote(659, 82));
        melody_connected.getCompositionSheet().add(new MonofonicNote(784, 82));
        melody_connected.getCompositionSheet().add(new MonofonicNote(1046, 82));
        melody_connected.getCompositionSheet().add(new MonofonicNote(1318, 82));

        melody_doubleSignal.getCompositionSheet().add(new MonofonicNote(1200, 100));
        melody_doubleSignal.getCompositionSheet().add(new MonofonicNote(0, 60));
        melody_doubleSignal.setRepeatationCount(2);

        melody_confirmationSignal.getCompositionSheet().add(new MonofonicNote(784, 82));
        melody_confirmationSignal.getCompositionSheet().add(new MonofonicNote(987, 82));
    }

    @Override
    public void close() {
        try {
            serialPort.closePort();
        } catch (SerialPortException ignored) {}
    }
}
