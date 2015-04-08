setTileColor(0, 24, 0, 0);
setTileColor(1, 15, 15, 15);

setSpriteColor(0, 15, 0, 15);
setSpriteColor(1, 31, 31, 31);
setSpriteColor(2, 31, 0, 0);
setSpriteColor(3, 24, 0, 31);
setSpriteColor(4, 0, 0, 0);

setTilePixels(0, [0, 0, 0, 1, 0, 0, 0, 0,
                  0, 0, 0, 1, 0, 0, 0, 0,
                  0, 0, 0, 1, 0, 0, 0, 0,
                  1, 1, 1, 1, 1, 1, 1, 1,
                  0, 0, 0, 0, 0, 0, 0, 1,
                  0, 0, 0, 0, 0, 0, 0, 1,
                  0, 0, 0, 0, 0, 0, 0, 1,
                  1, 1, 1, 1, 1, 1, 1, 1]);


setLargeSpritePixels(0,
  [15, 15, 15, 15, 15,  0,  0,  0,  0,  0,  0, 15, 15, 15, 15, 15,
   15, 15, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15, 15, 15,
   15, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15, 15,
   15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15,
   15,  0,  0,  1,  1,  1,  0,  0,  0,  0,  1,  1,  1,  0,  0, 15,
    0,  0,  0,  1,  4,  1,  0,  0,  0,  0,  1,  4,  1,  0,  0,  0,
    0,  0,  0,  1,  1,  1,  0,  0,  0,  0,  1,  1,  1,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  2,  2,  2,  2,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  2,  2,  2,  2,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
   15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15,
   15,  0,  0,  0,  0,  3,  3,  3,  3,  3,  3,  0,  0,  0,  0, 15,
   15, 15,  0,  0,  0,  0,  3,  3,  3,  3,  0,  0,  0,  0, 15, 15,
   15, 15, 15,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 15, 15, 15,
   15, 15, 15, 15, 15,  0,  0,  0,  0,  0,  0, 15, 15, 15, 15, 15]);


for (var r = 0; r < 24; r++)
  for (var c = 0; c < 64; c++)
    putAt(0, c, r);

var xpos = new array[32];
var ypos = new array[32];
var dx = new array[32];
var dy = new array[32];
for (var i = 0; i < 32; i++) {
    xpos[i] = random(240);
    ypos[i] = random(176);
    dx[i] = random(5) - 2;
    dy[i] = random(5) - 2;
}

var scroll = 0;
var n = 31;
var dn = -1;

vbi() {
    for (var i = 0; i < 32; i++) {
        xpos[i] += dx[i];
        ypos[i] += dy[i];
        if (xpos[i] <= 0 || xpos[i] >= 240)
            dx[i] = -dx[i];
        if (ypos[i] <= 0 || ypos[i] >= 176)
            dy[i] = -dy[i];
        putSpriteAt(i, xpos[i], ypos[i]);
    }
    n += dn;
    if (n <= 0 || n >= 31)
        dn = -dn;
    setSpriteColor(1, n, n, n);
    setSpriteColor(4, 31 - n, 31 - n, 31 - n);
    setSmoothScroll(scroll++, 0);
}
