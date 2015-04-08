pruebaSonido() {
  soundOn(0);
  pause(2);
  soundOff(0);
  pause(1);
}

setSoundFrequency(0, 3000);
setSoundAttack(0, 100);

setSoundSustain(0, 3);
setSoundRelease(0, 700);
pruebaSonido();

setSoundSustain(0, 1);
setSoundDecay(0, 300);
pruebaSonido();

setSoundVolume(0, 3);
pruebaSonido();

