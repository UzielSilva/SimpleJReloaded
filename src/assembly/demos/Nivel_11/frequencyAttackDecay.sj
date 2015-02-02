frecuencias() {
  setSoundFrequency(0, 1000);
  soundOn(0);
  pause(1);
  
  setSoundFrequency(0, 5000);
  soundOn(0);
  pause(1);
  
  setSoundFrequency(0, 10000);
  soundOn(0);
  pause(1);
  
  setSoundFrequency(0, 40000);
  soundOn(0);
  pause(1);
}

frecuencias();

setSoundDecay(0, 600);
frecuencias();

setSoundAttack(0, 300);
frecuencias();

setSoundDecay(0, 10);
frecuencias();