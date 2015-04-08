for (var i = 0; i < 4; i++) {
  setSoundVolume(i, 5);
  setSoundSustain(i, 3);
  soundOn(i);
}

sfi() {
  for (var i = 0; i < 4; i++)
    setSoundFrequency(i, random(10000) + 5000);
}