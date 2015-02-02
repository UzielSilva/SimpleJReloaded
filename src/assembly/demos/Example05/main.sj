// Example05

// Click on the "Start" button (at the top of this window)
// to run this program.

// Display text
showAt("Playing note:", 0, 0);

// Play a note and make a pause
playNote(nt, delay) {
    showAt(nt, 14, 0);   // Display note
    note(nt);                // Play note
    pause(delay / 1000.0);            // pause
}

playNote("C4", 250);
playNote("D4", 250);
playNote("E4", 250);
playNote("F4", 250);
playNote("G4", 250);
playNote("A5", 250);
playNote("B5", 250);
playNote("C5", 500);
playNote("C5", 250);
playNote("B5", 250);
playNote("A5", 250);
playNote("G4", 250);
playNote("F4", 250);
playNote("E4", 250);
playNote("D4", 250);
playNote("C4", 1000);


playNote("E5", 200);
playNote("E5", 200);
playNote("E5", 400);
playNote("E5", 200);
playNote("E5", 200);
playNote("E5", 400);
playNote("E5", 200);
playNote("G5", 200);
playNote("C5", 200);
playNote("D5", 200);
playNote("E5", 1000);

playNote("B4", 250);
playNote("B4", 250);
playNote("C4", 250);
playNote("D4", 250);
playNote("D4", 250);
playNote("C4", 250);
playNote("B4", 250);
playNote("A4", 250);
playNote("G3", 250);
playNote("G3", 250);



