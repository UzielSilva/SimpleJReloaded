showAt("variables", 0, _linea++);

var initProcs = Queue();

var mapa = new array[ALTO * 3][ANCHO * 3];

var cuartos;
var cercanos = new array[CUARTOS][CUARTOS];
var conectados = new array[CUARTOS][CUARTOS];
var lugares = new array[ALTO][ANCHO];
var puerta;
var personajeNivel;
var mapaCambio, xtViejo, ytViejo;

var fsmGlobal;

var jugador;
var digitos = new array[10];

var accionesPociones;
var accionesHechizos;

var monstruos;
var mArr;
var mArrN;

var spritesEnemigos;
var spritesProyectiles;

var botones;

var inicializando;

var nivel;

var tilesData = null;
var spritesData = null;
var tilesAnimacion = null;
var simpleJScreenData;
var ienjiniaScreenData;
var rogueScreenData;

var marcador = new array[MAX_RENGLONES_MARCADOR];
var iniciales = new array[3];
var numIniciales;
var colInicial;
var renInicial;
var idxMarcador;

var renItem;
var colItem;

var audioEngine = null;
var sonidos;
