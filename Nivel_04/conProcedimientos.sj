/* Constantes para los tiles del personaje el espacio en blanco */
final PERSONAJE = 20;
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
final MIN_Y = 0;
final MAX_Y = 23;

/* La posicion del personaje en la pantalla */
var xPersonaje = 16;   // posicion horizontal
var yPersonaje = 12;   // posicion vertical

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

// Ponemos el fondo de color negro
setBackground(0, 0, 0);

// Mostramos las instrucciones
showAt("Usa las flechas del teclado", 1, 4);
showAt("para mover al personaje por la", 1, 5);
showAt("pantalla.", 1, 6);

// Para "siempre"
while (true) {
  dibujaPersonaje();
  pause(0.1);
  borraPersonaje();
  var boton = readCtrlOne();
  muevePersonaje(boton);
}