var _linea = 0;
source("constantes.sj");
source("variables.sj");
source("util.sj");
source("graficas.sj");
source("audio.sj");
source("proyectil.sj");
source("jugador.sj");
source("monstruos.sj");
source("nivel.sj");
source("items.sj");
source("marcador.sj");

EstadoGlobal() {
  var contador = 0;
  
  accion() {
    botones = readCtrlOne();
    contador++;
    if (tilesAnimacion != null) {
      var i = contador % NUM_FUEGOS;
      arrayPoke(TILE_IMGS + FUEGO * 32,
                tilesAnimacion.fuegos[i], 0, 32);
      i = (contador / 6) % NUM_AMULETOS;
      arrayPoke(TILE_IMGS + AMULETO * 32,
                tilesAnimacion.amuletos[i], 0, 32);
      if ((contador / 6) % 2 == 0) {
        arrayPoke(TILE_IMGS + FLECHA_IZQUIERDA * 32,
                  tilesAnimacion.flechaIzquierda, 0, 32);
        arrayPoke(TILE_IMGS + FLECHA_DERECHA * 32,
                  tilesAnimacion.flechaDerecha, 0, 32);
      } else {
        arrayPoke(TILE_IMGS + FLECHA_IZQUIERDA * 32,
                  tilesAnimacion.vacio, 0, 32);
        arrayPoke(TILE_IMGS + FLECHA_DERECHA * 32,
                  tilesAnimacion.vacio, 0, 32);
      }
    }
  }
  
  return this -> Estado();
}

EstadoInicializando() {
  var anim = ["|", "\\", "-", "/"];
  var contador = 0;
  
  accion() {
    clear();
    showAt("Preparando... " + anim[contador++ % length(anim)],
           7, 10);
    if (!inicializando)
      fsmGlobal.pasaA(EstadoPantallaSimpleJ());
  }
  
  return this -> Estado();
}

EstadoPantallaSimpleJ() {
  var contador;

  accionEntrada() {
    setScreen(simpleJScreenData);
    contador = 125;
  }
  
  accion() {
    if (--contador == 0 || botones != 0)
      fsmGlobal.pasaA(EstadoPantallaIenjinia());
  }
  
  return this -> Estado();
}

EstadoPantallaIenjinia() {
  var contador;

  accionEntrada() {
    setScreen(ienjiniaScreenData);
    contador = 125;
  }
  
  accion() {
    if (--contador == 0 || botones != 0)
      fsmGlobal.pasaA(EstadoPortada());
  }
  
  return this -> Estado();
}

EstadoPortada() {
  accion() {
    if (botones != 0)
      fsmGlobal.pasaA(EstadoIniciaJuego());
  }
  
  accionEntrada() {
    setSmoothScroll(0, 0);
    tilesAnimacion = null;
    setScreen(rogueScreenData);
    fsmGlobal.push(EstadoSueltaBoton());
  }
  
  return this -> Estado();
}

EstadoIniciaJuego() {
  var contador = 0;
  var audioId;
  var pViejo = null;

  accion() {
    contador += 0.8;
    var s = round(contador);
    if (botones != 0 || s == (length(TEXTO_HISTORIA) + 24) * 8) {
      fsmGlobal.pasaA(EstadoNuevoNivel());
      return;
    }
    setSmoothScroll(0, s % 8);
    var p = floor(s / 8 - 24);
    if (p != pViejo) {
      clear();
      for (var i = 0; i < 32; i++)
        poke(24 * 64 + i, ' ');
      for (var i = 0; i < 25; i++) {
        var r = p + i;
        if (r >= 0 && r < length(TEXTO_HISTORIA))
          eShowAt(TEXTO_HISTORIA[r], 0, i);
      }
      pViejo = p;
    }
  }

  accionEntrada() {
    fsmGlobal.push(EstadoSueltaBoton());
    audioId = audioEngine.play(sonidos.MUSICA_HISTORIA);
    mezclaPocionesYHechizos();
    clear();
    instalaTilesJuego();
  }
  
  accionSalida() {
    nivel = 0;
    jugador = Jugador();
    setSmoothScroll(0, 0);
    audioEngine.stop(audioId);
  }
  
  return this -> Estado();
}

