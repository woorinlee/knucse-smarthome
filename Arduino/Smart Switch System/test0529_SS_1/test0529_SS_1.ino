#include <EEPROM.h>
#include <SoftwareSerial.h>

// LED

int upLED = 6;
int downLED = 7;

int upLight = 12;
int downLight = 13;


// Button

int upBtn = 4;
int downBtn = 5;


// 조도 센서

const uint8_t readLight = A0;


// Bluetooth

SoftwareSerial bt(2, 3);
String btTemp;


void setup()
{
  // 시리얼통신 시작 
  Serial.begin(9600);
  
  //LED, 버튼 초기 설정
  
  pinMode(upBtn, INPUT);
  pinMode(downBtn, INPUT);
  
  pinMode(upLED, OUTPUT);
  pinMode(downLED, OUTPUT);
  
  pinMode(upLight, OUTPUT);
  pinMode(downLight, OUTPUT);
  
  //EEPROM 설정
  EEPROM.write(0, 0);
  EEPROM.write(1, 0);
  
  char ssCode[] = "SWST";
  
  for (int i = 2; i < 6; i++) {
    EEPROM.write(i, ssCode[i-2]);
  }
  
  //LED 기본 설정 (꺼짐)
  digitalWrite(upLED, LOW);
  digitalWrite(downLED, LOW);
  digitalWrite(upLight, LOW);
  digitalWrite(downLight, LOW);
  
  //Bluetooth 설정
  bt.begin(38400);
}

void loop()
{
  int upLightStatus = EEPROM.read(0);
  int downLightStatus = EEPROM.read(1);
  
  int upButton = digitalRead(upBtn);
  int downButton = digitalRead(downBtn);
  
  //조도 센서
  int readLightValue = analogRead(readLight);
  
  //방이 어두우면 스위치 LED를 켜고, 밝으면 끔
  if (readLightValue > 200) {
    digitalWrite(upLED, HIGH);
    digitalWrite(downLED, HIGH);
  } else {
    digitalWrite(upLED, LOW);
    digitalWrite(downLED, LOW);
  }
  
  //위 버튼 클릭 시
  if (upButton == HIGH) {
    onUpLight(upLightStatus);
  }
  
  //아래 버튼 클릭 시
  if (downButton == HIGH) {
    onDownLight(downLightStatus);
  }
  
  //Bluetooth 연결
  String systemCode = callSystemCode();
  btTemp = "";
  while (bt.available()) {
    char buffer0 = (char) bt.read();
    btTemp += buffer0;
    if (buffer0 == '\n') {
      Serial.println("Bluetooth Value : " + btTemp);
    }
    if (btTemp.startsWith(systemCode)) {
        if (btTemp.endsWith("UP")) {
          onUpLight(upLightStatus);
        } else if (btTemp.endsWith("DW")) {
          onDownLight(downLightStatus);
        }
    }
  }
}

void onUpLight(int upLightStatus) {
  Serial.println(">> Up Button Click");
    //위 LED 상태가 0일 때 불을 켜고 위 LED 상태를 1로 변경
    if (upLightStatus == 0) {
      digitalWrite(upLight, HIGH);
      Serial.println(">> Up Light On");
      EEPROM.write(0, 1);
      delay(200);
      //Serial.println(tmp);
    } else {
      digitalWrite(upLight, LOW);
      Serial.println(">> Up Light Off");
      EEPROM.write(0, 0);
      delay(200);
    }
}

void onDownLight(int downLightStatus) {
  Serial.println(">> Down Button Click");
    //아래 LED 상태가 0일 때 불을 켜고 아래 LED 상태를 1로 변경
    if (downLightStatus == 0) {
      digitalWrite(downLight, HIGH);
      Serial.println(">> Down Light On");
      EEPROM.write(1, 1);
      delay(200);
    } else {
      digitalWrite(downLight, LOW);
      Serial.println(">> Down Light Off");
      EEPROM.write(1, 0);
      delay(200);
    }
}

String callSystemCode() {
  String codeTmp;
  for (int i = 2; i < 6; i++) {
    codeTmp += (char) EEPROM.read(i);
  }
  return codeTmp;
}
