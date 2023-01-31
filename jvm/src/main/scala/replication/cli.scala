package replication

import de.rmgk.options.*
import kofre.base.Id
import replication.fbdc.FbdcCli

import java.nio.file.Path


case class CliArgs(
    conn: Subcommand[fbdc.CliConnections] = Subcommand(fbdc.CliConnections()),
)

object cli {
  def main(args: Array[String]): Unit = {

    val instance = CliArgs()

    val parser = makeParser(
      instance,
      { b =>
        scopt.OParser.sequence(
          b.programName("repl-cli"),
          b.help("help").text("prints this usage text")
        )
      }
    )
    scopt.OParser.parse(parser, args, instance)

    val ipAndPort = """([\d.]*):(\d*)""".r

    instance.conn.value match
      case None =>
      case Some(connections) =>
        val serv = new FbdcCli(connections)
        serv.start()

  }
}
