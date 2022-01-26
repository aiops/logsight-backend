package ai.logsight.backend.ingestion.ports.input.web

import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@Controller
class IngestionController {

//    @PostMapping("/{userKey}/{applicationName}/upload_file")
//    fun uploadFile(
//        authentication: Authentication,
//        @PathVariable userKey: String,
//        @PathVariable applicationName: String,
//        @RequestParam("file") file: MultipartFile,
//    ): ResponseEntity<ApplicationResponse> {
//    }
}
