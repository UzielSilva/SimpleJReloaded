showAt("items", 0, _linea++);

muestraItems() {
  clear();
  showAt("Pociones Hechizos", 0, 1);
  showAt("-------- --------", 0, 2);
  for (var i = 0; i < POCIONES; i++) {
    putAt(i + POCION_ROJA, 1, i * 2 + 3);
    putAt('x', 2, i * 2 + 3);
    showAt(ceros(jugador.pociones[i], 2), 3, i * 2 + 3);
  }
  for (var i = 0; i < HECHIZOS; i++) {
    putAt(i + HECHIZO_ROJO, 10, i * 2 + 3);
    putAt('x', 11, i * 2 + 3);
    showAt(ceros(jugador.hechizos[i], 2), 12, i * 2 + 3);
  }
  putAt(FLECHA_IZQUIERDA, colItem * 9, renItem * 2 + 3);
  putAt(FLECHA_DERECHA, colItem * 9 + 5, renItem * 2 + 3);
  
  if (jugador.itemRecuperaVida != null) {
    putAt('+', 25, 2);
    putAt(jugador.itemRecuperaVida, 26, 2);
  }
  if (jugador.itemAumentaVida != null) {
    putAt('+', 29, 2);
    putAt(jugador.itemAumentaVida, 30, 2);
  }
  showAt("Vida: " + ceros(jugador.vida, 3) + "/" +
                    ceros(jugador.maxVida, 3),
         19, 3);
  
  if (jugador.itemPierdeArmadura != null) {
    putAt('-', 22, 5);
    putAt(jugador.itemPierdeArmadura, 23, 5);
  }
  if (jugador.itemRecuperaArmadura != null) {
    putAt('+', 25, 5);
    putAt(jugador.itemRecuperaArmadura, 26, 5);
  }
  if (jugador.itemAumentaArmadura != null) {
    putAt('+', 29, 5);
    putAt(jugador.itemAumentaArmadura, 30, 5);
  }
  showAt("Armadura: " + ceros(jugador.armadura, 3) + "/" +
                        ceros(jugador.maxArmadura, 3),
         15, 6);
  
  if (jugador.itemPierdeFuerza != null) {
    putAt('-', 22, 8);
    putAt(jugador.itemPierdeFuerza, 23, 8);
  }
  if (jugador.itemRecuperaFuerza != null) {
    putAt('+', 25, 8);
    putAt(jugador.itemRecuperaFuerza, 26, 8);
  }
  if (jugador.itemAumentaFuerza != null) {
    putAt('+', 29, 8);
    putAt(jugador.itemAumentaFuerza, 30, 8);
  }
  showAt("Fuerza: " + ceros(jugador.fuerza, 3) + "/" +
         ceros(jugador.maxFuerza, 3),
         17, 9);
  
  showAt("Flechas:   " + ceros(jugador.flechas, 2) + "/" +
         ceros(jugador.maxFlechas, 2),
         16, 11);
  
  showAt("Oro:    " + ceros(jugador.oro, 4), 20, 12);
  
  showAt("Llave:", 18, 14);
  if (jugador.tieneLlave)
    putAt(LLAVE, 27, 14);
  
  showAt("Amuleto:", 16, 15);
  if (jugador.tieneAmuleto)
    putAt(AMULETO, 27, 15);
  
  if (jugador.itemBorracho != null) {
    putAt(jugador.itemBorracho, 1, 16);
    showAt(": Whisky", 2, 16);
  }
  if (jugador.itemAcelerado != null) {
    putAt(jugador.itemAcelerado, 1, 18);
    eShowAt(": Caf`e Negro", 2, 18);
  }
  
  eShowAt("Usa las flechas para moverte", 0, 20);
  eShowAt("y `E`S`P`A`C`I`O para seleccionar.", 0, 21);
  eShowAt("Usa `E`N`T`E`R para regresar.", 0, 23);
}

seleccionaItem() {
  var presionado = false;
  if (botones == BOTON_ESPACIO) {
    switch (colItem) {
      case 0:
        if (jugador.pociones[renItem] > 0) {
          var accion = accionesPociones[renItem];
          if (accion(POCION_ROJA + renItem))
            jugador.pociones[renItem]--;
        } else
          audioEngine.play(sonidos.BUZZ);
        break;
      case 1:
        if (jugador.hechizos[renItem] > 0) {
          var accion = accionesHechizos[renItem];
          if (accion(HECHIZO_ROJO + renItem))
            jugador.hechizos[renItem]--;
        } else
          audioEngine.play(sonidos.BUZZ);
        break;
    }
    presionado = true;
  }
  if (botones == BOTON_ARRIBA) {
    renItem--;
    if (renItem < 0)
      renItem = 5;
    presionado = true;
    audioEngine.play(sonidos.LETRAS);
  }
  if (botones == BOTON_ABAJO) {
    renItem++;
    if (renItem > 5)
      renItem = 0;
    presionado = true;
    audioEngine.play(sonidos.LETRAS);
  }
  if (botones == BOTON_IZQUIERDA) {
    colItem--;
    if (colItem < 0)
      colItem = 1;
    presionado = true;
    audioEngine.play(sonidos.LETRAS);
  }
  if (botones == BOTON_DERECHA) {
    colItem++;
    if (colItem > 1)
      colItem = 0;
    presionado = true;
    audioEngine.play(sonidos.LETRAS);
  }
  muestraItems();
  if (presionado)
    fsmGlobal.push(EstadoSueltaBoton());
}

