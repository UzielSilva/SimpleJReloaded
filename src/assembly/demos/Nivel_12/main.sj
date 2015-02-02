/* Constantes para los botones*/
final BOTON_IZQUIERDA = 4;
final BOTON_DERECHA = 8;
final BOTON_RETURN = 16;
final BOTON_ESPACIO = 64;

/* El tile con un espacio en blanco,
   para borrar bloques */
final ESPACIO = 32;

/* Los cinco estados en los que puede estar el juego */
final ESTADO_CARGANDO = 0;
final ESTADO_PRESENTACION = 1;
final ESTADO_LANZA_PELOTA = 2;
final ESTADO_JUEGA = 3;
final ESTADO_GAME_OVER = 4;

/* Posicion vertical, en tiles, del primer y el ultimo
   renglon con bloques */
final PRIMER_RENGLON = 3;
final ULTIMO_RENGLON = 8;

/* Numero de renglones con bloques (en tiles) */
final RENGLONES = ULTIMO_RENGLON - PRIMER_RENGLON + 1;

/* Ancho de un bloque (en tiles) */
final ANCHO_BLOQUE = 4;

/* Cuantos bloques caben en un renglon */
final BLOQUES_POR_RENGLON = 32 / ANCHO_BLOQUE;

/* Cuantos bloques hay en total */
final TOTAL_BLOQUES = BLOQUES_POR_RENGLON * RENGLONES;

/* Posicion vertical, en pixeles, de la parte
   superior de la raqueta */
final Y_RAQUETA = 192 - 6;

/* Ancho de la raqueta (en pixeles) */
final ANCHO_RAQUETA = 32;

/* Velocidad de la raqueta.
   En pixeles por cada vez que se ejecuta vbi */
final VELOCIDAD_RAQUETA = 10;

/* Las tres velocidades para la pelota.
   En pixeles por cada vez que se ejecuta vbi */
final VELOCIDAD1_PELOTA = 4;
final VELOCIDAD2_PELOTA = 5;
final VELOCIDAD3_PELOTA = 6;

/* Posicion vertical inicial de la esquina
   superior de la pelota al lanzarla (en pixeles) */
final Y_INICIAL_PELOTA = 80;

/* El estado actual del juego */
var estado = ESTADO_CARGANDO;

/* Contador empleado en la animacion mientras que
   se cargan los archivos de tiles y de sprites.
   Tambien se emplea para la pause en GAME OVER */
var contador = 0;

/* Bandera para indicar cuando se terminaron de
   leer los archivos de tiles y de sprites */
var terminoDeCargar = false;

/* Secuencia de caracteres para la animacion
   mientras que se cargan los datos */
var animacionCargando = ["|", "/", "-", "\\"];

/* Cuantos bloques quedan sin destruir */
var cuantosBloques;

/* Arreglo bidimensional (en realidad es un arreglo
   de arreglos) de booleanos para saber si todavia
   hay un bloque en cierta posicion de la pantalla.
   Contiene un elemento por cada posicion en la que
   puede haber un bloque.
   Si hay un bloque en esa posicion entonces el
   elemento contiene true, de lo contario contiene
   false */
var bloques =
       new array[RENGLONES][BLOQUES_POR_RENGLON];

/* Cuantas pelotas le quedan al jugador (incluyendo
   la pelota con la que esta jugando */
var pelotas;

/* Cuantos puntos ha ganado hasta ahora el jugador */
var puntos;

/* La posicion horizontal del borde izquierdo de
   la raqueta (en pixeles) */
var xRaqueta;

/* La posicion horizontal del borde izquierdo de
   la pelota (en pixeles) */
var xPelota;

/* La posicion vertical del borde superior de
   la pelota (en pixeles) */
var yPelota;

/* La direccion horizontal en la que se esta
   moviendo la pelota.
   Un -1 indica hacia la izquierda.
   Un 1 indica hacia la derecha. */
var dxPelota;

