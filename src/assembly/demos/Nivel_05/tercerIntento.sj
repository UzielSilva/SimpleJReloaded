/* Constantes para los tiles del personaje, comida y el
   espacio en blanco */
final PERSONAJE = 20;
final COMIDA = 43;
final ENEMIGO = 42;
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
var xPersonaje;
var yPersonaje;

/* La posicion de la comida en la pantalla */
var xComida;   // columna
var yComida;   // renglon

/* La posicion del enemigo en la pantalla */
var xEnemigo;
var yEnemigo;

/* Cuantas vidas le quedan al jugador */
var vidas;

/* La puntuacion del jugador */
var puntos;

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

nuevaPosicionPersonaje() {
  xPersonaje = random(32);
  yPersonaje = random(MAX_Y - MIN_Y) + MIN_Y;
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

dibujaEnemigo() {
  putAt(ENEMIGO, xEnemigo, yEnemigo);
}

borraEnemigo() {
  putAt(ESPACIO, xEnemigo, yEnemigo);
}

enemigoComioPersonaje() {
  return xEnemigo == xPersonaje &&
         yEnemigo == yPersonaje;
}

abs(n) {
  if (n < 0)
    return -n;
  return n;
}

sign(n) {
  if (n < 0)
    return -1;
  if (n > 0)
    return 1;
  return 0;
}

mueveEnemigo() {
  var dx = xPersonaje - xEnemigo;
  var dy = yPersonaje - yEnemigo;
  if (abs(dx) > abs(dy))
    xEnemigo = xEnemigo + sign(dx);
  else
    yEnemigo = yEnemigo + sign(dy);
}

muestraPuntos() {
  showAt("Puntos: " + puntos, 20, 0);
}

muestraVidas() {
  showAt("Vidas: " + vidas, 0, 0);
}

muestraInstrucciones() {
  clear();
  showAt("Usa las flechas del teclado", 1, 2);
  showAt("para mover a tu personaje", 1, 3);
  showAt("por la pantalla.", 1, 4);
  showAt("Cada vez que te comes la", 1, 6);
  showAt("comida te da un punto.", 1, 7);
  showAt("Evita que te coman!", 1, 9);
  
  showAt("Personaje:", 5, 13);
  putAt(PERSONAJE, 17, 13);
  
  showAt("Comida:", 8, 15);
  putAt(COMIDA, 17, 15);
  
  showAt("Enemigo:", 7, 17);
  putAt(ENEMIGO, 17, 17);
  
  showAt("Presiona la barra de", 10, 20);
  showAt("espacio para empezar", 10, 21);
  
  while (readCtrlOne() != BOTON_ESPACIO)
    ;
}

init() {
  xPersonaje = 16;
  yPersonaje = 12;
  nuevaPosicionComida();
  xEnemigo = 31;
  yEnemigo = 23;
  puntos = 0;
  vidas = 3;
  clear();
  muestraPuntos();
  muestraVidas();
  dibujaComida();
}

juega() {
  init();
  while (vidas > 0) {
    dibujaPersonaje();
    dibujaEnemigo();
    pause(0.1);
    borraPersonaje();
    borraEnemigo();
    var boton = readCtrlOne();
    muevePersonaje(boton);
    if (personajeComioComida()) {
      nuevaPosicionComida();
      dibujaComida();
      puntos = puntos + 1;
      muestraPuntos();
      note("C6");
    }
    mueveEnemigo();
    if (enemigoComioPersonaje()) {
      nuevaPosicionPersonaje();
      vidas = vidas - 1;
      muestraVidas();
      note("E2");
    }
  }
  showAt("GAME OVER", 12, 12);
  pause(3);
}

// Ponemos el fondo de color negro
setBackground(0, 0, 0);

while (true) {
  muestraInstrucciones();
  juega();
}