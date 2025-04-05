package gitinsp.infrastructure

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import dev.langchain4j.model.scoring.ScoringModel
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor as Ingestor
import gitinsp.chatpipeline.RAGComponentFactory
import gitinsp.infrastructure.strategies.IngestionStrategy
import gitinsp.utils.Assistant
import gitinsp.utils.IndexName
import gitinsp.utils.Language
import io.qdrant.client.QdrantClient
import io.qdrant.client.grpc.Collections.Distance

import scala.collection.concurrent.TrieMap

object CacheService:
  def apply(): CacheService =
    new CacheServiceImpl()

  def apply(factory: RAGComponentFactory): CacheService =
    // This is not strictly necessary, but useful to simplify testing
    new CacheServiceImpl(factory)

  private class CacheServiceImpl(providedFactory: Option[RAGComponentFactory]) extends CacheService:

    def this() = this(None)
    def this(factory: RAGComponentFactory) = this(Some(factory))

    val config: Config               = ConfigFactory.load()
    val factory: RAGComponentFactory = providedFactory.getOrElse(RAGComponentFactory(config))

    private val aiServiceCache: TrieMap[String, Assistant] = TrieMap.empty
    private val _qdrantClient: QdrantClient                = factory.createQdrantClient()

    val scoringModel: ScoringModel = factory.createScoringModel()
    val fmt                        = ContentFormatter

    def getAIService(index: Option[IndexName]): Assistant =
      // Create the streaming model
      val model = factory.createStreamingChatModel()

      index match
        case Some(IndexName(name, _)) =>
          aiServiceCache.get(name) match
            case Some(aiservice) => aiservice
            case None            =>

            // Create a full RAG pipeline
          val modelRouter        = factory.createModelRouter()
          val embeddingStore     = factory.createEmbeddingStore(_qdrantClient, name)
          val textEmbeddingModel = factory.createTextEmbeddingModel()
          val codeEmbeddingModel = factory.createCodeEmbeddingModel()

          val markdownRetriever =
            factory.createMarkdownRetriever(embeddingStore, textEmbeddingModel, name)
          val codeRetriever = factory.createCodeRetriever(
            embeddingStore,
            codeEmbeddingModel,
            name,
            modelRouter,
          )
          val router =
            factory.createQueryRouter(List(markdownRetriever, codeRetriever), modelRouter)
          val contentAggregator = factory.createContentAggregator(scoringModel)
          val augmentor         = factory.createRetrievalAugmentor(router, contentAggregator)

          // Create the AI service
          val aiservice = factory.createAssistant(model, Some(augmentor))

          // Store in cache and return
          aiServiceCache.putIfAbsent(name, aiservice)
          aiServiceCache(name)

        case None =>
          val aiService = factory.createAssistant(model, None)
          aiServiceCache.putIfAbsent("default", aiService)
          aiServiceCache("default")

    def createCollection(name: String, distance: Distance): Unit =
      factory.createCollection(name, _qdrantClient, distance)

    override def qdrantClient: QdrantClient = _qdrantClient

    override def getIngestor(index: IndexName, strategy: IngestionStrategy): Ingestor =
      val embeddingModel = index.language match
        case Language.MARKDOWN => factory.createTextEmbeddingModel()
        case _                 => factory.createCodeEmbeddingModel()

      val embeddingStore = factory.createEmbeddingStore(_qdrantClient, index.name)

      factory.createIngestor(index.language, embeddingModel, embeddingStore, strategy)

trait CacheService:
  def qdrantClient: QdrantClient
  def getAIService(index: Option[IndexName]): Assistant
  def getIngestor(index: IndexName, strategy: IngestionStrategy): Ingestor
  def factory: RAGComponentFactory
  def createCollection(name: String, distance: Distance): Unit
