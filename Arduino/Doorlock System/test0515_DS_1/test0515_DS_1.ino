#include <Keypad.h>
#include <EEPROM.h>
#include <SoftwareSerial.h>

// Keypad

const byte ROWS = 4;
const byte COLS = 3;

char keys[ROWS][COLS] = {
  {'1', '2', '3'},
  {'4', '5', '6'},
  {'7', '8', '9'},
  {'*', '0', '#'}
};

String keyValue;
String passwd = "";

byte rowPins[ROWS] = {10, 9, 8, 7};
byte colPins[COLS] = {6, 5, 4};

Keypad keypad = Keypad(makeKeymap(keys), rowPins, colPins, ROWS, COLS);


// Speaker

int speakerPin = 11;


// LED

const uint8_t digitLED[4] = {A1, A2, A3, A4};
int nod = 0; //자릿수

const uint8_t resetLED = 13;


// Button

const uint8_t openKey = A0;
int resetKey = 12;


// Bluetooth

int btRx = 2;
int btTx = 3;

String btTemp;

SoftwareSerial bt(2, 3);


// EEPROM

String defaultTmp;
String changeTmp;
String fpTmp;


// Solenoid

const uint8_t lockKey = A5;


// Main

void setup() {
  Serial.begin(9600);
  //초기 비밀번호(EEPROM 0~3 공간의 값) 생성
  for (int i = 0; i < 4; i++) {
    EEPROM.write(i, 0);
  }
  for (int i = 0; i < 4; i++) {
    defaultTmp += EEPROM.read(i);
  }
  for (int i = 8; i < 12; i++) {
    EEPROM.write(i, 'A');
  }
  for (int i = 8; i < 12; i++) {
    fpTmp += (char) EEPROM.read(i);
  }
  //get_change_password();
  for (int i = 0; i < 4; i++) {
    pinMode(digitLED[i], OUTPUT);
  }
  pinMode(openKey, INPUT);
  pinMode(resetKey, INPUT);
  pinMode(resetLED, OUTPUT);
  pinMode(lockKey, OUTPUT);
  bt.begin(38400);
}

void loop() {
  char key = keypad.getKey();
  
  int openButton = digitalRead(openKey);
  int resetButton = digitalRead(resetKey);
  
  //키 입력 시
  if (key) {
    //키가 #일 경우
    if (key == '#') {
      get_change_password();
      Serial.println(keyValue);
      Serial.println(">> Default Password : " + defaultTmp);
      if (keyValue.length() == 4) {
        if (keyValue == defaultTmp) {
          correct_password();
        } else {
          wrong_password();
        }
      } else {
        Serial.println(">> " + keyValue + " : Wrong");
        wrong_password();
      }
    }
    //키가 숫자일 경우
    else if (isdigit(key)) {
      if (keyValue.length() > 3) {
        Serial.println(">> " + keyValue + " : Wrong");
        wrong_password();
      }
      else {
        click_key();
        keyValue += key;
        digitalWrite(digitLED[nod], HIGH);
        nod++;
      }
    }
    //키가 *일 경우
    else {
      if (digitalRead(resetLED) == HIGH) {
        if (keyValue.length() == 4) {
          digitalWrite(resetLED, LOW);
          save_password(keyValue);
          change_password();
        } else {
          Serial.println(">> Please click 4 number.");
          wrong_password();
        }
      } else {
        Serial.println(">> Please click reset button.");
        wrong_password();
      }
    }
  }
  
  //열림 버튼 클릭 시
  if (openButton == LOW){ 
    positive_signal();
    get_change_password();
    correct_password();
    String fpValue;
    for (int i = 8; i < 12; i++) {
      fpValue += (char) EEPROM.read(i);
    }
    Serial.println(">> Default Password : " + defaultTmp);
    Serial.println(">> Change Password : " + changeTmp);
    Serial.println(">> FingerPrint Value : " + fpValue);
    Serial.println(">> BT Value : " + btTemp);
    btTemp = "";
  } else {
  }
  
  //리셋 버튼 클릭 시
  if (resetButton == HIGH){ 
    reset_password();
    digitalWrite(resetLED, HIGH);
  }

  //블루투스
  /*if (bt.available()) {
    //Serial.write(bt.read());
    Serial.println(bt.read() + " : " + btValue);
  }
  if (Serial.available()) {
    bt.write(Serial.read());
    Serial.println("Serial.available()");
  }*/
  btTemp = "";
  while (bt.available()) {
    Serial.println("fpTmp : " + fpTmp);
    char buffer0 = (char) bt.read();
    btTemp += buffer0;
    if (buffer0 == '\n') {
      Serial.println("Bluetooth Value : " + btTemp);
    }
    if (btTemp == fpTmp) {
        Serial.println("Finger Print is Correct!");
        correct_password();
    }
  }
  
}


// Function

void positive_signal() {
  tone(speakerPin, 524);
  delay(250);
  tone(speakerPin, 392);
  delay(250);
  noTone(speakerPin);
}
 
void negative_signal() {
  tone(speakerPin, 330);
  delay(250);
  noTone(speakerPin);
}  

void click_key() {
  tone(speakerPin, 130);
  delay(100);
  noTone(speakerPin);
}

void wrong_password() {
  keyValue = "";
  negative_signal();
  digit_led_off();
  Serial.println(">> Wrong Password");
}

void correct_password() {
  keyValue = "";
  positive_signal();
  digit_led_off();
  // 솔레노이드 작동 함수
  digitalWrite(lockKey, HIGH);
  delay(2000);
  digitalWrite(lockKey, LOW);
  Serial.println(">> Correct Password");
}

void reset_password() {
  keyValue = "";
  negative_signal();
  digit_led_off();
  Serial.println(">> Reset Activated");
}

void change_password() {
  keyValue = "";
  positive_signal();
  digit_led_off();
  Serial.println(">> Password Change");
}

void save_password(String pswd) {
  int pswdNum = pswd.toInt();
  int firstValue = pswdNum/1000;
  pswdNum -= (firstValue*1000);
  int secondValue = pswdNum/100;
  pswdNum -= (secondValue*100);
  int thirdValue = pswdNum/10;
  pswdNum -= (thirdValue*10);
  int fourthValue = pswdNum;
  
  EEPROM.write(4, firstValue);
  EEPROM.write(5, secondValue);
  EEPROM.write(6, thirdValue);
  EEPROM.write(7, fourthValue);
}

void digit_led_off() {
  for (int i = 0; i < 4; i++) {
    digitalWrite(digitLED[i], LOW);
  }
  nod = 0;
}

void get_change_password() {
  changeTmp = "";
  for (int i = 4; i < 8; i++) {
    changeTmp += EEPROM.read(i);
  }
  if (changeTmp.length() == 4) {
    defaultTmp = changeTmp;
  }
}
