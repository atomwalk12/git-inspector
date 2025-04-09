package gitinsp.tests.contentformatter

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.Marshal
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import gitinsp.application.LangchainCoordinator
import gitinsp.tests.ExternalService
import org.scalatest.BeforeAndAfterAll
import org.scalatest.BeforeAndAfterEach
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.Failure
import scala.util.Success

class PipelineTest extends AnyFlatSpec with Matchers with MockitoSugar with BeforeAndAfterAll
    with BeforeAndAfterEach with LazyLogging:

  val config                                      = ConfigFactory.load()
  implicit val system: ActorSystem                = ActorSystem("pipeline-test-system")
  implicit val materializer: Materializer         = Materializer(system)
  implicit val executionContext: ExecutionContext = system.dispatcher

  val coordinator = LangchainCoordinator(prettyFmt = true)

  val langchainCoordinator: LangchainCoordinator = LangchainCoordinator(prettyFmt = true)

  "Langchain Coordinator" should "be able to list indexes" taggedAs ExternalService in:
    // Execute directly against Pipeline instead of through the coordinator
    val indexesTry = coordinator.listIndexes()
    val response   = Marshal(indexesTry)

    // Check if we got a result
    indexesTry.isSuccess shouldBe true

    // Extract and verify the indexes
    indexesTry.foreach { _.nonEmpty shouldBe true }

  it should "be able to delete an index" taggedAs ExternalService in:
    // ==========================
    // First we generate an index
    // ==========================
    // Data setup
    val index = "https://github.com/atomwalk12/PPS-22-git-insp"

    // Execute
    val indexTry = coordinator.generateIndex(index, "scala,md")

    // Verify
    indexTry match {
      case Success(result) => logger.info(s"Generate index result: $result")
      case Failure(ex)     => logger.error(s"Generate index failed: ${ex.getMessage}")
    }
    indexTry.isSuccess shouldBe true

    // ==========================
    // Then we list the indexes
    // ==========================

    // Execute
    val jsonTry = coordinator.listIndexes()

    // Verify
    jsonTry match {
      case Success(result) => logger.info(s"List indexes result: $result")
      case Failure(ex)     => logger.error(s"List indexes failed: ${ex.getMessage}")
    }
    jsonTry.isSuccess shouldBe true

    // ==========================
    // Then we delete the index
    // ==========================
    // Execute
    val deleteTry = coordinator.deleteIndex(index)

    // Verify
    deleteTry match {
      case Success(result) => logger.info(s"Delete index result: $result")
      case Failure(ex)     => logger.error(s"Delete index failed: ${ex.getMessage}")
    }
    deleteTry.isSuccess shouldBe true

  it should "be able to chat with the default model (no retrieval)" taggedAs ExternalService in:
    // Execute
    val tryResult = coordinator.chat("What is the main function of the project?", None)

    // Verify
    tryResult.fold(
      ex => fail(s"Expected success but got failure: $ex"),
      result => {
        val future  = result.map(println).runWith(Sink.seq)
        val results = Await.result(future, 600.seconds)
      },
    )

  it should "be able to chat with an index" taggedAs ExternalService in:
    // Data setup
    val indexName = "https://github.com/langchain-ai/langchain"

    // Execute
    val tryResult = coordinator.chat("What is the main function of the project?", Some(indexName))

    // Verify
    tryResult.fold(
      ex => fail(s"Expected success but got failure: $ex"),
      result => {
        val future  = result.map(println).runWith(Sink.seq)
        val results = Await.result(future, 600.seconds)
      },
    )
