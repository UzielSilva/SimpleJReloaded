// Example03

// Click on the "Start" button (at the top of this window)
// to run this program.

// Display instructions
// (these keys are the console's controller buttons)
showAt("Press any of these keys:", 0, 0);
showAt("Up arrow", 5, 2);
showAt("Down arrow", 5, 3);
showAt("Left arrow", 5, 4);
showAt("Right arrow", 5, 5);
showAt("Enter (return)", 5, 6);
showAt("Control (ctrl)", 5, 7);
showAt("Space", 5, 8);
showAt("P", 5, 9);

// Wait for "button" press
var keyNum;
while ((keyNum = readCtrlOne()) == 0)
    ;


// Clear screen
clear();

// Display what "button" was pressed
showAt("You pressed:", 0, 0);
switch(keyNum) {
    case 1:
        showAt("Up arrow", 13, 0);
        break;
        
    case 2:
        showAt("Down arrow", 13, 0);
        break;
        
    case 4:
        showAt("Left arrow", 13, 0);
        break;
        
    case 8:
        showAt("Right arrow", 13, 0);
        break;
        
    case 16:
        showAt("Enter", 13, 0);
        break;
        
    case 32:
        showAt("Control", 13, 0);
        break;
        
    case 64:
        showAt("Space", 13, 0);
        break;
        
    case 128:
        showAt("P", 13, 0);
        break;
        
}