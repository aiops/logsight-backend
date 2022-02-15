package ai.logsight.backend.results.ports.persistence

import ai.logsight.backend.logs.extensions.toLogsReceiptEntity
import ai.logsight.backend.results.domain.ResultInit
import ai.logsight.backend.results.domain.service.ResultInitStatus
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand
import ai.logsight.backend.results.extensions.toResultInit
import ai.logsight.backend.results.extensions.toResultInitEntity
import org.springframework.stereotype.Service

@Service
class ResultInitStorageServiceImpl(
    private val resultInitRepository: ResultInitRepository
) : ResultInitStorageService {

    override fun saveResultInit(createResultInitCommand: CreateResultInitCommand): ResultInit {
        val resultInitEntity = ResultInitEntity(
            status = ResultInitStatus.PENDING,
            logsReceipt = createResultInitCommand.logsReceipt.toLogsReceiptEntity()
        )
        return resultInitRepository.save(resultInitEntity).toResultInit()
    }

    override fun deleteResultInit(resultInit: ResultInit) =
        resultInitRepository.delete(resultInit.toResultInitEntity())

    override fun findAllResultInitByStatus(status: ResultInitStatus): List<ResultInit> =
        resultInitRepository.findAllByStatus(status).map(ResultInitEntity::toResultInit)

    override fun updateResultInitStatus(resultInit: ResultInit, status: ResultInitStatus): ResultInit {
        val resultInitEntity = resultInit.toResultInitEntity()
        resultInitEntity.status = status
        return resultInitRepository.save(resultInitEntity).toResultInit()
    }
}
