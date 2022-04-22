package ai.logsight.backend.application.utils

import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import javax.naming.InvalidNameException
import kotlin.test.assertFailsWith

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class NameParserTest {
    val nameParser = NameParser()

    private fun getInvalidNames(): List<Arguments> {
        return mapOf(
            "Empty String" to "",
            "Invalid symbol '!'" to "application!",
            "Invalid symbol '?'" to "Application?",
            "Invalid symbol '%'" to "application#",
            "Invalid symbol '$'" to "application$",
            "dash in beginning" to "-application-32"
        ).map { x -> Arguments.of(x.key, x.value) }
    }

    @ParameterizedTest(name = "Bad request for {0}. Value: \"{1}\"")
    @MethodSource("getInvalidNames")
    fun `should return bad request for invalid input`(
        reason: String,
        name: String
    ) {
        // given

        // when
        assertFailsWith<InvalidNameException> { nameParser.slugify(name) }
        // then
    }

    private fun getValidNames(): List<Arguments> {
        return mapOf(
            "lowercase" to "application",
            "lowercase with '_'" to "application_1",
            "lowercase with '-'" to "application-1",
            "lowercase with '.'" to "appplication.1",
            "Alphanumeric" to "Application32",
            "Capital letters" to "ApplicationOne",
            "pod id" to "0ae5c03d-5fb3-4eb9-9de8-2bd4b51606ba",
            "pod names" to "coredns-6955765f44-ccqgg"
        ).map { x -> Arguments.of(x.key, x.value) }
    }

    @ParameterizedTest(name = "Bad request for {0}. Value: \"{1}\"")
    @MethodSource("getValidNames")
    fun `should parse for valid inputs`(
        reason: String,
        name: String
    ) {
        // given
        nameParser.slugify(name)
        // when

        // then
    }
}
