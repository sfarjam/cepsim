package ca.uwo.eng.sel.cepsim.query

import scala.collection.mutable.Queue


object Query {
  def apply(vs: Set[Vertex], es: Set[(OutputVertex, InputVertex, Double)]) = {
    val q = new Query()
    q addVertices(vs.toSeq:_*)
    q addEdges(es toSeq:_*)
    q
  }
}


class Query (v: Set[Vertex], e: Map[Vertex, Set[Edge]]) {

  private [query] def this() = this(Set.empty, Map.empty)
  
  var vertices: Set[Vertex] = v
  private var outgoingEdges: Map[Vertex, Set[Edge]] = e withDefaultValue(Set.empty)
  private var incomingEdges: Map[Vertex, Set[Edge]] = {
    val tmpMap = scala.collection.mutable.Map[Vertex, Set[Edge]]() withDefaultValue(Set.empty)
    outgoingEdges.values.foreach{(set) =>
      set.foreach{(edge) =>
        val currentSet = tmpMap(edge.to)
        tmpMap(edge.to) = currentSet + edge
      }
    }
    tmpMap.toMap.withDefaultValue(Set.empty)
  }

  def producers: Set[EventProducer] =
    vertices collect { case e: EventProducer => e }

  def consumers: Set[EventConsumer] =
    vertices collect { case e: EventConsumer => e }

  def addVertex(v0: Vertex) = addVertices(v0)
  def addVertices(vs: Vertex*) = {
    vertices = vertices ++ vs
    vs foreach (_.addQuery(this))

//    val newQuery = new Query(vertices ++ (vs), outgoingEdges)
//    val oldQuery = this
//
//    newQuery.vertices.foreach{(v) =>
//      v.removeQuery(oldQuery)
//      v.addQuery(newQuery)
//    }
//    newQuery
  }

  def predecessors(v: Vertex): Set[OutputVertex] = incomingEdges(v).map(_.from.asInstanceOf[OutputVertex])
  def successors(v: Vertex): Set[InputVertex] = outgoingEdges(v).map(_.to.asInstanceOf[InputVertex])
  def edges(v: Vertex): Set[Edge] = outgoingEdges(v)
  def edge(from: Vertex, to: Vertex): Edge = outgoingEdges(from).find(_.to == to) match {
    case Some(e) => e
    case None    => throw new IllegalArgumentException
  }


  //def addEdge(v1: OutputVertex, v2: InputVertex): Query = addEdge(v1, v2, 1.0)
  def addEdge(v1: OutputVertex, v2: InputVertex, selectivity: Double = 1.0): Unit = addEdges((v1, v2, selectivity))

  def addEdges(es: (OutputVertex, InputVertex, Double)*): Unit =
    addEdges(es.map((e) => Edge(e._1, e._2, e._3)).toSet)

  def addEdges(es: Set[Edge]): Unit = {
    es foreach { e =>
      e.from addOutputQueue (e.to, e.selectivity)
      e.to   addInputQueue  (e.from)
      outgoingEdges = outgoingEdges updated(e.from, outgoingEdges(e.from) + e)
      incomingEdges = incomingEdges updated(e.to, incomingEdges(e.to) + e)
    }
  }


  // ---------------------------------------------
  def pathsToProducers(c: EventConsumer): List[VertexPath] = {
    pathsRecursive(c)
  }

  private def pathsRecursive(vx: Vertex): List[VertexPath] = {
    if (predecessors(vx).isEmpty) List(VertexPath(vx))
    else
      predecessors(vx).toList.sorted(Vertex.VertexIdOrdering).flatMap((pred) =>
        pathsRecursive(pred).map((vx, edge(pred, vx)) :: _)
      )
  }

}