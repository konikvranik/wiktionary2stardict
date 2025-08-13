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
 * Třída pro zápis StarDict .dict souborů
 */
public class DictFileWriter {

    /**
     * Zapíše definice slov do .dict souboru a vytvoří záznamy pro .idx soubor
     * @param dictFilename Cesta k .dict souboru
     * @param definitions Mapa slov a jejich definic
     * @param sameTypeSequence Hodnota sameTypeSequence pro .ifo soubor (může být null)
     * @return Seznam záznamů pro .idx soubor
     * @throws IOException Při chybě zápisu souboru
     */
    public static List<IdxEntry> writeDictFile(String dictFilename, Map<String, WordDefinition> definitions, String sameTypeSequence) throws IOException {
        List<IdxEntry> idxEntries = new ArrayList<>();
        
        try (FileOutputStream fos = new FileOutputStream(dictFilename);
             FileChannel channel = fos.getChannel()) {
            
            int currentOffset = 0;
            
            for (Map.Entry<String, WordDefinition> entry : definitions.entrySet()) {
                String word = entry.getKey();
                WordDefinition wordDef = entry.getValue();
                
                // Převedeme definici na pole bytů
                byte[] definitionBytes = wordDef.toBytes(sameTypeSequence);
                
                // Zapíšeme definici do souboru
                ByteBuffer buffer = ByteBuffer.wrap(definitionBytes);
                channel.write(buffer);
                
                // Vytvoříme záznam pro .idx soubor
                IdxEntry idxEntry = new IdxEntry(word, currentOffset, definitionBytes.length);
                idxEntries.add(idxEntry);
                
                // Aktualizujeme offset pro další záznam
                currentOffset += definitionBytes.length;
            }
        }
        
        return idxEntries;
    }
    
    /**
     * Zapíše definice slov do .dict souboru s použitím existujících .idx záznamů
     * @param dictFilename Cesta k .dict souboru
     * @param definitions Mapa slov a jejich definic
     * @param idxEntries Seznam existujících .idx záznamů
     * @param sameTypeSequence Hodnota sameTypeSequence pro .ifo soubor (může být null)
     * @throws IOException Při chybě zápisu souboru
     */
    public static void writeDictFileWithExistingIdx(String dictFilename, Map<String, WordDefinition> definitions, 
                                                   List<IdxEntry> idxEntries, String sameTypeSequence) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dictFilename);
             FileChannel channel = fos.getChannel()) {
            
            // Vytvoříme mapu offsetů pro rychlé vyhledávání
            Map<String, IdxEntry> offsetMap = new java.util.HashMap<>();
            for (IdxEntry entry : idxEntries) {
                offsetMap.put(entry.word(), entry);
            }
            
            // Zapíšeme definice na správné offsety
            for (Map.Entry<String, WordDefinition> entry : definitions.entrySet()) {
                String word = entry.getKey();
                WordDefinition wordDef = entry.getValue();
                
                IdxEntry idxEntry = offsetMap.get(word);
                if (idxEntry == null) {
                    throw new IllegalArgumentException("Chybí idx záznam pro slovo: " + word);
                }
                
                // Převedeme definici na pole bytů
                byte[] definitionBytes = wordDef.toBytes(sameTypeSequence);
                
                // Ověříme, že velikost odpovídá záznamu v idx
                if (definitionBytes.length != idxEntry.size()) {
                    throw new IllegalArgumentException("Velikost definice neodpovídá záznamu v idx pro slovo: " + word);
                }
                
                // Nastavíme pozici v souboru podle offsetu v idx záznamu
                channel.position(idxEntry.offset());
                
                // Zapíšeme definici do souboru
                ByteBuffer buffer = ByteBuffer.wrap(definitionBytes);
                channel.write(buffer);
            }
        }
    }
}