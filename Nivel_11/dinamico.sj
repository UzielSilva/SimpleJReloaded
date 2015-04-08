setSoundFrequency(0, 4000);
setSoundVolume(0, 15);
setSoundSustain(0, 3);
setSoundAttack(0, 0);
setSoundRelease(0, 0);

soundOn(0);
pause(1);

for (var f = 4000; f < 20000; f = f + 100) {
  setSoundFrequency(0, f);
  pause(.04);
}
for (var f = 20000; f >= 4000; f = f - 100) {
  setSoundFrequency(0, f);
  pause(.04);
}
pause(1);

for (var i = 0; i < 10; i++) {
  for (var f = 4000; f < 9000; f = f + 100) {
    setSoundFrequency(0, f);
    pause(0.01);
  }
  for (var f = 9000; f >= 4000; f = f - 100) {
    setSoundFrequency(0, f);
    pause(0.01);
  }
}
