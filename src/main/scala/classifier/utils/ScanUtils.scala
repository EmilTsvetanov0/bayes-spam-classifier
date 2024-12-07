package classifier.utils

import classifier.utils.StanfordNLPUtils.tokenizeAndLemmatize

import scala.io.Source
import scala.util.Using

object ScanUtils {
  private val specialChars = "[^a-zа-я0-9\\s]".r
  private val nonLetters = "[^a-zа-я\\s]".r

  private val ruStopWordsFile = "src/main/resources/stopwords-ru.txt"
  private val enStopWordsFile = "src/main/resources/stopwords-en.txt"

  private val ruStopWords: Set[String] = {
    Using(Source.fromFile(ruStopWordsFile)) { source =>
      source.getLines().toSet
    }.getOrElse(Set.empty[String]) map {word => ScanUtils.clean(word)}
  }

  private val enStopWords: Set[String] = {
    Using(Source.fromFile(enStopWordsFile)) { source =>
      source.getLines().toSet
    }.getOrElse(Set.empty[String]) map {word => ScanUtils.clean(word)}
  }

  val mixedLanguagePattern = "(?=.*[a-zA-Z])(?=.*[а-яА-Я])".r

  def hasMixedLetters(word: String): Boolean = {
    mixedLanguagePattern.findFirstIn(word).isDefined
  }

  def detectLanguage(word: String): String = {
    val russianPattern = "[а-яА-Я]".r
    val englishPattern = "[a-zA-Z]".r

    if (russianPattern.findFirstIn(word).isDefined) "ru"
    else if (englishPattern.findFirstIn(word).isDefined) "en"
    else "other"
  }


  def splitIntoWords(s: String): Array[String] = clean(s).split("\\s+").filterNot(_.length <= 3)

  def clean(s: String): String = nonLetters.replaceAllIn(s.toLowerCase, " ")

  def normalize(s: String): String = specialChars.replaceAllIn(s.toLowerCase, " ")

  def preprocessAndCount(text: String): Map[String, Long] = {
    val (russianWordsText, englishWordsText) = {
      val (russianBuffer, englishBuffer) = (new StringBuilder, new StringBuilder)

      val tokens = splitIntoWords(text)

      for (token <- tokens) {
        ScanUtils.detectLanguage(token) match {
          case "ru" => russianBuffer.append(token).append(" ")
          case "en" => englishBuffer.append(token).append(" ")
          case _ =>
        }
      }

      (russianBuffer.toString.trim, englishBuffer.toString.trim)
    }

    val cleanedRussianWords: Array[String] = tokenizeAndLemmatize(russianWordsText, "ru")
      .filterNot(word => ruStopWords.contains(word))

    val cleanedEnglishWords: Array[String] = tokenizeAndLemmatize(englishWordsText, "en")
      .filterNot(word => enStopWords.contains(word))

    val normalizedWords = Array.concat(cleanedRussianWords, cleanedEnglishWords)

    normalizedWords.foldLeft(Map.empty[String, Long]) {
      (countMap, word) => countMap.updated(word, countMap.getOrElse(word, 0L) + 1)
    }
  }
}
