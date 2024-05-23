package org.example.Utils;

// Class to represent a data symbol with its frequency and probability
public class BitReader {
    private final byte[] inputBytes;
    private int currentByte;
    private int numBitsRemaining;
    private int byteIndex;

    public BitReader(byte[] inputBytes) {
        this.inputBytes = inputBytes;
        this.currentByte = 0;
        this.numBitsRemaining = 0;
        this.byteIndex = 0;
    }

    public int readBit() {
        if (numBitsRemaining == 0) {
            if (byteIndex < inputBytes.length) {
                currentByte = inputBytes[byteIndex++] & 0xFF;
                numBitsRemaining = 8;
            } else {
                return 0; // Padding bit (should not happen in practice)
            }
        }
        numBitsRemaining--;
        return (currentByte >> numBitsRemaining) & 1;
    }
}