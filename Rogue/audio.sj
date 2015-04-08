showAt("audio", 0, _linea++);

AudioEngine() {
  var audioEngine = this;
  
  var notas = {};
  {
    aHW(f) {
      return round(f * 23.77723356);
    }
    var nr = exp(log(2) / 12.0);
    var freq = 27.5;
    for (var i = 1; i < 7; i++) {
      var f = freq;
      notas["A" + i] = aHW(f);
      f *= nr;
      notas["A#" + i] = aHW(f);
      notas["Bb" + i] = aHW(f);
      f *= nr;
      notas["B" + i] = aHW(f);
      f *= nr;
      notas["C" + i] = aHW(f);
      f *= nr;
      notas["C#" + i] = aHW(f);
      notas["Db" + i] = aHW(f);
      f *= nr;
      notas["D" + i] = aHW(f);
      f *= nr;
      notas["D#" + i] = aHW(f);
      notas["Eb" + i] = aHW(f);
      f *= nr;
      notas["E" + i] = aHW(f);
      f *= nr;
      notas["F" + i] = aHW(f);
      f *= nr;
      notas["F#" + i] = aHW(f);
      notas["Gb" + i] = aHW(f);
      f *= nr;
      notas["G" + i] = aHW(f);
      f *= nr;
      notas["G#" + i] = aHW(f);
      notas["Ab" + (i + 1)] = aHW(f);
      freq *= 2;
    }
  }
  
  var ruidoBlanco = new array[25600];
  var rbTmp = new array[256];
  var ptr = 0;
  for (var i = 0; i < 25600; i += 4) {
    var v = random(256);
    for (var j = 0; j < 4; j++)
      ruidoBlanco[i + j] = v;
  }
  
  var ruidoRep = new array[256];
  for (var i = 0; i < 256; i++)
    ruidoRep[i] = random(256);
  
  var cuadrado = new array[256];
  for (var i = 0; i < 128; i++)
    cuadrado[i] = -128;
  for (var i = 128; i < 256; i++)
    cuadrado[i] = 127;
  
  var triangulo = new array[256];
  {
    var v = -128;
    for (var i = 0; i < 128; i++) {
      triangulo[i] = v;
      v += 2;
    }
    v = 127;
    for (var i = 128; i < 256; i++) {
      triangulo[i] = v;
      v -= 2;
    }
  }
  
  var seno = new array[256];
  {
    var incr = 3.1415926 * 2 / 256;
    var a = 0;
    for (var i = 0; i < 256; i++) {
      seno[i] = round(sin(a) * 127);
      a += incr;
    }
  }
  
  var ruidosBlancos = Set();
  
  ondaRuidoBlanco(canal) {
    ruidosBlancos.put(canal);
    setSoundWave(canal, rbTmp);
  }
  
  ondaRuidoRep(canal) {
    ruidosBlancos.remove(canal);
    setSoundWave(canal, ruidoRep);
  }
  
  ondaCuadrado(canal) {
    ruidosBlancos.remove(canal);
    setSoundWave(canal, cuadrado);
  }
  
  ondaTriangulo(canal) {
    ruidosBlancos.remove(canal);
    setSoundWave(canal, triangulo);
  }
  
  ondaSeno(canal) {
    ruidosBlancos.remove(canal);
    setSoundWave(canal, seno);
  }
  
  var sequencers = Set();
  var nextId = 1;
  var canales = Queue();
  for (var i = 0; i < 4; i++)
    canales.put(i);
    
  obtenCanal() {
    return canales.get();
  }
  
  devuelveCanal(canal) {
    soundOff(canal);
    ruidosBlancos.remove(canal);
    canales.put(canal);
  }
  
  play(sonido) {
    synchronized (sequencers) {
      if (length(sonido) <= canales.size()) {
        var id = nextId++;
        sequencers.put(Sequencer(id, audioEngine, sonido));
        return id;
      }
    }
    return null;
  }
  
  stop(id) {
    synchronized (sequencers) {
      var tmp = sequencers.toArray();
      for (var i = 0; i < length(tmp); i++)
        if (tmp[i].id == id) {
          tmp[i].liberaCanales();
          return;
        }
    }
  }
  
  isPlaying(id) {
    synchronized (sequencers) {
      var tmp = sequencers.toArray();
      for (var i = 0; i < length(tmp); i++)
        if (tmp[i].id == id)
          return true;
      return false;
    }
  }
  
  accion() {
    ptr += 256;
    if (ptr == 25600)
      ptr = 0;
    arrayCopy(ruidoBlanco, ptr, rbTmp, 0, 256);
    var r = ruidosBlancos.toArray();
    for (var i = 0; i < length(r); i++)
      setSoundWave(r[i], rbTmp);
    var s = sequencers.toArray();
    for (var i = 0; i < length(s); i++) {
      s[i].accion();
      if (s[i].termino)
        sequencers.remove(s[i]);
    }
  }
  
  return this;
}

