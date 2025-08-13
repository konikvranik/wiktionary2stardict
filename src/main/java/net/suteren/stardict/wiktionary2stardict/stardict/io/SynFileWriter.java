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
 * Class for writing StarDict .syn files
 */
public class SynFileWriter {

    /**
     * Writes synonyms to a .syn file
     * @param filename Path to the .syn file
     * @param entries List of synonym records
     * @throws IOException When a file writing error occurs
     */
    public static void writeSynFile(String filename, List<SynonymumEntry> entries) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filename);
             FileChannel channel = fos.getChannel()) {

            for (SynonymumEntry entry : entries) {
                // Convert the record to a byte array
                byte[] entryBytes = entryToBytes(entry);
                
                // Write the record to the file
                ByteBuffer buffer = ByteBuffer.wrap(entryBytes);
                channel.write(buffer);
            }
        }
    }
    
    /**
     * Converts a synonym record to a byte array
     * @param entry Synonym record
     * @return Byte array representing the record
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