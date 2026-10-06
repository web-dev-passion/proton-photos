/*
 * Photos for Proton
 * Copyright (C) 2026 Akoos <https://akoos.eu>
 *
 * Source:  https://github.com/gitakoos/proton-photos
 * Website: https://www.photosforproton.eu
 *
 * This file is part of Photos for Proton.
 *
 * Photos for Proton is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package eu.akoos.photos.domain.usecase

import java.util.Locale

/**
 * Name and MIME type for an upload whose bytes were re-encoded to JPEG (photo compression or the HEIC
 * strip transcode), so Drive never holds JPEG bytes called `.heic`.
 */
object UploadFormatNaming {

    private const val JPEG = "image/jpeg"

    /** Extensions the re-encode replaces. Any other name, such as "trip.2024", gets `.jpg` appended. */
    private val IMAGE_EXTENSIONS = setOf(
        "jpg", "jpeg", "jpe", "png", "webp", "heic", "heif", "avif", "bmp", "gif", "dng", "tif", "tiff",
    )

    /**
     * The (name, MIME type) to upload under when the bytes are [producedMime]. Unchanged when nothing
     * was re-encoded or the bytes were JPEG already; otherwise the extension becomes `.jpg`, in the
     * original's case.
     */
    fun forProducedFormat(originalName: String, originalMime: String, producedMime: String?): Pair<String, String> {
        if (normalize(producedMime) != JPEG || normalize(originalMime) == JPEG) return originalName to originalMime
        val dot = originalName.lastIndexOf('.')
        val ext = if (dot > 0) originalName.substring(dot + 1) else ""
        val base = if (ext.lowercase(Locale.ROOT) in IMAGE_EXTENSIONS) originalName.substring(0, dot) else originalName
        val upper = ext.isNotEmpty() && ext == ext.uppercase(Locale.ROOT) && ext != ext.lowercase(Locale.ROOT)
        return "$base.${if (upper) "JPG" else "jpg"}" to JPEG
    }

    /** The name a re-encoded copy of this photo went up under, or null when it would keep its own. */
    fun reencodedName(localName: String, localMime: String): String? {
        if (!localMime.lowercase(Locale.ROOT).startsWith("image/")) return null
        return forProducedFormat(localName, localMime, JPEG).first.takeIf { it != localName }
    }

    /** "image/jpg" and "image/pjpeg" are JPEG too. */
    private fun normalize(mime: String?): String? = mime?.substringBefore(';')?.trim()?.lowercase(Locale.ROOT)
        ?.let { if (it == "image/jpg" || it == "image/pjpeg") JPEG else it }
}
