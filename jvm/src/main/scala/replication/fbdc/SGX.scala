package replication.fbdc

import de.rmgk.script.extensions
import kofre.datatypes.LastWriterWins

object SGX {

  def enableConditional(exampleData: FbdcExampleData) = {
    //if process"which fortune".runResult().isRight
    //then
    //  println(s"enabling fortunes")
    //  enableFortuneProcessing(exampleData)
    //else
    //  println(s"fortunes not installed")
    println("Something is working")
  }

  def enableSGXProcessing(exampleData: FbdcExampleData) =
    import exampleData.dataManager
    //exampleData.addCapability("fortune")

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