/* La direccion vertical en la que se esta
   moviendo la pelota.
   Un -1 indica hacia arriba.
   Un 1 indica hacia abajo. */
var dyPelota;

/* La velocidad con la cual se esta moviendo
   la pelota (en pixeles por cada vez que se
   ejecuta vbi) */
var velocidadPelota;

/* Coloca todos los bloques en la pantalla */
colocaBloques() {
  /* Dibuja los seis renglones de bloques */
  for (var y = PRIMER_RENGLON;
       y <= ULTIMO_RENGLON;
       y++) {
    // Los tiles a emplear para este renglon. En este
    // ciclo la variable 'base' va teniendo los valores
    // 0, 0, 3, 3, 6 y 6
    var base = (y - PRIMER_RENGLON) / 2 * 3;
    for (var x = 0; x < 32; x++) {
      // El tile que corresponde a esta posicion de la
      // pantalla. La variable 'x' va tomando los
      // valores 0, 1, 1 y 2 por cada uno de los bloques
      var tile = x % 4;
      if (tile > 1)
        tile--;
      putAt(base + tile, x, y);
    }
  }
  
  // Falta destruir todos los bloques
  cuantosBloques = TOTAL_BLOQUES;
  
  // Indica que hay un bloque en cada posicion
  // almacenando 'true' en todos los elementos
  // del arreglo 'bloques'
  for (var r = 0; r < RENGLONES; r++)
    for (var c = 0; c < BLOQUES_POR_RENGLON; c++)
      bloques[r][c] = true; 
}


/* Despliega en la esquina superior izquierda
   de la pantalla cuantas pelotas le quedan al
   jugador */
muestraPelotas() {
  showAt("PELOTAS: " + pelotas, 0, 0);
}


/* Convierte un numero a un string con ceros
   a la izquierda (para desplegar los puntos).
   n: cuantos digitos debe contener el string
   numero: el numero */
cerosAlPrincipio(n, numero) {
  // Convierte 'numero' a un string
  numero = "" + numero;
  
  // Agrega el numero necesario de ceros a la
  // izquierda
  while (length(numero) < n)
    numero = "0" + numero;

  return numero;
}

/* Depliega en la esquina superior derecha
   de la pantalla cuantos puntos ha ganado
   el jugador */
muestraPuntos() {
  showAt("PUNTOS:", 18, 0);
  showAt(cerosAlPrincipio(5, puntos), 26, 0);
}


/* Modifica la posicion horizontal de la
   raqueta para que quede centrada en la
   pantalla */
centraRaqueta() {
  xRaqueta = (256 - ANCHO_RAQUETA) / 2;
}


/* Posiciona los dos sprites de la raqueta
   en la pantalla. Calculando antes su nueva
   posicion en caso de que el jugador presione
   la flecha izquierda o derecha */
mueveRaqueta() {
  // Lee control del jugador
  var btn = readCtrlOne();
  
  // Si esta presionado el boton IZQUIERDA
  // mueve la raqueta a la izquierda sin
  // salirse de la pantalla
  if (btn == BOTON_IZQUIERDA) {
    xRaqueta = xRaqueta - VELOCIDAD_RAQUETA;
    if (xRaqueta < 0)
      xRaqueta = 0;
  }
  
  // Si esta presionado el boton DERECHA
  // mueve la raqueta a la derecha sin
  // salirse de la pantalla
  if (btn == BOTON_DERECHA) {
    xRaqueta = xRaqueta + VELOCIDAD_RAQUETA;
    if (xRaqueta > 256 - ANCHO_RAQUETA)
      xRaqueta = 256 - ANCHO_RAQUETA;
  }
  
  // Coloca los dos sprites de la raqueta
  // en su posicion en la pantalla
  putSpriteAt(1, xRaqueta, Y_RAQUETA);
  putSpriteAt(2, xRaqueta + 16, Y_RAQUETA);
}


