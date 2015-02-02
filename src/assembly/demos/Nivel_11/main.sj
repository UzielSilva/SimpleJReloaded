final BOTON_ESPACIO = 64;

var sonido = false;
var haciendoSonido = false;
var contadorSonido;

setSoundAttack(0, 150);
setSoundDecay(0, 850);

sfi() {
  if (haciendoSonido) {
    setSoundFrequency(0, contadorSonido * 200 + 10000);
    contadorSonido++;
    if (contadorSonido == 25) {
      haciendoSonido = false;
      sonido = false;
      soundOff(0);
    }
  } else {
    if (sonido) {
      haciendoSonido = true;
      contadorSonido = 0;
      setSoundFrequency(0, 10000);
      soundOn(0);
    }
  }
}

showAt("Apoya la barra de espacio", 3, 10);
showAt("para escuchar un sonido", 3, 11);

while (true) {
  if (readCtrlOne() == BOTON_ESPACIO && sonido == false)
    sonido = true;
}