package net.suteren.stardict.wiktionary2stardict.converter;

import java.util.Optional;
import java.util.stream.Collectors;

import net.suteren.stardict.wiktionary2stardict.model.Sense;
import net.suteren.stardict.wiktionary2stardict.model.Sound;
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
        if (e.getPos() != null && !e.getPos().isBlank()) {
            sb.append(" <small><i>(").append(escapeXml(e.getPos())).append(")</i></small>");
        }
        // IPA line(s)
        var ipa = Optional.ofNullable(e.getSounds()).stream().flatMap(java.util.Collection::stream)
            .map(Sound::getIpa).filter(s -> s != null && !s.isBlank()).collect(Collectors.toList());
        if (!ipa.isEmpty()) {
            sb.append("\n");
            for (int i = 0; i < ipa.size(); i++) {
                if (i>0) sb.append(", ");
                sb.append("/<i>").append(escapeXml(ipa.get(i))).append("</i>/");
            }
        }
        // Senses: number them manually
        var senses = Optional.ofNullable(e.getSenses()).orElse(java.util.List.of());
        if (!senses.isEmpty()) {
            int n = 1;
            for (Sense s : senses) {
                if (s == null) continue;
                String text = s.getSense();
                if (text == null || text.isBlank()) continue;
                sb.append("\n").append(n++).append(". ").append(escapeXml(text));
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
        var ipa = Optional.ofNullable(e.getSounds()).stream().flatMap(java.util.Collection::stream)
            .map(Sound::getIpa).filter(s -> s != null && !s.isBlank()).collect(Collectors.toList());
        if (!ipa.isEmpty()) {
            sb.append("<div class=\"pron\">");
            for (int i = 0; i < ipa.size(); i++) {
                if (i>0) sb.append(", ");
                sb.append("/<span class=\"ipa\">").append(escapeHtml(ipa.get(i))).append("</span>/");
            }
            sb.append("</div>");
        }

        // Senses/definitions
        var senses = Optional.ofNullable(e.getSenses()).orElse(java.util.List.of());
        if (!senses.isEmpty()) {
            sb.append("<ol class=\"senses\">");
            for (Sense s : senses) {
                if (s == null) continue;
                String text = s.getSense();
                if (text == null || text.isBlank()) continue;
                sb.append("<li>").append(escapeHtml(text)).append("</li>");
            }
            sb.append("</ol>");
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
        if (e.getPos() != null && !e.getPos().isBlank()) {
            sb.append(" <i>").append(escapeXml(e.getPos())).append("</i>");
        }
        // IPA as [trn]
        var ipa = Optional.ofNullable(e.getSounds()).stream().flatMap(java.util.Collection::stream)
            .map(Sound::getIpa).filter(s -> s != null && !s.isBlank()).collect(Collectors.toList());
        for (String i : ipa) {
            sb.append(" <trn>").append(escapeXml(i)).append("</trn>");
        }
        var senses = Optional.ofNullable(e.getSenses()).orElse(java.util.List.of());
        if (!senses.isEmpty()) {
            sb.append(" <def>");
            boolean first = true;
            for (Sense s : senses) {
                if (s == null) continue;
                String text = s.getSense();
                if (text == null || text.isBlank()) continue;
                if (!first) sb.append("; ");
                sb.append(escapeXml(text));
                first = false;
            }
            sb.append("</def>");
        }
        return sb.toString();
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
    private static String escapeXml(String s) { // same minimal escaping
        return escapeHtml(s).replace("\"", "&quot;").replace("'", "&apos;");
    }
}
