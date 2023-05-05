package replication.webapp

import org.scalajs.dom
import org.scalajs.dom.html.Element
import org.scalajs.dom.{MouseEvent, document}
import replication.DataManager
import replication.webapp.MetaInfo
import rescala.default.*
import rescala.extra.Tags.*
import scalatags.JsDom.TypedTag
import scalatags.JsDom.all.{*, given}
import scalatags.JsDom.tags2.{article, aside, nav, section}
import kofre.base.Id
import loci.transmitter.RemoteRef
import replication.fbdc.{FbdcExampleData, Req}

import scala.collection.immutable.LinearSeq

object HTML {

  def leftClickHandler(action: => Unit) = { (e: MouseEvent) =>
    if (e.button == 0) {
      e.preventDefault()
      action
    }
  }

  val RemoteRegex = raw"""^remote#(\d+).*""".r.anchored
  def remotePrettyName(rr: RemoteRef) =
    val RemoteRegex(res) = rr.toString: @unchecked
    res

  def connectionManagement(ccm: ContentConnectionManager, fbdcExampleData: FbdcExampleData) = {
    import fbdcExampleData.dataManager
    List(
      h1("connection management"),
      section(table(
        tr(td("total state size"), dataManager.encodedStateSize.map(s => td(s)).asModifier),
        tr(
          td("request queue"),
          dataManager.mergedState.map(v => td(v.store.requests.elements.size)).asModifier
        ),
      )),
      section(
        button("disseminate local", onclick := leftClickHandler(dataManager.disseminateLocalBuffer())),
        button("disseminate all", onclick   := leftClickHandler(dataManager.disseminateFull()))
      ),
      section(table(
        thead(th("remote ref"), th("connection"), th("request"), th("dots")),
        tr(
          td(Id.unwrap(dataManager.replicaId)),
          td(),
          td(),
          table(
            dataManager.currentContext.map(dotsToRows).asModifierL
          )
        ),
        ccm.connectedRemotes.map { all =>
          all.toList.sortBy(_._1.toString).map { (rr, connected) =>
            tr(
              td(remotePrettyName(rr)),
              if !connected
              then td("disconnected")
              else
                List(
                  td(button("disconnect", onclick := leftClickHandler(rr.disconnect()))),
                  td(button("request", onclick := leftClickHandler(dataManager.requestMissingFrom(rr)))),
                  td(table(
                    dataManager.contextOf(rr).map(dotsToRows).asModifierL
                  ))
                )
            )
          }
        }.asModifierL,
      )),
      section(aside(
        "remote url: ",
        ccm.wsUri,
        button("connect", onclick := leftClickHandler(ccm.connect()))
      ))
    )
  }

  def dotsToRows(dots: kofre.time.Dots) =
    dots.internal.toList.sortBy(t => Id.unwrap(t._1)).map { (k, v) =>
      tr(td(Id.unwrap(k)), td(v.toString))
    }.toSeq

  def providers(exdat: FbdcExampleData) = {
    div(
      h1("make a request"),
      exdat.providers.map { prov =>
        prov.observeRemoveMap.entries.map { (id, provided) =>
          section(
            header(h2("Executor:", Id.unwrap(id))),
            provided.elements.iterator.map {
              case "SGX"     => sgxBox(exdat, id)
              case "ReadFile" => readFile(exdat,id)
              case "fortune" => fortuneBox(exdat, id)
              case "northwind"     => northwindBox(exdat, id)
              case other =>
                dom.window.alert(s"received unknown provider $other")
                throw IllegalStateException(s"unknown provider $other")
            }.toList
          ).asInstanceOf[TypedTag[Element]]
        }.toList
      }.asModifierL
    )

  }

  def fortuneBox(exdat: FbdcExampleData, id: Id) = aside(
    button(
      "get fortune",
      onclick := leftClickHandler {
        exdat.dataManager.transform { curr =>
          curr.modReq { reqs =>
            reqs.enqueue(Req.Fortune(id))
          }
        }
      }
    ),
    exdat.latestFortune.map(f => p(f.map(_.result).getOrElse(""))).asModifier
  )

  def sgxBox(exdat: FbdcExampleData, id: Id) =
    val dropdownLabel_model = label(`for` := "model")("Model : ").render
    val model = select(
      //option(value := "")("Select an option"),
      option(value := "mlp")("mlp")
      //option(value := "option2")("Option 2"),
      //option(value := "option3")("Option 3")
    ).render
    val dropdownLabel_threads = label(`for` := "thread")("thread : ").render
    val thread = select(
      option(value := "1")("1"),
      option(value := "16")("16")
    ).render
    val dropdownLabel_batchsize = label(`for` := "batchsize")("batchsize : ").render
    val batchsize = select(
      //option(value := "")("Select an option"),
      option(value := "1")("1")
      //option(value := "option2")("Option 2"),
      //option(value := "option3")("Option 3")
    ).render
    val dropdownLabel_sgxsecurity = label(`for` := "sgx security")("sgxsecurity : ").render
    val sgxsecurity = select(
      //option(value := "")("Select an option"),
      option(value := "1")("enabled"),
      option(value := "0")("disabled")
      //option(value := "option3")("Option 3")
    ).render

    aside(
      dropdownLabel_model,
      model,
      dropdownLabel_threads,
      thread,
      dropdownLabel_batchsize,
      batchsize,
      dropdownLabel_sgxsecurity,
      sgxsecurity,
      button(
        "get sgx",
        onclick := leftClickHandler {
          exdat.dataManager.transform { curr =>
            curr.modReq { reqs =>
              reqs.enqueue(Req.SGX(id,model.value,thread.value,batchsize.value,sgxsecurity.value))
            }
          }
        }
      ),
      exdat.latestSGX.map(f => p(f.map(_.result).getOrElse(""))).asModifier
    )


  def readFile(exdat: FbdcExampleData, id: Id) = aside(
    button(
      "get output",
      onclick := leftClickHandler {
        exdat.dataManager.transform { curr =>
          curr.modReq { reqs =>
            reqs.enqueue(Req.ReadFile(id))
          }
        }
      }
    ),
    exdat.latestReadFile.map(f => p(f.map(_.result).getOrElse(""))).asModifier
  )

  def northwindBox(exdat: FbdcExampleData, id: Id) =
    val ip = input().render

    aside(
      ip,
      button(
        "query northwind",
        onclick := leftClickHandler {
          exdat.dataManager.transform { curr =>
            curr.modReq { reqs =>
              reqs.enqueue(Req.Northwind(id, ip.value))
            }
          }
        }
      ),
      p(
        table(
          exdat.latestNorthwind.map {
            case None => Nil
            case Some(res) =>
              val keys = res.result.head.keys.toList.sorted
              thead(keys.map(th(_)).toList: _*) ::
              res.result.map { row =>
                tr(keys.map(k => td(row(k))))
              }
          }.asModifierL
        )
      )
    )
}
