package com.example.researcherapp.data.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

@Root(name = "feed", strict = false)
data class ArxivFeed(
    @field:ElementList(inline = true, required = false)
    var entry: List<ArxivEntry>? = null
)

@Root(name = "entry", strict = false)
data class ArxivEntry(
    @field:Element(name = "title") var title: String = "",
    @field:Element(name = "id") var id: String = "",
    @field:Element(name = "summary", required = false) var summary: String = "",
    @field:ElementList(inline = true, required = false)
    var link: List<Link> = mutableListOf()
)

@Root(name = "link", strict = false)
data class Link(
    @field:Attribute(name = "href", required = false)
    var href: String = "",

    @field:Attribute(name = "rel", required = false)
    var rel: String = "",

    @field:Attribute(name = "title", required = false)
    var title: String = ""
)