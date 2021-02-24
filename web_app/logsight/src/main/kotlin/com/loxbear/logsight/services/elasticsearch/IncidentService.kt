package com.loxbear.logsight.services.elasticsearch

import com.loxbear.logsight.incidents.data.DataSet
import com.loxbear.logsight.incidents.data.TopKIncidentsTable

import com.loxbear.logsight.repositories.elasticsearch.IncidentRepository
import org.springframework.stereotype.Service


@Service
class IncidentService(val repository: IncidentRepository) {


    fun getTopKIncidentsTableData(): TopKIncidentsTable {
        val data = mutableListOf<DataSet>()
        val labels = mutableListOf<String>()
        repository.getTopKIncidentData()
//        get the data
        return TopKIncidentsTable(datasets = data, labels = labels)
    }


}
