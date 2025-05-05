package gitinsp.domain.models

import akka.NotUsed
import akka.stream.scaladsl.Source
import dev.langchain4j.rag.content.Content

/** Type alias for search results */
type SearchResult = Content

/** Type alias for streamed responses */
type StreamedResponse = Source[String, NotUsed]