/* Checa si el punto con coordenadas (x, y),
   en pixeles, toca alguno de los bloques.
   En caso de ser cierto decrementa de uno
   el numero de bloques que quedan por
   destruir, almacena false en el elemento
   correspondiente del arreglo 'bloque', y
   devuelve 'true'. De lo contrario simplemente
   devuelve 'false' */
tocaEn(x, y) {
  // Convierte la coordenada horizontal 'x'
  // (en pixeles) a una columna 'c' (en bloques)
  var c = x / 32;
  
  // Si esta mas alla del borde derecho de
  // la pantalla entonces no toco ninguno
  // de los bloques (en este programa nunca
  // ocurre que x sea negativo y por lo
  // tanto no es necesario checar del lado
  // izquierdo
  if (c >= BLOQUES_POR_RENGLON)
    return false;
    
  // Convierte la coordenada vertical 'y'
  // (en pixeles) a un renglon 'r' (en bloques)
  var r = y / 8 - PRIMER_RENGLON;
  
  // Si esta antes del primer renglon o
  // despues del ultimo renglon entonces
  // no puede haber tocado alguno de los
  // bloques
  if (r < 0 || r >= RENGLONES)
    return false;
  
  // Si no hay un bloque ahi entonces no
  // es necesario hacer nada
  if (!bloques[r][c])
    return false;
  
  // Si llegamos hasta aqui quiere decir
  // que si toco un bloque.
  // Marca en el arreglo 'bloques' que ya
  // no esta ese bloque
  bloques[r][c] = false;
  
  // Decrementa de uno el numero de bloques
  // que quedan por destruir
  cuantosBloques--;
  
  // Borra el bloque de la pantalla
  for (var i = 0; i < ANCHO_BLOQUE; i++)
    putAt(ESPACIO,
          c * ANCHO_BLOQUE + i,
          PRIMER_RENGLON + r);
  
  // Agrega un punto al jugador y actualiza
  // la informacion de los puntos en la pantalla
  puntos++;
  muestraPuntos();
  
  // Si toco un bloque rojo (renglon es 0 o 1)
  // asegura que la velocidad de la pelota sea
  // por lo menos la velocidad que corresponde
  // a destruir un bloque rojo
  if (r < 2 && velocidadPelota < VELOCIDAD3_PELOTA)
    velocidadPelota = VELOCIDAD3_PELOTA;
  
  // Si toco un bloque amarillo (renglon es 2 o 3)
  // asegura que la velocidad de la pelota sea
  // por lo menos la velocidad que corresponde
  // a destruir un bloque amarillo
  if (r < 4 && velocidadPelota < VELOCIDAD2_PELOTA)
    velocidadPelota = VELOCIDAD2_PELOTA;
  
  // Devuelve 'true' porque si toco un bloque
  return true;
}


/* Checa si el borde izquierdo de la pelota
   le pego a un bloque */
tocaBordeIzquierdo() {
  // Checa en las coordenadas que corresponden
  // a la esquina superior izquierda y a la
  // esquina inferior izquierda de la pelota
  return tocaEn(xPelota, yPelota) ||
         tocaEn(xPelota, yPelota + 8);
}


/* Checa si el borde derecho de la pelota
   le pego a un bloque */
tocaBordeDerecho() {
  // Checa en las coordenadas que corresponden
  // a la esquina superior derecha y a la
  // esquina inferior derecha de la pelota
  return tocaEn(xPelota+8, yPelota) ||
         tocaEn(xPelota+8, yPelota + 8);
}


/* Checa si el borde superior de la pelota
   le pego a un borde */
tocaBordeSuperior() {
  // Checa en las coordenadas que corresponden
  // a la esquina superior izquiera y a la
  // esquina superior derecha de la pelota
  return tocaEn(xPelota, yPelota) ||
         tocaEn(xPelota + 8, yPelota);
}


/* Checa si el borde inferior de la pelota
   le pego a un borde */
tocaBordeInferior() {
  // Checa en las coordenadas que corresponden
  // a la esquina inferior izquierda y a la
  // esquina inferior derecha de la pelota
  return tocaEn(xPelota, yPelota + 8) ||
         tocaEn(xPelota + 8, yPelota + 8);
}


