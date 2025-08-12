from ebook_dictionary_creator import DictionaryCreator


def extract_dict(from_lang, to_lang):
    dict_creator = DictionaryCreator(source_language=from_lang, target_language=to_lang)
    dict_creator.download_data_from_kaikki()
    dict_creator.create_database()
    dict_creator.export_to_stardict("hPa", "%s to %s dictionary" % (from_lang, to_lang),
                                    "%s-%s-dict.ifo" % (from_lang, to_lang))


for lang in ["Italian", "French", "Latin", "Polish", "Esperanto", "Hungarian", "Translingual", "Serbo-Croatian", "Slovak",
             "Spanish"]:
    extract_dict("Czech", lang)
    extract_dict(lang, "Czech")
