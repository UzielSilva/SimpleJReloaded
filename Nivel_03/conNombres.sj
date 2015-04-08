final OBJETO = 20;
final ESPACIO = 32;

final X = 20;
final MIN_Y = 0;
final MAX_Y = 23;

putAt(OBJETO, X, MIN_Y);
var y = MIN_Y;
while (y < MAX_Y) {
  pause(.05);
  putAt(ESPACIO, X, y);
  y = y + 1;
  putAt(OBJETO, X, y);
}