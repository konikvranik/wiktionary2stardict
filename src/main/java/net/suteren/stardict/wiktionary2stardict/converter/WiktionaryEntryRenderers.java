package net.suteren.stardict.wiktionary2stardict.converter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import net.suteren.stardict.wiktionary2stardict.model.Derived;
import net.suteren.stardict.wiktionary2stardict.model.Form;
import net.suteren.stardict.wiktionary2stardict.model.Related;
import net.suteren.stardict.wiktionary2stardict.model.Sense;
import net.suteren.stardict.wiktionary2stardict.model.Sound;
import net.suteren.stardict.wiktionary2stardict.model.Translation;
import net.suteren.stardict.wiktionary2stardict.model.WiktionaryEntry;

/**
 * Simple renderers for WiktionaryEntry to different markup outputs.
 *
 * The goal is to keep it minimal and dependency-free while producing
 * valid-enough XDXF and HTML for StarDict usage.
 */
public final class WiktionaryEntryRenderers {

    private WiktionaryEntryRenderers() {}

    public static String toPango(WiktionaryEntry e) {
        if (e == null) return "";
        StringBuilder sb = new StringBuilder();
        String title = Optional.ofNullable(e.getWord()).orElse("");
        // Title in bold
        sb.append("<b>").append(escapeXml(title)).append("</b>");
        if (isNotBlank(e.getPos())) {
            sb.append(" <small><i>(").append(escapeXml(e.getPos())).append(")</i></small>");
        }
        // IPA line(s)
        var ipa = Optional.ofNullable(e.getSounds()).stream().flatMap(Collection::stream)
            .map(Sound::getIpa).filter(WiktionaryEntryRenderers::isNotBlank).collect(Collectors.toList());
        if (!ipa.isEmpty()) {
            sb.append("\n");
            for (int i = 0; i < ipa.size(); i++) {
                if (i>0) sb.append(", ");
                sb.append("/<i>").append(escapeXml(ipa.get(i))).append("</i>/");
            }
        }
        // Senses: number them manually
        var senses = Optional.ofNullable(e.getSenses()).orElse(List.of());
        if (!senses.isEmpty()) {
            int n = 1;
            for (Sense s : senses) {
                if (s == null) continue;
                String text = firstNonBlank(s.getSense(), joinNonBlank(s.getGlosses(), ", "));
                if (isBlank(text)) continue;
                sb.append("\n").append(n++).append(". ").append(escapeXml(text));
            }
        }
        // Forms
        var forms = Optional.ofNullable(e.getForms()).orElse(List.of());
        if (!forms.isEmpty()) {
            sb.append("\n").append("Forms: ");
            boolean first = true;
            for (Form f : forms) {
                if (f == null || isBlank(f.getForm())) continue;
                if (!first) sb.append("; ");
                sb.append(escapeXml(f.getForm()));
                var tags = Optional.ofNullable(f.getTags()).orElse(List.of());
                if (!tags.isEmpty()) {
                    sb.append(" (" ).append(escapeXml(String.join(", ", tags))).append(")");
                }
                first = false;
            }
        }
        // Etymology
        var etys = Optional.ofNullable(e.getEtymology_texts()).orElse(List.of());
        if (!etys.isEmpty()) {
            sb.append("\nEtymology: ").append(escapeXml(String.join("; ", etys.stream().filter(WiktionaryEntryRenderers::isNotBlank).toList())));
        }
        // Derived & Related
        var derived = Optional.ofNullable(e.getDerived()).orElse(List.of());
        if (!derived.isEmpty()) {
            sb.append("\nDerived: ").append(escapeXml(derived.stream().map(Derived::getWord).filter(WiktionaryEntryRenderers::isNotBlank).collect(Collectors.joining("; "))));
        }
        var related = Optional.ofNullable(e.getRelated()).orElse(List.of());
        if (!related.isEmpty()) {
            sb.append("\nRelated: ").append(escapeXml(related.stream().map(Related::getWord).filter(WiktionaryEntryRenderers::isNotBlank).collect(Collectors.joining("; "))));
        }
        // Translations (compact)
        var translations = Optional.ofNullable(e.getTranslations()).orElse(List.of());
        if (!translations.isEmpty()) {
            sb.append("\nTranslations: ");
            boolean first = true;
            for (Translation t : translations) {
                if (t == null || isBlank(t.getWord())) continue;
                if (!first) sb.append("; ");
                String lang = firstNonBlank(t.getLang_code(), t.getLang());
                if (isNotBlank(lang)) sb.append("[" + escapeXml(lang) + "] ");
                sb.append(escapeXml(t.getWord()));
                first = false;
            }
        }
        return sb.toString();
    }

