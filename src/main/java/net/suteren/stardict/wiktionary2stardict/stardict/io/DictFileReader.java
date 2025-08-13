package net.suteren.stardict.wiktionary2stardict.stardict.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.suteren.stardict.wiktionary2stardict.stardict.files.IdxEntry;
import net.suteren.stardict.wiktionary2stardict.stardict.files.WordDefinition;

/**
 * Class for reading StarDict .dict files
 */
public class DictFileReader {

    /**
     * Loads word definitions from a .dict file according to records in the .idx file
     * @param dictFilename Path to the .dict file
     * @param idxEntries List of records from the .idx file
     * @param sameTypeSequence The sameTypeSequence value from the .ifo file (can be null)
     * @return Map of words and their definitions
     * @throws IOException When a file reading error occurs
     */
    public static Map<String, WordDefinition> readDictFile(String dictFilename, List<IdxEntry> idxEntries, String sameTypeSequence) throws IOException {
        Map<String, WordDefinition> definitions = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(dictFilename);
             FileChannel channel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();

            for (IdxEntry entry : idxEntries) {
                // Set the position in the buffer according to the offset in the idx record
                buffer.position(entry.offset());
                
                // Load the word definition
                WordDefinition wordDef = WordDefinition.fromBytes(buffer, entry.size(), sameTypeSequence);
                wordDef.setWord(entry.word());
                
                definitions.put(entry.word(), wordDef);
            }
        }

        return definitions;
    }
    
    /**
     * Loads the definition of a specific word from a .dict file
     * @param dictFilename Path to the .dict file
     * @param entry Record from the .idx file
     * @param sameTypeSequence The sameTypeSequence value from the .ifo file (can be null)
     * @return Word definition
     * @throws IOException When a file reading error occurs
     */
    public static WordDefinition readWordDefinition(String dictFilename, IdxEntry entry, String sameTypeSequence) throws IOException {
        try (FileInputStream fis = new FileInputStream(dictFilename);
             FileChannel channel = fis.getChannel()) {

            // Allocate buffer only for the size of the word definition
            ByteBuffer buffer = ByteBuffer.allocate(entry.size());
            
            // Set the position in the file according to the offset in the idx record
            channel.position(entry.offset());
            
            // Load data into the buffer
            channel.read(buffer);
            buffer.flip();
            
            // Create the word definition
            WordDefinition wordDef = WordDefinition.fromBytes(buffer, entry.size(), sameTypeSequence);
            wordDef.setWord(entry.word());
            
            return wordDef;
        }
    }
}