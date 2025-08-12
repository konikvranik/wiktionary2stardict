import argparse
import os
import sys
import urllib.request
import subprocess
import wiktextract

def get_dump_url_for_lang(lang_code):
    lang = lang_code.lower()
    return f"https://dumps.wikimedia.org/{lang}wiktionary/latest/{lang}wiktionary-latest-pages-articles.xml.bz2"

def download_dump(dump_url, dump_file):
    if os.path.exists(dump_file):
        print(f"Dump {dump_file} už existuje, přeskočuju stahování.")
        return
    print(f"Stahuji dump z {dump_url} ...")
    urllib.request.urlretrieve(dump_url, dump_file)
    print("Staženo.")

def run_wiktextract(dump_file, jsonl_file):
    print("Spouštím wiktextract ...")
    # Parametry lze podle potřeby rozšířit
    wiktextract.wiktextract(input_file=dump_file, json=jsonl_file)
    print("wiktextract dokončen.")

def convert_jsonl_to_csv(jsonl_file, csv_file):
    print("Konvertuji JSONL na CSV ...")
    # Jednoduchý příklad konverze (slovník: heslo -> překlad)
    import json

    with open(jsonl_file, encoding="utf-8") as f_in, open(csv_file, "w", encoding="utf-8") as f_out:
        f_out.write("word\ttranslation\n")
        for line in f_in:
            entry = json.loads(line)
            # Tady musíš upravit podle struktury jsonu a hledaných jazyků!
            word = entry.get("word", "")
            # Příklad: vybereme překlad do cílového jazyka (zjednodušeno)
            translation = ""
            for lang, defs in entry.get("definitions", {}).items():
                translation = defs[0]["glosses"][0] if defs and "glosses" in defs[0] else ""
                break
            f_out.write(f"{word}\t{translation}\n")
    print("Konverze dokončena.")

def run_pyglossary(csv_file, stardict_file):
    print("Spouštím pyglossary pro převod do Stardict ...")
    # Použij CLI - je třeba mít pyglossary nainstalovaný
    subprocess.run([
        sys.executable, "-m", "pyglossary", csv_file, stardict_file
    ], check=True)
    print("Převod dokončen.")

def main():
    parser = argparse.ArgumentParser(description="Vytvoření Stardict slovníku z dumpu Wikislovníku.")
    parser.add_argument("source_lang", help="Zdrojový jazyk (např. czech)")
    parser.add_argument("target_lang", help="Cílový jazyk (např. italian)")
    parser.add_argument("--dump-url", help="URL dumpu (pokud není zadáno, vytvoří se automaticky)")
    parser.add_argument("--dump-file", help="Lokální soubor dumpu (pokud není zadán, stáhne se podle URL)")
    parser.add_argument("--jsonl-file", default="wiktextract.jsonl", help="Výstupní JSONL soubor z wiktextract")
    parser.add_argument("--csv-file", default="dict.csv", help="CSV soubor pro pyglossary")
    parser.add_argument("--stardict-file", default="dict.stardict", help="Výstup Stardict slovník")
    args = parser.parse_args()

    if not args.dump_url:
        args.dump_url = get_dump_url_for_lang(args.source_lang)

    if not args.dump_file:
        args.dump_file = os.path.basename(args.dump_url)

    download_dump(args.dump_url, args.dump_file)
    run_wiktextract(args.dump_file, args.jsonl_file)
    convert_jsonl_to_csv(args.jsonl_file, args.csv_file)
    run_pyglossary(args.csv_file, args.stardict_file)

if __name__ == "__main__":
    main()
