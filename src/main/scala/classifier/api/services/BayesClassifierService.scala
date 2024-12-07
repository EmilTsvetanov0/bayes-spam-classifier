package classifier.api.services

import org.slf4j.LoggerFactory
import org.json4s.Formats
import org.json4s._
import org.json4s.jackson.JsonMethods._

import classifier.utils.{ScanUtils, Config}
import classifier.domain.Domain.ModelCounts

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future.sequence
import scala.collection.concurrent.TrieMap
import scala.io.Source
import scala.util.Using
import java.io.PrintWriter
import scala.concurrent.duration.DurationInt

object BayesClassifierService {

  private val log = LoggerFactory.getLogger(getClass)
  implicit val formats: Formats = org.json4s.DefaultFormats.withLong.withDouble.withStrictOptionParsing

  private var totalSpamWords: Long = 0L
  private var totalHamWords: Long = 0L
  private var totalSpamPages: Long = 0L
  private var totalHamPages: Long = 0L
  private var offset: Long = 0L
  private val wordBag: TrieMap[String, (Long, Long)] = {
    Using(Source.fromFile(Config.getModelPath)) { source: Source =>
      val jsonString = source.getLines().mkString
      val parsedJson = parse(jsonString)

      val intermediateMap = parsedJson.extract[Map[String, List[Long]]]

      val transformedMap = intermediateMap.map { case (word, counts) =>
        word -> (counts.head, counts(1))
      }

      log.info("BayesClassifierService initialized word bag")

      updateCounts(Config.getModelCounts)

      transformedMap
    }.recover {
      case e: Exception =>
        log.error(s"BayesClassifierService Failed to load wordBag: ${e.getMessage}")
        Map.empty
    }.get
  }.to(TrieMap)

  private def updateCounts(modelCounts: ModelCounts): Unit = {
    this.totalSpamWords = modelCounts.spamWords
    this.totalHamWords = modelCounts.hamWords
    this.totalSpamPages = modelCounts.spamPages
    this.totalHamPages = modelCounts.hamPages
    this.offset = modelCounts.uniqueWords
  }

  def updateWordBag(newWordBag: Map[String, (Long, Long)]): Unit = {
    wordBag.clear()
    wordBag ++= newWordBag
  }

  def isSpam(wordsCountMap: Map[String, Long]): (Boolean, Double, Double) = {

    log.debug("BayesClassifierService[isSpam] started")

    if (wordsCountMap.keys.exists(ScanUtils.hasMixedLetters)) {
      log.info("BayesClassifierService[isSpam] found word with mixed letters; signing the document as spam by default")
      return (true, 1.0, 0.0)
    }

    if (totalSpamWords > 0 && totalHamWords > 0 && totalSpamPages > 0 && totalHamPages > 0 && offset > 0) {
      var logSpamScore = math.log(totalSpamPages.toDouble / (totalSpamPages.toDouble + totalHamPages.toDouble))
      var logHamScore = math.log(totalHamPages.toDouble / (totalSpamPages.toDouble + totalHamPages.toDouble))
      var foundWordsCount: Long = 0

      wordsCountMap foreach { case (word, wordsInText) =>
        val (spamCount, hamCount) = wordBag.getOrElse(word, (0L, 0L))
        logSpamScore += wordsInText * math.log((spamCount.toDouble + 1) / (totalSpamWords + offset))
        logHamScore += wordsInText * math.log((hamCount.toDouble + 1) / (totalHamWords + offset))
        //            logSpamScore += wordsInText * math.log((totalHamWords + offset) / (wordStats.hamCount.toDouble + 1.0))
        //            logHamScore += wordsInText * math.log((totalHamWords + offset) / (wordStats.spamCount.toDouble + 1.0))
        foundWordsCount += wordsInText
      }

      val spamProb = 1.0 / (1.0 + math.exp(logHamScore - logSpamScore))
      val hamProb = 1.0 - spamProb
      log.info(s"BayesClassifierService[isSpam] returned ${logSpamScore >= logHamScore} or $spamProb VS $hamProb")
      (logSpamScore >= logHamScore, spamProb, hamProb)
    } else {
      log.info("BayesClassifierService[isSpam] value counts is incomplete, some of the values hasn't been obtained and/or equals to zero")
      (false, 0.0, 1.0)
    }
  }

  def testIsSpam(): Future[Unit] = Future {
    log.info("BayesClassifierService[testIsSpamWithCSV] started")

    val bufferedSource = Source.fromFile("src/main/resources/test.csv")

    val writer = new PrintWriter("src/main/resources/predictions.csv")

    val lines = bufferedSource.getLines()
    val headers = lines.next().split(",").map(_.trim)

    val contentIndex = headers.indexOf("content")
    val labelIndex = headers.indexOf("label")

    writer.println("is_spam,spam_prob,label")

    for (line <- lines) {
      val cols = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)").map(_.trim)
      val content = cols(contentIndex)
      val label = cols(labelIndex)

      val result = isSpam(ScanUtils.preprocessAndCount(content))
      val (isSpamResult, spamProb, hamProb) = result

      val prediction = if (isSpamResult) 1 else 0

      writer.println(s"$prediction,$spamProb,$label")
    }

    log.info("BayesClassifierService[testIsSpam] ended")

    bufferedSource.close()
    writer.close()
  }


}
