showAt("jugador", 0, _linea++);

digitos[0] = lambda(spriteImg, pos, r) {
  var base = SPRITE_IMGS + spriteImg * 128 + (3 - pos) * 2 + r * 48;
  pokew(base, 0xfeee);
  pokew(base + 8, 0xfefe);
  pokew(base + 16, 0xfefe);
  pokew(base + 24, 0xfefe);
  pokew(base + 32, 0xfeee);
};

digitos[1] = lambda(spriteImg, pos, r) {
  var base = SPRITE_IMGS + spriteImg * 128 + (3 - pos) * 2 + r * 48;
  pokew(base, 0xfffe);
  pokew(base + 8, 0xfffe);
  pokew(base + 16, 0xfffe);
  pokew(base + 24, 0xfffe);
  pokew(base + 32, 0xfffe);
};

digitos[2] = lambda(spriteImg, pos, r) {
  var base = SPRITE_IMGS + spriteImg * 128 + (3 - pos) * 2 + r * 48;
  pokew(base, 0xfeee);
  pokew(base + 8, 0xfffe);
  pokew(base + 16, 0xfeee);
  pokew(base + 24, 0xfeff);
  pokew(base + 32, 0xfeee);
};

digitos[3] = lambda(spriteImg, pos, r) {
  var base = SPRITE_IMGS + spriteImg * 128 + (3 - pos) * 2 + r * 48;
  pokew(base, 0xfeee);
  pokew(base + 8, 0xfffe);
  pokew(base + 16, 0xfeee);
  pokew(base + 24, 0xfffe);
  pokew(base + 32, 0xfeee);
};

digitos[4] = lambda(spriteImg, pos, r) {
  var base = SPRITE_IMGS + spriteImg * 128 + (3 - pos) * 2 + r * 48;
  pokew(base, 0xfefe);
  pokew(base + 8, 0xfefe);
  pokew(base + 16, 0xfeee);
  pokew(base + 24, 0xfffe);
  pokew(base + 32, 0xfffe);
};

digitos[5] = lambda(spriteImg, pos, r) {
  var base = SPRITE_IMGS + spriteImg * 128 + (3 - pos) * 2 + r * 48;
  pokew(base, 0xfeee);
  pokew(base + 8, 0xfeff);
  pokew(base + 16, 0xfeee);
  pokew(base + 24, 0xfffe);
  pokew(base + 32, 0xfeee);
};

digitos[6] = lambda(spriteImg, pos, r) {
  var base = SPRITE_IMGS + spriteImg * 128 + (3 - pos) * 2 + r * 48;
  pokew(base, 0xfeee);
  pokew(base + 8, 0xfeff);
  pokew(base + 16, 0xfeee);
  pokew(base + 24, 0xfefe);
  pokew(base + 32, 0xfeee);
};

digitos[7] = lambda(spriteImg, pos, r) {
  var base = SPRITE_IMGS + spriteImg * 128 + (3 - pos) * 2 + r * 48;
  pokew(base, 0xfeee);
  pokew(base + 8, 0xfffe);
  pokew(base + 16, 0xfffe);
  pokew(base + 24, 0xfffe);
  pokew(base + 32, 0xfffe);
};

digitos[8] = lambda(spriteImg, pos, r) {
  var base = SPRITE_IMGS + spriteImg * 128 + (3 - pos) * 2 + r * 48;
  pokew(base, 0xfeee);
  pokew(base + 8, 0xfefe);
  pokew(base + 16, 0xfeee);
  pokew(base + 24, 0xfefe);
  pokew(base + 32, 0xfeee);
};

digitos[9] = lambda(spriteImg, pos, r) {
  var base = SPRITE_IMGS + spriteImg * 128 + (3 - pos) * 2 + r * 48;
  pokew(base, 0xfeee);
  pokew(base + 8, 0xfefe);
  pokew(base + 16, 0xfeee);
  pokew(base + 24, 0xfffe);
  pokew(base + 32, 0xfeee);
};