    public static String toHtml(WiktionaryEntry e) {
        if (e == null) return "";
        StringBuilder sb = new StringBuilder();
        String title = Optional.ofNullable(e.getWord()).orElse("");
        sb.append("<div class=\"entry\">");
        sb.append("<div class=\"head\">").append(escapeHtml(title));
        if (e.getPos() != null) {
            sb.append(" <span class=\"pos\">(").append(escapeHtml(e.getPos())).append(")</span>");
        }
        sb.append("</div>");

        // IPA
        var ipa = Optional.ofNullable(e.getSounds()).stream().flatMap(Collection::stream)
            .map(Sound::getIpa).filter(WiktionaryEntryRenderers::isNotBlank).collect(Collectors.toList());
        if (!ipa.isEmpty()) {
            sb.append("<div class=\"pron\">");
            for (int i = 0; i < ipa.size(); i++) {
                if (i>0) sb.append(", ");
                sb.append("/<span class=\"ipa\">").append(escapeHtml(ipa.get(i))).append("</span>/");
            }
            sb.append("</div>");
        }

        // Forms
        var forms = Optional.ofNullable(e.getForms()).orElse(List.of());
        if (!forms.isEmpty()) {
            sb.append("<div class=\"forms\"><span class=\"label\">Forms:</span> <ul>");
            for (Form f : forms) {
                if (f == null || isBlank(f.getForm())) continue;
                sb.append("<li>").append(escapeHtml(f.getForm()));
                var tags = Optional.ofNullable(f.getTags()).orElse(List.of());
                if (!tags.isEmpty()) {
                    sb.append(" <span class=\"tags\">(").append(escapeHtml(String.join(", ", tags))).append(")</span>");
                }
                sb.append("</li>");
            }
            sb.append("</ul></div>");
        }

        // Etymology
        var etys = Optional.ofNullable(e.getEtymology_texts()).orElse(List.of());
        if (!etys.isEmpty()) {
            sb.append("<div class=\"etymology\"><span class=\"label\">Etymology:</span> ")
              .append(escapeHtml(String.join("; ", etys.stream().filter(WiktionaryEntryRenderers::isNotBlank).toList())))
              .append("</div>");
        }

        // Senses/definitions
        var senses = Optional.ofNullable(e.getSenses()).orElse(List.of());
        if (!senses.isEmpty()) {
            sb.append("<ol class=\"senses\">");
            for (Sense s : senses) {
                if (s == null) continue;
                String text = firstNonBlank(s.getSense(), joinNonBlank(s.getGlosses(), ", "));
                if (isBlank(text)) continue;
                sb.append("<li>").append(escapeHtml(text)).append("</li>");
            }
            sb.append("</ol>");
        }

        // Derived & Related
        var derived = Optional.ofNullable(e.getDerived()).orElse(List.of());
        if (!derived.isEmpty()) {
            sb.append("<div class=\"derived\"><span class=\"label\">Derived:</span> <ul>");
            for (Derived d : derived) {
                if (d == null || isBlank(d.getWord())) continue;
                sb.append("<li>").append(escapeHtml(d.getWord())).append("</li>");
            }
            sb.append("</ul></div>");
        }
        var related = Optional.ofNullable(e.getRelated()).orElse(List.of());
        if (!related.isEmpty()) {
            sb.append("<div class=\"related\"><span class=\"label\">Related:</span> <ul>");
            for (Related r : related) {
                if (r == null || isBlank(r.getWord())) continue;
                sb.append("<li>").append(escapeHtml(r.getWord())).append("</li>");
            }
            sb.append("</ul></div>");
        }

        // Translations
        var translations = Optional.ofNullable(e.getTranslations()).orElse(List.of());
        if (!translations.isEmpty()) {
            sb.append("<div class=\"translations\"><span class=\"label\">Translations:</span> <ul>");
            for (Translation t : translations) {
                if (t == null || isBlank(t.getWord())) continue;
                String lang = firstNonBlank(t.getLang_code(), t.getLang());
                sb.append("<li>");
                if (isNotBlank(lang)) sb.append("<span class=\"lang\">[").append(escapeHtml(lang)).append("] </span>");
                sb.append(escapeHtml(t.getWord())).append("</li>");
            }
            sb.append("</ul></div>");
        }

        sb.append("</div>");
        return sb.toString();
    }

