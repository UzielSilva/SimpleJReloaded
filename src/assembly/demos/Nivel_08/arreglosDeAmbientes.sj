var datos = [
  {x: 10, y: 20, contador: 3},
  {x: 31, y: 23, contador: 1},
  {x: 25, y: 12, contador: 2}
];

print(datos[0].x);
print(datos[1].y);
print(datos[2].contador);

datos[1].y = 19;

muestraYModifica(amb) {
  print(amb.x);
  print(amb.y);
  print(amb.contador);
  amb.contador++;
}

for (var i = 0; i < length(datos); i++)
  muestraYModifica(datos[i]);