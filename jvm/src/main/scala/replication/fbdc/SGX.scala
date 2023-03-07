package replication.fbdc

import de.rmgk.script.extensions
import kofre.datatypes.LastWriterWins

import java.nio.file.{Files, Paths}

object SGX {

  def enableConditional(exampleData: FbdcExampleData) = {
    // adapt for other requirements
    if Files.exists(Paths.get("hello.py")) &&
      process"which python3".runResult().isRight
    then
      println(s"enabling sgx")
      enableSGXProcessing(exampleData)
    else
      println(s"python or hello.py not installed")
  }

  def enableSGXProcessing(exampleData: FbdcExampleData) =
    import exampleData.dataManager
    exampleData.addCapability("SGX")

    exampleData.requestsOf[Req.SGX].observe { sgx =>
      dataManager.transform { current =>
        current.modRes { reqq =>
          sgx.foreach { q =>
            val resp = processSGX(q.value)
            reqq.observeRemoveMap.insert("SGX", Some(LastWriterWins.now(resp, exampleData.replicaId)))
          }
        }
      }
    }

  def processSGX(r: Req.SGX) =
    Res.SGX(r, process"python3 hello.py".run())

}
