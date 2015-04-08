// Primera seccion
parpadea() {
  var i = 0;
  while (i < 4) {
    setForeground(0, 0, 31);
    pause(0.2);
    setForeground(31, 31, 31);
    pause(0.2);
    i = i + 1;
  }
}

showAt("simpleJ", 12, 4);
parpadea();
showAt("Procedimientos", 8, 6);
parpadea();

// Segunda seccion
multiShowAt(mensaje, x, y, cuantos) {
  var i = 0;
  while (i < cuantos) {
    showAt(mensaje, x, y + i);
    i = i + 1;
  }
}

multiShowAt("Hola", 2, 8, 3);
multiShowAt("Adios", 12, 8, 4);

// Tercera seccion
porLaDerecha(mensaje, x, y, tiempo) {
  var i = 31;
  while (i >= x) {
    showAt(mensaje, i, y);
    pause(tiempo);
    i = i - 1;
  }
}

porLaDerecha("Uno", 25, 8, .1);

// Cuarta seccion
nuevoPorLaDerecha(mensaje, x, y, tiempo) {
  var i = 31;
  mensaje = mensaje + " ";
  while (i >= x) {
    showAt(mensaje, i, y);
    pause(tiempo);
    i = i - 1;
  }
}

nuevoPorLaDerecha("Uno", 25, 9, .1);

// Quinta seccion
porLaIzquierda(mensaje, x, y, tiempo) {
  var i = length(mensaje);
  i = -i;
  while (i < x) {
    showAt(" " + mensaje, i, y);
    pause(tiempo);
    i = i + 1;
  }
}

porLaIzquierda("Dos", 10, 15, .1);

// Fin
print("Fin");

