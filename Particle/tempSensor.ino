


double temperature = 0.0;

void setup()
{



    // register API variable
    //Particle.variable("temperature", &temperature, DOUBLE);

    pinMode(A5, INPUT);
}

void loop()
{
    delay(1000);
    int analogValue = analogRead(A5);
    double voltage = 3.3 * ((double)analogValue / 4095.0);


    temperature = (voltage - 0.5) * 100;
    String conTemp = String(temperature, 2);
    Spark.publish("temperature",conTemp);


}