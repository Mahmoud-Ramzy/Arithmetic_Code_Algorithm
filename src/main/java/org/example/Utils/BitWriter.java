package org.example.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

// Class to represent a data symbol with its frequency and probability
public class BitWriter {
    private final ByteArrayOutputStream outputStream;
    private int currentByte;
    private int numBitsFilled;

    public BitWriter(ByteArrayOutputStream outputStream) {
        this.outputStream = outputStream;
        this.currentByte = 0;
        this.numBitsFilled = 0;
    }

    public void writeBit(int bit) throws IOException {
        currentByte = (currentByte << 1) | bit;
        numBitsFilled++;
        if (numBitsFilled == 8) {
            outputStream.write(currentByte);
            numBitsFilled = 0;
        }
    }

    public void flush() throws IOException {
        if (numBitsFilled > 0) {
            currentByte <<= (8 - numBitsFilled);
            outputStream.write(currentByte);
        }
    }
}