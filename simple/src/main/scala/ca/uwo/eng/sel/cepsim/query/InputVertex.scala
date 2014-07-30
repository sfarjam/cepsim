package ca.uwo.eng.sel.cepsim.query

import scala.collection.immutable.TreeMap

/**
 * Created by virso on 2014-07-24.
 */
trait InputVertex extends Vertex  { this: Vertex =>

  var inputQueues: Map[Vertex, Int] = TreeMap[Vertex, Int]()(Vertex.VertexIdOrdering)
  //var inputSelectivities: Map[Vertex, Double] = Map.empty


  def initInputQueues(predecessors: Set[Vertex]) = {
    predecessors.foreach(addInputQueue(_))
  }

  def addInputQueue(v: Vertex) = {
    inputQueues = inputQueues + (v -> 0)
  }

  def retrieveFromInput(instructions: Double): Map[Vertex, Int] = {

    // total number of input events
    val totalInputEvents = totalFromMap(inputQueues)

    // number of events that can be processed
    val events = Math.min(totalInputEvents, Math.floor(instructions / ipe) toInt)

    // number of events processed from each queue
    // current implementation distribute processing according to the queue size
    var toProcess = inputQueues.map(elem =>
      (elem._1 -> Math.floor( (elem._2.toDouble / totalInputEvents) * events ).toInt)
    )

    // events not processed due to rounding
    var correctionFactor = events - toProcess.foldLeft(0)((sum, elem) => sum + elem._2)

    // distribute the correction in round robin fashion among all input queues
    val iterator = inputQueues.keys.iterator
    var v = iterator.next
    while (correctionFactor > 0) {
      toProcess = toProcess updated (v, toProcess(v) + 1)
      correctionFactor -= 1
      v = iterator.next
    }

    // update the input queues
    dequeueFromInput(toProcess.toList:_*)

    // return the number of elements per input
    toProcess
  }

  def enqueueIntoInput(v: Vertex, x: Int): Int = {
    inputQueues = inputQueues updated(v, inputQueues(v) + x)
    inputQueues(v)
  }

  private def dequeueFromInput(pairs: (Vertex, Int)*) =
    pairs.foreach {(pair) =>
      inputQueues = inputQueues updated(pair._1, inputQueues(pair._1) - pair._2)
    }


}