Sequencer(id, audioEngine, sonido) {
  var sequencer = this;
  var n = length(sonido);
  var trackers = new array[n];
  for (var i = 0; i < n; i++)
    trackers[i] = Tracker(sequencer, sonido[i],
                          audioEngine.obtenCanal());
  var termino = false;
  
  liberaCanales() {
    if (!termino) {
      termino = true;
      for (var i = 0; i < n; i++)
        audioEngine.devuelveCanal(trackers[i].canal);
    }
  }
  
  accion() {
    if (termino)
      return;
    for (var i = 0; i < n; i++)
      trackers[i].accion();
  }
  
  return sequencer;
}

Tracker(sequencer, track, canal) {
  var tracker = this;
  var ptr = 0;
  var fin = length(track);
  var esperando = 0;
  var stack = Stack();
  var etiquetas = {};
  for (var i = 0; i < fin; i++)
    if (isString(track[i]))
      etiquetas[track[i]] = i;
  
  espera(t) {
    esperando = t;
  }
  
  goto(etiqueta) {
    ptr = etiquetas[etiqueta];
  }
  
  gosub(etiqueta) {
    stack.push(ptr);
    ptr = etiquetas[etiqueta];
  }
  
  ret() {
    ptr = stack.pop();
  }
  
  ondaRuidoBlanco() {
    audioEngine.ondaRuidoBlanco(canal);
  }
  
  ondaRuidoRep() {
    audioEngine.ondaRuidoRep(canal);
  }
  
  ondaCuadrado() {
    audioEngine.ondaCuadrado(canal);
  }
  
  ondaTriangulo() {
    audioEngine.ondaTriangulo(canal);
  }
  
  ondaSeno() {
    audioEngine.ondaSeno(canal);
  }
  
  accion() {
    if (esperando != 0) {
      esperando--;
      return;
    }
    if (ptr == fin) {
      sequencer.liberaCanales();
      return;
    }
    while (ptr < fin && esperando == 0) {
      var instr = track[ptr++];
      if (!isString(instr))
        instr.accion(tracker);
    }
  }
  
  return tracker;
}

TAttack(n) {
  accion(tracker) {
    setSoundAttack(tracker.canal, n);
  }
  return this;
}

TDecay(n) {
  accion(tracker) {
    setSoundDecay(tracker.canal, n);
  }
  return this;
}

TSustain(n) {
  accion(tracker) {
    setSoundSustain(tracker.canal, n);
  }
  return this;
}

TRelease(n) {
  accion(tracker) {
    setSoundRelease(tracker.canal, n);
  }
  return this;
}

TVolume(n) {
  accion(tracker) {
    setSoundVolume(tracker.canal, n);
  }
  return this;
}

TFrequency(n) {
  if (isString(n))
    n = audioEngine.notas[n];
  accion(tracker) {
    setSoundFrequency(tracker.canal, n);
  }
  return this;
}

TOn() {
  accion(tracker) {
    soundOn(tracker.canal);
  }
  return this;
}

TOff() {
  accion(tracker) {
    soundOff(tracker.canal);
  }
  return this;
}

TWait(n) {
  accion(tracker) {
    tracker.espera(n);
  }
  return this;
}

