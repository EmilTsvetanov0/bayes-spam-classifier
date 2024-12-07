package classifier.utils

import org.slf4j.LoggerFactory

import java.util.Properties
import scala.jdk.CollectionConverters._
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation
import edu.stanford.nlp.pipeline.{Annotation, StanfordCoreNLP}

object StanfordNLPUtils {

  private val log = LoggerFactory.getLogger(getClass)

  private val russianPipeline: StanfordCoreNLP = {
    val props: Properties = new Properties()
    props.setProperty ("customAnnotatorClass.custom.lemma", "edu.stanford.nlp.international.russian.process.RussianLemmatizationAnnotator")
    props.setProperty ("pos.model", "edu/stanford/nlp/models/pos-tagger/russian-ud-pos.tagger")
    props.setProperty ("annotators", "tokenize, pos, custom.lemma")
    props.setProperty ("custom.lemma.dictionaryPath", "edu/stanford/nlp/international/russian/process/dict.tsv")

    new StanfordCoreNLP(props)
  }

  private val englishPipeline: StanfordCoreNLP = {
    val props: Properties = new Properties()
    props.setProperty("annotators", "tokenize, pos, lemma")
    props.setProperty("tokenize.language", "en")

    new StanfordCoreNLP(props)
  }

  def tokenizeAndLemmatize(text: String, lang: String): Array[String] = {
    val document = new Annotation(text)

    lang match {
      case "ru" =>
        russianPipeline.annotate(document)
      case "en" =>
        englishPipeline.annotate(document)
      case _ =>
        log.info("""StanfordNLPUtils[tokenizeAndLemmatize] wrong language, use options: "en" and "ru" (now using "en" as default) """)
        englishPipeline.annotate(document)
    }

    for (token <- document.get(classOf[TokensAnnotation]).asScala.toArray)
      yield token.get(classOf[LemmaAnnotation])
  }
}