EstadoNuevoNivel() {
  var nivelCreado = false;
  
  accion() {
    if (nivelCreado)
      fsmGlobal.pasaA(EstadoFadeOutAJuega());
  }
  
  accionEntrada() {
    setSmoothScroll(0, 0);
    clear();
    aNegros();
    if (!jugador.tieneAmuleto)
      nivel++;
    else {
      nivel--;
      if (nivel == 0) {
        fsmGlobal.pasaA(EstadoGano());
        return;
      }
    }
    showAt("Nivel " + nivel, 12, 13);
    if (jugador.tieneAmuleto)
      putAt(AMULETO, 15, 11);
    if (nivel > jugador.maxNivel)
      jugador.maxNivel = nivel;
    fsmGlobal.timer.addEvent(25,
                              lambda() {
                                creaNivel();
                                nivelCreado = true;
                              });
    fsmGlobal.push(EstadoFadeIn());
  }
  
  return this -> Estado();
}

EstadoFin() {
  accionEntrada() {
    if (entraAMarcador(jugador.oro,
                        jugador.tieneAmuleto))
      fsmGlobal.pasaA(EstadoIniciales());
    else
      fsmGlobal.pasaA(EstadoPortada());
  }
  
  return this -> Estado();
}

EstadoGano() {
  var musica;
  
  accion() {
    if (!audioEngine.isPlaying(musica))
      fsmGlobal.pasaA(EstadoFin());
  }
  
  accionEntrada() {
    setSmoothScroll(0, 0);
    eShowAt("`!Misi`on cumplida!", 8, 12);
    musica = audioEngine.play(sonidos.MUSICA_FIN);
    fsmGlobal.push(EstadoFadeIn());
  }
  
  return this -> Estado();
}

EstadoFadeOutAJuega() {
  accion() {
    fsmGlobal.pasaA(EstadoJuega());
  }
  
  accionEntrada() {
    fsmGlobal.push(EstadoFadeOut());
  }
  
  return this -> Estado();
}

