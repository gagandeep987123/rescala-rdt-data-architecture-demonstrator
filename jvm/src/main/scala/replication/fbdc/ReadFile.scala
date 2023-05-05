package replication.fbdc

import de.rmgk.script.extensions
import kofre.datatypes.LastWriterWins

import java.nio.file.{Files, Paths}

object ReadFile {

  def enableConditional(exampleData: FbdcExampleData) = {
    // adapt for other requirements
    if Files.exists(Paths.get("hello.py")) &&
      //Files.exists(Paths.get("readFile.py")) &&
      process"which python3".runResult().isRight
    then
      println(s"enabling Read Box")
      enableReadFileProcessing(exampleData)
    else
      println(s"python or hello.py not installed")
  }

  def enableReadFileProcessing(exampleData: FbdcExampleData) =
    import exampleData.dataManager
    exampleData.addCapability("ReadFile")

    exampleData.requestsOf[Req.ReadFile].observe { ReadFile =>
      dataManager.transform { current =>
        current.modRes ( reqq =>
            ReadFile.foreach { q =>
            val resp = processReadFile(q.value)
            reqq.observeRemoveMap.insert("ReadFile", Some(LastWriterWins.now(resp, exampleData.replicaId)))
          }
        )
      }
    }

  def processReadFile(r: Req.ReadFile) =
    Res.ReadFile(r, Files.readString(Paths.get("../sgx4ml-python/results")))

}
