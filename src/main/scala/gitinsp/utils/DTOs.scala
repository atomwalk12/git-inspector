package gitinsp.utils

/** Enum for supported programming languages.
  * Each case includes a short string representation (`value`) often used as a file extension.
  */
enum Language(val value: String):
  case CPP        extends Language("cpp")
  case GO         extends Language("go")
  case JAVA       extends Language("java")
  case KOTLIN     extends Language("kt")
  case JS         extends Language("js")
  case TS         extends Language("ts")
  case PHP        extends Language("php")
  case PROTO      extends Language("proto")
  case PYTHON     extends Language("py")
  case CODE       extends Language("code") // Placeholder for all code languages
  case RST        extends Language("rst")
  case RUBY       extends Language("rb")
  case RUST       extends Language("rs")
  case SCALA      extends Language("scala")
  case SWIFT      extends Language("swift")
  case MARKDOWN   extends Language("md")
  case LATEX      extends Language("latex")
  case HTML       extends Language("html")
  case SOL        extends Language("sol")
  case CSHARP     extends Language("cs")
  case COBOL      extends Language("cob")
  case C          extends Language("c")
  case LUA        extends Language("lua")
  case HASKELL    extends Language("hs")
  case ELIXIR     extends Language("ex")
  case POWERSHELL extends Language("ps")

  // Override toString to return the custom value
  override def toString: String = value
