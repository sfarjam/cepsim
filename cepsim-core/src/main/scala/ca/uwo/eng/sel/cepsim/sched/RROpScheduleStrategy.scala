package ca.uwo.eng.sel.cepsim.sched

import ca.uwo.eng.sel.cepsim.placement.Placement
import ca.uwo.eng.sel.cepsim.query.Vertex
import ca.uwo.eng.sel.cepsim.sched.alloc.{WeightedAllocationStrategy, UniformAllocationStrategy, AllocationStrategy}

/** Companion object to RROpScheduleStrategy. */
object RROpScheduleStrategy {

  def apply(allocStrategy: AllocationStrategy, iterations: Int) = new RROpScheduleStrategy(allocStrategy, iterations)
}


/**
 * Scheduling strategy that iterates many times over all placement vertices. First, it uses the same
 * algorithm than DefaultOpScheduleStrategy to distribute the instructions among the available vertices.
 * Then, it distributes the number of instructions over the number of iterations configured.
 *
 * @param iterations Number of passes over the vertices.
 */
class RROpScheduleStrategy(allocStrategy: AllocationStrategy, iterations: Int) extends OpScheduleStrategy {

  /**
   * Allocates instructions to vertices from a placement.
   *
   * @param instructions Number of instructions to be allocated.
   * @param placement Placement object encapsulating the vertices.
   * @return A list of pairs, in which the first element is a vertices and the second the number of
   *         instructions allocated to that vertex.
   */
  override def allocate(instructions: Double, placement: Placement): Iterator[(Vertex, Double)] = {
    val instrPerOperator = allocStrategy.instructionsPerOperator(instructions, placement)
    new RRScheduleIterator(placement, iterations, instrPerOperator)
  }

  /**
    * Iterator returned by the strategy.
    * @param placement Placement object encapsulating the vertices.
    * @param iterations Number of passes over the vertices.
    * @param instrPerOperator Map containing the total number of instructions allocated to each vertex.
    */
  class RRScheduleIterator(placement: Placement, iterations: Int, instrPerOperator: Map[Vertex, Double])
    extends Iterator[(Vertex, Double)] {

    /** Count the number of passes. */
    var count = 0;

    /** Current iterator over the vertices. */
    var it = placement.iterator

    override def hasNext: Boolean = (count < iterations) && (it.hasNext)

    override def next(): (Vertex, Double) = {
      val v = it.next()
      val instructions = Math.floor(instrPerOperator(v) / iterations)

      // if it is the last iteration, then return all remaining instructions
      val ret = (v, if (count == iterations - 1) instrPerOperator(v) - (count * instructions) else instructions)

      // update variables if needed
      if ((!it.hasNext) && (count < iterations - 1)) {
        count += 1
        it = placement.iterator
      }
      ret
    }
  }

}