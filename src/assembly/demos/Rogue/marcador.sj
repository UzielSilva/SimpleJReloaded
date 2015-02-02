showAt("marcador", 0, _linea++);

RenglonMarcador(inicia, nivel, oro, amuleto) {
  var iniciales = [inicia[0], inicia[1], inicia[2]];
  return this;
}

grabaMarcador() {
  var data = new array[512];
  for (var i = 0; i < 512; i++)
    data[i] = 0;
  var ptr = 0;
  for (var i = 0; i<MAX_RENGLONES_MARCADOR && marcador[i] != null; i++) {
    var r = marcador[i];
    data[ptr++] = r.amuleto ? 1 : 2;
    data[ptr++] = r.iniciales[0];
    data[ptr++] = r.iniciales[1];
    data[ptr++] = r.iniciales[2];
    data[ptr++] = r.nivel;
    data[ptr++] = r.oro >> 8;
    data[ptr++] = r.oro & 0xff;
  }
  memCardSave(data);
}

leeMarcador() {
  for (var i = 0; i < MAX_RENGLONES_MARCADOR; i++)
    marcador[i] = null;
  var data = memCardLoad();
  var idx = 0;
  var ptr = 0;
  while (data[ptr] != 0) {
    var iniciales = new array[3];
    var amuleto = data[ptr++] == 1 ? true : false;
    iniciales[0] = data[ptr++];
    iniciales[1] = data[ptr++];
    iniciales[2] = data[ptr++];
    var nivel = data[ptr++];
    var oro = data[ptr++] << 8;
    oro |= data[ptr++];
    marcador[idx++] = RenglonMarcador(iniciales, nivel, oro, amuleto);
  }
}

vaAntes(oro, amuleto, ren) {
  if (amuleto && !ren.amuleto)
    return true;
  if (!amuleto && ren.amuleto)
    return false;
  return oro > ren.oro;
}

entraAMarcador(oro, amuleto) {
  for (var i = 0; i < MAX_RENGLONES_MARCADOR; i++) {
    if (marcador[i] == null)
      return true;
    if (vaAntes(oro, amuleto, marcador[i]))
      return true;
  }
  return false;
}

insertaEnMarcador(iniciales, nivel, oro, amuleto) {
  var idx = 0;
  while (marcador[idx] != null &&
         !vaAntes(oro, amuleto, marcador[idx]))
    idx++;
  for (var i = MAX_RENGLONES_MARCADOR - 1; i > idx; i--)
    marcador[i] = marcador[i - 1];
  marcador[idx] = RenglonMarcador(iniciales, nivel, oro, amuleto);
  idxMarcador = idx;
}

muestraMarcador() {
  clear();
  showAt("Guerreros Legendarios", 5, 1);
  showAt("---------------------", 5, 2);
  showAt("Quien  Nivel    Oro  Amuleto", 2, 5);
  showAt("-----  -----    ---  -------", 2, 6);
  var idx = 0;
  while (idx < MAX_RENGLONES_MARCADOR && marcador[idx] != null) {
    var r = marcador[idx];
    var c = (idx == idxMarcador ? 128 : 0);
    putAt((r.iniciales[0] != 0 ? r.iniciales[0] : ' ') + c,
          3, idx + 7);
    putAt((r.iniciales[1] != 0 ? r.iniciales[1] : ' ') + c,
          4, idx + 7);
    putAt((r.iniciales[2] != 0 ? r.iniciales[2] : ' ') + c,
          5, idx + 7);
    showAt(aLaIzquierda(r.nivel, 2), 10, idx + 7);
    showAt(aLaIzquierda(r.oro, 4), 17, idx + 7);
    showAt(r.amuleto ? "Si" : "No", 25, idx + 7);
    idx++;
  }
}

muestraIniciales() {
  clear();
  showAt("Escribe tus iniciales", 5, 1);
  for (var i = 0; i < 3; i++)
    putAt(iniciales[i] != 0 ? iniciales[i] : '_', 13 + i, 3);
  for (var ren = 0; ren < 4; ren++)
    for (var col = 0; col < 7; col++) {
      var ch = ren * 7 + col + 65;
      if (ren == renInicial && col == colInicial)
        ch += 128;
      putAt(ch, 9 + col * 2, 9 + ren * 2);
    }
  eShowAt("Usa las flechas para moverte", 0, 19);
  eShowAt("y `E`S`P`A`C`I`O para seleccionar.", 0, 20);
  eShowAt("Al terminar, selecciona \\.", 0, 23);
}

capturaIniciales() {
  var presionado = false;
  if (botones == BOTON_ESPACIO) {
    if (renInicial == 3 && colInicial == 6) {
      audioEngine.play(sonidos.SELEC_LETRA);
      fsmGlobal.pasaA(EstadoMarcador());
      return;
    }
    if (renInicial == 3 && colInicial == 5) {
      if (numIniciales > 0) {
        iniciales[--numIniciales] = 0;
        audioEngine.play(sonidos.SELEC_LETRA);
      } else
        audioEngine.play(sonidos.BUZZ);
    } else if (numIniciales < 3) {
      iniciales[numIniciales++] = renInicial * 7 + colInicial + 65;
      audioEngine.play(sonidos.SELEC_LETRA);
    } else
      audioEngine.play(sonidos.BUZZ);
    presionado = true;
  }
  if (botones == BOTON_ARRIBA) {
    renInicial--;
    if (renInicial < 0)
      renInicial = 3;
    presionado = true;
    audioEngine.play(sonidos.LETRAS);
  }
  if (botones == BOTON_ABAJO) {
    renInicial++;
    if (renInicial > 3)
      renInicial = 0;
    presionado = true;
    audioEngine.play(sonidos.LETRAS);
  }
  if (botones == BOTON_IZQUIERDA) {
    colInicial--;
    if (colInicial < 0)
      colInicial = 6;
    presionado = true;
    audioEngine.play(sonidos.LETRAS);
  }
  if (botones == BOTON_DERECHA) {
    colInicial++;
    if (colInicial > 6)
      colInicial = 0;
    presionado = true;
    audioEngine.play(sonidos.LETRAS);
  }
  muestraIniciales();
  if (presionado)
    fsmGlobal.push(EstadoSueltaBoton());
}

initProcs.put(lambda() {
                leeMarcador();
              });
