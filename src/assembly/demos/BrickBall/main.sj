setBackground(0, 0, 0);

setSoundFrequency(0, 10000);
setSoundAttack(0, 10);
setSoundDecay(0, 100);
setSoundSustain(0, 0);
setSoundRelease(0, 1);
setSoundVolume(0, 15);

setSoundFrequency(1, 2000);
setSoundAttack(1, 100);
setSoundDecay(1, 300);
setSoundSustain(1, 0);
setSoundRelease(1, 1);
setSoundVolume(1, 15);

final BUTTONS = 0x8c4;
final LEFT = 0x04;
final RIGHT = 0x08;
final ENTER = 0x10;
final SPACE = 0x40;

var paddlePos = 10;
var paddleLineOffset = 23 * 64;
var paddleWidth = 6;
var ballX = 10;
var ballY = 10;
var ballDx = 1;
var ballDy = 1;
var firstBrickLine = 3;
var lastBrickLine = 8;
var brickWidth = 4;
var totalBricks = (32 / brickWidth) *
                  (lastBrickLine - firstBrickLine + 1);
var brickCount = totalBricks;
var initialBallDelay = 2;
var ballDelay = initialBallDelay;
var ballWait = ballDelay;
var score = 0;

erasePaddle() {
  for (var i = 0; i < paddleWidth; i++)
    poke(paddleLineOffset + paddlePos + i, 32);
}

drawPaddle() {
  for (var i = 0; i < paddleWidth; i++)
    poke(paddleLineOffset + paddlePos + i, 160);
}

getByte(x, y) {
  return peek(y * 64 + x);
}

leadingZeros(n, msg) {
  while (length(msg) < n)
    msg = "0" + msg;
  return msg;
}

displayScore() {
  showAt("SCORE:", 19, 0);
  showAt(leadingZeros(5, "" + score), 26, 0);
}

eraseBall() {
  putAt(32, ballX, ballY);
}

drawBall() {
  putAt(20, ballX, ballY);
}

resetBall() {
  ballX = 10;
  ballY = 10;
  ballDx = 1;
  ballDy = 1;
  ballDelay = initialBallDelay;
  ballWait = ballDelay;
}

reset() {
  resetBall();
  for (var y = firstBrickLine; y <= lastBrickLine; y++)
    for (var x = 0; x < 32; x++)
      putAt(160, x, y);
  brickCount = totalBricks;
}

resetAll() {
  reset();
  score = 0;
}

clearBrick(x, y) {
  var xs = (x / brickWidth) * brickWidth;
  for (var i = 0; i < brickWidth; i++)
    putAt(32, xs + i, y);
  if (y <= firstBrickLine + 1 && ballDelay > 0)
    ballDelay--;
  score++;
  if (--brickCount == 0)
    reset();
}

reset();

vbi() {
  var btns = peek(BUTTONS);
  if (ballDy == 0) {
    showAt("'SPACE': new ball", 0, 0);
    showAt("'ENTER': reset", 0, 1);
    var doClear = false;
    if ((btns & ENTER) != 0) {
      resetAll();
      var doClear = true;
    }
    if ((btns & SPACE) != 0) {
      resetBall();
      var doClear = true;
    }
    if (doClear) {
      showAt("                 ", 0, 0);
      showAt("              ", 0, 1);
    }
    return;
  }
  erasePaddle();
  if ((btns & LEFT) != 0 && paddlePos > 0)
    paddlePos--;
  if ((btns & RIGHT) != 0 && paddlePos + paddleWidth < 32)
    paddlePos++;
  drawPaddle();
  if (--ballWait > 0)
    return;
  ballWait = ballDelay;
  eraseBall();
  ballX += ballDx;
  ballY += ballDy;
  if (ballX == 0 || ballX == 31) {
    ballDx = -ballDx;
    soundOn(0);
  }
  if (ballY == 0) {
    ballDy = -ballDy;
    soundOn(0);
  }
  if (ballY == 23) {
    if (ballX != 32)
      soundOn(1);
    ballX = 32;
    ballDx = 0;
    ballDy = 0;
  }
  if (ballY == 22 && ballX >= paddlePos - 1 &&
        ballX <= paddlePos + paddleWidth) {
    ballDy = -ballDy;
    soundOn(0);
  }
  if (ballY >= firstBrickLine -1 && ballY <= lastBrickLine + 1) {
    if (getByte(ballX + ballDx, ballY) == 160) {
      clearBrick(ballX + ballDx, ballY);
      ballDx = -ballDx;
      soundOn(0);
    }
    if (getByte(ballX, ballY + ballDy) == 160) {
      clearBrick(ballX, ballY + ballDy);
      ballDy = -ballDy;
      soundOn(0);
    }
  }
  displayScore();
  drawBall();
}
