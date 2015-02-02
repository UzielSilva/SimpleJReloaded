/*
  Cambia las letras de blanco a negro, y de regreso varias
  veces.
  n: cuantas veces
  tiempo: duracion de la pausa entre los cambios
*/
parpadea(n, tiempo) {
  var i = 0;
  while (i < n) {
    setForeground(0, 0, 0);
    pause(tiempo);
    setForeground(31, 31, 31);
    pause(tiempo);
    i = i + 1;
  }
}

/*
  Cambia el color de las letras gradualmente de negro a
  blanco.
  tiempo: duracion de la pausa entre cada cambio
*/
aparece(tiempo) {
  var i = 0;
  while (i < 32) {
    setForeground(i, i, i);
    pause(tiempo);
    i = i + 1;
  }
}

/*
  Cambia el color de las letras gradualmente de blanco a
  negro.
  tiempo: duracion de la pausa entre cada cambio
*/
desaparece(tiempo) {
  var i = 31;
  while (i >= 0) {
    setForeground(i, i, i);
    pause(tiempo);
    i = i - 1;
  }
}

/*
  Despliega un mensaje haciendolo llegar por la derecha de
  la pantalla.
  mensaje: el mensaje a desplegar
  x: la columna en la que debe quedar finalmente el mensaje
  y: el renglon en el cual debe estar el mensaje
  tiempo: pausa en cada posicion del mensaje en la pantalla
*/
porLaDerecha(mensaje, x, y, tiempo) {
  var i = 31;
  while (i >= x) {
    showAt(mensaje + " ", i, y);
    pause(tiempo);
    i = i - 1;
  }
}

/*
  Despliega un mensaje haciendolo llegar por la izquierda de
  la pantalla.
  mensaje: el mensaje a desplegar
  x: la columna en la que debe quedar finalmente el mensaje
  y: el renglon en el cual debe estar el mensaje
  tiempo: pausa en cada posicion del mensaje en la pantalla
*/
porLaIzquierda(mensaje, x, y, tiempo) {
  var i = -length(mensaje);
  while (i < x) {
    showAt(" " + mensaje, i, y);
    pause(tiempo);
    i = i + 1;
  }
}

/*
  Ponemos el fondo del color negro y mostramos los primeros
  mensajes.
*/
setBackground(0, 0, 0);
showAt("Ahora vas a aprender a hacer", 1, 3);
showAt("unas animaciones sencillas.", 1, 4);
pause(2);

/* Mostramos el texto que parpadea */
showAt("Texto que parpadea", 1, 6);
pause(1);
parpadea(6, 0.2);
showAt("Texto que parpadea lentamente", 1, 7);
pause(1);
parpadea(4, 0.5);
pause(1);
showAt("Texto que parpadea rapidamente", 1, 8);
pause(1);
parpadea(20, 0.05);
pause(2);

/* Mostramos el texto que desaparece y aparece */
showAt("Texto que desaparece", 1, 10);
pause(1);
desaparece(0.1);
showAt("y vuelve a aparecer", 11, 11);
aparece(0.1);
pause(2);

/* Mostramos el texto que se mueve en la pantalla */
porLaDerecha("Texto que llega por un lado", 1, 13, 0.1);
pause(1);
porLaIzquierda("y por el otro lado", 1, 14, 0.1);
pause(3);

/*
  Al terminar borramos la pantalla y la regresamos a su
  color de fondo inicial
*/
clear();
setBackground(0, 0, 31);