EstadoJuega() {
  var salio = false;
  var fsmJuega;
    
  accion() {
    fsmJuega.accion();
    if (!salio)
      muestraMundo(jugador.posx - JUGADOR_CX,
                  jugador.posy - JUGADOR_CY);
  }
  
  accionEntrada() {
    fsmJuega = FSM(Estado(), EstadoJuegaJugando());
    mapaCambio = true;
    jugador.muestra();
    muestraMundo(jugador.posx - JUGADOR_CX,
                jugador.posy - JUGADOR_CY);
    aNegros();
    fsmGlobal.push(EstadoSueltaBoton());
    fsmGlobal.push(EstadoFadeIn());
  }
  
  accionSalida() {
    jugador.oculta();
    ocultaMonstruos();
    salio = true;
  }
  
  recibeMensaje(msj) {
    switch (msj.tipo) {
      case MSJ_ABRE_PUERTA:
        fsmJuega.pasaA(EstadoJuegaAbrePuerta());
        return true;
      
      case MSJ_TOMA_OBJETO:
        if (msj.objeto == AMULETO) {
          fsmJuega.pasaA(EstadoJuegaObtuvoAmuleto());
          return true;
        }
        break;
      
      case MSJ_MURIO:
        fsmGlobal.pasaA(EstadoFin());
        return true;
    }
    return false;
  }
  
  EstadoJuegaJugando() {
    accion() {
      if (isButtonDown(botones, BOTON_ENTER)) {
        fsmGlobal.pasaA(EstadoFadeOutAItems());
        return;
      }
      jugador.accion();
      accionesMonstruos();
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoJuegaAbrePuerta() {
    accionEntrada() {
      abrePuerta();
      audioEngine.play(sonidos.ABRE_PUERTA);
      fsmJuega.timer.addEvent(25,
                          lambda() {
                            fsmGlobal.pasaA(EstadoNuevoNivel());
                          });
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoJuegaObtuvoAmuleto() {
    accionEntrada() {
      var musica = audioEngine.play(sonidos.MUSICA_AMULETO);
      fsmJuega.timer.addEvent(10,
                            lambda() {
                              fsmGlobal.pasaA(EstadoAmuleto1(musica));
                            });
    }
  
    return this -> Estado() -> super;
  }
  
  return this -> Estado();
}

EstadoFadeOutAItems() {
  accion() {
    if (!isButtonDown(botones, BOTON_ENTER))
      fsmGlobal.pasaA(EstadoItems());
  }
  
  accionEntrada() {
    fsmGlobal.push(EstadoFadeOut());
  }
  
  return this -> Estado();
}

EstadoItems() {
  accion() {
    if (isButtonDown(botones, BOTON_ENTER)) {
      fsmGlobal.pasaA(EstadoFadeOutAJuega());
      return;
    }
    seleccionaItem();
  }
  
  accionEntrada() {
    setSmoothScroll(0, 0);
    renItem = 0;
    colItem = 0;
    muestraItems();
    fsmGlobal.push(EstadoFadeIn());
  }
  
  return this -> Estado();
}

EstadoRecuperaVida() {
  var contador = 0;
    
  accion() {
    if (jugador.vida < jugador.maxVida) {
      if (++contador % 2 == 0) {
        jugador.modificaVida(5);
        audioEngine.play(sonidos.RECUPERA);
        muestraItems();
      }
    } else {
      jugador.quitaBorrachera();
      fsmGlobal.pop();
    }
  }
  
  return this -> Estado();
}

EstadoRecuperaFuerza() {
  var contador = 0;
    
  accion() {
    if (jugador.fuerza < jugador.maxFuerza) {
      if (++contador % 2 == 0) {
        jugador.modificaFuerza(5);
        audioEngine.play(sonidos.RECUPERA);
        muestraItems();
      }
    } else
      fsmGlobal.pop();
  }
  
  return this -> Estado();
}

EstadoAumentaVida() {
  var contador = 0;
  var nuevaMaxVida = clamp(jugador.maxVida + 50, 0, 999);
    
  accion() {
    if (jugador.maxVida < nuevaMaxVida) {
      if (++contador % 2 == 0) {
        jugador.modificaMaxVida(5);
        audioEngine.play(sonidos.RECUPERA);
        muestraItems();
      }
    } else
      fsmGlobal.pasaA(EstadoRecuperaVida());
  }
  
  return this -> Estado();
}

EstadoAumentaFuerza() {
  var contador = 0;
  var nuevaMaxFuerza = clamp(jugador.maxFuerza + 50, 0, 999);
    
  accion() {
    if (jugador.maxFuerza < nuevaMaxFuerza) {
      if (++contador % 2 == 0) {
        jugador.modificaMaxFuerza(5);
        audioEngine.play(sonidos.RECUPERA);
        muestraItems();
      }
    } else
      fsmGlobal.pasaA(EstadoRecuperaFuerza());
  }
  
  return this -> Estado();
}

EstadoPierdeFuerza() {
  var contador = 0;
    
  accion() {
    if (jugador.fuerza > 0) {
      if (++contador % 2 == 0) {
        jugador.modificaFuerza(-5);
        audioEngine.play(sonidos.PIERDE);
        muestraItems();
      }
    } else
      fsmGlobal.pop();
  }
  
  return this -> Estado();
}

EstadoRecuperaArmadura() {
  var contador = 0;
    
  accion() {
    if (jugador.armadura < jugador.maxArmadura) {
      if (++contador % 2 == 0) {
        jugador.modificaArmadura(5);
        audioEngine.play(sonidos.RECUPERA);
        muestraItems();
      }
    } else
      fsmGlobal.pop();
  }
  
  return this -> Estado();
}

EstadoAumentaArmadura() {
  var contador = 0;
  var nuevaMaxArmadura = clamp(jugador.maxArmadura + 50, 0, 999);
    
  accion() {
    if (jugador.maxArmadura < nuevaMaxArmadura) {
      if (++contador % 2 == 0) {
        jugador.modificaMaxArmadura(5);
        audioEngine.play(sonidos.RECUPERA);
        muestraItems();
      }
    } else
      fsmGlobal.pasaA(EstadoRecuperaArmadura());
  }
  
  return this -> Estado();
}

EstadoPierdeArmadura() {
  var contador = 0;
  var nuevaArmadura = clamp(jugador.armadura / 2, 10, 999);
    
  accion() {
    if (jugador.armadura > nuevaArmadura) {
      if (++contador % 2 == 0) {
        jugador.modificaArmadura(-5);
        audioEngine.play(sonidos.PIERDE);
        muestraItems();
      }
    } else
      fsmGlobal.pop();
  }
  
  return this -> Estado();
}

EstadoAmuleto1(musica) {
  accion() {
    if (!isButtonDown(botones, BOTON_ESPACIO))
      fsmGlobal.pasaA(EstadoAmuleto2(musica));
  }
  
  accionEntrada() {
    setSmoothScroll(0, 0);
    clear();
    eShowAt("`!Ya tienes el amuleto!", 5, 6);
    showAt("Ahora debes salir de los", 1, 10);
    showAt("calabozos...", 1, 11);
    eShowAt("`E`S`P`A`C`I`O para continuar", 5, 22);
  }
  
  return this -> Estado();
}

EstadoAmuleto2(musica) {
  accion() {
    if (isButtonDown(botones, BOTON_ESPACIO))
      fsmGlobal.pasaA(EstadoAmuleto3(musica));
  }
  
  return this -> Estado();
}

EstadoAmuleto3(musica) {
  accion() {
    if (!isButtonDown(botones, BOTON_ESPACIO)) {
      audioEngine.stop(musica);
      fsmGlobal.pasaA(EstadoJuega());
    }
  }
  
  return this -> Estado();
}    

EstadoIniciales() {
  accion() {
    capturaIniciales();
  }
  
  accionEntrada() {
    setSmoothScroll(0, 0);
    iniciales[0] = iniciales[1] = iniciales[2] = 0;
    numIniciales = 0;
    colInicial = 0;
    renInicial = 0;
    muestraIniciales();
  }
  
  accionSalida() {
    insertaEnMarcador(iniciales, jugador.maxNivel, jugador.oro,
                      jugador.tieneAmuleto);
  }
  
  return this -> Estado();
}

EstadoMarcador() {
  var evento;
  
  accion() {
    if (botones != 0) {
      fsmGlobal.timer.cancelEvent(evento);
      fsmGlobal.pasaA(EstadoPortada());
    }
  }
  
  accionEntrada() {
    muestraMarcador();
    fsmGlobal.timer.addEvent(1, lambda() {
                                  grabaMarcador();
                                });
    evento = fsmGlobal.timer.addEvent(250,
                                  lambda() {
                                    fsmGlobal.pasaA(EstadoPortada());
                                  });
    fsmGlobal.push(EstadoSueltaBoton());
  }
  
  return this -> Estado();
}

EstadoSueltaBoton() {
  accion() {
    if (botones == 0)
      fsmGlobal.pop();
  }
  
  return this -> Estado();
}

EstadoFadeOut() {
  var v = 1.0;
  
  accion() {
    if (v == 0.0) {
      fsmGlobal.pop();
      return;
    }
    v -= 0.15;
    if (v < 0.0)
      v = 0.0;
    for (var i = 0; i < 16; i++)
      setTileColor(i, round(tilesData.colors[i].red * v),
                      round(tilesData.colors[i].green * v),
                      round(tilesData.colors[i].blue * v));
    for (var i = 0; i < 15; i++)
      setSpriteColor(i, round(spritesData.colors[i].red * v),
                        round(spritesData.colors[i].green * v),
                        round(spritesData.colors[i].blue * v));
  }
  
  return this -> Estado();
}

EstadoFadeIn() {
  var v = 0.0;
  
  accion() {
    if (v == 1.0) {
      fsmGlobal.pop();
      return;
    }
    v += 0.15;
    if (v > 1.0)
      v = 1.0;
    for (var i = 0; i < 16; i++)
      setTileColor(i, round(tilesData.colors[i].red * v),
                      round(tilesData.colors[i].green * v),
                      round(tilesData.colors[i].blue * v));
    for (var i = 0; i < 15; i++)
      setSpriteColor(i, round(spritesData.colors[i].red * v),
                        round(spritesData.colors[i].green * v),
                        round(spritesData.colors[i].blue * v));
  }
  
  return this -> Estado();
}

fsmGlobal = FSM(EstadoGlobal(), EstadoInicializando());
inicializando = true;
    
vbi() {
  fsmGlobal.accion();
}

init();
inicializando = false;
