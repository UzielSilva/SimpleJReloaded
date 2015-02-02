/* Constantes para los tiles del personaje, la comida,
   los enemigos y el espacio en blanco */
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

final PUNTOS_NUEVA_VIDA = 10;

/* La posicion del personaje en la pantalla */
var xPersonaje;
var yPersonaje;

/* La posicion de la comida en la pantalla */
var xComida;   // columna
var yComida;   // renglon

/* Las posiciones de los enemigos en la pantalla */
var xEnemigos;
var yEnemigos;

/* La puntuacion del jugador */
var puntos;

var vidas;

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

dibujaEnemigos() {
  for (var i = 0; i < 4; i++)
    putAt(ENEMIGO, xEnemigos[i], yEnemigos[i]);
}

borraEnemigos() {
  for (var i = 0; i < 4; i++)
    putAt(ESPACIO, xEnemigos[i], yEnemigos[i]);
}

hayEnemigoEn(x, y) {
  for (var i = 0; i < 4; i++)
    if (xEnemigos[i] == x && yEnemigos[i] == y)
      return true;
  return false;
}

enemigoComioPersonaje() {
  return hayEnemigoEn(xPersonaje, yPersonaje);
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

mueveEnemigos() {
  for (var i = 0; i < 4; i++) {
    if (random(100) > 40)
      continue;
    var dx = xPersonaje - xEnemigos[i];
    var dy = yPersonaje - yEnemigos[i];
    if (abs(dx) > abs(dy)) {
      var xNueva = xEnemigos[i] + sign(dx);
      if (!hayEnemigoEn(xNueva, yEnemigos[i]))
        xEnemigos[i] = xNueva;
    } else {
      var yNueva = yEnemigos[i] + sign(dy);
      if (!hayEnemigoEn(xEnemigos[i], yNueva))
        yEnemigos[i] = yNueva;
    }
  }
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
  showAt("Nueva vida cada " + PUNTOS_NUEVA_VIDA +
         " puntos.",
         1, 10);
  
  showAt("Personaje:", 5, 13);
  putAt(PERSONAJE, 17, 13);
  
  showAt("Comida:", 8, 15);
  putAt(COMIDA, 17, 15);
  
  showAt("Enemigos:", 6, 17);
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
  xEnemigos = [MAX_X, MAX_X, MIN_X, MIN_X];
  yEnemigos = [MAX_Y, MIN_Y, MAX_Y, MIN_Y];
  puntos = 0;
  vidas = 3;
  clear();
  muestraPuntos();
  muestraVidas();
}

juega() {
  init();
  while (vidas > 0) {
    dibujaPersonaje();
    dibujaComida();
    dibujaEnemigos();
    pause(0.1);
    borraPersonaje();
    borraEnemigos();
    var boton = readCtrlOne();
    muevePersonaje(boton);
    if (personajeComioComida()) {
      nuevaPosicionComida();
      puntos = puntos + 1;
      muestraPuntos();
      if (puntos % PUNTOS_NUEVA_VIDA == 0) {
        vidas = vidas + 1;
        muestraVidas();
        note("C5");
      } else
        note("C6");
    }
    mueveEnemigos();
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