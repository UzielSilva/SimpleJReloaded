showAt("Demo vbi()", 12, 12);
pause(.5);

setSmallSpriteImage(0, 20);
var x = -8;

vbi() {
  putSpriteAt(0, x, 12 * 8);
  x++;
  if (x == 256)
    x = -8;
}  

while (true) {
  setBackground(31, 0, 0);
  pause(.5);
  setBackground(0, 0, 31);
  pause(.5);
}