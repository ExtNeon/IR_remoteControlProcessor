#include <PatternPlayer.h>
#include <toneAC.h>
#include <IRremote.h>

#define COM_BOD_VALUE 115200 //Скорость передачи данных для UART, бод.

#define IR_PIN 2 //Пин для инфракрасного датчика
#define OUT_SPEAKER_PIN 9 //Пин для пищалки
#define OUT_INDICATION_PIN 13 //Пин для внешней индикации (светодиод)

#define SPEAKER_BEEP_VOLUME 10 //Громкость кратковременного сигнала. Число от 0 до 10. 10 - максимум

word SERVICE_DELAY_BEFORE_RECEIVE_NEXT = 2; //Задержка перед следующим приёмом сигнала, миллисекунд, по умолчанию


IRrecv irrecv(IR_PIN);
PatternPlayer player(beep, stopBeep, 1);

decode_results results;
long lastCode = 0;

String serialReceiveBuf = "";

void setup()
{
  Serial.begin(COM_BOD_VALUE);
  pinMode(OUT_SPEAKER_PIN, OUTPUT);
  pinMode(OUT_INDICATION_PIN, OUTPUT);
  // Timer0 уже используется millis() - мы создаем прерывание где-то
  // в середине и вызываем ниже функцию "Compare A"
  OCR0A = 0xAF;
  TIMSK0 |= _BV(OCIE0A);
  irrecv.enableIRIn(); // Start the receiver
  Serial.print(F("DEVICE_ACTIVE"));
}

void loop() {
  while (Serial.available()) {
    char readedByte = (char) Serial.read();
    if (readedByte == '\n') {
      processCommand(serialReceiveBuf);
      serialReceiveBuf = "";
    } else {
      serialReceiveBuf += readedByte;
    }
  }
  if (irrecv.decode(&results)) {
    long currentCode = results.value;
    if (currentCode == 0xFFFFFFFF) {
      currentCode = lastCode;
    }
    Serial.print(currentCode, HEX);
    lastCode = currentCode;
    digitalWrite(OUT_INDICATION_PIN, HIGH);
    delay(10);
    digitalWrite(OUT_INDICATION_PIN, LOW);
    delay(SERVICE_DELAY_BEFORE_RECEIVE_NEXT);
    irrecv.resume();
  }
}

// Прерывание вызывается один раз в миллисекунду 
SIGNAL(TIMER0_COMPA_vect) {
 player.processStep();
}

void processCommand(String inputCommand) {
  if (inputCommand.substring(0,4) == "PLAY") {
    player.play(inputCommand.substring(inputCommand.indexOf(')')+1, inputCommand.length()), inputCommand.substring(inputCommand.indexOf('(')+1, inputCommand.indexOf(',')).toInt(), inputCommand.substring(inputCommand.indexOf(',')+1, inputCommand.indexOf(')')).toInt());
  }
}

void beep(int frequency, int duration) {
  toneAC(frequency, SPEAKER_BEEP_VOLUME, duration, true);
}

void stopBeep() {
  noToneAC();
}

