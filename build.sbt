import Dependencies.*
import RescalaDependencies.*
import Settings.*

lazy val replicationExamples =
  crossProject(JVMPlatform, JSPlatform).crossType(CrossType.Full).in(file("."))
    .settings(
      scalaVersion_3,
      jitpackResolver,
      noPublish,
      run / fork         := true,
      run / connectInput := true,
      jitpackResolver,
      libraryDependencies ++= jsoniterScalaAll.value ++ Seq(
        loci.tcp.value,
        loci.jsoniterScala.value,
        munitScalacheck.value,
        munit.value,
        scalacheck.value,
        slips.options.value,
        slips.delay.value,
        "com.github.rescala-lang.rescala" %%% "rescala" % "79d0c4f020",
        "com.github.rescala-lang.rescala" %%% "kofre"   % "79d0c4f020",
      ),
    )
    .jvmSettings(
      libraryDependencies ++= Seq(
        loci.wsJetty11.value,
        jetty.value,
        scribeSlf4j2.value,
        slips.script.value,
        sqliteJdbc.value,
      )
    )
    .jsSettings(
      libraryDependencies ++= Seq(
        scalatags.value,
        loci.wsWeb.value,
      ),
      TaskKey[File]("deploy", "generates a correct index.html") := {
        val fastlink   = (Compile / fastLinkJS).value
        val jspath     = (Compile / fastLinkJS / scalaJSLinkerOutputDirectory).value
        val bp         = baseDirectory.value.toPath
        val tp         = jspath.toPath
        val template   = IO.read(bp.resolve("index.template.html").toFile)
        val targetpath = tp.resolve("index.html").toFile
        IO.write(targetpath, template.replace("JSPATH", s"main.js"))
        IO.copyFile(bp.resolve("style.css").toFile, tp.resolve("style.css").toFile)
        targetpath
      }
    )