/* Mueve la pelota de un pixel (tanto en la
   direccion horizontal como vertical).
   Checa si le pego a un bloque, a un borde
   de la pantalla o a la raqueta para modificar
   la direccion en la que se mueve. Si destruyo
   el ultimo bloque que quedaba entonces vuelve
   a colocar todos los bloques. Si la pelota se
   salio de la pantalla entonces decrementa el
   numero de pelotas, genera la transicion al
   estado LANZA_PELOTA o al estado GAME_OVER
   y delvuelve 'false' para indicar que la
   pelota ya no se esta moviendo, de lo contrario
   devuelve 'true' para que se siga moviendo la
   pelota */
muevePelota() {
  // Calcula la nueva posicion de la pelota
  xPelota = xPelota + dxPelota;
  yPelota = yPelota + dyPelota;
  
  // Si la pelota pega con el borde izquierdo de
  // la pantalla entonces ahora se mueve hacia
  // la derecha
  if (xPelota <= 0) {
    xPelota = 0;
    dxPelota = 1;
    soundOn(0);
  }
  
  // Si la pelota pega con el borde derecho de la
  // pantalla entonces ahora se mueve hacia la
  // izquierda
  if (xPelota >= 256 - 8) {
    xPelota = 256 - 8;
    dxPelota = -1;
    soundOn(0);
  }
  
  // Si la pelota pega con el borde superior de la
  // pantalla entonces ahora se mueve hacia abajo
  if (yPelota <= 0) {
    yPelota = 0;
    dyPelota = 1;
    soundOn(0);
  }
  
  // Si el borde inferior de la raqueta esta a la
  // altura de la raqueta, o mas abajo.
  if (yPelota + 8 >= Y_RAQUETA) {
    // Si el borde derecho de la pelota esta a la
    // izquierda de la raqueta o si el borde
    // izquierdo de la pelota esta a la derecha de
    // la raqueta (es decir, si no toca la raqueta)
    if (xPelota + 8 < xRaqueta ||
        xPelota > xRaqueta + ANCHO_RAQUETA) {
      // Si la pelota ya esta abajo del borde superior
      // de la raqueta
      if (yPelota >= Y_RAQUETA) {
        soundOn(1);
        
        // Una pelota menos
        pelotas--;
        muestraPelotas();
        
        // Si todavia le quedan pelotas se pasa al
        // estado para lanzar otra pelota, de lo
        // contrario ya se acabo el juego
        if (pelotas > 0)
          transicionALanzaPelota();
        else
          transicionAGameOver();
        
        // Quita la pelota de la pantalla
        putSpriteAt(0, -10, 0);
        
        // Ya no se debe de seguir moviendo la pelota
        return false;
      }
    } else {
      // Rebota la pelota contra la raqueta
      yPelota = Y_RAQUETA - 8;
      dyPelota = -1;
      soundOn(0);
    }
  }
  
  // Checa si la pelota va hacia abajo
  if (dyPelota == 1) {
    // Checa si el borde inferior de la
    // pelota toco un bloque
    if (tocaBordeInferior()) {
      // Rebota hacia arriba
      dyPelota = -1;
      soundOn(0);
    }
  } else {
    // La pelota va hacia arriba, entonces
    // checa si su borde superior toco un
    // bloque
    if (tocaBordeSuperior()) {
      // Rebota hacia abajo
      dyPelota = 1;
      soundOn(0);
    }
  }
  
  // Checa si la pelota va hacia la derecha
  if (dxPelota == 1) {
    // Checa si el borde derecho de la
    // pelota toco un bloque
    if (tocaBordeDerecho()) {
      // Rebota hacia la izquierda
      dxPelota = -1;
      soundOn(0);
    }
  } else {
    // La pelota va hacia la izquierda,
    // entonces checa si su borde
    // izquierdo toco un bloque 
    if (tocaBordeIzquierdo()) {
      // Rebota hacia la derecha
      dxPelota = 1;
      soundOn(0);
    }
  }
  
  // Si ya no hay bloques por destruir
  // entonces volver a colocar todos los
  // bloques
  if (cuantosBloques == 0)
    colocaBloques();
  
  // Coloca el sprite de la pelota en su
  // nueva posicion en la pantalla
  putSpriteAt(0, xPelota, yPelota);
  
  // Indica que la pelota sigue en movimiento
  return true;
}


