//package ai.logsight.backend.compare.controller
//
//
// @AutoConfigureMockMvc
// @ActiveProfiles("test")
// internal class CompareControllerTest {
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//
//    @Autowired
//    private lateinit var userRepository: UserRepository
//
//    @Autowired
//    private lateinit var appRepository: ApplicationRepository
//
//    companion object {
//        val mapper = ObjectMapper().registerModule(KotlinModule())!!
//        const val endpoint = "/api/v1/compare"
//        val request = GetCompareResultRequest(applicationId = TestInputConfig.baseApp.id, "base", "compare", "now-180m")
//    }
//
//    @BeforeEach
//    fun setup() {
//        userRepository.deleteAll()
//        appRepository.deleteAll()
//        userRepository.save(TestInputConfig.baseUserEntity)
//        appRepository.save(TestInputConfig.baseAppEntity)
//    }
//
//    @Test
//    @WithMockUser(TestInputConfig.baseEmail)
//    fun `should return something`() {
//        // given
//
//        // when
//        mockMvc.get(endpoint) {
//            content = mapper.writeValueAsString(request)
//            contentType = MediaType.APPLICATION_JSON
//            accept = MediaType.APPLICATION_JSON
//        }
//            .andDo { print() }
//
//        // then
//    }
// }
