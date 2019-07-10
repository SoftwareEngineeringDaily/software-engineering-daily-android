package com.koalatea.sedaily.util

object EpisodeHtmlUtils {

    fun cleanHtml(html: String) : String =
            removeImage(html = addScaleMeta(html = removePlayerAndLinksTags(html = html)))

    fun removePlayerAndLinksTags(html: String) : String {
        var modifiedHtml = html

        modifiedHtml = modifiedHtml.removePrefix("<!--powerpress_player-->")

        // Delete player div.
        val playerRange = IntRange(
                start = modifiedHtml.indexOf("<div class=\"powerpress_player\""),
                endInclusive = modifiedHtml.indexOf("</div>") + "</div>".length - 1)
        if (playerRange.first >= 0 && playerRange.first < playerRange.last) {
            modifiedHtml = modifiedHtml.removeRange(playerRange)
        }

        // Delete links p.
        val linksRange = IntRange(
                start = modifiedHtml.indexOf("<p class=\"powerpress_links powerpress_links_mp3\""),
                endInclusive = modifiedHtml.indexOf("</p>") + "</p>".length - 1)
        if (linksRange.first >= 0 && linksRange.first < linksRange.last) {
            modifiedHtml = modifiedHtml.removeRange(linksRange)
        }

        return modifiedHtml
    }

    private fun removeImage(html: String) : String {
        var modifiedHtml = html

        val endTag = "data-recalc-dims=\"1\" />"
        val playerRange = IntRange(
                start = modifiedHtml.indexOf("<img"),
                endInclusive = modifiedHtml.indexOf(endTag) + endTag.length - 1)
        if (playerRange.first >= 0 && playerRange.first < playerRange.last) {
            modifiedHtml = modifiedHtml.removeRange(playerRange)
        }

        return modifiedHtml
    }

    private fun addScaleMeta(html: String) : String =
            "<meta name=\"viewport\" content=\"initial-scale=1.0\" />$html"

}