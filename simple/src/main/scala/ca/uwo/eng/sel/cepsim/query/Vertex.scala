package ca.uwo.eng.sel.cepsim.query

trait Vertex {

  def id: String
  def ipe: Double

  def init(q: Query): Unit = { }
  def run(instructions: Double): Unit

  override def toString: String = s"[id: $id]"

  def compare(that: Vertex) = id.compare(that.id)

  def totalFromMap(map: Map[Vertex, Int]): Int = map.foldLeft(0)((sum, elem) => sum + elem._2)



}

object Vertex {
  implicit object VertexIdOrdering extends Ordering[Vertex] {
    override def compare(x: Vertex, y: Vertex): Int = {
      x.id.compare(y.id)
    }
  }


}
