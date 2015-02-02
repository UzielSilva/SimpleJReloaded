// Primera seccion
putAt(65, 20, 0);
putAt(66, 21, 0);
putAt(67, 22, 0);
pause(.5);

// Segunda seccion
putAt(97, 20, 1);
putAt(98, 21, 1);
putAt(99, 22, 1);
pause(.5);

// Tercera seccion
putAt(48, 20, 2);
putAt(49, 21, 2);
putAt(50, 22, 2);
pause(.5);

// Cuarta seccion
putAt(42, 20, 3);
putAt(43, 21, 3);
putAt(44, 22, 3);
pause(.5);

// Quinta seccion
putAt(0, 20, 4);
putAt(20, 21, 4);
putAt(127, 22, 4);
pause(.5);

// Sexta seccion
var tile = 0;
while (tile < 256) {
  var x = tile % 16;
  var y = tile / 16;
  putAt(tile, x, y);
  tile = tile + 1;
}
pause(.5);

// Septima seccion
putAt(8, 22, 9);
putAt(10, 23, 9);
putAt(25, 22, 10);
putAt(153, 23, 10);
putAt(8, 21, 11);
putAt(160, 22, 11);
putAt(160, 23, 11);
putAt(10, 24, 11);
putAt(8, 20, 12);
putAt(160, 21, 12);
putAt(160, 22, 12);
putAt(160, 23, 12);
putAt(160, 24, 12);
putAt(10, 25, 12);
pause(.5);

// Octava seccion
putAt(20, 28, 0);
var y = 0;
while (y < 23) {
  pause(.05);
  putAt(32, 28, y);
  y = y + 1;
  putAt(20, 28, y);
}
