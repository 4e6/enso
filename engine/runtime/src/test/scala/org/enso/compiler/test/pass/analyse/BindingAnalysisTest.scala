package org.enso.compiler.test.pass.analyse

import org.enso.compiler.Passes
import org.enso.compiler.context.{FreshNameSupply, ModuleContext}
import org.enso.compiler.core.IR
import org.enso.compiler.data.BindingsMap
import org.enso.compiler.data.BindingsMap.{Cons, ModuleMethod, PolyglotSymbol}
import org.enso.compiler.pass.analyse.BindingAnalysis
import org.enso.compiler.pass.{PassConfiguration, PassGroup, PassManager}
import org.enso.compiler.test.CompilerTest

class BindingAnalysisTest extends CompilerTest {

  // === Test Setup ===========================================================

  def mkModuleContext: ModuleContext =
    buildModuleContext(
      freshNameSupply = Some(new FreshNameSupply)
    )

  val passes = new Passes

  val precursorPasses: PassGroup = passes.getPrecursors(BindingAnalysis).get

  val passConfiguration: PassConfiguration = PassConfiguration()

  implicit val passManager: PassManager =
    new PassManager(List(precursorPasses), passConfiguration)

  /** Adds an extension method to analyse an Enso module.
    *
    * @param ir the ir to analyse
    */
  implicit class AnalyseModule(ir: IR.Module) {

    /** Performs tail call analysis on [[ir]].
      *
      * @param context the module context in which analysis takes place
      * @return [[ir]], with tail call analysis metadata attached
      */
    def analyse(implicit context: ModuleContext) = {
      BindingAnalysis.runModule(ir, context)
    }
  }

  // === The Tests ============================================================

  "Module binding resolution" should {
    implicit val ctx: ModuleContext = mkModuleContext

    val ir =
      """
        |polyglot java import foo.bar.baz.MyClass
        |
        |type Foo a b c
        |type Bar
        |type Baz x y
        |
        |Baz.foo = 123
        |Bar.baz = Baz 1 2 . foo
        |
        |foo = 123
        |""".stripMargin.preprocessModule.analyse

    "discover all atoms, methods, and polyglot symbols in a module" in {
      ir.getMetadata(BindingAnalysis) shouldEqual Some(
        BindingsMap(
          List(Cons("Foo", 3), Cons("Bar", 0), Cons("Baz", 2)),
          List(PolyglotSymbol("MyClass")),
          List(ModuleMethod("foo")),
          ctx.module
        )
      )
    }
  }
}
