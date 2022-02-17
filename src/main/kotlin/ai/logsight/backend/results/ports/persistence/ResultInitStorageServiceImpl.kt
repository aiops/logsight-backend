package ai.logsight.backend.results.ports.persistence

import ai.logsight.backend.application.exceptions.ApplicationNotFoundException
import ai.logsight.backend.logs.extensions.toLogsReceiptEntity
import ai.logsight.backend.results.domain.ResultInit
import ai.logsight.backend.results.domain.service.ResultInitStatus
import ai.logsight.backend.results.domain.service.command.CreateResultInitCommand
import ai.logsight.backend.results.exceptions.ResultInitNotFoundException
import ai.logsight.backend.results.extensions.toResultInit
import ai.logsight.backend.results.extensions.toResultInitEntity
import org.springframework.stereotype.Service
import java.util.*

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

    override fun findAllResultInitByStatusAndApplicationId(status: ResultInitStatus, applicationId: UUID): List<ResultInit> =
        resultInitRepository.findAllByStatusAndLogsReceipt_Application_Id(status, applicationId).map(ResultInitEntity::toResultInit)

    override fun findResultInitById(resultInitId: UUID): ResultInit = findResultInitByIdPrivate(resultInitId).toResultInit()

    override fun updateResultInitStatus(resultInit: ResultInit, status: ResultInitStatus): ResultInit {
        val resultInitEntity = resultInit.toResultInitEntity()
        resultInitEntity.status = status
        return resultInitRepository.save(resultInitEntity).toResultInit()
    }

    private fun findResultInitByIdPrivate(resultInitId: UUID): ResultInitEntity =
        resultInitRepository.findById(resultInitId)
            .orElseThrow { ResultInitNotFoundException("ResultInit with ID $resultInitId does not exist.") }
}
