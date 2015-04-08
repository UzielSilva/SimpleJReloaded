putAt(65, 16, 12);
showAt("ABCD", 4, 20);
pause(.5);

setTilePixels(65, [1, 1, 1, 1, 1, 1, 1, 1,
                   1, 0, 0, 0, 0, 0, 0, 1,
                   1, 0, 1, 1, 1, 1, 0, 1,
                   1, 0, 1, 0, 0, 1, 0, 1,
                   1, 0, 1, 0, 0, 1, 0, 1,
                   1, 0, 1, 1, 1, 1, 0, 1,
                   1, 0, 0, 0, 0, 0, 0, 1,
                   1, 1, 1, 1, 1, 1, 1, 1]);
pause(.5);

putAt(65, 0, 0);
showAt("ABABABA", 10, 20);
pause(.5);

setTilePixels(65, [0, 0, 1, 1, 1, 1, 0, 0,
                   0, 1, 1, 1, 1, 1, 1, 0,
                   1, 0, 0, 1, 1, 0, 0, 1,
                   1, 0, 0, 1, 1, 0, 0, 1,
                   1, 0, 0, 1, 1, 0, 0, 1,
                   1, 0, 1, 0, 0, 1, 0, 1,
                   0, 0, 1, 0, 0, 1, 0, 0,
                   0, 1, 1, 0, 1, 1, 0, 0]);
pause(.5);

setTileColor(0, 0, 0, 0);
setTileColor(1, 31, 31, 0);
pause(.5);

setTilePixels(65, [ 0,  1,  2,  3,  4,  5,  6,  7,
                    0,  1,  2,  3,  4,  5,  6,  7,
                    0,  1,  2,  3,  4,  5,  6,  7,
                    0,  1,  2,  3,  4,  5,  6,  7,
                    8,  9, 10, 11, 12, 13, 14, 15,
                    8,  9, 10, 11, 12, 13, 14, 15,
                    8,  9, 10, 11, 12, 13, 14, 15,
                    8,  9, 10, 11, 12, 13, 14, 15]);

setTileColor(2, 31, 24, 0);
setTileColor(3, 31, 16, 0);
setTileColor(4, 31, 8, 0);
setTileColor(5, 31, 0, 0);
setTileColor(6, 31, 0, 8);
setTileColor(7, 31, 0, 16);
setTileColor(8, 31, 0, 24);
setTileColor(9, 31, 0, 31);
setTileColor(10, 24, 0, 31);
setTileColor(11, 16, 0, 31);
setTileColor(12, 8, 0, 31);
setTileColor(13, 0, 0, 31);
setTileColor(14, 0, 16, 31);
setTileColor(15, 0, 31, 31);
pause(.5);           

for (var y = 0; y < 24; y++)
  for (var x = 0; x < 32; x++)
    putAt(65, x, y);
pause(1);
