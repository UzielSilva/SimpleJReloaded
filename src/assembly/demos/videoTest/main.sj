final TILE_CMAP = 0x800;
final TILE_IMGS = 0x2000;
final TILE_DATA_OFFSET = 32;
final SCREEN_DATA_OFFSET = TILE_DATA_OFFSET + 256 * 32;
final FRAME_SIZE = 8992;

var slides = readFile("slides.vmap");
var video = readFile("bike-video.vmap");

var start = 0;
var counter = 75;

showSlides() {
  if (++counter < 75)
    return;
  if (start == length(slides)) {
    start = 0;
    vbiProc = showVideo;
    counter = 0;
    arrayPoke(TILE_CMAP, video, start, 32);
    arrayPoke(TILE_IMGS, video, start + TILE_DATA_OFFSET, 8192);
    for (var i = 0; i < 24; i++)
      arrayPoke(i * 64, video, start + SCREEN_DATA_OFFSET + i * 32, 32);
    return;
  }
  counter = 0;
  arrayPoke(TILE_CMAP, slides, start, 32);
  arrayPoke(TILE_IMGS, slides, start + TILE_DATA_OFFSET, 8192);
  for (var i = 0; i < 24; i++)
    arrayPoke(i * 64, slides, start + SCREEN_DATA_OFFSET + i * 32, 32);
  start += FRAME_SIZE;
}

showVideo() {
  if (++counter < 75)
    return;
  arrayPoke(TILE_CMAP, video, start, 32);
  arrayPoke(TILE_IMGS, video, start + TILE_DATA_OFFSET, 8192);
  for (var i = 0; i < 24; i++)
    arrayPoke(i * 64, video, start + SCREEN_DATA_OFFSET + i * 32, 32);
  start += FRAME_SIZE;
  if (start == length(video))
    start = 0;
}

var vbiProc = showSlides;

vbi() {
  vbiProc();
}