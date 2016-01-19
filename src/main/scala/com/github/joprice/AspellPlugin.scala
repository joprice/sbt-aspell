package com.joprice.sbt

import sbt._
import Keys._
import scala.language.postfixOps
import java.io.File
import plugins.JvmPlugin
import com.lucidchart.aspell.{Aspell, WordSuggestions}
import nak.util.CleanStringTokenizer

object Spellcheck {
  case class Suggestion(
    word: String,
    file: File,
    line: Int,
    suggestions: Seq[String]
  )

  val defaultLanguage = "en"

  val scalaKeywords = {
    val global = new scala.tools.nsc.Global(new scala.tools.nsc.Settings)
    global.nme.keywords.map(_.toString)
  }

  val stopWords = scalaKeywords.toSet

  def tokenize(s: String): Array[String] = {
    s.replaceAll("[^A-Za-z0-9\\s]", "")
      // remove bare digit literals
      .replaceAll("""\b\d+\b""", "")
      .split("\\s+")
      .flatMap(_.split("(?=[A-Z])"))
      // split on camelcase
      //.toLowerCase ?
      .map(_.trim.toLowerCase)
      .filter(_.nonEmpty)
  }

  def apply(line: String, language: String): Seq[WordSuggestions] = {
    val words = tokenize(line).filterNot(stopWords.contains)
    Aspell.check(language, words, userWords = Array.empty)
  }

  def apply(files: Seq[File], language: String): Seq[Suggestion] = {
    files.flatMap { file =>
      val spellingErrors = IO.readLines(file).flatMap(apply(_, language))
      spellingErrors.filter(!_.valid).zipWithIndex.map { case (result, lineNumber) =>
        Suggestion(result.word, file, lineNumber + 1, result.suggestions)
      }
    }
  }
}

object AspellPlugin extends AutoPlugin { self =>

  override def requires = JvmPlugin

  override def trigger = allRequirements

  object autoImport {
    lazy val AspellPlugin = self

    lazy val spellCheck = taskKey[Unit]("Checks source for spelling errors")

    lazy val spellCheckLanguage = settingKey[String]("Two-letter language code to pass to aspell. Defaults to english")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    spellCheckLanguage := "en",
    spellCheck := {
      def report(suggestion: Spellcheck.Suggestion) = suggestion match {
        case Spellcheck.Suggestion(word, file, lineNumber, suggestions) =>
          streams.value.log.warn(s"""|
            |$file:$lineNumber:
            |$word
            |suggestions: ${suggestions.mkString("[", ",", "]")}
            |""".stripMargin.trim
          )
      }

      val sourceFiles = (unmanagedSources in Compile).value
      Spellcheck(sourceFiles, spellCheckLanguage.value).foreach(report)
    }
  )
}

