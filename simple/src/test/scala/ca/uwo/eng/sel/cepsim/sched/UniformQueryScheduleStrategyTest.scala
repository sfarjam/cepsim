package ca.uwo.eng.sel.cepsim.sched

import ca.uwo.eng.sel.cepsim.Host
import ca.uwo.eng.sel.cepsim.placement.Placement
import ca.uwo.eng.sel.cepsim.query.{EventConsumer, Operator, EventProducer}
import org.junit.runner.RunWith
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, FlatSpec}
import org.scalatest.junit.JUnitRunner

/**
 * Created by virso on 2014-07-21.
 */
@RunWith(classOf[JUnitRunner])
class UniformQueryScheduleStrategyTest extends FlatSpec
  with Matchers
  with MockitoSugar {

  "A UniformScheduleStrategy" should "allocate same amount of MIPS for all placements in the same VM" in {
    val strategy = new UniformQueryScheduleStrategy()
    val p1, p2 = mock[EventProducer]
    val f1, f2 = mock[Operator]
    val c1, c2 = mock[EventConsumer]
    val host = mock[Host]
    doReturn(1000.0).when(host).getVmMips(1L)

    val placement1 = mock[Placement]
    doReturn(Set(p1, f1, c1)).when(placement1).vertices
    doReturn(1L).when(placement1).vmId

    val placement2 = mock[Placement]
    doReturn(Set(p2, f2, c2)).when(placement2).vertices
    doReturn(1L).when(placement2).vmId

    val ret = strategy.allocate(host, Set(placement1, placement2))

    ret should have size (2)
    ret(placement1) should be (500.0)
    ret(placement2) should be (500.0)
  }


}