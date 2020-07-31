package hu.bagoly.snow.merengoapp.query

import org.jsoup.nodes.Document

fun isStoryPage(doc: Document) = doc.select("script[src='textsizer.js']").isNotEmpty()
