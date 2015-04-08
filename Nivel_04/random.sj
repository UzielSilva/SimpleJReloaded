final PUNTO = 46;
final ESPACIO = 32;

setBackground(0, 0, 0);

while (true) {
  var x = random(32);
  var y = random(24);
  putAt(PUNTO, x, y);
  
  x = random(32);
  y = random(24);
  putAt(ESPACIO, x, y);
}