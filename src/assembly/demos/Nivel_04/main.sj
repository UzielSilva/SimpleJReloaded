/* Constantes para los tiles del personaje, la comida y
   el espacio en blanco */
final PERSONAJE = 20;
final COMIDA = 43;
final ESPACIO = 32;

/* Constantes para los "botones" de las flechas */
final BOTON_ARRIBA = 1;
final BOTON_ABAJO = 2;
final BOTON_IZQUIERDA = 4;
final BOTON_DERECHA = 8;
final BOTON_ESPACIO = 64;

/* Constantes para los limites de la pantalla */
final MIN_X = 0;
final MAX_X = 31;
final MIN_Y = 1;
final MAX_Y = 23;

/* La posicion del personaje en la pantalla */
var xPersonaje = 16;   // posicion horizontal
var yPersonaje = 12;   // posicion vertical

/* La posicion de la comida en la pantalla */
var xComida;   // posicion horizontal
var yComida;   // posicion vertical

/* La puntuacion del jugador */
var puntos = 0;

dibujaPersonaje() {
  putAt(PERSONAJE, xPersonaje, yPersonaje);
}

borraPersonaje() {
  putAt(ESPACIO, xPersonaje, yPersonaje);
}

muevePersonaje(boton) {
  if (boton == BOTON_ARRIBA && yPersonaje > MIN_Y)
    yPersonaje = yPersonaje - 1;
  if (boton == BOTON_ABAJO && yPersonaje < MAX_Y)
    yPersonaje = yPersonaje + 1;
  if (boton == BOTON_IZQUIERDA && xPersonaje > MIN_X)
    xPersonaje = xPersonaje - 1;
  if (boton == BOTON_DERECHA && xPersonaje < MAX_X)
    xPersonaje = xPersonaje + 1;
}

personajeComioComida() {
  return xPersonaje == xComida && yPersonaje == yComida;
}

dibujaComida() {
  putAt(COMIDA, xComida, yComida);
}

nuevaPosicionComida() {
  xComida = random(32);
  yComida = random(MAX_Y - MIN_Y) + MIN_Y;
}

muestraPuntos() {
  showAt("Puntos: " + puntos, 20, 0);
}

// Ponemos el fondo de color negro
setBackground(0, 0, 0);

// Mostramos las instrucciones
showAt("Usa las flechas del teclado", 1, 4);
showAt("para mover a tu personaje", 1, 5);
showAt("por la pantalla.", 1, 6);
showAt("Cada vez que te comes la", 1, 8);
showAt("comida te da un punto.", 1, 9);
showAt("Personaje:", 5, 13);
putAt(PERSONAJE, 17, 13);
showAt("Comida:", 8, 15);
putAt(COMIDA, 17, 15);
showAt("Presiona la barra de", 10, 20);
showAt("espacio para empezar", 10, 21);

while (readCtrlOne() != BOTON_ESPACIO)
  ;

clear();
nuevaPosicionComida();
dibujaComida();
muestraPuntos();
// Para "siempre"
while (true) {
  dibujaPersonaje();
  pause(0.1);
  borraPersonaje();
  var boton = readCtrlOne();
  muevePersonaje(boton);
  if (personajeComioComida()) {
    nuevaPosicionComida();
    dibujaComida();
    puntos = puntos + 1;
    muestraPuntos();
    note("C6");
  }
}