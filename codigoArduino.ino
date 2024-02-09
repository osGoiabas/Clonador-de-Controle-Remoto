#define IRpin_PIN PIND
#define IRpin 2
#define IRledPin 13
#define MAXPULSE 65000
#define RESOLUTION 20

// IFZAO
uint8_t modo = 0;
int total = 0;
boolean cabou = false;
boolean sabeTotal = false;

uint16_t pulses[100][2]; // 100 pares de ON e OFF
uint8_t currentpulse = 0; // indice dos pulsos que estamos guardando

void setup() {
  pinMode (IRledPin, OUTPUT);     //pin do Led setado
  digitalWrite(IRledPin, LOW);    //Led começa desligado
  Serial.begin(9600);             //Inicia Serial port
}

void loop() {               //INICIO DO LOOP----------------------------------

                                
//la em cima tem: "int modo"
  while (!Serial.available() > 0){}
  if (Serial.available() > 0){
      modo = Serial.read();
      switch(modo){
        case 0:   //MODO NEUTRO
          break;
        case 1:   //MODO SENSOR
          whiles();
          break;
        case 3:   //MODO RECEBE ARRAY
          recebeArray();
          break;
      }
  }
}                           //FIM DO LOOP-------------------------------------



//*************************************************************************** Enviar Array pelo Serial


void whiles(){
  cabou = false;
  int dados = 0;
  while (!Serial.available() > 0){}
  if (Serial.available() > 0){   //verifica se existe comunicacao com a porta serial
      dados = Serial.read();    //le os dados da porta serial
  }
  
  while (dados == 2){                      //INICIO DO WHILE 3 -------------------------------------
      uint16_t highpulse, lowpulse; // temporary storage timing
      highpulse = lowpulse = 0; // comeca zerado
        
      // WHILE SENSOR 1 (ON)
      while (IRpin_PIN & (1 << IRpin)) {      // enquanto o sensor estiver mandando
        highpulse++;                          // highpulse eh 1 agora
        delayMicroseconds(RESOLUTION);        // espera mais um pouquinho
      
        // If the pulse is too long, we 'timed out' - either nothing
        // was received or the code is finished, so print what
        // we've grabbed so far, and then reset
        
        if ((highpulse >= MAXPULSE) && (currentpulse != 0)) {
          printpulses(); //imprima os pulsos
          currentpulse=0;
          return;
        }
      }  
      // we didn't time out so lets stash the reading
      pulses[currentpulse][0] = highpulse; //
    
      if (!cabou){
        // WHILE SENSOR 0 (OFF)
        while (! (IRpin_PIN & _BV(IRpin))) {    // enquanto o sensor NÃO estiver mandando
           // pin esta LOW
           lowpulse++; //lowpulse eh 1 agora
           delayMicroseconds(RESOLUTION);
           if ((lowpulse >= MAXPULSE) && (currentpulse != 0)) {
             printpulses();
             currentpulse=0;
             return;
           }
        }
        pulses[currentpulse][1] = lowpulse;
      
        // conseguimos ler um pulso ON-OFF com sucesso, vamos continuar!
        currentpulse++;
      }
  }                                       //FIM DO WHILE 3-------------------------------------
}



//MANDA OS DADOS PRO SERIAL
void printpulses(void) {
  for (uint8_t i = 0; i < currentpulse-1; i++) {
    //Serial.print("\t"); // tab
    Serial.print(pulses[i][1] * RESOLUTION / 10, DEC);
    Serial.print(",");
    Serial.print(pulses[i+1][0] * RESOLUTION / 10, DEC);
    Serial.print(",");
  }
  //Serial.print("\t"); // tab
  Serial.print(pulses[currentpulse-1][1] * RESOLUTION / 10, DEC);
  
  Serial.println(",0");
  cabou = true;
}

//*************************************************************************** RECEBE ARRAY
void recebeArray(){
  int dadosArr = 0;
  while (!Serial.available() > 0){}
  if (Serial.available() > 0){   //verifica se existe comunicacao com a porta serial
      dadosArr = Serial.read();    //le os dados da porta serial
  }
  
  if (dadosArr == 4){
    
    total = 0;
    sabeTotal = false;
    while (!Serial.available() > 0){} 
    total = Serial.read();
    
    sabeTotal = true;
    if (sabeTotal){
      boolean passouFor = false;
      int arraySize = total*2;
        
      int IRsinalBR[arraySize+1];
      while (!Serial.available() > 0){}
      if(Serial.available() > 0){
        for (uint8_t i=0; i<arraySize; i++){
          while (!Serial.available() > 0){}
            IRsinalBR[i] = Serial.read();
            passouFor = true;
        }
        IRsinalBR[arraySize] = 0;
      }
      if (passouFor){
     
          //**************************************************** RECEBE ERROS !!!!!
          while(! Serial.available() > 0){}
          if(Serial.read() == 8){
            while(! Serial.available() > 0){}
            int numErros = Serial.read();
            int posicoesErros[numErros];
            int correcoes[numErros];
            
            for (uint8_t i=0; i < numErros; i++){
              while(! Serial.available() > 0){}
              posicoesErros[i] = Serial.read();
            }    
            for (uint8_t i=0; i < numErros; i++){
              while(! Serial.available() > 0){}
              byte upper = Serial.read();
              while(! Serial.available() > 0){}
              byte lower = Serial.read();
              correcoes[i] = (upper<<8) | lower; //Reassemble the number
            }
        
            for (uint8_t i=0; i<numErros; i++){
              IRsinalBR[posicoesErros[i]] = correcoes[i];
            }
            //**************************************************** FIM DE RECEBE ERROS !!!!!
            
            //delay(1000);
            //*************************************************************************** PISCAR LED
            for (int i = 0; i < total; i+=2) {        //Loop through all of the IR timings
               pulseIR(IRsinalBR[i]*10);              //Flash IR LED at 38khz for the right amount of time
               delayMicroseconds(IRsinalBR[i+1]*10);  //Then turn it off for the right amount of time
            }
            //*************************************************************************** FIM DE PISCAR LED
            
          }

          Serial.println ("Arduino: ");
          for (uint8_t i=0; i<arraySize+1; i++){
            Serial.print(IRsinalBR[i]);
            Serial.print(",");
          }
          Serial.println("");
      }
      sabeTotal = false;
    }
  }    
}
//*************************************************************************** FIM DE RECEBE ARRAY

//************************************************************************************* PULSA O LED


void pulseIR(long microsecs) { //essa funcao roda enquanto houverem microsecs
  cli();  // this turns off any background interrupts
  while (microsecs > 0) {
    // 38 kHz eh em torno de 13 microsegundos high e 13 microsegundos low
   digitalWrite(IRledPin, HIGH);  // demora uns 3 microsegundos
   delayMicroseconds(10);         // se nao funcionar, pode colocar 9
   digitalWrite(IRledPin, LOW);   // demora uns 3 microsegundos
   delayMicroseconds(10);         // se nao funcionar, pode colocar 9
   // passaram 26 microsegs
   microsecs -= 26;
  }
  sei();  // this turns them back on
}

