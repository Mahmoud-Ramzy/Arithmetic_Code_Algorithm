package org.example;

import org.example.Utils.BitReader;
import org.example.Utils.BitWriter;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;

public class ArithmeticCoding {

    private static final int END_OF_FILE = 256;
    private static final int BITS_TO_FOLLOW = 16; // Number of bits for followup (precision of the interval)

    public static void main(String[] args) throws IOException {
        Scanner read = new Scanner(System.in);
        System.out.println("Welcome to our Arithmetic coding compressor and decompressor ^_^");
        System.out.println("Enter the file path");
        String filePath = read.nextLine();
        System.out.println("Enter the operation you need:\n 1 --> Compression\n 2 --> Decompression");
        int op = read.nextInt();
        read.close();

        if (op == 1) {
            System.out.println("Compression is being processed");
            System.out.println("fill your time with azkar");
            compress(filePath);
        } else if (op == 2) {
            System.out.println("Decompression is being processed");
            System.out.println("fill your time with azkar");
            decompress(filePath, filePath + ".decompressed");
        } else {
            System.out.println("You entered wrong number! please choose 1 or 2");
        }
    }

    private static void compress(String filePath) throws IOException {
        long start = System.currentTimeMillis();
        FileInputStream inputStream = new FileInputStream(filePath);
        FileOutputStream outputStream = new FileOutputStream(filePath + ".compressed");
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        // Calculate frequencies
        int[] frequencies = new int[END_OF_FILE + 1];
        int totalSymbols = 0;
        int symbol;
        while ((symbol = inputStream.read()) != -1) {
            frequencies[symbol]++;
            totalSymbols++;
        }
        int uniqueSymboles = 0;
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] != 0) {
                uniqueSymboles++;
            }
        }
        System.out.println("Number of Unique Symbols = " + uniqueSymboles);
        frequencies[END_OF_FILE]++;
        // System.out.println(Arrays.toString(frequencies));
        totalSymbols++;
        // write unique symbols count to know end of the header
        dataOutputStream.writeInt(uniqueSymboles);
        // Write frequencies to the output file
        for (int i = 0; i < END_OF_FILE; i++) {
            if (frequencies[i] != 0) {
                dataOutputStream.writeByte(i);
                dataOutputStream.writeInt(frequencies[i]);
            }
        }

        inputStream.close();
        inputStream = new FileInputStream(filePath);

        // Compute cumulative frequencies
        int[] cumulativeFrequencies = new int[END_OF_FILE + 2];
        for (int i = 0; i <= END_OF_FILE; i++) {
            cumulativeFrequencies[i + 1] = cumulativeFrequencies[i] + frequencies[i];
        }

        // Arithmetic coding compression
        long lower = 0;
        long upper = (1L << BITS_TO_FOLLOW) - 1;
        long range;
        long bitsToFollow = 0;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BitWriter bitWriter = new BitWriter(byteArrayOutputStream);

        while ((symbol = inputStream.read()) != -1) {
            range = upper - lower + 1;
            upper = lower + range * cumulativeFrequencies[symbol + 1] / totalSymbols - 1;
            lower = lower + range * cumulativeFrequencies[symbol] / totalSymbols;

            while (true) {
                if (upper < (1L << (BITS_TO_FOLLOW - 1))) {
                    bitWriter.writeBit(0);
                    for (; bitsToFollow > 0; bitsToFollow--) {
                        bitWriter.writeBit(1);
                    }
                } else if (lower >= (1L << (BITS_TO_FOLLOW - 1))) {
                    bitWriter.writeBit(1);
                    for (; bitsToFollow > 0; bitsToFollow--) {
                        bitWriter.writeBit(0);
                    }
                    lower -= (1L << (BITS_TO_FOLLOW - 1));
                    upper -= (1L << (BITS_TO_FOLLOW - 1));
                } else if (lower >= (1L << (BITS_TO_FOLLOW - 2)) && upper < (3L << (BITS_TO_FOLLOW - 2))) {
                    bitsToFollow++;
                    lower -= (1L << (BITS_TO_FOLLOW - 2));
                    upper -= (1L << (BITS_TO_FOLLOW - 2));
                } else {
                    break;
                }
                lower <<= 1;
                upper = (upper << 1) + 1;
            }
        }

        // Handle the end of file symbol
        range = upper - lower + 1;
        upper = lower + range * cumulativeFrequencies[END_OF_FILE + 1] / totalSymbols - 1;
        lower = lower + range * cumulativeFrequencies[END_OF_FILE] / totalSymbols;

        while (true) {
            if (upper < (1L << (BITS_TO_FOLLOW - 1))) {
                bitWriter.writeBit(0);
                for (; bitsToFollow > 0; bitsToFollow--) {
                    bitWriter.writeBit(1);
                }
            } else if (lower >= (1L << (BITS_TO_FOLLOW - 1))) {
                bitWriter.writeBit(1);
                for (; bitsToFollow > 0; bitsToFollow--) {
                    bitWriter.writeBit(0);
                }
                lower -= (1L << (BITS_TO_FOLLOW - 1));
                upper -= (1L << (BITS_TO_FOLLOW - 1));
            } else if (lower >= (1L << (BITS_TO_FOLLOW - 2)) && upper < (3L << (BITS_TO_FOLLOW - 2))) {
                bitsToFollow++;
                lower -= (1L << (BITS_TO_FOLLOW - 2));
                upper -= (1L << (BITS_TO_FOLLOW - 2));
            } else {
                break;
            }
            lower <<= 1;
            upper = (upper << 1) + 1;
        }
        bitsToFollow++;
        if (lower < (1L << (BITS_TO_FOLLOW - 2))) {
            bitWriter.writeBit(0);
            for (; bitsToFollow > 0; bitsToFollow--) {
                bitWriter.writeBit(1);
            }
        } else {
            bitWriter.writeBit(1);
            for (; bitsToFollow > 0; bitsToFollow--) {
                bitWriter.writeBit(0);
            }
        }

        inputStream.close();
        bitWriter.flush();
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        dataOutputStream.writeInt(byteArray.length);
        dataOutputStream.write(byteArray);
        dataOutputStream.close();
        long end = System.currentTimeMillis();
        File file = new File(filePath);
        Long inputSize = file.length();
        File file2 = new File(filePath + ".compressed");
        Long outputSize = file2.length();
        System.out.println("Compression Ratio = " + ((double) inputSize / outputSize));
        System.out.println("Time taken to compress = " + (end - start) / 1000.0 + " Sec");
        System.out.println("File Compressed Successfully");
    }

    private static void decompress(String compressedFilePath, String outputFilePath) throws IOException {
        long start = System.currentTimeMillis();
        FileInputStream inputStream = new FileInputStream(compressedFilePath);
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        FileOutputStream outputStream = new FileOutputStream(outputFilePath);

        // Read frequencies
        int[] frequencies = new int[END_OF_FILE + 1];
        int totalSymbols = 0;
        int uniqueSymboles = dataInputStream.readInt();
        System.out.println("number of unique symbols: " + uniqueSymboles);
        for (int i = 0; i < uniqueSymboles; i++) {
            int symbol = dataInputStream.readByte();
            frequencies[symbol] = dataInputStream.readInt();
            totalSymbols += frequencies[symbol];
        }
        frequencies[END_OF_FILE] = 1;
        totalSymbols += 1;
        // System.out.println(Arrays.toString(frequencies));
        // Compute cumulative frequencies
        int[] cumulativeFrequencies = new int[END_OF_FILE + 2];
        for (int i = 0; i <= END_OF_FILE; i++) {
            cumulativeFrequencies[i + 1] = cumulativeFrequencies[i] + frequencies[i];
        }

        // Read encoded bytes
        int encodedBytesLength = dataInputStream.readInt();
        byte[] encodedBytes = new byte[encodedBytesLength];
        dataInputStream.readFully(encodedBytes);
        inputStream.close();

        BitReader bitReader = new BitReader(encodedBytes);

        // Arithmetic decoding
        long lower = 0;
        long upper = (1L << BITS_TO_FOLLOW) - 1;
        long code = 0;

        for (int i = 0; i < BITS_TO_FOLLOW; i++) {
            code = (code << 1) | bitReader.readBit();
        }

        while (true) {
            long range = upper - lower + 1;
            long scaledValue = ((code - lower + 1) * totalSymbols - 1) / range;
            int symbol;
            for (symbol = 0; cumulativeFrequencies[symbol + 1] <= scaledValue; symbol++)
                ;

            if (symbol == END_OF_FILE)
                break;

            outputStream.write(symbol);

            upper = lower + range * cumulativeFrequencies[symbol + 1] / totalSymbols - 1;
            lower = lower + range * cumulativeFrequencies[symbol] / totalSymbols;

            while (true) {
                if (upper < (1L << (BITS_TO_FOLLOW - 1))) {
                } else if (lower >= (1L << (BITS_TO_FOLLOW - 1))) {
                    lower -= (1L << (BITS_TO_FOLLOW - 1));
                    upper -= (1L << (BITS_TO_FOLLOW - 1));
                    code -= (1L << (BITS_TO_FOLLOW - 1));
                } else if (lower >= (1L << (BITS_TO_FOLLOW - 2)) && upper < (3L << (BITS_TO_FOLLOW - 2))) {
                    lower -= (1L << (BITS_TO_FOLLOW - 2));
                    upper -= (1L << (BITS_TO_FOLLOW - 2));
                    code -= (1L << (BITS_TO_FOLLOW - 2));
                } else {
                    break;
                }
                lower = (lower << 1) & ((1L << BITS_TO_FOLLOW) - 1);
                upper = ((upper << 1) | 1) & ((1L << BITS_TO_FOLLOW) - 1);
                code = ((code << 1) | bitReader.readBit()) & ((1L << BITS_TO_FOLLOW) - 1);
            }
        }
        outputStream.close();
        long end = System.currentTimeMillis();
        System.out.println("Time taken to decompress = " + (end - start) / 1000.0 + " Sec");
        System.out.println("File Decompressed Successfully");
    }
}