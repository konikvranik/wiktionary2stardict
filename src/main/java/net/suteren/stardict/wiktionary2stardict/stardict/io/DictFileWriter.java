package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.suteren.stardict.wiktionary2stardict.stardict.files.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.files.WordDefinition;

/**
 * Class for writing StarDict .dict files
 */
public class DictFileWriter {

    /**
     * Writes word definitions to a .dict file and creates records for the .idx file
     * @param dictFilename Path to the .dict file
     * @param definitions Map of words and their definitions
     * @param sameTypeSequence The sameTypeSequence value for the .ifo file (can be null)
     * @return List of records for the .idx file
     * @throws IOException When a file writing error occurs
     */
    public static List<IdxEntry> writeDictFile(String dictFilename, Map<String, WordDefinition> definitions, String sameTypeSequence) throws IOException {
        List<IdxEntry> idxEntries = new ArrayList<>();
        
        try (FileOutputStream fos = new FileOutputStream(dictFilename);
             FileChannel channel = fos.getChannel()) {
            
            int currentOffset = 0;
            
            for (Map.Entry<String, WordDefinition> entry : definitions.entrySet()) {
                String word = entry.getKey();
                WordDefinition wordDef = entry.getValue();
                
                // Convert the definition to a byte array
                byte[] definitionBytes = wordDef.toBytes(sameTypeSequence);
                
                // Write the definition to the file
                ByteBuffer buffer = ByteBuffer.wrap(definitionBytes);
                channel.write(buffer);
                
                // Create a record for the .idx file
                IdxEntry idxEntry = new IdxEntry(word, currentOffset, definitionBytes.length);
                idxEntries.add(idxEntry);
                
                // Update the offset for the next record
                currentOffset += definitionBytes.length;
            }
        }
        
        return idxEntries;
    }
    
    /**
     * Writes word definitions to a .dict file using existing .idx records
     * @param dictFilename Path to the .dict file
     * @param definitions Map of words and their definitions
     * @param idxEntries List of existing .idx records
     * @param sameTypeSequence The sameTypeSequence value for the .ifo file (can be null)
     * @throws IOException When a file writing error occurs
     */
    public static void writeDictFileWithExistingIdx(String dictFilename, Map<String, WordDefinition> definitions, 
                                                   List<IdxEntry> idxEntries, String sameTypeSequence) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dictFilename);
             FileChannel channel = fos.getChannel()) {
            
            // Create a map of offsets for quick lookup
            Map<String, IdxEntry> offsetMap = new java.util.HashMap<>();
            for (IdxEntry entry : idxEntries) {
                offsetMap.put(entry.word(), entry);
            }
            
            // Write definitions at the correct offsets
            for (Map.Entry<String, WordDefinition> entry : definitions.entrySet()) {
                String word = entry.getKey();
                WordDefinition wordDef = entry.getValue();
                
                IdxEntry idxEntry = offsetMap.get(word);
                if (idxEntry == null) {
                    throw new IllegalArgumentException("Missing idx record for word: " + word);
                }
                
                // Convert the definition to a byte array
                byte[] definitionBytes = wordDef.toBytes(sameTypeSequence);
                
                // Verify that the size matches the record in idx
                if (definitionBytes.length != idxEntry.size()) {
                    throw new IllegalArgumentException("Definition size does not match idx record for word: " + word);
                }
                
                // Set the position in the file according to the offset in the idx record
                channel.position(idxEntry.offset());
                
                // Write the definition to the file
                ByteBuffer buffer = ByteBuffer.wrap(definitionBytes);
                channel.write(buffer);
            }
        }
    }
}