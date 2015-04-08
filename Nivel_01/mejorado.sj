/*
  Empezamos por poner el fondo de color negro
  y desplegar unos mensajes de bienvenida.
  Con unas pausas para que se vea mas interesante
*/
setBackground(0, 0, 0);
showAt("Hola!", 12, 4);
pause(1.0);
showAt("Bienvenido a simpleJ", 5, 8);
pause(1.0);
showAt("Lo primero que vas a aprender", 1, 12);
showAt("es como desplegar mensajes en", 1, 13);
showAt("la pantalla.", 1, 14);
pause(4.0);

/* Demostramos que se puede cambiar el color del fondo */
showAt("Tambien a cambiar el color", 1, 16);
pause(1.0);
showAt("del fondo", 1, 17);
pause(0.6);
setBackground(0, 0, 31);    // azul
pause(0.6);
setBackground(0, 31, 0);    // verde
pause(0.6);
setBackground(31, 0, 0);    // rojo
pause(0.6);
setBackground(31, 31, 0);   // amarillo
pause(0.6);
setBackground(0, 31, 31);   // cian
pause(0.6);
setBackground(31, 0, 31);   // magenta
pause(0.6);
setBackground(0, 0, 0);     // negro
pause(1.0);

/* Y ahora cambiamos el color de las letras */
showAt("y de las letras.", 11, 17);
pause(0.6);
setForeground(0, 0, 31);    // azul
pause(0.6);
setForeground(0, 31, 0);    // verde
pause(0.6);
setForeground(31, 0, 0);    // rojo
pause(0.6);
setForeground(31, 31, 0);   // amarillo
pause(0.6);
setForeground(0, 31, 31);   // cian
pause(0.6);
setForeground(31, 0, 31);   // magenta
pause(0.6);
setForeground(31, 31, 31);  // blanco
pause(2.0);

/* Mostramos que se puede borrar la pantalla */
showAt("Y como borrar la pantalla...", 1, 20);
pause(2.5);
clear();
pause(2.0);

/*
  Regresamos el fondo a su color inicial, para que quede
  claro que ya termino de ejecutarse el programa
*/
setBackground(0, 0, 31);
