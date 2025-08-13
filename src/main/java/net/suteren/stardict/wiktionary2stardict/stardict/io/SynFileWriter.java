package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;

import net.suteren.stardict.wiktionary2stardict.stardict.files.SynonymumEntry;

/**
 * Třída pro zápis StarDict .syn souborů
 */
public class SynFileWriter {

    /**
     * Zapíše synonyma do .syn souboru
     * @param filename Cesta k .syn souboru
     * @param entries Seznam záznamů synonym
     * @throws IOException Při chybě zápisu souboru
     */
    public static void writeSynFile(String filename, List<SynonymumEntry> entries) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filename);
             FileChannel channel = fos.getChannel()) {

            for (SynonymumEntry entry : entries) {
                // Převedeme záznam na pole bytů
                byte[] entryBytes = entryToBytes(entry);
                
                // Zapíšeme záznam do souboru
                ByteBuffer buffer = ByteBuffer.wrap(entryBytes);
                channel.write(buffer);
            }
        }
    }
    
    /**
     * Převede záznam synonyma na pole bytů
     * @param entry Záznam synonyma
     * @return Pole bytů reprezentující záznam
     */
    private static byte[] entryToBytes(SynonymumEntry entry) {
        byte[] wordBytes = entry.word().getBytes(StandardCharsets.UTF_8);
        
        ByteBuffer buffer = ByteBuffer.allocate(wordBytes.length + 1 + 4); // word + null terminator + 32-bit int
        buffer.order(ByteOrder.BIG_ENDIAN);
        
        buffer.put(wordBytes);        // UTF-8 encoded word
        buffer.put((byte) 0);         // null terminator
        buffer.putInt(entry.indexPosition()); // index position in network byte order
        
        return buffer.array();
    }
}