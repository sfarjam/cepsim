package ca.uwo.eng.sel.cepsim.query

import ca.uwo.eng.sel.cepsim.event.{EventSetQueue, EventSet}

/** Trait that represent vertices that have incoming edges. */
trait InputVertex extends Vertex  { this: Vertex =>

  /** Event sets that represent the input queues. */
  var inputEventQueues: Map[Vertex, EventSetQueue] = Map.empty

  /**
    * Total sizes of all input queues. Used to avoid recalculation of this total in the
    * totalInputEvents method.
    */
  var inputEventQueuesSize: Double = 0.0

  /** The maximum queue size. */
  val queueMaxSize: Int

  /**
   * Gets the set of vertex predecessors.
   * @return set containing all vertex predecessors.
   */
  override def predecessors: Set[OutputVertex] = inputEventQueues.keySet.asInstanceOf[Set[OutputVertex]]

  /**
    * Indicates if this vertex has bounded queues.
    * @return <code>true</code> if the vertex has bounded queues.
    */
  def isBounded() = queueMaxSize > 0

  /**
    * Initializes the input queues with a set of predecessors.
    * @param predecessors Set containing the vertex predecessors.
    */
  def initInputQueues(predecessors: Set[Vertex]) = {
    predecessors.foreach(addInputQueue(_))
  }

  /**
   * Add a new input queue for a new predecessor.
   * @param v New predecessor vertex.
   */
  def addInputQueue(v: Vertex) =
    inputEventQueues = inputEventQueues + (v -> EventSetQueue())

  /**
   * Obtains the number of events in a input queue.
   * @param v Predecessor vertex to which the input queue is associated.
   * @return Number of events on the informed queue.
   */
  def inputQueues(v: Vertex): Double = inputEventQueues(v).size

  /** 
    * Gets the total number of events in all input queues.
    * @return total number of events in all input queues.
    */
  def totalInputEvents = inputEventQueuesSize//inputEventQueues.foldLeft(0.0)((acc, elem) => acc + elem._2.size)


  /**
    * Enqueue an event set to an input queue.
    * @param pred Vertex used to locate the input queue.
    * @param es Event set to be enqueued.
    */
  def enqueueIntoInput(pred: Vertex, es: EventSet): Unit = {
    inputEventQueuesSize += es.size
    inputEventQueues(pred).enqueue(es)
  }

  /**
    * Dequeue a number of events from an input queue.
    * @param pred Vertex used to locate the input queue.
    * @param quantity Number of events to be dequeued.
    * @return EventSet encapsulating the dequeued events.
    */
  def dequeueFromInput(pred: Vertex, quantity: Double): EventSet = {
    inputEventQueuesSize -= quantity
    if (inputEventQueuesSize < 0.0001) inputEventQueuesSize = 0.0
    inputEventQueues(pred).dequeue(quantity)
  }

  /**
    * Dequeue events from N input queues.
    * @param pairs Pairs containing a vertex (to locate the input queue) and the number of events to be dequeued.
    * @return Map containing the event sets extracted from each input queue.
    */
  def dequeueFromInput(pairs: (Vertex, Double)*): Map[Vertex, EventSet] =
    pairs.map((pair) => (pair._1, dequeueFromInput(pair._1, pair._2))).toMap


  /**
   * Retrieve events from the input queues.
   * @param instructions Number of instructions that can be used.
   * @param maximumNumberOfEvents Maximum number of events that can be sent to the output queues.
   * @return Map from the predecessors to the event sets extracted from each input queue..
   */
  def retrieveFromInput(instructions: Double, maximumNumberOfEvents: Double = Double.MaxValue): Map[Vertex, EventSet]  = {

    // total number of input events
    val total = totalInputEvents

    // number of events that can be processed
    val events = total.min(instructions / ipe).min(maximumNumberOfEvents)

    // number of events processed from each queue
    // current implementation distribute processing according to the queue size
    inputEventQueues.map(elem =>
      (elem._1 -> dequeueFromInput(elem._1, (if (total == 0) 0.0 else (elem._2.size / total) * events)))
    )
  }

}
