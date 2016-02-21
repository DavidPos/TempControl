


double temperature = 0.0;
double setTemp = 0.0;

void setup()
{



  // register API variable
  Particle.variable("temperature", &temperature, DOUBLE);
  Particle.variable("setTemp", &setTemp, DOUBLE);//will be set by app
  Particle.function("set", requiredTemp);

  pinMode(A5, INPUT);
}

void loop()
{
  delay(1000);
  int analogValue = analogRead(A5);
  double voltage = 3.3 * ((double)analogValue / 4095.0);


  temperature = (voltage - 0.5) * 100;
  String conTemp = String(temperature, 2);
  Particle.publish("temperature",conTemp);
  if(temperature < setTemp){
      Particle.publish("toohot", "Too Hot");
  }



}

int requiredTemp(String command){
    double temp = double(command);
    setTemp = temp;
    return 1;
}