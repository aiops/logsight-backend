package com.loxbear.logsight.auth

import com.loxbear.logsight.repositories.elasticsearch.AnomalyRepository
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/users")
class AuthController(val anomalyRep: AnomalyRepository) {

}