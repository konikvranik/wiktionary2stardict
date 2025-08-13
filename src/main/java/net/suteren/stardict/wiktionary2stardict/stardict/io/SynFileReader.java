package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import net.suteren.stardict.wiktionary2stardict.stardict.files.SynonymumEntry;

/**
 * Třída pro čtení StarDict .syn souborů
 */
public class SynFileReader {

    /**
     * Načte synonyma z .syn souboru
     * @param filename Cesta k .syn souboru
     * @return Seznam záznamů synonym
     * @throws IOException Při chybě čtení souboru
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
                // Načtení UTF-8 string až do null terminátoru
                StringBuilder wordBuilder = new StringBuilder();
                byte b;
                while ((b = buffer.get()) != 0) {
                    wordBuilder.append((char) (b & 0xFF));
                }
                String word = new String(wordBuilder.toString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

                // Načtení indexu původního slova (32-bit číslo v network byte order)
                int originalWordIndex = buffer.getInt();

                entries.add(new SynonymumEntry(word, originalWordIndex));
            }
        }

        return entries;
    }
}