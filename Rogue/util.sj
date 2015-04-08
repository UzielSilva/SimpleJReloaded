showAt("util", 0, _linea++);

abs(n) {
  return n < 0 ? -n : n;
}

clamp(v, min, max) {
  if (v < min)
    v = min;
  if (v > max)
    v = max;
  return v;
}

eShowAt(msg, col, ren) {
  var ptr = ren * 64 + col;
  var len = length(msg);
  var p = 0;
  while (p < len) {
    var c = charAt(msg, p++);
    if (c == '~')
      c = FUEGO;
    else if (c == '`')
      c = charAt(msg, p++) + 128;
    poke(ptr++, c);
  }
}

aLaIzquierda(num, ancho) {
  num = "" + num;
  while (length(num) < ancho)
    num = " " + num;
  return num;
}

ceros(num, ancho) {
  num = "" + num;
  while (length(num) < ancho)
    num = "0" + num;
  return num;
}

aNegros() {
  for (var i = 0; i < 16; i++)
    setTileColor(i, 0, 0, 0);
  for (var i = 0; i < 15; i++)
    setSpriteColor(i, 0, 0, 0);
}

mezcla(datos) {
  var s = Set();
  s.putAll(datos);
  var q = Queue();
  while (!s.isEmpty()) {
    var e = chooseOne(s);
    s.remove(e);
    q.put(e);
  }
  return q.toArray();
}

Coords() {
  var x = 0;
  var y = 0;
  return this;
}

Estado() {
  accion() {}
  accionEntrada() {}
  accionSalida() {}
  recibeMensaje(msj) {return false;}
  return this;
}

Mensaje(tipo) {
  return this;
}

MensajeTomaObjeto(objeto) {
  var tipo = MSJ_TOMA_OBJETO;
  return this;
}

MensajeGolpe(quien, danio) {
  var tipo = MSJ_GOLPE;
  return this;
}

MensajeRuido(quien) {
  var tipo = MSJ_RUIDO;
  return this;
}

Timer() {
  var head;
  var nextId;
  
  clear() {
    head = null;
    nextId = 1;
  }

  addEvent(delay, func) {
    var event = {func: func, delay: delay, id: nextId++, next: null};
    var q = null;
    for (var ptr = head; ptr != null &&
                        ptr.delay <= event.delay;
        ptr = ptr.next) {
      q = ptr;
      event.delay -= ptr.delay;
    }
    if (q == null) {
      event.next = head;
      head = event;
    } else {
      event.next = q.next;
      q.next = event;
    }
    if (event.delay != 0 && event.next != null)
      event.next.delay -= event.delay;
    return event.id;
  }

  clockTick() {
    if (head == null)
      return;
    head.delay--;
    while (head != null && head.delay == 0) {
      var event = head;
      head = event.next;
      event.func();
    }
  }

  cancelEvent(id) {
    var q = null;
    var ptr = head;
    while (ptr != null && ptr.id != id) {
      q = ptr;
      ptr = ptr.next;
    }
    if (ptr != null) {
      if (q != null)
        q.next = ptr.next;
      else
        head = ptr.next;
      if (ptr.next != null)
        ptr.next.delay += ptr.delay;
    }
  }
  
  clear();
  return this;    
}
  
FSM(global, estado) {
  var timer = Timer();
  var stack = Stack();
  
  accion() {
    global.accion();
    estado.accion();
    timer.clockTick();
  }
  
  pasaA(nuevoEstado) {
    var viejoEstado = estado;
    estado = nuevoEstado;
    viejoEstado.accionSalida();
    nuevoEstado.accionEntrada();
  }

  push(nuevoEstado) {
    stack.push(estado);
    estado = nuevoEstado;
    nuevoEstado.accionEntrada();
  }
  
  pop() {
    estado.accionSalida();
    estado = stack.pop();
  }
  
  recibeMensaje(msj) {
    if (estado.recibeMensaje(msj))
      return true;
    return global.recibeMensaje(msj);
  }
  
  return this;
}

haySpriteEnemigo() {
  return !spritesEnemigos.isEmpty();
}

obtenSpriteEnemigo() {
  return spritesEnemigos.pop();
}

devuelveSpriteEnemigo(s) {
  spritesEnemigos.push(s);
  putSpriteAt(s, -16, -16);
}

haySpriteProyectil() {
  return !spritesProyectiles.isEmpty();
}

obtenSpriteProyectil() {
  return spritesProyectiles.pop();
}

devuelveSpriteProyectil(s) {
  spritesProyectiles.push(s);
  putSpriteAt(s, -16, -16);
}

resetSpritesEnemigosYProyectiles() {
  spritesEnemigos = Stack();
  for (var i = 0; i < NUM_SPRITES_ENEMIGOS; i++)
    devuelveSpriteEnemigo(SPRITES_ENEMIGOS + i);
  spritesProyectiles = Stack();
  for (var i = 0; i < NUM_SPRITES_PROYECTILES; i++)
    devuelveSpriteProyectil(SPRITES_PROYECTILES + i);
}

init() {
  var tmp = toArray(initProcs);
  for (var i = 0; i < length(tmp); i++) {
    var initProc = tmp[i];
    initProc();
  }
}

initProcs.put(lambda() {
  resetSpritesEnemigosYProyectiles();
});
