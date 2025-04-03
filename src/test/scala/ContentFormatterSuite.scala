package gitinsp.tests.contentformatter

import com.typesafe.config.Config
import dev.langchain4j.data.document.Metadata as DocMetadata
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.ollama.OllamaChatModel
import dev.langchain4j.rag.query.Metadata
import dev.langchain4j.rag.query.Query
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey
import gitinsp.infrastructure.ContentFormatter
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.util.Arrays

class ContentFormatterSuite extends AnyFlatSpec with Matchers:
  "ContentFormatter" should "format HTML content" in:
    val formatter = ContentFormatter
    val content   = "test"
    val formatted = formatter.toHtml(content)
    formatted should be("<pre style=\"white-space: pre-wrap;\">test</pre>")

  it should "format plain text content" in:
    val formatter = ContentFormatter
    val content   = "test"
    val formatted = formatter.toPlainText(content)
    formatted should be("test\n")

  it should "apply dynamic filter" in:
    // Setup mocks
    val config       = mock(classOf[Config])
    val model        = mock(classOf[OllamaChatModel])
    val chatResponse = mock(classOf[ChatResponse])
    val metadata     = mock(classOf[Metadata])
    val userMessage  = mock(classOf[UserMessage])

    // Setup data
    val formatter      = ContentFormatter
    val content        = "test"
    val query          = mock(classOf[Query])
    val aiMessage      = AiMessage.from("yes: py")
    val expectedFilter = metadataKey("code").isEqualTo("py")

    // Setup behaviour
    when(config.getBoolean("gitinsp.rag.use-dynamic-filter")).thenReturn(true)
    when(query.metadata()).thenReturn(metadata)
    when(metadata.userMessage()).thenReturn(userMessage)
    when(userMessage.contents()).thenReturn(Arrays.asList(TextContent.from("test")))
    when(model.chat(any(classOf[UserMessage]))).thenReturn(chatResponse)
    when(chatResponse.aiMessage()).thenReturn(aiMessage)

    // Apply
    val result = formatter.applyDynamicFilter(query, config, model)

    // Assert
    Option(result) should not be None

    // The filter should match the test metadata
    val testMetadata = new DocMetadata().put("code", "py")
    expectedFilter.test(testMetadata) should be(true)
    result.test(testMetadata) should be(true)

    // Neither should match a different language
    val nonPythonMetadata = new DocMetadata().put("code", "java")
    expectedFilter.test(nonPythonMetadata) should be(false)
    result.test(nonPythonMetadata) should be(false)

  it should "disable dynamic filter" in:
    // Setup mocks
    val config = mock(classOf[Config])
    val model  = mock(classOf[OllamaChatModel])
    val query  = mock(classOf[Query])

    // Setup data
    val formatter = ContentFormatter

    // Setup behaviour
    when(config.getBoolean("gitinsp.rag.use-dynamic-filter")).thenReturn(false)

    // Apply
    val result = formatter.applyDynamicFilter(query, config, model)

    // Assert
    Option(result) should be(None)