/* Dibuja la pantalla de presentacion y
   pasa al estado PRESENTACION */
transicionAPresentacion() {
  for (var r = 0; r < 24; r++)  
    for (var c = 0; c < 32; c++)
      putAt(tilesData.rows[r][c], c, r);
  estado = ESTADO_PRESENTACION;
}


/* Muestra un mensaje indicando como
   lanzar la pelota y pasa al estado
   LANZA_PELOTA */
transicionALanzaPelota() {
  // Despliega el mensaje
  showAt("Presiona ESPACIO para", 4, 12);
  showAt("lanzar la pelota", 4, 13);
  
  // Pasa al estado LANZA_PELOTA
  estado = ESTADO_LANZA_PELOTA;
}


/* Borra el mensaje que indica como
   lanzar la pelota, lanza la pelota y
   pasa al estado JUEGA */
transicionAJuega() {
  // Borra el mensaje
  showAt("                     ", 4, 12);
  showAt("                ", 4, 13);
  
  // Lanza la pelota asignandole una posicion
  // inicial cerca del centro de la pantalla,
  // un movimiento vertical hacia abajo y
  // eligiendo al azar si se mueve hacia la
  // izquiera o hacia la derecha
  yPelota = Y_INICIAL_PELOTA;
  xPelota = random(48) + 100;
  if (random(2) == 0)
    dxPelota = 1;
  else
    dxPelota = -1;
  dyPelota = 1;
  velocidadPelota = VELOCIDAD1_PELOTA;
  
  // Pasa al estado JUEGA
  estado = ESTADO_JUEGA;
}


/* Despliega el mensaje de "GAME OVER",
   modifica 'contador' para la pausa,
   esconde los dos sprites de la raqueta
   y pasa al estado GAME_OVER */
transicionAGameOver() {
  // Despliega el mensaje
  showAt("GAME OVER", 12, 13);
  
  // Duracion de la pausa antes de pasar
  // al estado de PRESENTACION
  contador = 60;
  
  // Esconde (poniendo fuera de la pantalla)
  // los dos sprites de la raqueta
  putSpriteAt(1, -16, 0);
  putSpriteAt(2, -16, 0);
  
  // Pasa al estado GAME_OVER
  estado = ESTADO_GAME_OVER;
}


/* Se ejecuta en cada llamado al procedimiento vbi
   mientras que el juego esta en el estado CARGANDO.
   Hace una animacion sencilla mientras que se estan
   leyendo los datos para los tiles y los sprites.
   Al terminar de leerlos hace la transicion al
   estado PRESENTACION */
cargandoVBI() {
  // Despliega mensaje cambiando a cada vez el
  // caracter empleado para la animacion
  showAt("Cargando " + animacionCargando[contador],
         10, 12);
  contador++;
  if (contador == length(animacionCargando))
    contador = 0;
  
  // Si ya termino de cargar los datos entonces
  // pasa al estado PRESENTACION
  if (terminoDeCargar)
    transicionAPresentacion();
}


/* Se ejecuta en cada llamado al procedimiento
   vbi mientras que el juego esta en el estado
   PRESENTACION. Cuando el jugador presiona el
   boton RETURN inicializa las variables del
   juego, dibuja la pantalla, y hace la
   transicion al estado LANZA_PELOTA */
