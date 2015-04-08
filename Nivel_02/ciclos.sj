// Primera seccion
var contador = 0;
while (contador < 3)
  contador = contador + 1;

// Segunda seccion
contador = 0;
while (contador < 3) {
  print(contador);
  contador = contador + 1;
}

// Tercera seccion
var renglon = 10;
while (renglon < 14) {
  showAt("Ciclos", 2, renglon);
  renglon = renglon + 1;
}

// Cuarta seccion
while (renglon < 5) {
  showAt("Esto no se ejecuta", 2, renglon);
  renglon = renglon + 1;
}

// Quinta seccion
contador = 0;
while (contador < 4) {
  setForeground(0, 0, 31);
  pause(.2);
  setForeground(31, 31, 31);
  pause(.2);
  contador = contador + 1;
}

// Sexta seccion
var columna = 11;
while (columna < 32) {
  var renglon = 0;
  while (renglon < 24) {
    showAt("<:+++<", columna, renglon);
    renglon = renglon + 1;
  }
  columna = columna + 7;
}

// Fin
print("Fin");