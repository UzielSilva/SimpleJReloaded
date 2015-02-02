showAt("Jugador 1:", 0, 0);
showAt("Jugador 2:", 0, 12);

while (true) {
  var boton = readCtrlOne();
  showAt("   ", 11, 0);  // borrar numero anterior
  showAt(boton, 11, 0);
  
  boton = readCtrlTwo();
  showAt("   ", 11, 12); // borrar numero anterior
  showAt(boton, 11, 12);
}