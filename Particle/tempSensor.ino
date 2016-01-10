

#define PRINTF_BUFFER_SIZE 128
void Serial_printf(const char* fmt, ...) {
   char buff[PRINTF_BUFFER_SIZE];
   va_list args;
   va_start(args, fmt);
   vsnprintf(buff, PRINTF_BUFFER_SIZE, fmt, args);
   va_end(args);
   Serial.println(buff);
}

double temperature = 0.0;

void setup()
{
  // Use serial port for debug print
  Serial.begin(9600);

  // register API variable
  Particle.variable("temperature", &temperature, DOUBLE);
  pinMode(A5, INPUT);
}

void loop()
{
  int analogValue = analogRead(A5);
  double voltage = 3.3 * ((double)analogValue / 4095.0);

  // http://www.analog.com/static/imported-files/data_sheets/TMP35_36_37.pdf
  temperature = (voltage - 0.5) * 100;
  Serial_printf("voltage=%g temperature=%g", voltage, temperature);
  delay(500);
}
