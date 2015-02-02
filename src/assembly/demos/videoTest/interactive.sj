final BOTON_IZQUIERDA = 4;
final BOTON_DERECHA = 8;
final TILE_CMAP = 0x800;
final TILE_IMGS = 0x2000;
final TILE_DATA_OFFSET = 32;
final SCREEN_DATA_OFFSET = TILE_DATA_OFFSET + 256 * 32;
final FRAME_SIZE = 8992;

var video = readFile("bike-video.vmap");

var start = 0;

vbi() {
  var b = readCtrlOne();
  if (b == BOTON_IZQUIERDA) {
    start += 8992;
    if (start == length(video))
      start = 0;
  }
  if (b == BOTON_DERECHA) {
    start -= 8992;
    if (start < 0)
      start += length(video);
  }
  arrayPoke(TILE_CMAP, video, start, 32);
  arrayPoke(TILE_IMGS, video, start + TILE_DATA_OFFSET, 8192);
  for (var i = 0; i < 24; i++)
    arrayPoke(i * 64, video, start + SCREEN_DATA_OFFSET + i * 32, 32);
}
