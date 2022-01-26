package ai.logsight.backend.user.ports.out.elasticsearch

import ai.logsight.backend.common.config.ElasticsearchConfigProperties
import ai.logsight.backend.connectors.RestTemplateConnector

class ElasticsearchAdmin(val elasticsearchConfig: ElasticsearchConfigProperties) {
    fun createESUser(userName: String, password: String, role: String) {
        query = "{ \"password\" : \"${password}\", " + "\"roles\" : [\"${role}\"] }"

        query2 = "{ \"metadata\" : { \"version\" : 1 }," +
            "\"kibana\": [ { \"base\": [], \"feature\": { \"discover\": [ \"all\" ], \"visualize\": [ \"all\" ], " +
            "\"dashboard\":  [ \"all\" ], \"advancedSettings\": [ \"all\" ], \"indexPatterns\": [ \"all\" ] }, " +
            "\"spaces\": [ \"kibana_space_$userKey\" ] } ] }"
    }
}
