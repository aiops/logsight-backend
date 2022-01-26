package ai.logsight.backend.ingestion.domain

interface IngestionService {
    fun uploadFile(fileContent: String)
}
