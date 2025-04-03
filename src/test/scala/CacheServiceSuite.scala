package gitinsp.tests.cacheservice

import gitinsp.chatpipeline.RAGComponentFactory
import gitinsp.infrastructure.CacheService
import gitinsp.tests.Integration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.ExecutionContext

class CacheServiceSuite
    extends AnyFlatSpec
    with Matchers
    with MockitoSugar
    with ScalaFutures:

  // Create a dedicated execution context for tests
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(
    java.util.concurrent.Executors.newFixedThreadPool(4),
  )

  val mockFactory = mock[RAGComponentFactory]

  "CacheService" should "run without any exceptions" taggedAs Integration in:
    // Get the CacheService implementation
    val cacheService = CacheService(mockFactory)

    // Now test the service with mocked dependencies
    val aiService = cacheService.getAIService("test-repository")
    noException should be thrownBy aiService

  it should "return the same AI service instance for the same repository name" taggedAs Integration in:
    val cacheService = CacheService(mockFactory)
    val aiService1   = cacheService.getAIService("test-repository")
    val aiService2   = cacheService.getAIService("test-repository")
    aiService1 should be theSameInstanceAs aiService2
