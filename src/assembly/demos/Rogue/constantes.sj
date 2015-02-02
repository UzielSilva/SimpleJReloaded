showAt("constantes", 0, _linea++);

final CUARTOS_H = 3;
final CUARTOS_V = 3;
final CUARTOS = CUARTOS_H * CUARTOS_V;
final ANCHO = 30;
final ALTO = 30;
final NIVEL_AMULETO = 2;

final PUERTA_CERRADA = 0;
final PUERTA_ABIERTA = 9;
final PISO = 32;
final ROCA = 34;
final FLECHAS = 176;
final ORO = 177;
final LLAVE = 178;
final FUEGO = 179;
final NUM_FUEGOS = 3;
final AMULETO = 182;
final NUM_AMULETOS = 2;
final TUBO_FLECHAS = 184;
final POCION_ROJA = 18;
final POCION_AZUL = 19;
final POCION_VERDE = 20;
final POCION_CIAN = 21;
final POCION_MAGENTA = 22;
final POCION_AMARILLA = 23;
final POCIONES = POCION_AMARILLA - POCION_ROJA + 1;
final HECHIZO_ROJO = 24;
final HECHIZO_AZUL = 25;
final HECHIZO_VERDE = 26;
final HECHIZO_CIAN = 27;
final HECHIZO_MAGENTA = 28;
final HECHIZO_AMARILLO = 29;
final HECHIZOS = HECHIZO_AMARILLO - HECHIZO_ROJO + 1;
final FLECHA_IZQUIERDA = 126;
final FLECHA_DERECHA = 127;

final ARCO = 0;
final ESPADA = 1;

final POCION = 1;
final HECHIZO = 2;

final IZQUIERDA = 0;
final ARRIBA = 1;
final DERECHA = 2;
final ABAJO = 3;

final DIRECCIONES = [IZQUIERDA, ARRIBA, DERECHA, ABAJO];
final DIRS_OPUESTAS = [DERECHA, ABAJO, IZQUIERDA, ARRIBA];

final BOTON_ARRIBA = 1;
final BOTON_ABAJO = 2;
final BOTON_IZQUIERDA = 4;
final BOTON_DERECHA = 8;
final BOTON_ENTER = 16;
final BOTON_CONTROL = 32;
final BOTON_ESPACIO = 64;

final MIN_X = 0;
final MAX_X = ANCHO * 24 - 257;
final MIN_Y = 0;
final MAX_Y = ALTO * 24 - 193;

final TIEMPO_INVISIBLE = 125;

final SPRITE_VIDA = 0;
final SPRITE_ARMADURA = 1;
final SPRITE_FUERZA = 2;
final SPRITE_FLECHAS = 3;
final SPRITE_ORO = 4;
final SPRITES_ENEMIGOS = 5;
final NUM_SPRITES_ENEMIGOS = 20;
final SPRITES_PROYECTILES = 25;
final NUM_SPRITES_PROYECTILES = 4;
final SPRITE_JUGADOR = 29;
final SPRITE_FLECHA = 30;
final SPRITE_ESPADA = 31;

final SPRITE_VIDA_IMG = 123;
final SPRITE_ARMADURA_IMG = 124;
final SPRITE_FUERZA_IMG = 125;
final SPRITE_FLECHAS_IMG = 126;
final SPRITE_ORO_IMG = 127;
final SPRITE_FLECHA_IMG = 0;

final SPRITE_GUSANO_IMG = 32;
final SPRITE_ESCUPIDOR_IMG = 48;
final SPRITE_ESCUPE_IMG = 12;

final TILE_CMAP = 0x800;
final TILE_IMGS = 0x2000;
final SPRITE_IMGS = 0x4000;

final TILE_DATA_OFFSET = 32;
final SCREEN_DATA_OFFSET = TILE_DATA_OFFSET + 256 * 32;

final JUGADOR_CX = (256 - 10) / 2;
final JUGADOR_CY = (192 - 16) / 2;

final MAX_VIDA_INICIAL = 60;
final MAX_ARMADURA_INICIAL = 50;
final MAX_FUERZA_INICIAL = 100;
final MAX_FLECHAS_INICIAL = 20;
final MAX_POCIONES = 15;
final MAX_HECHIZOS = 10;

final JG_LENTO = 3;
final JG_RAPIDO = 5;

final FUERZA_ARCO = 2;
final DANIO_FLECHA = 4;
final FUERZA_ESPADA = 3;
final DANIO_ESPADA = 8;
final VEL_FLECHA = 6;

final DANIO_ESCUPE = 4;
final VEL_ESCUPE = 6;

final MAX_RENGLONES_MARCADOR = 12;

final MSJ_TOMA_OBJETO = 1;
final MSJ_ABRE_PUERTA = 2;
final MSJ_MURIO = 3;
final MSJ_BORRACHO = 4;
final MSJ_ACELERADO = 5;
final MSJ_GOLPE = 6;
final MSJ_TOCA = 7;
final MSJ_ENTRO = 8;
final MSJ_SALIO = 9;
final MSJ_RUIDO = 10;
final MSJ_CAMBIO_LUGAR = 11;


final TEXTO_HISTORIA = [
  "",
  "           ~ Rogue ~",
  "",
  "",
  "",
  "Acabas de terminar tus estudios",
  "en el gremio de guerreros.",
  "Despu`es de mucho esfuerzo, al",
  "f`in est`as listo para emprender",
  "alguna aventura.",
  "",
  "",
  "",
  "Como una prueba de tu habilidad,",
  "los maestros del gremio te han",
  "mandado a los Calabozos de la",
  "Perdici`on. Tu misi`on es regresar",
  "con el Amuleto de Yendor.",
  "",
  "",
  "",
  "Si lo logras ser`as aceptado como",
  "miembro permanente del gremio.",
  "Adem`as, te permitir`an conservar",
  "todo el oro que encuentres en",
  "los calabozos.",
  "",
  "",
  "",
  "Para tu misi`on te dan una",
  "espada, un arco y unas cuantas",
  "flechas. Te despides de tu",
  "familia y emprendes el camino",
  "hacia las Monta`nas Obscuras",
  "donde se encuentran los",
  "calabozos.",
  "",
  "",
  "",
  "Unos dias despu`es, al anochecer,",
  "llegas a las ruinas que marcan",
  "la entrada a los Calabozos de",
  "la Perdici`on.",
  "",
  "",
  "",
  "Acampas al aire libre.",
  "Al amanecer, bajas a los",
  "calabozos...",
  "",
  "",
  "",
  "           ~   ~   ~"
];
