package org.enso.launcher.cli.impl

import org.enso.launcher.cli.Argument

class PositionalArgument[A: Argument](metavar: String, helpComment: String)
    extends BaseOpts[A] {
  val empty                                  = Right(None)
  var value: Either[List[String], Option[A]] = empty

  override private[cli] val requiredArguments = Seq(metavar)

  override private[cli] def wantsArgument() =
    value match {
      case Right(None) => true
      case _           => false
    }

  override private[cli] def consumeArgument(arg: String): Unit = {
    value = for {
      parsed <- Argument[A].read(arg)
    } yield Some(parsed)
  }

  override private[cli] def reset(): Unit = {
    value = empty
  }

  override private[cli] def result() =
    value.flatMap {
      case Some(value) => Right(value)
      case None        => Left(List(s"Missing required argument <$metavar>."))
    }

  override def helpExplanations(): Seq[String] = {
    if (helpComment.nonEmpty) {
      Seq(s"$metavar\t$helpComment")
    } else Seq()
  }
}