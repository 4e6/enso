package org.enso.launcher.cli.internal

class Flag(
  name: String,
  short: Option[Char],
  helpComment: String,
  showInUsage: Boolean
) extends BaseOpts[Boolean] {
  if (name.exists(_.isWhitespace)) {
    throw new IllegalArgumentException(
      s"Flag name '$name' cannot contain whitespace."
    )
  }

  override private[cli] val flags = {
    val shortParser = for {
      opt <- short
    } yield opt.toString -> (update _)

    Map(name -> (update _)) ++ shortParser
  }
  override private[cli] val usageOptions =
    if (showInUsage) Seq(s"[--$name]") else Seq()

  val empty                                = Right(false)
  var value: Either[List[String], Boolean] = empty

  override private[cli] def reset(): Unit = {
    value = empty
  }

  private def update(): Unit = {
    value = value.flatMap(
      if (_) Left(List(s"Flag $name is set more than once"))
      else Right(true)
    )
  }

  override private[cli] def result() = value

  override def helpExplanations(): Seq[String] =
    short match {
      case Some(short) =>
        Seq(s"[-$short | --$name]\t$helpComment")
      case None =>
        Seq(s"[--$name]\t$helpComment")
    }
}