/* Constantes para los tiles del personaje, la comida,
   los enemigos y el espacio en blanco */
final PERSONAJE_ARRIBA = 28;
final PERSONAJE_ABAJO = 29;
final PERSONAJE_IZQUIERDA = 30;
final PERSONAJE_DERECHA = 31;
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

/* Constantes para las direcciones */
final ARRIBA = 1;
final ABAJO = 2;
final IZQUIERDA = 3;
final DERECHA = 4;

/* Constantes para los tipos de enemigos */
final ATACA = 1;
final DEFIENDE = 2;

/* Constante para la distancia maxima al
   defender la comida */
final MAX_DIST_COMIDA = 12;

final PUNTOS_NUEVA_VIDA = 10;

/* La posicion y direccion del personaje en la pantalla */
var xPersonaje;
var yPersonaje;
var dirPersonaje;

/* La posicion de la comida en la pantalla */
var xComida;   // columna
var yComida;   // renglon

/* Los enemigos */
var enemigos;

/* La puntuacion del jugador */
var puntos;

var vidas;

dibujaPersonaje() {
  switch (dirPersonaje) {
    case ARRIBA:
      putAt(PERSONAJE_ARRIBA, xPersonaje, yPersonaje);
      break;
    
    case ABAJO:
      putAt(PERSONAJE_ABAJO, xPersonaje, yPersonaje);
      break;
    
    case IZQUIERDA:
      putAt(PERSONAJE_IZQUIERDA, xPersonaje, yPersonaje);
      break;
      
    case DERECHA:
      putAt(PERSONAJE_DERECHA, xPersonaje, yPersonaje);
      break;
  }
}

borraPersonaje() {
  putAt(ESPACIO, xPersonaje, yPersonaje);
}

actualizaDireccion(boton) {
  if (boton == BOTON_ARRIBA)
    dirPersonaje = ARRIBA;
  if (boton == BOTON_ABAJO)
    dirPersonaje = ABAJO;
  if (boton == BOTON_IZQUIERDA)
    dirPersonaje = IZQUIERDA;
  if (boton == BOTON_DERECHA)
    dirPersonaje = DERECHA;
}

muevePersonaje(boton) {
  actualizaDireccion(boton);
  switch (dirPersonaje) {
    case ARRIBA:
      if (yPersonaje > MIN_Y)
        yPersonaje--;
      break;
      
    case ABAJO:
      if (yPersonaje < MAX_Y)
        yPersonaje++;
      break;
    
    case IZQUIERDA:
      if (xPersonaje > MIN_X)
        xPersonaje--;
      break;
    
    case DERECHA:
      if (xPersonaje < MAX_X)
        xPersonaje++;
      break;
  }
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
  for (var i = 0; i < length(enemigos); i++)
    putAt(ENEMIGO, enemigos[i].x, enemigos[i].y);
}

borraEnemigos() {
  for (var i = 0; i < length(enemigos); i++)
    putAt(ESPACIO, enemigos[i].x, enemigos[i].y);
}

hayEnemigoEn(x, y) {
  for (var i = 0; i < length(enemigos); i++)
    if (enemigos[i].x == x && enemigos[i].y == y)
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

ataca(enemigo) {
  var dx = xPersonaje - enemigo.x;
  var dy = yPersonaje - enemigo.y;
  if (abs(dx) > abs(dy)) {
    var xNueva = enemigo.x + sign(dx);
    if (!hayEnemigoEn(xNueva, enemigo.y))
      enemigo.x = xNueva;
  } else {
    var yNueva = enemigo.y + sign(dy);
    if (!hayEnemigoEn(enemigo.x, yNueva))
      enemigo.y = yNueva;
  }
}

defiende(enemigo) {
  var dx = xComida - enemigo.x;
  var dy = yComida - enemigo.y;
  if (abs(dx) + abs(dy) > MAX_DIST_COMIDA) {
    if (abs(dx) > abs(dy)) {
      var xNueva = enemigo.x + sign(dx);
      if (!hayEnemigoEn(xNueva, enemigo.y))
        enemigo.x = xNueva;
    } else {
      var yNueva = enemigo.y + sign(dy);
      if (!hayEnemigoEn(enemigo.x, yNueva))
        enemigo.y = yNueva;
    }
  } else
    ataca(enemigo);
}

mueveEnemigos() {
  for (var i = 0; i < length(enemigos); i++) {
    if (random(100) > enemigos[i].velocidad)
      continue;
    switch (enemigos[i].tipo) {
      case ATACA:
        ataca(enemigos[i]);
        break;
      
      case DEFIENDE:
        defiende(enemigos[i]);
        break;
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
  showAt("para cambiar la direccion", 1, 3);
  showAt("de tu personaje.", 1, 4);
  showAt("Cada vez que te comes la", 1, 6);
  showAt("comida te da un punto.", 1, 7);
  showAt("Evita que te coman!", 1, 9);
  showAt("Hay enemigos que te atacan", 1, 10);
  showAt("y otros que defienden la comida", 1, 11);
  showAt("Nueva vida cada " + PUNTOS_NUEVA_VIDA + " puntos.",
         1, 13);
  
  showAt("Personaje:", 5, 15);
  putAt(PERSONAJE_DERECHA, 17, 15);
  
  showAt("Comida:", 8, 17);
  putAt(COMIDA, 17, 17);
  
  showAt("Enemigos:", 6, 19);
  putAt(ENEMIGO, 17, 19);
  
  showAt("Presiona la barra de", 10, 21);
  showAt("espacio para empezar", 10, 22);
  
  while (readCtrlOne() != BOTON_ESPACIO)
    ;
}

init() {
  xPersonaje = 16;
  yPersonaje = 12;
  dirPersonaje = DERECHA;
  nuevaPosicionComida();
  enemigos = [
    {tipo: ATACA, velocidad: 60, x: MAX_X, y: MAX_Y},
    {tipo: ATACA, velocidad: 40, x: MIN_X, y: MAX_Y},
    {tipo: DEFIENDE, velocidad: 60, x: MAX_X, y: MIN_Y},
    {tipo: DEFIENDE, velocidad: 40, x: MIN_X, y: MIN_Y}
  ];
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