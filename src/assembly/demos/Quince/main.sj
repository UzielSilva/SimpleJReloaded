/* Quince.
 *  Un ejemplo sencillo de un juego con simpleJ.
 *  Es el rompecabezas, conocido en ingles como
 *  "Fifteen-Puzzle", donde 15 piezas numeradas
 *  del 1 al 15 se encuentran en organizadas en
 *  un cuadrado de 4x4 dejando un hueco que
 *  permite ir desplazando las piezas una por una
 *  hasta que se logra tenerlas ordenadas.
 *  
 *  Este programa emplea tiles para dibujar el
 *  fondo, el cual contiene el tablero y las
 *  instrucciones de como jugar. Para dibujar
 *  las piezas emplea 15 sprites que
 *  corresponde cada uno a una de las piezas
 *  que se pueden deslizar.
 */

/* Constantes para los botones*/
final BOTON_ARRIBA = 1;
final BOTON_ABAJO = 2;
final BOTON_IZQUIERDA = 4;
final BOTON_DERECHA = 8;
final BOTON_ESPACIO = 64;

/* Los tres estados en los que puede estar el juego */
final ESTADO_CARGANDO = 1;
final ESTADO_PROCESA_BOTON = 2;
final ESTADO_SUELTA_BOTON = 3;

/* Posicion de la esquina superior izquierda del
   tablero en la pantalla (en pixeles) */
final TABLERO_X = 16;
final TABLERO_Y = 16;

/* La representacion del tablero en memoria como
   un arreglo de 4x4. El 0 representa el hueco
   donde no hay ninguna pieza.
   Empieza con las piezas ordenadas */
var tablero = [[  1,  2,  3,  4],
               [  5,  6,  7,  8],
               [  9, 10, 11, 12],
               [ 13, 14, 15,  0]];

/* La columna en la que se encuentra el hueco
   (un entero entre 0 y 3). Inicialmente esta
   en la ultima columna */
var huecoX = 3;

/* El renglon en el que se encuentra el hueco
   (un entero entre 0 y 3). Inicialmente esta
   en el ultimo renglon */
var huecoY = 3;

/* El estado actual del juego */
var estado = ESTADO_CARGANDO;

/* Bandera para indicar cuando se terminaron de
   leer los archivos de tiles y de sprites */
var terminoDeCargar = false;

/* Secuencia de caracteres para la animacion
   mientras que se cargan los datos */
var animacionCargando = ["|", "/", "-", "\\"];

/* Contador empleado en la animacion mientras que
   se cargan los archivos de tiles y de sprites */
var contador = 0;

/* Coloca los 15 sprites de las piezas en la
   pantalla para que su posicion corresponda
   a los datos almacenados en el arreglo
   'tablero' */
dibujaTablero() {
  for (var y = 0; y < 4; y++)
    for (var x = 0; x < 4; x++) {
      var pieza = tablero[y][x];
      if (pieza != 0)
        // Se multiplican 'x' y 'y' por 16 por que
        // estamos empleando sprites grandes (16 x 16)
        putSpriteAt(pieza, TABLERO_X + x * 16,
                           TABLERO_Y + y * 16);
    }
}

/* Se puede deslizar una pieza hacia arriba? */
puedeArriba() {
  // Verdadero si el hueco no esta en el ultimo renglon
  return huecoY < 3;
}

/* Se puede deslizar una pieza hacia abajo? */
puedeAbajo() {
  // Verdadero si el hueco no esta en el primer renglon
  return huecoY > 0;
}

/* Se puede deslizar una pieza hacia la izquierda? */
puedeIzquierda() {
  // Verdadero si el hueco no esta en la ultima columna
  return huecoX < 3;
}

/* Se puede deslizar una pieza hacia la derecha? */
puedeDerecha() {
  // Verdadero si el hueco no esta en la primera columna
  return huecoX > 0;
}

/* Desliza una pieza hacia arriba */
arriba() {
  // Pieza debajo del hueco a la posicion del hueco
  tablero[huecoY][huecoX] = tablero[huecoY + 1][huecoX];
  // Baja de una posicion el hueco
  tablero[huecoY + 1][huecoX] = 0;
  huecoY++;
}

/* Desliza una pieza hacia abajo */
abajo() {
  // Pieza arriba del hueco a la posicion del hueco
  tablero[huecoY][huecoX] = tablero[huecoY - 1][huecoX];
  // Sube de una posicion el hueco
  tablero[huecoY - 1][huecoX] = 0;
  huecoY--;
}

/* Desliza una pieza hacia la derecha */
izquierda() {
  // Pieza a la derecha del hueco a la posicion del hueco
  tablero[huecoY][huecoX] = tablero[huecoY][huecoX + 1];
  // Mueve el hueco de una posicion hacia la derecha
  tablero[huecoY][huecoX + 1] = 0;
  huecoX++;
}

/* Desliza una pieza hacia la izquierda */
derecha() {
  // Piezas a la izquierda del hueco a la posicion del hueco
  tablero[huecoY][huecoX] = tablero[huecoY][huecoX - 1];
  // Mueve el hueco de una posicion hacia la izquierda
  tablero[huecoY][huecoX - 1] = 0;
  huecoX--;
}

