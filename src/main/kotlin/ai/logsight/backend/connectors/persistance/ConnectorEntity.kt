// package ai.logsight.backend.connectors.persistance

import ai.logsight.backend.application.adapters.persistence.ApplicationEntity
import javax.persistence.*

@MappedSuperclass
@Embeddable
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class ConnectorEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    open var id: Long = 0,

    @OneToOne
    val application: ApplicationEntity
)
//
// //charts <- application <- conntor_params
// chartsService.getAnomaliesChartData(application):
//        esRepoService.get(query, application)
//
//
//

// 1. Create Application:
// - create apliaction in db
// - create ApplicationSpecificConnectorParams
// - (implements builder/factory )
//
//
// ChartsService(DataLayerService,ConnectorService)
//
// chartsService.getBarChartAnomaly(application):
// connectorParamsForApp = ConnectorService.getParamsForApp(application)
// DataLayerService.getData(functionSpecificParams, connectorParamsForApp)
//
//
//
// DataLayer:
// query = QueryBuilder.buildQuery(funcion_params,connectorParamsForApp)
//
// esRepoService(query, application){
//    repoService.getApplicationIndices()
// }
// CREATE APPLICATION

// connect to backend using CONNECTION:?
// kafka, WebSocket, Socket..RabbitMQ

// AppSpecificParams q
// kafka has topicName
// WebSocket has socketParams s

// ChartsService
// GetAnomaliesBarChartData(application, startTime,STopTime)
// should receive applicationDomainObject in function
// Should ask the DataLayer for the data

// DataLayer
// EsData
// receives applicationInfo, additional params
// gets ApplicationSpecificParams == HOW?
// QueryBuilder: BuildsQuery
// call repository with query

// DatabaseData
// receives applicationInfo, additional params
// gets ApplicationSpecificParams == HOW?
// QueryBuilder: BuildsQuery
// call repository with query

// DATA LAYER SERVICE
// EsService depends on Repository
// EsQueryBuilder

// DATA LAYER SERVICE
// DatabaseService depends on repository
// DatabaseQueryBuilder

// OPTIONAL FOR LATER
// Option1:
// QueryBuilderEsService
// QueryBuilderDatabaseService

// Option2
// QueryBuilder:
// BuildEsFunction
// BuildDatabaseFunction
