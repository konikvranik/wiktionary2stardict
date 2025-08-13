# StarDict File Format Implementation Summary

This document summarizes the implementation of StarDict file format reading and writing functionality.

## Components Implemented

### Index (.idx) File Handling
- **IdxEntry.java**: Record class representing an entry in the index file with word, offset, and size.
- **IdxFileReader.java**: Reads .idx files and parses them into a list of IdxEntry objects.
- **IdxFileWriter.java**: Writes a collection of IdxEntry objects to a .idx file.

### Dictionary (.dict) File Handling
- **DefinitionEntry.java**: Represents a single definition entry with a type and content.
- **WordDefinition.java**: Represents a word's complete definition with multiple DefinitionEntry objects.
- **DictFileReader.java**: Reads .dict files using information from .idx entries.
- **DictFileWriter.java**: Writes WordDefinition objects to a .dict file.

### Synonyms (.syn) File Handling
- **SynonymumEntry.java**: Record class representing an entry in the synonyms file.
- **SynFileReader.java**: Reads .syn files and parses them into a list of SynonymumEntry objects.
- **SynFileWriter.java**: Writes a list of SynonymumEntry objects to a .syn file.

### Utilities
- **StardictIoUtil.java**: Provides utility methods for handling network byte order and UTF-8 string encoding/decoding.

## Implementation Details

All implementations follow the StarDict format specification as described in the documentation, including:
- Proper handling of network byte order (big-endian) for numeric values
- UTF-8 encoding for strings
- Support for the sameTypeSequence optimization
- Handling of different data types (m, g, h, etc.)

## Usage Example

```java
// Reading a dictionary
List<IdxEntry> idxEntries = IdxFileReader.readIdxFile("dictionary.idx");
Map<String, WordDefinition> definitions = DictFileReader.readDictFile("dictionary.dict", idxEntries, "mgh");
List<SynonymumEntry> synonyms = SynFileReader.readSynFile("dictionary.syn");

// Writing a dictionary
List<IdxEntry> idxEntries = DictFileWriter.writeDictFile("dictionary.dict", definitions, "mgh");
IdxFileWriter.writeIdxFile("dictionary.idx", idxEntries);
SynFileWriter.writeSynFile("dictionary.syn", synonyms);
```