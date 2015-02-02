// Example04

// Click on the "Start" button (at the top of this window)
// to run this program.

// Clear screen and display instructions
clear();
showAt("Use arrow keys to move the ball", 0, 0);
showAt("Press 'Space' to exit", 0, 23);

// Are we finished
var finished = false;

// Ball location
var ballX = 10;
var ballY = 10;

// Character for the ball
final BALL = 20;

// Character for an empty space
final SPACE = 32;

// Button values
final BTN_UP = 1;
final BTN_DOWN = 2;
final BTN_LEFT = 4;
final BTN_RIGHT = 8;
final BTN_SPACE = 64;

// To draw the ball
drawBall() {
    putAt(BALL, ballX, ballY);
}

// To erase the ball
eraseBall() {
    putAt(SPACE, ballX, ballY);
}

// Keep moving the ball until the user presses 'SPACE' key
while (!finished) {
    drawBall();               // Draw the ball
    pause(0.1);               // wait one tenth of a second
    var btnNum = readCtrlOne();    // read "controller" buttons
    eraseBall();              // Erase the ball
    
    // Check what button was pressed
    switch (btnNum) {
        case BTN_UP:          // Pressed 'up'
            if (ballY > 0)    // If can move up
                ballY--;      // then move up
            break;
        
        case BTN_DOWN:        // Pressed 'down'
            if (ballY < 23)   // If can move down
                ballY++;      // then move down
            break;
        
        case BTN_LEFT:        // Pressed 'left'
            if (ballX > 0)    // If can move left
                ballX--;      // then move left
            break;
        
        case BTN_RIGHT:       // Pressed 'right'
            if (ballX < 31)   // If can move right
                ballX++;      // then move right
            break;
        
        case BTN_SPACE:       // Pressed 'space'
            finished = true;  // nothing more to do
            break;
    }
}

// Clear the screen
clear();

// Display goodbye message
showAt("That's all folks!", 0, 0);