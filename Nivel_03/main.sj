/* Constantes para los tiles de la bola y el espacio en blanco */
final BOLA = 20;
final ESPACIO = 32;

/* Constantes para los "botones" de las flechas */
final BOTON_ARRIBA = 1;
final BOTON_ABAJO = 2;
final BOTON_IZQUIERDA = 4;
final BOTON_DERECHA = 8;

/* Constantes para los limites de la pantalla */
final MIN_X = 0;
final MAX_X = 31;
final MIN_Y = 0;
final MAX_Y = 23;

/* La posicion de la bola en la pantalla */
var xBola = 16;   // posicion horizontal
var yBola = 12;   // posicion vertical

// Ponemos el fondo de color negro
setBackground(0, 0, 0);

// Mostramos las instrucciones
showAt("Usa las flechas del teclado", 1, 4);
showAt("para mover la bola por la", 1, 5);
showAt("pantalla.", 1, 6);

// Para "siempre"
while (true) {
  // Dibujamos la bola
  putAt(BOLA, xBola, yBola);

  // Esperamos una decima de segundo
  pause(0.1);

  // Borramos la bola
  putAt(ESPACIO, xBola, yBola);

  // Vemos que boton esta apoyado
  var boton = readCtrlOne();

  // y lo usamos para calcular la nueva
  // posicion de la bola  
  if (boton == BOTON_ARRIBA && yBola > MIN_Y)
    yBola = yBola - 1;
  if (boton == BOTON_ABAJO && yBola < MAX_Y)
    yBola = yBola + 1;
  if (boton == BOTON_IZQUIERDA && xBola > MIN_X)
    xBola = xBola - 1;
  if (boton == BOTON_DERECHA && xBola < MAX_X)
    xBola = xBola + 1;
}