pocionRecuperaVida(tileItem) {
  if (jugador.vida < jugador.maxVida) {
    fsmGlobal.push(EstadoRecuperaVida());
    jugador.itemRecuperaVida = tileItem;
    return true;
  } else {
    audioEngine.play(sonidos.BUZZ);
    return false;
  }
}


pocionRecuperaFuerza(tileItem) {
  if (jugador.fuerza < jugador.maxFuerza) {
    fsmGlobal.push(EstadoRecuperaFuerza());
    jugador.itemRecuperaFuerza = tileItem;
    return true;
  } else {
    audioEngine.play(sonidos.BUZZ);
    return false;
  }
}


pocionAumentaVida(tileItem) {
  if (jugador.maxVida < 999 || jugador.vida < jugador.maxVida) {
    fsmGlobal.push(EstadoAumentaVida());
    jugador.itemAumentaVida = tileItem;
    return true;
  } else {
    audioEngine.play(sonidos.BUZZ);
    return false;
  }
}


pocionAcelerado(tileItem) {
  if (jugador.velocidad == JG_LENTO) {
    jugador.recibeMensaje(Mensaje(MSJ_ACELERADO));
    jugador.itemAcelerado = tileItem;
    return true;
  } else {
    audioEngine.play(sonidos.BUZZ);
    return false;
  }
}

pocionPierdeFuerza(tileItem) {
  if (jugador.fuerza > 0) {
    fsmGlobal.push(EstadoPierdeFuerza());
    jugador.itemPierdeFuerza = tileItem;
    return true;
  } else {
    audioEngine.play(sonidos.BUZZ);
    return false;
  }
}


pocionBorracho(tileItem) {
  if (jugador.eventoBorracho == null) {
    jugador.recibeMensaje(Mensaje(MSJ_BORRACHO));
    jugador.itemBorracho = tileItem;
    return true;
  } else {
    audioEngine.play(sonidos.BUZZ);
    return false;
  }
}

hechizoRecuperaArmadura(tileItem) {
  if (jugador.armadura < jugador.maxArmadura) {
    fsmGlobal.push(EstadoRecuperaArmadura());
    jugador.itemRecuperaArmadura = tileItem;
    return true;
  } else {
    audioEngine.play(sonidos.BUZZ);
    return false;
  }
}

hechizoAumentaArmadura(tileItem) {
  if (jugador.maxArmadura < 999 ||
      jugador.armadura < jugador.maxArmadura) {
    fsmGlobal.push(EstadoAumentaArmadura());
    jugador.itemAumentaArmadura = tileItem;
    return true;
  } else {
    audioEngine.play(sonidos.BUZZ);
    return false;
  }
}

hechizoPierdeArmadura(tileItem) {
  if (jugador.armadura > 10) {
    fsmGlobal.push(EstadoPierdeArmadura());
    jugador.itemPierdeArmadura = tileItem;
    return true;
  } else {
    audioEngine.play(sonidos.BUZZ);
    return false;
  }
}

hechizoAumentaFuerza(tileItem) {
  if (jugador.maxFuerza < 999 || jugador.fuerza < jugador.maxFuerza) {
    fsmGlobal.push(EstadoAumentaFuerza());
    jugador.itemAumentaFuerza = tileItem;
    return true;
  } else {
    audioEngine.play(sonidos.BUZZ);
    return false;
  }
}


hechizo4(tileItem) {
  return hechizoAumentaFuerza(tileItem);
}

hechizo5(tileItem) {
  return hechizoAumentaFuerza(tileItem);
}

mezclaPocionesYHechizos() {
  accionesPociones = mezcla(accionesPociones);
  accionesHechizos = mezcla(accionesHechizos);
}

initProcs.put(lambda() {
    accionesPociones = [
      pocionRecuperaVida,
      pocionRecuperaFuerza,
      pocionAumentaVida,
      pocionAcelerado,
      pocionPierdeFuerza,
      pocionBorracho
    ];
    accionesHechizos = [
      hechizoRecuperaArmadura,
      hechizoAumentaArmadura,
      hechizoPierdeArmadura,
      hechizoAumentaFuerza,
      hechizo4,
      hechizo5
    ];
  });