TNote(freq, duration) {
  if (isString(freq))
    freq = audioEngine.notas[freq];
  accion(tracker) {
    setSoundFrequency(tracker.canal, freq);
    soundOn(tracker.canal);
    tracker.espera(duration);
  }
  return this;
}

TGoto(e) {
  accion(tracker) {
    tracker.goto(e);
  }
  return this;
}

TGosub(e) {
  accion(tracker) {
    tracker.gosub(e);
  }
  return this;
}

TRet() {
  accion(tracker) {
    tracker.ret();
  }
  return this;
}

TStop() {
  accion(tracker) {
    tracker.sequencer.liberaCanales();
  }
  return this;
}

TOndaRuidoBlanco() {
  accion(tracker) {
    tracker.ondaRuidoBlanco();
  }
  return this;
}

TOndaRuidoRep() {
  accion(tracker) {
    tracker.ondaRuidoRep();
  }
  return this;
}

TOndaCuadrado() {
  accion(tracker) {
    tracker.ondaCuadrado();
  }
  return this;
}

TOndaTriangulo() {
  accion(tracker) {
    tracker.ondaTriangulo();
  }
  return this;
}

TOndaSeno() {
  accion(tracker) {
    tracker.ondaSeno();
  }
  return this;
}

Sonidos() {
  final MUSICA_HISTORIA = [
    [
      TOndaTriangulo(),
      TAttack(150),
      TDecay(250),
      TSustain(1),
      TRelease(100),
      TVolume(12),
      TWait(10),
      "loop",
      TNote("Ab4", 12),
      TNote("Gb3", 12),
      TNote("Eb3", 24),
      TNote("Bb3", 36),
      TOff(),
      TWait(12),
      TNote("Ab3", 12),
      TNote("Ab3", 12),
      TNote("Bb3", 12),
      TNote("Eb3", 12),
      TNote("Ab4", 12),
      TNote("Gb3", 12),
      TNote("Eb3", 24),
      TNote("Bb3", 36),
      TOff(),
      TWait(12),
      TNote("Ab3", 12),
      TNote("Ab3", 12),
      TNote("Bb3", 12),
      TNote("Eb3", 12),
      TNote("Ab4", 12),
      TNote("Gb3", 12),
      TNote("Eb3", 24),
      TNote("Gb3", 36),
      TOff(),
      TWait(12),
      TGoto("loop")
    ],
    [
      TOndaTriangulo(),
      TAttack(10),
      TDecay(100),
      TSustain(0),
      TVolume(10),
      TWait(10),
      "loop",
      TNote("Ab3", 24),
      TNote("Gb2", 24),
      TNote("Eb2", 24),
      TNote("Bb2", 24),
      TNote("Ab2", 24),
      TNote("Ab2", 24),
      TNote("Bb2", 24),
      TNote("Eb2", 24),
      TGoto("loop")
    ]
  ];
  
  final MUSICA_MUERTE = [
    [
      TOndaTriangulo(),
      TAttack(50),
      TDecay(100),
      TSustain(0),
      TVolume(12),
      TWait(10),
      TNote("D4", 12),
      TNote("D4", 9),
      TNote("D4", 3),
      TNote("D4", 12),
      TNote("F4", 9),
      TNote("E4", 3),
      TNote("E4", 9),
      TNote("D4", 3),
      TNote("D4", 9),
      TNote("D4", 3),
      TNote("D4", 24)
    ]
  ];
  
  final MUSICA_AMULETO = [
    [
      TOndaTriangulo(),
      TAttack(150),
      TDecay(250),
      TSustain(0),
      TVolume(12),
      TNote("E4", 12),
      TNote("E4", 12),
      TNote("F4", 12),
      TNote("G4", 12),
      TNote("G4", 12),
      TNote("F4", 12),
      TNote("E4", 12),
      TNote("D4", 12),
      TNote("C4", 6),
      TNote("C4", 18),
      TNote("D4", 12),
      TNote("E4", 12),
      TNote("E4", 18),
      TNote("D4", 6),
      TNote("D4", 24),
      TNote("E4", 6),
      TNote("E4", 6),
      TNote("E4", 12),
      TNote("F4", 12),
      TNote("G4", 12),
      TNote("G4", 12),
      TNote("F4", 12),
      TNote("E4", 12),
      TNote("D4", 12),
      TNote("C4", 6),
      TNote("C4", 18),
      TNote("D4", 12),
      TNote("E4", 12),
      TNote("D4", 18),
      TNote("C4", 6),
      TNote("C4", 24)
    ],
    [
      TOndaSeno(),
      TAttack(10),
      TDecay(100),
      TSustain(0),
      TVolume(15),
      TNote("E3", 12),
      TNote("E3", 12),
      TNote("F3", 12),
      TNote("G3", 12),
      TNote("G3", 12),
      TNote("F3", 12),
      TNote("E3", 12),
      TNote("D3", 12),
      TNote("C3", 6),
      TNote("C3", 18),
      TNote("D3", 12),
      TNote("E3", 12),
      TNote("E3", 18),
      TNote("D3", 6),
      TNote("D3", 24),
      TNote("E3", 6),
      TNote("E3", 6),
      TNote("E3", 12),
      TNote("F3", 12),
      TNote("G3", 12),
      TNote("G3", 12),
      TNote("F3", 12),
      TNote("E3", 12),
      TNote("D3", 12),
      TNote("C3", 6),
      TNote("C3", 18),
      TNote("D3", 12),
      TNote("E3", 12),
      TNote("D3", 18),
      TNote("C3", 6),
      TNote("C3", 24)
    ]
  ];
  
  final MUSICA_FIN = [
    [
      TOndaTriangulo(),
      TAttack(150),
      TDecay(250),
      TSustain(0),
      TVolume(12),
      TNote("D5", 18),
      TNote("F#5", 6),
      TNote("B6", 6),
      TNote("A6", 6),
      TWait(12),
      TNote("A6", 18),
      TNote("F#5", 6),
      TNote("G5", 6),
      TNote("F#5", 6),
      TWait(6),
      TNote("A6", 6),
      TNote("G5", 6),
      TNote("F#5", 6),
      TNote("E5", 12),
      TNote("D5", 6),
      TNote("A5", 6),
      TNote("B5", 6),
      TNote("C#5", 6),
      TNote("D5", 18),
      TNote("A5", 6),
      TNote("B5", 6),
      TNote("A5", 6),
      TWait(12),
      TNote("D5", 18),
      TNote("A5", 6),
      TNote("B5", 6),
      TNote("A5", 6),
      TWait(6),
      TNote("D5", 3),
      TNote("D5", 3),
      TNote("D5", 6),
      TNote("D5", 6),
      TWait(6),
      TNote("D5", 3),
      TNote("D5", 3),
      TNote("D5", 6),
      TNote("D5", 6),
      TWait(6),
      TNote("D5", 6),
      TNote("C#5", 6),
      TNote("D5", 12),
      TNote("C#5", 6),
      TNote("D5", 12)
    ],
    [
      TOndaTriangulo(),
      TAttack(150),
      TDecay(250),
      TSustain(0),
      TVolume(12),
      TNote("F#4", 6),
      TNote("G4", 6),
      TNote("A5", 6),
      TNote("D5", 6),
      TNote("D5", 12),
      TWait(12),
      TNote("F#5", 18),
      TNote("A5", 6),
      TNote("C#5", 6),
      TNote("A5", 6),
      TWait(6),
      TNote("D5", 6),
      TNote("C#5", 6),
      TNote("D5", 12),
      TNote("A5", 6),
      TNote("F#4", 6),
      TNote("F#4", 6),
      TNote("G4", 6),
      TNote("F#4", 6),
      TNote("A5", 18),
      TNote("A5", 6),
      TNote("G4", 6),
      TNote("F#4", 6),
      TWait(12),
      TNote("A5", 18),
      TNote("A5", 6),
      TNote("G4", 6),
      TNote("F#4", 6),
      TWait(6),
      TNote("A5", 3),
      TNote("A5", 3),
      TNote("B5", 6),
      TNote("A5", 6),
      TWait(6),
      TNote("A5", 3),
      TNote("A5", 3),
      TNote("B5", 6),
      TNote("A5", 6),
      TWait(6),
      TNote("A5", 6),
      TNote("G4", 6),
      TNote("F#4", 6),
      TNote("E4", 6),
      TNote("E4", 6),
      TNote("F#4", 12)
    ]
  ];
  
  final MUSICA_ACELERADO = [
    [
      TOndaTriangulo(),
      TAttack(25),
      TDecay(50),
      TSustain(0),
      TVolume(12),
      TNote("C4", 1),
      TNote("C4", 1),
      TNote("C4", 3),
      TNote("C4", 1),
      TNote("C4", 1),
      TNote("C4", 3),
      TNote("C4", 1),
      TNote("C4", 1),
      TNote("F4", 3),
      TNote("G4", 3),
      TNote("A5", 3),
      TNote("C4", 1),
      TNote("C4", 1),
      TNote("C4", 3),
      TNote("C4", 1),
      TNote("C4", 1),
      TNote("F4", 3),
      TNote("A5", 1),
      TNote("A5", 1),
      TNote("G4", 3),
      TNote("E4", 3),
      TNote("C4", 6)
    ]
  ];
  
  final MUSICA_BORRACHO = [
    [
      TOndaTriangulo(),
      TAttack(250),
      TDecay(30),
      TSustain(1),
      TRelease(100),
      TVolume(12),
      TGosub("notas"),
      TOndaSeno(),
      TGosub("notas"),
      TStop(),
      "notas",
      TNote("C#3", 6),
      TNote("E3", 12),
      TNote("C4", 12),
      TNote("B4", 12),
      TNote("A4", 12),
      TNote("E3", 18),
      TRet()
    ]
  ];
  
  final FIN_BORRACHERA = [
    [
      TOndaTriangulo(),
      TAttack(250),
      TDecay(30),
      TSustain(1),
      TRelease(100),
      TVolume(12),
      TNote("C4", 3),
      TNote("E4", 12)
    ]
  ];
  
  final PASOS = [
    [
      TOndaRuidoBlanco(),
      TAttack(5),
      TDecay(30),
      TSustain(0),
      TVolume(1),
      TFrequency(1000),
      "loop",
      TOn(),
      TWait(5),
      TGoto("loop")
    ]
  ];
  
  final PASOS_RAPIDOS = [
    [
      TOndaRuidoBlanco(),
      TAttack(5),
      TDecay(10),
      TSustain(0),
      TVolume(1),
      TFrequency(1000),
      "loop",
      TOn(),
      TWait(2),
      TGoto("loop")
    ]
  ];
  
  final RECOGE = [
    [
      TOndaCuadrado(),
      TAttack(10),
      TDecay(80),
      TSustain(0),
      TVolume(5),
      TNote(15000, 3)
    ]
  ];
  
  final DEJA_ALGO = [
    [
      TOndaCuadrado(),
      TAttack(10),
      TDecay(120),
      TSustain(0),
      TVolume(5),
      TNote("G5", 4),
      TNote("D5", 8)
    ]
  ];
  
  final QUEMA = [
    [
      TOndaRuidoBlanco(),
      TAttack(100),
      TDecay(600),
      TSustain(0),
      TVolume(2),
      TNote(1000, 8)
    ]
  ];
  
  final ABRE_PUERTA = [
    [
      TOndaRuidoRep(),
      TAttack(250),
      TDecay(250),
      TSustain(0),
      TVolume(3),
      TNote(7000, 1),
      TFrequency(6500), TWait(1),
      TFrequency(6000), TWait(1),
      TFrequency(5500), TWait(1),
      TFrequency(5000), TWait(1),
      TFrequency(4500), TWait(1),
      TFrequency(4000), TWait(1),
      TFrequency(3500), TWait(1),
      TFrequency(3000), TWait(1),
      TFrequency(2500), TWait(1),
      TFrequency(2000), TWait(1),
      TFrequency(1500), TWait(1)
    ]
  ];
  
  final DISPARA_FLECHA = [
    [
      TOndaRuidoBlanco(),
      TAttack(10),
      TDecay(150),
      TSustain(0),
      TVolume(3),
      TNote(2000, 5)
    ]
  ];
  
  final NO_HAY_FLECHA = [
    [
      TOndaRuidoBlanco(),
      TAttack(10),
      TDecay(50),
      TSustain(0),
      TVolume(3),
      TNote(2000, 3)
    ]
  ];
  
  final FLECHA_IMPACTO = [
    [
      TOndaRuidoBlanco(),
      TAttack(10),
      TDecay(10),
      TSustain(0),
      TVolume(3),
      TNote(800, 1)
    ]
  ];
  
  final ATAQUE_ESPADA = [
    [
      TOndaRuidoBlanco(),
      TAttack(25),
      TDecay(50),
      TSustain(0),
      TVolume(2),
      TNote(1500, 3)
    ]
  ];
  
  final LETRAS = [
    [
      TOndaRuidoBlanco(),
      TAttack(5),
      TDecay(20),
      TSustain(0),
      TVolume(3),
      TNote(2000, 3)
    ]
  ];
  
  final SELEC_LETRA = [
    [
      TOndaRuidoBlanco(),
      TAttack(5),
      TDecay(20),
      TSustain(0),
      TVolume(3),
      TNote(700, 3)
    ]
  ];
  
  final BUZZ = [
    [
      TOndaCuadrado(),
      TAttack(100),
      TDecay(300),
      TSustain(0),
      TVolume(6),
      TNote(2000, 12)
    ]
  ];
  
  final RECUPERA = [
    [
      TOndaCuadrado(),
      TAttack(10),
      TDecay(65),
      TSustain(0),
      TVolume(6),
      TNote(10000, 2)
    ]
  ];
  
  final PIERDE = [
    [
      TOndaCuadrado(),
      TAttack(70),
      TDecay(5),
      TSustain(0),
      TVolume(6),
      TNote(4000, 2)
    ]
  ];
  
  final MONSTRUO_LASTIMADO = [
    [
      TOndaRuidoBlanco(),
      TAttack(100),
      TDecay(600),
      TSustain(0),
      TVolume(2),
      TNote(10000, 8)
    ]
  ];
  
  final MONSTRUO_MUERTE = [
    [
      TOndaCuadrado(),
      TAttack(230),
      TDecay(250),
      TSustain(0),
      TVolume(2),
      TNote(5000, 1),
      TFrequency(5100), TWait(1),
      TFrequency(5200), TWait(1),
      TFrequency(5300), TWait(1),
      TFrequency(5400), TWait(1),
      TFrequency(5500), TWait(1),
      TFrequency(5600), TWait(1),
      TFrequency(5700), TWait(1),
      TFrequency(5800), TWait(1),
      TFrequency(5900), TWait(1),
      TFrequency(6000), TWait(1),
      TFrequency(6100), TWait(1)
    ]
  ];
  
  final GUSANO_ACELERADO = [
    [
      TOndaCuadrado(),
      TAttack(200),
      TDecay(10),
      TSustain(0),
      TVolume(2),
      TNote("C4", 6)
    ]
  ];
  
  final PREPARA_ESCUPE = [
    [
      TOndaTriangulo(),
      TAttack(100),
      TDecay(10),
      TSustain(0),
      TVolume(14),
      TNote(5000, 1),
      TFrequency(4900), TWait(1),
      TFrequency(4800), TWait(1),
      TFrequency(4700), TWait(1)
    ]
  ];
  
  final ESCUPE = [
    [
      TOndaTriangulo(),
      TAttack(10),
      TDecay(100),
      TSustain(0),
      TVolume(14),
      TNote(4700, 1),
      TFrequency(4800), TWait(1),
      TFrequency(4900), TWait(1),
      TFrequency(5000), TWait(1)
    ]
  ];
  
  final ESCUPE_IMPACTO = [
    [
      TOndaRuidoRep(),
      TAttack(20),
      TDecay(60),
      TSustain(0),
      TVolume(4),
      TNote(500, 2)
    ]
  ];
  
  return this;
}

sfi() {
  if (audioEngine != null)
    audioEngine.accion();
}

initProcs.put(lambda() {
  audioEngine = AudioEngine();
  sonidos = Sonidos();
});

