#include <AESLib.h>
#include <EEPROM.h>
#include <SoftwareSerial.h>
#include <LiquidCrystal.h>

LiquidCrystal lcd(13, 12, 11, 10, 9, 8);
SoftwareSerial BLESerial(0,1);

long randomValue;
char datapacket[20];
char s_key[6];
char ms_key[16];
bool isCreated = false;
bool isSent = false;
bool isDebug = true;
byte dataReceived[20];

void setup() {
  // put your setup code here, to run once:
Serial.begin(9600);
//Serial.setTimeout(1000);
BLESerial.begin(9600);
//BLESerial.setTimeout(1000);
randomSeed(analogRead(0));
}

void loop() {
  // put your main code here, to run repeatedly:
  if (Serial.available()) {
    //Serial.setTimeout(100);
    int numLength = 20;
    Serial.readBytes(dataReceived, numLength);
    //Check if send connect require

    if(dataReceived[0] == 0xA0){//Create s_key
      createSessionKey();
      createPacket(s_key, sizeof(s_key));
      sendData();
      //memset(datapacket, 0, sizeof(datapacket));
      //datapacket = s_key;
      isSent = false;
    }else if(dataReceived[0] == 0xA1) {
      readData(ms_key,0,16);
      createPacket(ms_key, sizeof(ms_key));
      sendData();
      isSent = false;
    }else if(dataReceived[0] == 0xA3) {//Create ms_key
      createMasterKey();
      writeData(ms_key, 0, 16);
      createPacket(ms_key, sizeof(ms_key));
      sendData();
//      lcd.clear();
//      lcd.setCursor(0, 1);
//      lcd.print(ms_key);
//      delay(1000);
      isSent = false;
    }
  }
//  if(!isSent)
//  {
//    isSent = sendData();
//  }
//  
}
bool createPacket(char data[], int len){
  if(len>20)
    return false;
  for(int i=0; i<20; i++){
    if(i<len)
      datapacket[i] = data[i];
    else
      datapacket[i] = char(0);
  }
  return true;
}

bool sendData(){
  
  if (BLESerial.available()) {
    Serial.print(datapacket);
    lcd.clear();
    lcd.setCursor(0, 1);
    lcd.print(">:");
    lcd.print(datapacket);
    delay(1000);
    return true;
  }
  return false;
}

void createSessionKey(){
  lcd.clear();
  lcd.setCursor(0,1);
  lcd.print("create s_key");
  delay(1000);
  randomSeed(analogRead(0));
  for(int i=0; i < 6; i++)
  {
    randomValue = random(0,61);
    char letter = randomValue + 'a';
    if(randomValue > 25 && randomValue < 36)
      letter = (randomValue - 26) + '0';
    if(randomValue > 35)
      letter = (randomValue - 36) + 'A';
    s_key[i]=letter;
   }
}

void createMasterKey()
{
  lcd.clear();
  lcd.setCursor(0,1);
  lcd.print("create m_key");
  delay(1000);
  for(int i=0; i < 16; i++)
  {
    randomValue = random(0,61);
    char letter = randomValue + 'a';
    if(randomValue > 25 && randomValue < 36)
      letter = (randomValue - 26) + '0';
    if(randomValue > 35)
      letter = (randomValue - 36) + 'A';
    ms_key[i]=letter;
   }
}
  
void writeData(char data[],int pos, int len){
  if(len > 512 || pos > 512 || pos < 0 || (len + pos > 512))
    return;
  lcd.clear();
  lcd.setCursor(0,1);
  lcd.print("Writing...");
  delay(500);
  for(int i=pos; i < len + pos; i++)
  {
    char c = data[i];
    EEPROM.write(i, c);
    delay(5);
  }
}

void readData(char *m_array,int pos, int len){
  if(len > 512 || pos > 512 || pos < 0 || (len + pos > 512))
    return;
//  char new_data[len];
  lcd.clear();
  lcd.setCursor(0,1);
  lcd.print("reading...");
  delay(1000);
  for(int i=pos; i<len + pos; i++)
  {
    int value;
    value = EEPROM.read(i);
    delay(5);
    m_array[i] = value;
  }
//  s_key = new_key;
  //lcd.print(s_key);
  //delay(1000);
//  return new_key;
}
