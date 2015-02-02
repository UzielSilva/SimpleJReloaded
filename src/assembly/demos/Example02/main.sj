// Example02

// Click on the "Start" button (at the top of this window)
// to run this program.

// Display the 256 available characters
for (var ch = 0; ch < 256; ch++) {
    // Place them in a 16x16 square
    var row = ch / 16;
    var col = ch % 16;
    putAt(ch, col, row);
}