var xBola = 16;
var yBola = 12;

// Para "siempre"
while (true) {
  // Dibujamos la bola en la pantalla
  putAt(20, xBola, yBola);

  // Hacemos una pausa
  pause(0.1);

  // Borramos la bola de la pantalla
  putAt(32, xBola, yBola);

  // Vemos que boton esta apoyado
  var boton = readCtrlOne();

  // y lo usamos para calcular la nueva posicion de la bola  
  if (boton == 1)
    yBola = yBola - 1;
  if (boton == 2)
    yBola = yBola + 1;
  if (boton == 4)
    xBola = xBola - 1;
  if (boton == 8)
    xBola = xBola + 1;
}