dibujaNumeroEnSprite(spriteImg, n, r, digs) {
  for (var pos = 0; pos < digs; pos++) {
    var p = digitos[n % 10];
    p(spriteImg, pos, r);
    n /= 10;
  }
}

Jugador() {
  var yo = this;
  var posx, posy;
  var ancho = 10;
  var alto = 16;
  var dir = IZQUIERDA;
  var contadorAnimacion = 0;
  var velocidad = JG_LENTO;
  var arma = ESPADA;
  var flecha = null;
  var atacando = false;
  var lastimado = false;
  var muerto = false;
  var tieneAmuleto = false;
  var vida, maxVida;
  var armadura, maxArmadura;
  var fuerza, maxFuerza;
  var flechas, maxFlechas;
  var tieneLlave = false;
  var eventoLastimado = null;
  var oro;
  var lugar;
  var fsm;
  var spriteMuerte;
  var maxNivel = 0;
  var audioPasos = null;
  var caminando = 0;
  var descansando = 0;
  var pociones = new array[POCIONES];
  var hechizos = new array[HECHIZOS];
  var dirs = DIRECCIONES;
  var eventoBorracho = null;
  var ruido;
  var itemRecuperaVida, itemAumentaVida;
  var itemRecuperaFuerza, itemAumentaFuerza, itemPierdeFuerza;
  var itemRecuperaArmadura, itemAumentaArmadura, itemPierdeArmadura;
  var itemBorracho, itemAcelerado;
  
  reset() {
    vida = maxVida = MAX_VIDA_INICIAL;
    dibujaNumeroEnSprite(SPRITE_VIDA_IMG, vida, 0, 3);
    dibujaNumeroEnSprite(SPRITE_VIDA_IMG, maxVida, 1, 3);
    armadura = maxArmadura = MAX_ARMADURA_INICIAL;
    dibujaNumeroEnSprite(SPRITE_ARMADURA_IMG, armadura, 0, 3);
    dibujaNumeroEnSprite(SPRITE_ARMADURA_IMG, maxArmadura, 1, 3);
    fuerza = maxFuerza = MAX_FUERZA_INICIAL;
    dibujaNumeroEnSprite(SPRITE_FUERZA_IMG, fuerza, 0, 3);
    dibujaNumeroEnSprite(SPRITE_FUERZA_IMG, maxFuerza, 1, 3);
    flechas = (maxFlechas = MAX_FLECHAS_INICIAL) / 2;
    dibujaNumeroEnSprite(SPRITE_FLECHAS_IMG, flechas, 0, 2);
    dibujaNumeroEnSprite(SPRITE_FLECHAS_IMG, maxFlechas, 1, 2);
    oro = 0;
    dibujaNumeroEnSprite(SPRITE_ORO_IMG, oro, 0, 4);
    fsm = FSM(Estado(), EstadoNormal());
    for (var i = 0; i < POCIONES; i++)
      pociones[i] = 0;
    for (var i = 0; i < HECHIZOS; i++)
      hechizos[i] = 0;
    itemRecuperaVida = null;
    itemAumentaVida = null;
    itemRecuperaFuerza = null;
    itemAumentaFuerza = null;
    itemPierdeFuerza = null;
    itemRecuperaArmadura = null;
    itemAumentaArmadura = null;
    itemPierdeArmadura = null;
    itemBorracho = null;
    itemAcelerado = null;
  }
  
  modificaVida(n) {
    vida = clamp(vida + n, 0, maxVida);
    dibujaNumeroEnSprite(SPRITE_VIDA_IMG, vida, 0, 3);
  }
  
  modificaMaxVida(n) {
    maxVida = clamp(maxVida + n, 0, 999);
    dibujaNumeroEnSprite(SPRITE_VIDA_IMG, maxVida, 1, 3);
  }
  
  modificaArmadura(n) {
    armadura = clamp(armadura + n, 0, maxArmadura);
    dibujaNumeroEnSprite(SPRITE_ARMADURA_IMG, armadura, 0, 3);
  }
  
  modificaMaxArmadura(n) {
    maxArmadura = clamp(maxArmadura + n, 0, 999);
    dibujaNumeroEnSprite(SPRITE_ARMADURA_IMG, maxArmadura, 1, 3);
  }
  
  modificaFuerza(n) {
    fuerza = clamp(fuerza + n, 0, maxFuerza);
    dibujaNumeroEnSprite(SPRITE_FUERZA_IMG, fuerza, 0, 3);
  }
  
  modificaMaxFuerza(n) {
    maxFuerza = clamp(maxFuerza + n, 0, 999);
    dibujaNumeroEnSprite(SPRITE_FUERZA_IMG, maxFuerza, 1, 3);
  }
  
  modificaFlechas(n) {
    flechas = clamp(flechas + n, 0, maxFlechas);
    dibujaNumeroEnSprite(SPRITE_FLECHAS_IMG, flechas, 0, 2);
  }
  
  modificaMaxFlechas(n) {
    maxFlechas = clamp(maxFlechas + n, 0, 99);
    dibujaNumeroEnSprite(SPRITE_FLECHAS_IMG, maxFlechas, 1, 2);
  }
  
  modificaOro(n) {
    oro = clamp(oro + n, 0, 9999);
    dibujaNumeroEnSprite(SPRITE_ORO_IMG, oro, 0, 4);
  }

  checaPiso() {
    var p = hayAlgo(yo);
    if (p != null) {
      var objeto = mapa[p.y][p.x];
      switch (objeto) {
        case FLECHAS:
            if (flechas < maxFlechas) {
              modificaFlechas(random(3) + 3);
              modificaMapa(p, PISO);
              audioEngine.play(sonidos.RECOGE);
              fsmGlobal.recibeMensaje(MensajeTomaObjeto(objeto));
            }
          break;
        
        case ORO:
          modificaOro(random(10) + 5);
          modificaMapa(p, PISO);
          audioEngine.play(sonidos.RECOGE);
          fsmGlobal.recibeMensaje(MensajeTomaObjeto(objeto));
          break;
        
        case LLAVE:
          tieneLlave = true;
          modificaMapa(p, PISO);
          audioEngine.play(sonidos.RECOGE);
          fsmGlobal.recibeMensaje(MensajeTomaObjeto(objeto));
          break;
        
        case POCION_ROJA:
        case POCION_AZUL:
        case POCION_VERDE:
        case POCION_CIAN:
        case POCION_MAGENTA:
        case POCION_AMARILLA:
          var idx = mapa[p.y][p.x] - POCION_ROJA;
          if (pociones[idx] < MAX_POCIONES) {
            pociones[idx]++;
            modificaMapa(p, PISO);
            audioEngine.play(sonidos.RECOGE);
            fsmGlobal.recibeMensaje(MensajeTomaObjeto(objeto));
          }
          break;
        
        case HECHIZO_ROJO:
        case HECHIZO_AZUL:
        case HECHIZO_VERDE:
        case HECHIZO_CIAN:
        case HECHIZO_MAGENTA:
        case HECHIZO_AMARILLO:
          var idx = mapa[p.y][p.x] - HECHIZO_ROJO;
          if (hechizos[idx] < MAX_HECHIZOS) {
            hechizos[idx]++;
            modificaMapa(p, PISO);
            audioEngine.play(sonidos.RECOGE);
            fsmGlobal.recibeMensaje(MensajeTomaObjeto(objeto));
          }
          break;
          
        case FUEGO:
          danio(5, sonidos.QUEMA);
          break;
        
        case AMULETO:
          tieneAmuleto = true;
          modificaMapa(p, PISO);
          fsmGlobal.recibeMensaje(MensajeTomaObjeto(objeto));
          break;
        
        case TUBO_FLECHAS:
          if (maxFlechas < 99 || flechas < maxFlechas) {
            modificaMaxFlechas(10);
            modificaFlechas(10);
            modificaMapa(p, PISO);
            audioEngine.play(sonidos.RECOGE);
            fsmGlobal.recibeMensaje(MensajeTomaObjeto(TUBO_FLECHAS));
          }
          break;
      }
    }
    if (lugar == puerta && tieneLlave) {
      tieneLlave = false;
      if (audioPasos != null) {
        audioEngine.stop(audioPasos);
        audioPasos = null;
      }
      fsmGlobal.recibeMensaje(Mensaje(MSJ_ABRE_PUERTA));
    }
  }
  
  danio(cuanto, sonido) {
    cuanto = (cuanto * 1200 - armadura) / 1200;
    modificaVida(-cuanto);
    if (vida > 0) {
      fsm.pasaA(EstadoLastimado());
      if (sonido != null)
        audioEngine.play(sonido);
    } else
      fsm.pasaA(EstadoMuerte());
  }
  
  cambioDeArma() {
    if (isButtonDown(botones, BOTON_ESPACIO)) {
      arma = (arma == ESPADA ? ARCO : ESPADA);
      fsm.push(EstadoSueltaEspacio());
    }
  }
  
  movimiento() {
    var seMovio = false;
    if (fuerza > 0) {
      var b = botones & ~BOTON_CONTROL;
      var newDir = null;
      if (b == BOTON_ARRIBA)
        newDir = dirs[ARRIBA];
      if (b == BOTON_ABAJO)
        newDir = dirs[ABAJO];
      if (b == BOTON_IZQUIERDA)
        newDir = dirs[IZQUIERDA];
      if (b == BOTON_DERECHA)
        newDir = dirs[DERECHA];
      if (newDir != null) {
        seMovio = true;
        dir = newDir;
        var quien = intentaMuevePersonaje(yo);
        if (quien != null)
          quien.recibeMensaje(Mensaje(MSJ_TOCA));
      }
    }
    if (seMovio) {
      contadorAnimacion++;
      ruido = true;
      if (audioPasos == null)
        audioPasos = audioEngine.play(velocidad == JG_LENTO ?
                                      sonidos.PASOS :
                                      sonidos.PASOS_RAPIDOS);
      if (++caminando >= 75) {
        caminando = 0;
        modificaFuerza(-1);
      }
    } else {
      if (audioPasos != null) {
        audioEngine.stop(audioPasos);
        audioPasos = null;
      }
      if (++descansando >= 25) {
        descansando = 0;
        modificaFuerza(2);
      }
    }
  }
  
  ataque() {
    atacando = false;
    if (!isButtonDown(botones, BOTON_CONTROL))
      return;
    switch(arma) {
      case ARCO:
        if (flecha == null && fuerza >= FUERZA_ARCO) {
          if (flechas > 0) {
            modificaFuerza(-FUERZA_ARCO);
            modificaFlechas(-1);
            ruido = true;
            audioEngine.play(sonidos.DISPARA_FLECHA);
            switch (dir) {
              case ARRIBA:
                flecha = Proyectil(yo, posx + 1, posy - 8, ARRIBA,
                                   SPRITE_FLECHA_IMG, SPRITE_FLECHA,
                                   VEL_FLECHA,
                                   sonidos.FLECHA_IMPACTO,
                                   DANIO_FLECHA);
                break;
              
              case ABAJO:
                flecha = Proyectil(yo, posx + 1, posy + 16, ABAJO,
                                   SPRITE_FLECHA_IMG, SPRITE_FLECHA,
                                   VEL_FLECHA,
                                   sonidos.FLECHA_IMPACTO,
                                   DANIO_FLECHA);
                break;
              
              case IZQUIERDA:
                flecha = Proyectil(yo, posx - 11 + 4, posy + 1,
                                   IZQUIERDA,
                                   SPRITE_FLECHA_IMG, SPRITE_FLECHA,
                                   VEL_FLECHA,
                                   sonidos.FLECHA_IMPACTO,
                                   DANIO_FLECHA);
                break;
              
              case DERECHA:
                flecha = Proyectil(yo, posx + 13, posy + 1, DERECHA,
                                   SPRITE_FLECHA_IMG, SPRITE_FLECHA,
                                   VEL_FLECHA,
                                   sonidos.FLECHA_IMPACTO,
                                   DANIO_FLECHA);
                break;
            }
          } else
            audioEngine.play(sonidos.NO_HAY_FLECHA);
        }
        break;
      
      case ESPADA:
        if (fuerza >= FUERZA_ESPADA) {
          modificaFuerza(-FUERZA_ESPADA);
          atacando = true;
          ruido = true;
          audioEngine.play(sonidos.ATAQUE_ESPADA);
          var px, py;
          switch (dir) {
            case ARRIBA:
              px = posx + 5;
              py = posy - 6;
              break;
            
            case ABAJO:
              px = posx + 4;
              py = posy + 21;
              break;
            
            case IZQUIERDA:
              px = posx - 9;
              py = posy + 5;
              break;
            
            case DERECHA:
              px = posx + 17;
              py = posy + 5;
              break;
          }
          var quien = toca(px, py);
          if (quien != null)
            quien.recibeMensaje(MensajeGolpe(yo, DANIO_ESPADA));
        }
        break;
    }
    fsm.push(EstadoSueltaAtaque());
  }
  
  accionFlecha() {
    if (flecha != null) {
      flecha.accion();
      if (!flecha.activo)
        flecha = null;
    }
  }
  
  procesaMensaje(msj) {
    switch (msj.tipo) {
      case MSJ_BORRACHO:
        var contador = 0;
        mezclaDirecciones() {
          if (++contador == 50)
            quitaBorrachera();
          else {
            dirs = mezcla(DIRECCIONES);
            eventoBorracho = fsm.timer.addEvent(5, mezclaDirecciones);
          }
        }
        mezclaDirecciones();
        audioEngine.play(sonidos.MUSICA_BORRACHO);
        return true;
      
      case MSJ_ACELERADO:
        velocidad = JG_RAPIDO;
        audioEngine.play(sonidos.MUSICA_ACELERADO);
        fsm.timer.addEvent(250, lambda() {
                                  velocidad = JG_LENTO;
                                  if (audioPasos != null) {
                                    audioEngine.stop(audioPasos);
                                    audioPasos = null;
                                  }
                                });
        return true;
      
      case MSJ_GOLPE:
        danio(msj.danio, sonidos.QUEMA);
        return true;
    }
    return false;
  }
  
  quitaBorrachera() {
    if (eventoBorracho != null) {
      fsm.timer.cancelEvent(eventoBorracho);
      eventoBorracho = null;
      dirs = DIRECCIONES;
      audioEngine.play(sonidos.FIN_BORRACHERA);
    }
  }
  
  EstadoNormal() {
    accion() {
      ruido = false;
      cambioDeArma();
      movimiento();
      ataque();
      accionFlecha();
      checaPiso();
      if (ruido)
        hizoRuido(yo);
    }
    
    recibeMensaje(msj) {
      return procesaMensaje(msj);
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoSueltaAtaque() {
    accion() {
      ruido = false;
      accionFlecha();
      movimiento();
      checaPiso();
      if (ruido)
        hizoRuido(yo);
      if (!isButtonDown(botones, BOTON_CONTROL))
        fsm.pop();
    }
    
    accionEntrada() {
      fsm.timer.addEvent(3, lambda() {
                              atacando = false;
                            });
    }
    
    recibeMensaje(msj) {
      return procesaMensaje(msj);
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoSueltaEspacio() {
    accion() {
      if (!isButtonDown(botones, BOTON_ESPACIO))
        fsm.pop();
    }
    
    recibeMensaje(msj) {
      return procesaMensaje(msj);
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoLastimado() {
    accion() {
      ruido = false;
      movimiento();
      accionFlecha();
      if (ruido)
        hizoRuido(yo);
    }
    
    accionEntrada() {
      lastimado = true;
      atacando = false;
      if (eventoLastimado != null)
        fsm.timer.cancelEvent(eventoLastimado);
      eventoLastimado = fsm.timer.addEvent(12,
                                          lambda() {
                                            fsm.pasaA(EstadoNormal());
                                          });
    }
    
    accionSalida() {
      lastimado = false;
      fsm.timer.cancelEvent(eventoLastimado);
      eventoLastimado = null;
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoMuerte() {
    accionEntrada() {
      muerto = true;
      spriteMuerte = 30;
      if (audioPasos != null)
        audioEngine.stop(audioPasos);
      audioEngine.play(sonidos.MUSICA_MUERTE);
      fsm.timer.addEvent(20,
                         lambda() {
                           fsm.pasaA(EstadoMuerto());
                         });
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoMuerto() {
    accionEntrada() {
      spriteMuerte = 31;
      fsm.timer.addEvent(90,
                         lambda() {
                          fsmGlobal.recibeMensaje(Mensaje(MSJ_MURIO));
                         });
    }
    
    return this -> Estado() -> super;
  }
  
  accion() {
    fsm.accion();
  }
  
  recibeMensaje(msj) {
    return fsm.recibeMensaje(msj);
  }
  
  coloca(px, py) {    
    var spriteImg;
    var dVel = (velocidad == JG_LENTO ? 2 : 1);
    if (muerto)
      spriteImg = spriteMuerte;
    else if (lastimado)
      spriteImg = (dir == ABAJO ? ARRIBA : dir) * 2 +
                  (contadorAnimacion / dVel) % 2 + 24;
    else
      spriteImg = arma * 8 + dir * 2 + (contadorAnimacion / dVel) % 2;
    if (atacando)
      spriteImg += 8;
    setLargeSpriteImage(SPRITE_JUGADOR, spriteImg);
    putSpriteAt(SPRITE_JUGADOR,
                posx - 3 - px,
                posy - py);
    if (atacando) {
      setSmallSpriteImage(SPRITE_ESPADA, dir + 4);
      switch (dir) {
        case IZQUIERDA:
          putSpriteAt(SPRITE_ESPADA,
                      posx - 11 - px,
                      posy - py);
          break;
        case ARRIBA:
          putSpriteAt(SPRITE_ESPADA,
                      posx + 5 - px,
                      posy - 8 - py);
          break;
        case DERECHA:
          putSpriteAt(SPRITE_ESPADA,
                      posx + 13 - px,
                      posy - py);
          break;
        case ABAJO:
          putSpriteAt(SPRITE_ESPADA,
                      posx - 3 - px,
                      posy + 16 - py);
          break;
      }
    } else
      putSpriteAt(SPRITE_ESPADA, -16, -16);
    if (flecha != null)
      flecha.coloca(px, py);
  }
  
  muestra() {
    putSpriteAt(SPRITE_VIDA, 28, 2);
    putSpriteAt(SPRITE_ARMADURA, 78, 2);
    putSpriteAt(SPRITE_FUERZA, 128, 2);
    putSpriteAt(SPRITE_FLECHAS, 178, 2);
    putSpriteAt(SPRITE_ORO, 228, 2);
  }
  
  oculta() {
    putSpriteAt(SPRITE_JUGADOR, -16, -16);
    putSpriteAt(SPRITE_ESPADA, -16, -16);
    putSpriteAt(SPRITE_FLECHA, -16, -16);
    putSpriteAt(SPRITE_VIDA, -16, -16);
    putSpriteAt(SPRITE_ARMADURA, -16, -16);
    putSpriteAt(SPRITE_FUERZA, -16, -16);
    putSpriteAt(SPRITE_FLECHAS, -16, -16);
    putSpriteAt(SPRITE_ORO, -16, -16);
    if (audioPasos != null) {
      audioEngine.stop(audioPasos);
      audioPasos = null;
    }
    if (flecha != null)
      flecha.oculta();
  }
  
  reset();   
  return this;
}

initProcs.put(lambda() {
  setLargeSpriteImage(SPRITE_JUGADOR, 0);
  setLargeSpriteImage(SPRITE_VIDA, SPRITE_VIDA_IMG);
  setLargeSpriteImage(SPRITE_ARMADURA, SPRITE_ARMADURA_IMG);
  setLargeSpriteImage(SPRITE_FUERZA, SPRITE_FUERZA_IMG);
  setLargeSpriteImage(SPRITE_FLECHAS, SPRITE_FLECHAS_IMG);
  setLargeSpriteImage(SPRITE_ORO, SPRITE_ORO_IMG);
});
