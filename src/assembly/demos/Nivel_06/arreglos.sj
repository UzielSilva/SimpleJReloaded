var datos = new array[5];

datos[0] = 10;
datos[1] = 3;
datos[2] = -1;
datos[3] = 45;
datos[4] = 28;

print(datos[0]);
print(datos[1]);
print(datos[2]);
print(datos[3]);
print(datos[4]);

var i = 0;
while (i < 5) {
  print(datos[i]);
  i = i + 1;
}

print(length(datos));

i = 0;
while (i < length(datos)) {
  print(datos[i]);
  i = i + 1;
}

for (var i = 0; i < length(datos); i = i + 1)
  print(datos[i]);

i++;

for (var i = 0; i < length(datos); i++)
  print(datos[i]);

var a = [3, 7, 25, 46, 31, 29];

for (var i = 0; i < length(a); i++)
  print(a[i]);