presentacionVBI() {
  // El jugador presiono RETURN?
  if (readCtrlOne() == BOTON_RETURN) {
    // entonces inicializa el juego
    puntos = 0;
    pelotas = 5;
    clear();
    muestraPelotas();
    muestraPuntos();
    colocaBloques();
    centraRaqueta();
    
    // y pasa al estado LANZA_PELOTA
    transicionALanzaPelota();
  }
}


/* Se ejecuta en cada llamado al procedimiento
   vbi mientras que el juego esta en el estado
   LANZA_PELOTA. Permite que el jugador mueva
   la raqueta y cuando este presiona ESPACIO
   hace la transicion al estado JUEGA */
lanzaPelotaVBI() {
  // Permite al jugador mover la raqueta
  mueveRaqueta();
  
  // El jugador presiono ESPACIO?
  if (readCtrlOne() == BOTON_ESPACIO)
    // entonces pasa al estado JUEGA
    transicionAJuega();
}


/* Se ejecuta en cada llamado al procedimiento
   vbi mientras que el juego esta en el estado
   JUEGA. Mueve la raqueta y la pelota.
   El cambio de estado a LANZA_PELOTA o a
   GAME_OVER esta incorporado dentro del
   procedimiento muevePelota. */
juegaVBI() {
  // Permite al jugador mover la raqueta
  mueveRaqueta();
  
  // Mueve la pelota. El control de la velocidad
  // se obtiene llamando al procedimiento
  // muevePelota el numero de veces indicado por
  // el valor de la variable 'velocidadPelota'.
  // La razon de hacerlo asi es que permite detectar
  // mas facilmente las colisiones con un bloque, la
  // raqueta o un borde de la pantalla.
  for (var i = 0; i < velocidadPelota; i++)
    // Ya no se esta moviendo la pelota?
    // (salio de la pantalla)
    if (!muevePelota())
      // entonces ya no hay que seguirla moviendo
      break;
}


/* Se ejecuta en cada llamado al procedimiento
   vbi mientras que el juego esta en el estado
   GAME_OVER. Hace una pausa decrementando un
   contador para controlar la duracion de la
   pausa. Cuando el valor del contador llega a
   cero hace la transicion al estado PRESENTACION */
gameOverVBI() {
  contador--;
  if (contador == 0)
    transicionAPresentacion();
}


/* El procedimiento vbi, que se ejecuta
  automaticamente cada vez que se termina de
  redibujar la pantalla (25 veces por segundo),
  simplemente le delega el trabajo al
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
      
    case ESTADO_PRESENTACION:
      presentacionVBI();
      break;
    
    case ESTADO_LANZA_PELOTA:
      lanzaPelotaVBI();
      break;
    
    case ESTADO_JUEGA:
      juegaVBI();
      break;
    
    case ESTADO_GAME_OVER:
      gameOverVBI();
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

/* Graba nuevas definiciones de sprites de 16 por 16 */
for (var i = 0; i < 128; i++)
  setLargeSpritePixels(i, spritesData.largePixels[i]);

/* Graba nuevas definiciones de sprites de 8 por 8 */
for (var i = 0; i < 128; i++)
  setSmallSpritePixels(i, spritesData.smallPixels[i]);

/* Imagen de la pelota */
setSmallSpriteImage(0, 0);

/* Imagen de la raqueta */
setLargeSpriteImage(1, 0);
setLargeSpriteImage(2, 1);

/* Sonido para pelota pegandole a un bloque
   o al borde */
setSoundFrequency(0, 10000);
setSoundAttack(0, 10);
setSoundDecay(0, 100);
setSoundSustain(0, 0);
setSoundRelease(0, 1);
setSoundVolume(0, 15);

/* Sonido para pelota saliendo de la pantalla */
setSoundFrequency(1, 2000);
setSoundAttack(1, 100);
setSoundDecay(1, 300);
setSoundSustain(1, 0);
setSoundRelease(1, 1);
setSoundVolume(1, 15);

/* Modifica la bandera para que se ejecute
   la transicion a ESTADO_PRESENTACION  */
terminoDeCargar = true;
