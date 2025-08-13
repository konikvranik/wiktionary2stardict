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
 * Třída pro čtení StarDict .dict souborů
 */
public class DictFileReader {

    /**
     * Načte definice slov z .dict souboru podle záznamů v .idx souboru
     * @param dictFilename Cesta k .dict souboru
     * @param idxEntries Seznam záznamů z .idx souboru
     * @param sameTypeSequence Hodnota sameTypeSequence z .ifo souboru (může být null)
     * @return Mapa slov a jejich definic
     * @throws IOException Při chybě čtení souboru
     */
    public static Map<String, WordDefinition> readDictFile(String dictFilename, List<IdxEntry> idxEntries, String sameTypeSequence) throws IOException {
        Map<String, WordDefinition> definitions = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(dictFilename);
             FileChannel channel = fis.getChannel()) {

            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();

            for (IdxEntry entry : idxEntries) {
                // Nastavíme pozici v bufferu podle offsetu v idx záznamu
                buffer.position(entry.offset());
                
                // Načteme definici slova
                WordDefinition wordDef = WordDefinition.fromBytes(buffer, entry.size(), sameTypeSequence);
                wordDef.setWord(entry.word());
                
                definitions.put(entry.word(), wordDef);
            }
        }

        return definitions;
    }
    
    /**
     * Načte definici konkrétního slova z .dict souboru
     * @param dictFilename Cesta k .dict souboru
     * @param entry Záznam z .idx souboru
     * @param sameTypeSequence Hodnota sameTypeSequence z .ifo souboru (může být null)
     * @return Definice slova
     * @throws IOException Při chybě čtení souboru
     */
    public static WordDefinition readWordDefinition(String dictFilename, IdxEntry entry, String sameTypeSequence) throws IOException {
        try (FileInputStream fis = new FileInputStream(dictFilename);
             FileChannel channel = fis.getChannel()) {

            // Alokujeme buffer pouze pro velikost definice slova
            ByteBuffer buffer = ByteBuffer.allocate(entry.size());
            
            // Nastavíme pozici v souboru podle offsetu v idx záznamu
            channel.position(entry.offset());
            
            // Načteme data do bufferu
            channel.read(buffer);
            buffer.flip();
            
            // Vytvoříme definici slova
            WordDefinition wordDef = WordDefinition.fromBytes(buffer, entry.size(), sameTypeSequence);
            wordDef.setWord(entry.word());
            
            return wordDef;
        }
    }
}