/* Revuelve al azar las piezas */
revuelve() {
  // Hace cien movimientos al azar
  for (var i = 0; i < 100; i++)
    // Escoge una direccion al azar e intenta
    // mover una pieza en esa direccion
    switch (random(4)) {
      case 0:
        if (puedeArriba())
          arriba();
        break;
      
      case 1:
        if (puedeAbajo())
          abajo();
        break;
      
      case 2:
        if (puedeIzquierda())
          izquierda();
        break;
      
      case 3:
        if (puedeDerecha())
          derecha();
        break;
    }
}    

/* Se ejecuta en cada llamado al procedimiento vbi
   mientras que el juego esta en el estado CARGANDO.
   Hace una animacion sencilla mientras que se estan
   leyendo los datos para los tiles y los sprites.
   Al terminar de leerlos hace la transicion a
   ESTADO_PROCESA_BOTON */
cargandoVBI() {
  // Despliega mensaje cambiando a cada vez el
  // caracter empleado para la animacion
  showAt("Cargando " + animacionCargando[contador],
         10, 12);
  contador++;
  if (contador == length(animacionCargando))
    contador = 0;
  
  // Si ya termino de cargar los datos entonces
  // dibuja el fondo y pasa al estado PROCESA_BOTON
  if (terminoDeCargar) {
    /* Dibuja el fondo */  
    for (var r = 0; r < 24; r++)  
      for (var c = 0; c < 32; c++)
        putAt(tilesData.rows[r][c], c, r);
    estado = ESTADO_PROCESA_BOTON;
  }
}

/* Se ejecuta en cada llamado al procedimiento
   vbi mientras que el juego esta en el estado
   PROCESA_BOTON. Si hay un boton apoyado entonces
   ejecuta la accion correspondiente y pasa al
   estado SUELTA_BOTON. Siempre vuelve a colocar
   los sprites de las piezas en la pantalla para
   que correspondan a la infomacion almacenada en
   el arreglo 'tablero' */
procesaBotonVBI() {
  var b = readCtrlOne();
  switch (b) {
    case BOTON_ARRIBA:
      if (puedeArriba())
        arriba();
      break;
    
    case BOTON_ABAJO:
      if (puedeAbajo())
        abajo();
      break;
    
    case BOTON_IZQUIERDA:
      if  (puedeIzquierda())
        izquierda();
      break;
    
    case BOTON_DERECHA:
      if (puedeDerecha())
        derecha();
      break;
    
    case BOTON_ESPACIO:
      revuelve();
      break;
  }
  
  // Si hubo un boton apoyado
  if (b != 0)
    estado = ESTADO_SUELTA_BOTON;

  // Actualiza la posicion de los sprites de
  // las piezas en la pantalla
  dibujaTablero();
}

/* Se ejecuta en cada llamado al procedimiento
   vbi mientras que el juego esta en el estado
   SUELTA_BOTON. Si no hay ningun boton apoyado
   entonces pasa al estado PROCESA_BOTON. Este
   estado es para esperar a que el jugador deje
   de presionar un boton antes de volver a checar
   si hay un boton presionado. Si no hicieramos
   esto entonces la computadora moveria varias
   piezas cada vez que el jugador presiona una
   de las flechas (hay que recordar que el
   procedimiento vbi se ejecuta 25 veces por
   segundo; la computadora puede mover una pieza
   y volver a preguntar si un boton esta apoyado
   antes del que el jugador tenga tiempo de
   soltarlo) */
sueltaBotonVBI() {
  if (readCtrlOne() == 0)
    estado = ESTADO_PROCESA_BOTON;
}

/* El procedimiento vbi, que se ejecuta
   automaticamente cada vez que se termina de
   redibujar la pantalla (25 veces por segundo),
   simplemente delega el trabajo al
   procedimiento que corresponde al estado
   actual del juego. Definimos este procedimiento
   antes de leer los datos de los tiles y los
   sprites para que se ejecute la animacion del
   estado CARGANDO mientras tanto */
vbi() {
  switch (estado) {
    case ESTADO_CARGANDO:
      cargandoVBI();
      break;
    
    case ESTADO_PROCESA_BOTON:
      procesaBotonVBI();
      break;
    
    case ESTADO_SUELTA_BOTON:
      sueltaBotonVBI();
      break;
  }
}

/* Lee definiciones de tiles creadas con
   el tiles editor */
var tilesData = readTilesFile("tiles.tmap");

/* Pon colores en el mapa de colores */
for (var i = 0; i < 16; i++)
  setTileColor(i, tilesData.colors[i].red,
                  tilesData.colors[i].green,
                  tilesData.colors[i].blue);

/* Graba nuevas definiciones de tiles */
for (var i = 0; i < 256; i++)
  setTilePixels(i, tilesData.pixels[i]);

/* Lee definiciones de sprites creadas con
   el sprites editor */
var spritesData = readSpritesFile("sprites.smap");

/* Pon colores en el mapa de colores */
for (var i = 0; i < 15; i++)
  setSpriteColor(i, spritesData.colors[i].red,
                    spritesData.colors[i].green,
                    spritesData.colors[i].blue);

/* Graba nuevas definiciones de sprites */
for (var i = 1; i < 16; i++)
  setLargeSpritePixels(i, spritesData.largePixels[i]);

/* Asigna a los sprites del 1 al 15 las imagenes de las
   piezas */
for (var i = 1; i < 16; i++)
  setLargeSpriteImage(i, i);
  
/* Modifica la bandera para que se ejecute
   la transicion a ESTADO_PROCESA_BOTON  */
terminoDeCargar = true;