    public static String toXdxf(WiktionaryEntry e) {
        if (e == null) return "";
        // Minimal XDXF body per standard used in StarDict (without dictionary wrapper)
        StringBuilder sb = new StringBuilder();
        String k = Optional.ofNullable(e.getWord()).orElse("");
        sb.append("<k>").append(escapeXml(k)).append("</k>");
        if (isNotBlank(e.getPos())) {
            sb.append(" <i>").append(escapeXml(e.getPos())).append("</i>");
        }
        // IPA as [trn]
        var ipa = Optional.ofNullable(e.getSounds()).stream().flatMap(Collection::stream)
            .map(Sound::getIpa).filter(WiktionaryEntryRenderers::isNotBlank).collect(Collectors.toList());
        for (String i : ipa) {
            sb.append(" <trn>").append(escapeXml(i)).append("</trn>");
        }
        var senses = Optional.ofNullable(e.getSenses()).orElse(List.of());
        if (!senses.isEmpty()) {
            sb.append(" <def>");
            boolean first = true;
            for (Sense s : senses) {
                if (s == null) continue;
                String text = firstNonBlank(s.getSense(), joinNonBlank(s.getGlosses(), ", "));
                if (isBlank(text)) continue;
                if (!first) sb.append("; ");
                sb.append(escapeXml(text));
                first = false;
            }
            sb.append("</def>");
        }
        // Append other sections as additional defs
        var forms = Optional.ofNullable(e.getForms()).orElse(List.of());
        if (!forms.isEmpty()) {
            sb.append(" <def>").append(escapeXml("Forms: "))
              .append(escapeXml(forms.stream()
                  .filter(Objects::nonNull)
                  .map(f -> f.getForm() + tagsSuffix(f.getTags()))
                  .filter(WiktionaryEntryRenderers::isNotBlank)
                  .collect(Collectors.joining("; "))))
              .append("</def>");
        }
        var etys = Optional.ofNullable(e.getEtymology_texts()).orElse(List.of());
        if (!etys.isEmpty()) {
            sb.append(" <def>").append(escapeXml("Etymology: "))
              .append(escapeXml(String.join("; ", etys.stream().filter(WiktionaryEntryRenderers::isNotBlank).toList())))
              .append("</def>");
        }
        var derived = Optional.ofNullable(e.getDerived()).orElse(List.of());
        if (!derived.isEmpty()) {
            sb.append(" <def>").append(escapeXml("Derived: "))
              .append(escapeXml(derived.stream().map(Derived::getWord).filter(WiktionaryEntryRenderers::isNotBlank).collect(Collectors.joining("; "))))
              .append("</def>");
        }
        var related = Optional.ofNullable(e.getRelated()).orElse(List.of());
        if (!related.isEmpty()) {
            sb.append(" <def>").append(escapeXml("Related: "))
              .append(escapeXml(related.stream().map(Related::getWord).filter(WiktionaryEntryRenderers::isNotBlank).collect(Collectors.joining("; "))))
              .append("</def>");
        }
        return sb.toString();
    }

    private static String tagsSuffix(List<String> tags){
        if (tags == null || tags.isEmpty()) return "";
        return " (" + String.join(", ", tags) + ")";
    }

    private static boolean isBlank(String s){ return s == null || s.isBlank(); }
    private static boolean isNotBlank(String s){ return !isBlank(s); }

    private static String firstNonBlank(String a, String b){
        return isNotBlank(a) ? a : (isNotBlank(b) ? b : null);
    }
    private static String joinNonBlank(List<String> list, String sep){
        if (list == null) return null;
        String j = list.stream().filter(WiktionaryEntryRenderers::isNotBlank).collect(Collectors.joining(sep));
        return j.isBlank()? null : j;
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    private static String escapeXml(String s) { // same minimal escaping
        return escapeHtml(s).replace("\"", "&quot;").replace("'", "&apos;");
    }
}
