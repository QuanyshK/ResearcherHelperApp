package com.example.researcherapp.data.model

import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root
import android.os.Parcel
import android.os.Parcelable

@Root(name = "feed", strict = false)
data class ArxivFeed(
    @field:ElementList(inline = true, required = false)
    var entry: List<ArxivEntry>? = null
)

@Root(name = "entry", strict = false)
data class ArxivEntry(
    @field:Element(name = "id", required = false)
    var id: String = "",

    @field:Element(name = "title", required = false)
    var title: String = "",

    @field:Element(name = "summary", required = false)
    var summary: String = "",

    @field:ElementList(inline = true, entry = "name", required = false)
    var authors: List<String> = emptyList(),

    @field:Element(name = "published", required = false)
    var published: String = "",

    @field:Element(name = "updated", required = false)
    var updated: String = "",

    @field:ElementList(inline = true, required = false)
    var link: List<Link> = mutableListOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(Link.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(summary)
        parcel.writeStringList(authors)
        parcel.writeString(published)
        parcel.writeString(updated)
        parcel.writeTypedList(link)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ArxivEntry> {
        override fun createFromParcel(parcel: Parcel): ArxivEntry {
            return ArxivEntry(parcel)
        }

        override fun newArray(size: Int): Array<ArxivEntry?> {
            return arrayOfNulls(size)
        }
    }
}

@Root(name = "link", strict = false)
data class Link(
    @field:Attribute(name = "href", required = false)
    var href: String = "",

    @field:Attribute(name = "rel", required = false)
    var rel: String = "",

    @field:Attribute(name = "title", required = false)
    var title: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(href)
        parcel.writeString(rel)
        parcel.writeString(title)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Link> {
        override fun createFromParcel(parcel: Parcel): Link {
            return Link(parcel)
        }

        override fun newArray(size: Int): Array<Link?> {
            return arrayOfNulls(size)
        }
    }
}
