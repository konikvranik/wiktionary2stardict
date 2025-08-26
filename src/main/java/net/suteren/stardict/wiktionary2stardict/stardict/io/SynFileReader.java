package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.suteren.stardict.wiktionary2stardict.stardict.domain.SynonymumEntry;

/**
 * Class for reading StarDict .syn files
 */
public class SynFileReader {

    /**
     * Loads synonyms from a .syn file
     * @param filename Path to the .syn file
     * @return List of synonym records
     * @throws IOException When a file reading error occurs
     */
    public static List<SynonymumEntry> readSynFile(String filename) throws IOException {
        List<SynonymumEntry> entries = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filename);
             FileChannel channel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            buffer.order(ByteOrder.BIG_ENDIAN);
            channel.read(buffer);
            buffer.flip();

            while (buffer.hasRemaining()) {
                // Reading UTF-8 string up to the null terminator
                StringBuilder wordBuilder = new StringBuilder();
                byte b;
                while ((b = buffer.get()) != 0) {
                    wordBuilder.append((char) (b & 0xFF));
                }
                String word = new String(wordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

                // Reading the index of the original word (32-bit number in network byte order)
                int originalWordIndex = buffer.getInt();

                entries.add(new SynonymumEntry(word, originalWordIndex));
            }
        }

        return entries;
    }
}