showAt("monstruos", 0, _linea++);

/**********
 * Gusano *
 **********/
Gusano(sprite) {
  var yo = this;
  var posx, posy;
  var ancho = 8;
  var alto = 8;
  var vida = 8;
  var lugar;
  var dir = IZQUIERDA;
  var velocidad = 1;
  var siguePared = false;
  var lastimado = false;
  var contadorAnimacion = 0;
  var muerto = false;
  var fsm;
  var tiempoInvisible = TIEMPO_INVISIBLE;

  movimiento() {
    if (siguePared)
      switch (dir) {
        case IZQUIERDA:
          if (choca(yo, posx, posy + 1) == null) {
            dir = ABAJO;
            siguePared = false;
          }
          break;
        
        case ARRIBA:
          if (choca(yo, posx - 1, posy) == null) {
            dir = IZQUIERDA;
            siguePared = false;
          }
          break;
        
        case DERECHA:
          if (choca(yo, posx, posy - 1) == null) {
            dir = ARRIBA;
            siguePared = false;
          }
          break;
          
        case ABAJO:
          if (choca(yo, posx + 1, posy) == null) {
            dir = DERECHA;
            siguePared = false;
          }
          break;
      }
    var quien = intentaMuevePersonaje(yo);
    if (quien != null) {
      switch (dir) {
        case IZQUIERDA:
          dir = ARRIBA;
          break;
        
        case ARRIBA:
          dir = DERECHA;
          break;
          
        case DERECHA:
          dir = ABAJO;
          break;
          
        case ABAJO:
          dir = IZQUIERDA;
          break;
      }
      siguePared = true;
      quien.recibeMensaje(MensajeGolpe(yo, 3));
    }
  }
  
  procesaMensaje(msj) {
    switch (msj.tipo) {
      case MSJ_GOLPE:
        if (msj.quien == jugador) {
          tiempoInvisible = 0;
          vida -= msj.danio;
          if (vida <= 0)
            fsm.pasaA(EstadoMuere());
          else
            fsm.pasaA(EstadoLastimado(msj.danio));
          return true;
        }
        return false;
      
      case MSJ_TOCA:
        if (velocidad == 1) {
          velocidad = 6;
          audioEngine.play(sonidos.GUSANO_ACELERADO);
          fsm.timer.addEvent(13, lambda() {
                                   velocidad = 1;
                                 });
          return false;
        }
        break;
      
      case MSJ_ENTRO:
        tiempoInvisible = 0;
        return true;
    }
    return false;
  }
  
  EstadoPasea() {
    accion() {
      movimiento();
    }
    
    recibeMensaje(msj) {
      return procesaMensaje(msj);
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoLastimado(danio) {
    accion() {
      movimiento();
    }
    
    accionEntrada() {
      lastimado = true;
      audioEngine.play(sonidos.MONSTRUO_LASTIMADO);
      fsm.timer.addEvent(danio * 2, lambda() {
                                      if (!muerto)
                                        fsm.pasaA(EstadoPasea());
                                    });
    }
    
    accionSalida() {
      lastimado = false;
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoMuere() {
    accion() {
      dir = (dir + 1) % 4;
    }

    accionEntrada() {
      muerto = true;
      audioEngine.play(sonidos.MONSTRUO_MUERTE);
      fsm.timer.addEvent(12, lambda() {
                               fsm.pasaA(EstadoMuerto());
                             });
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoMuerto() {
    accion() {
      eliminaMonstruo(yo);
      dejaAlgo(yo, 5, ORO);
    }
    
    return this -> Estado() -> super;
  }
  
  accion() {
    if (tiempoInvisible >= TIEMPO_INVISIBLE)
      return;
    fsm.accion();
  }
  
  recibeMensaje(msj) {
    return fsm.recibeMensaje(msj);
  }
  
  muestra(px, py) {
    var x = posx - px;
    var y = posy - py;
    if (x <= -8 || x >= 256 || y <= -8 || y >= 192)
      tiempoInvisible++;
    else
      tiempoInvisible = 0;
    if (tiempoInvisible >= TIEMPO_INVISIBLE)
      return;
    var x = clamp(x, -8, 256);
    var y = clamp(y, -8, 192);
    contadorAnimacion++;
    if (lastimado && contadorAnimacion % 2 == 0)
      x = -16;
    var spriteImg =
      SPRITE_GUSANO_IMG + dir + ((contadorAnimacion / 1) % 2) * 4;
    setSmallSpriteImage(sprite, spriteImg);
    putSpriteAt(sprite, x, y);   
  }
  
  oculta() {
    putSpriteAt(sprite, -16, -16);
  }
  
  devuelveSprites() {
    devuelveSpriteEnemigo(sprite);
  }
  
  fsm = FSM(Estado(), EstadoPasea());
  return this;
}


/*************
 * Escupidor *
 *************/
Escupidor(sprite) {
  var yo = this;
  var posx, posy;
  var ancho = 16;
  var alto = 16;
  var vida = 15;
  var lugar;
  var dir = chooseOne(DIRECCIONES);
  var velocidad = 2;
  var lastimado = false;
  var contadorAnimacion = 0;
  var muerto = false;
  var escupiendo = false;
  var fsm;
  var fsmActua;
  var proyectil = null;
  var tiempoDir;
  var noHaVisto = 0;
  var tiempoInvisible = TIEMPO_INVISIBLE;
  
  movimientoPatrulla() {
    if (!mueveMismoLugar(yo)) {
      dir = DIRS_OPUESTAS[dir];
      return;
    }
    var quien = intentaMuevePersonaje(yo);
    if (quien != null) {
      dir = DIRS_OPUESTAS[dir];
      quien.recibeMensaje(MensajeGolpe(yo, 10));
    }
  }
  
  movimientoAtaca() {
    var mcx = posx + ancho / 2;
    var mcy = posy + alto / 2;
    var jcx = jugador.posx + jugador.ancho / 2;
    var jcy = jugador.posy + jugador.alto / 2;
    var dx = jcx - mcx;
    var dy = jcy - mcy;
    if (--tiempoDir <= 0) {
      if (abs(dx) > abs(dy))
        dir = (dx > 0) ? DERECHA : IZQUIERDA;
      else
        dir = (dy > 0) ? ABAJO : ARRIBA;
      tiempoDir = 5;
    }
    if (!mueveMismoLugar(yo)) {
      dir = DIRS_OPUESTAS[dir];
      fsmActua.pasaA(EstadoActuaPatrulla());
      return;
    }
    var quien = intentaMuevePersonaje(yo);
    if (quien != null) {
      quien.recibeMensaje(MensajeGolpe(yo, 10));
      dir = DIRS_OPUESTAS[dir];
      tiempoDir = 25;
    }
    if (proyectil == null && haySpriteProyectil() &&
        !jugador.muerto && random(8) == 0)
      switch (dir) {
        case ARRIBA:
          if (abs(dx) <= jugador.ancho && dy <= -32)
            fsmActua.pasaA(EstadoActuaEscupe());
          break;
        
        case ABAJO:
          if (abs(dx) <= jugador.ancho && dy >= 32)
            fsmActua.pasaA(EstadoActuaEscupe());
          break;
        
        case IZQUIERDA:
          if (dx <= -32 && abs(dy) <= jugador.alto)
            fsmActua.pasaA(EstadoActuaEscupe());
          break;
        
        case DERECHA:
          if (dx >= 32 && abs(dy) <= jugador.alto)
            fsmActua.pasaA(EstadoActuaEscupe());
          break;
      }
  }
  
  escupe(sprite) {
    switch (dir) {
      case ARRIBA:
        proyectil = Proyectil(yo, posx + 4, posy - 8, ARRIBA,
                              SPRITE_ESCUPE_IMG, sprite, VEL_ESCUPE,
                              sonidos.ESCUPE_IMPACTO,
                              DANIO_ESCUPE);
        break;
      
      case ABAJO:
        proyectil = Proyectil(yo, posx + 4, posy + 16, ABAJO,
                              SPRITE_ESCUPE_IMG, sprite, VEL_ESCUPE,
                              sonidos.ESCUPE_IMPACTO,
                              DANIO_ESCUPE);
        break;
      
      case IZQUIERDA:
        proyectil = Proyectil(yo, posx - 8, posy + 4, IZQUIERDA,
                              SPRITE_ESCUPE_IMG, sprite, VEL_ESCUPE,
                              sonidos.ESCUPE_IMPACTO,
                              DANIO_ESCUPE);
        break;
      
      case DERECHA:
        proyectil = Proyectil(yo, posx + 16, posy + 4, DERECHA,
                              SPRITE_ESCUPE_IMG, sprite, VEL_ESCUPE,
                              sonidos.ESCUPE_IMPACTO,
                              DANIO_ESCUPE);
        break;
    }
    audioEngine.play(sonidos.ESCUPE);
  }

  procesaMensaje(msj) {
    switch (msj.tipo) {
      case MSJ_GOLPE:
        if (msj.quien == jugador) {
          tiempoInvisible = 0;
          vida -= msj.danio;
          if (vida <= 0)
            fsm.pasaA(EstadoMuere());
          else
            fsm.pasaA(EstadoLastimado(msj.danio));
          return true;
        }
        break;
      
      case MSJ_TOCA:
        fsmActua.pasaA(EstadoActuaAtaca());
        return true;
        
      case MSJ_SALIO:
        fsmActua.pasaA(EstadoActuaPatrulla());
        return true;
      
      case MSJ_ENTRO:
        tiempoInvisible = 0;
        return true;
    }
    return false;
  }
  
  EstadoGlobal() {
    accion() {
      if (proyectil != null) {
        proyectil.accion();
        if (!proyectil.activo) {
          devuelveSpriteProyectil(proyectil.sprite);
          proyectil = null;
        }
      }
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoActuaPatrulla() {
    accion() {
      movimientoPatrulla();
      if (veo(yo, jugador))
        fsmActua.pasaA(EstadoActuaAtaca());
    }
    
    recibeMensaje(msj) {
      return procesaMensaje(msj);
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoActuaAtaca() {
    accion() {
      movimientoAtaca();
      if (veo(yo, jugador))
        noHaVisto = 0;
      else if (++noHaVisto == 50)
        fsmActua.pasaA(EstadoActuaPatrulla());
    }
    
    accionEntrada() {
      tiempoDir = 0;
    }
    
    recibeMensaje(msj) {
      return procesaMensaje(msj);
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoActuaEscupe() {
    var sprite;
    var contador = 0;
    
    accionEntrada() {
      escupiendo = true;
      sprite = obtenSpriteProyectil();
      audioEngine.play(sonidos.PREPARA_ESCUPE);
      contador = 0;
    }
    
    accion() {
      if (++contador >= 10) {
        escupe(sprite);
        fsmActua.pasaA(EstadoActuaAtaca());
      }
    }

    accionSalida() {
      escupiendo = false;
      if (contador < 10)
        devuelveSpriteProyectil(sprite);
    }
    
    recibeMensaje(msj) {
      return procesaMensaje(msj);
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoActua() {
    accion() {
      fsmActua.accion();
    }
    
    recibeMensaje(msj) {
      return fsmActua.recibeMensaje(msj);
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoLastimado(danio) {
    accionEntrada() {
      lastimado = true;
      audioEngine.play(sonidos.MONSTRUO_LASTIMADO);
      fsmActua.pasaA(EstadoActuaAtaca());
      fsm.timer.addEvent(danio * 2, lambda() {
                                      if (!muerto)
                                        fsm.pasaA(EstadoActua());
                                    });
    }
    
    accionSalida() {
      lastimado = false;
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoMuere() {
    accion() {
      dir = (dir + 1) % 4;
    }

    accionEntrada() {
      muerto = true;
      audioEngine.play(sonidos.MONSTRUO_MUERTE);
      fsm.timer.addEvent(12, lambda() {
                               fsm.pasaA(EstadoMuerto());
                             });
    }
    
    return this -> Estado() -> super;
  }
  
  EstadoMuerto() {
    accion() {
      eliminaMonstruo(yo);
      dejaAlgo(yo, 2, ORO);
    }
    
    return this -> Estado() -> super;
  }
  
  accion() {
    if (tiempoInvisible >= TIEMPO_INVISIBLE && proyectil == null)
      return;
    fsm.accion();
  }
  
  recibeMensaje(msj) {
    return fsm.recibeMensaje(msj);
  }
  
  muestra(px, py) {
    if (proyectil != null)
      proyectil.coloca(px, py);
    var x = posx - px;
    var y = posy - py;  
    if (x <= -16 || x >= 256 || y <= -16 || y >= 192)
      tiempoInvisible++;
    else
      tiempoInvisible = 0;
    if (tiempoInvisible >= TIEMPO_INVISIBLE)
      return;
    x = clamp(x, -16, 256);
    y = clamp(y, -16, 192);
    contadorAnimacion++;
    if (lastimado && contadorAnimacion % 2 == 0)
      x = -16;
    var spriteImg =
      SPRITE_ESCUPIDOR_IMG + dir + (escupiendo ? 0 : 4);
    setLargeSpriteImage(sprite, spriteImg);
    putSpriteAt(sprite, x, y);
  }
  
  oculta() {
    putSpriteAt(sprite, -16, -16);
    if (proyectil != null)
      proyectil.oculta();
  }
  
  devuelveSprites() {
    devuelveSpriteEnemigo(sprite);
    if (proyectil != null)
      devuelveSpriteProyectil(proyectil.sprite);
  }
  
  fsm = FSM(EstadoGlobal(), EstadoActua());
  fsmActua = FSM(Estado(), EstadoActuaPatrulla());
  return this;
}


/******************
 * Util Monstruos *
 ******************/
agregaMonstruo(m) {
  monstruos.put(m);
  mArr = monstruos.toArray();
  mArrN = length(mArr);
}

eliminaMonstruo(m) {
  m.devuelveSprites();
  monstruos.remove(m);
  quita(m);
  mArr = monstruos.toArray();
  mArrN = length(mArr);
}

muestraMonstruos(px, py) {for(var i=0;i<mArrN;i++)mArr[i].muestra(px,py);}

ocultaMonstruos() {
  for (var i = 0; i < mArrN; i++)
    mArr[i].oculta();
}

accionesMonstruos() {for (var i=0; i<mArrN; i++) mArr[i].accion();}

resetMonstruos() {
  for (var i = 0; i < mArrN; i++) {
    monstruos.remove(mArr[i]);
  }
  mArr = monstruos.toArray();
  mArrN = length(mArr);
}

initProcs.put(lambda() {
  monstruos = Set();
  mArr = [];
  mArrN = 0;
});
