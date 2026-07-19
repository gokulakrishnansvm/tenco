package com.tenco.backend.web

import com.tenco.backend.storage.StorageService
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

data class UploadResponse(val url: String)

@RestController
@RequestMapping("/api/uploads")
class UploadController(private val storage: StorageService) {

    /** Uploads a complaint photo; returns its URL (S3 or local). */
    @PostMapping("/photo", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadPhoto(@RequestParam("file") file: MultipartFile): UploadResponse {
        if (file.isEmpty) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file")
        val ext = when (file.contentType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        return UploadResponse(storage.uploadComplaintPhoto(file.bytes, file.contentType ?: "image/jpeg", ext))
    }

    /** Serves a locally-stored file (dev fallback; S3 URLs are served by S3 directly). */
    @GetMapping("/files/**")
    fun serve(request: org.springframework.web.context.request.WebRequest): ResponseEntity<ByteArrayResource> {
        val path = (request.getDescription(false)) // e.g. "uri=/api/uploads/files/complaints/xxx.jpg"
            .substringAfter("/api/uploads/files/", "")
        val bytes = storage.readLocal(path) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND)
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(ByteArrayResource(bytes))
    }
}
