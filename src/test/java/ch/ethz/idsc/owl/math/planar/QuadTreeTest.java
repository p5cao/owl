// code by gjoel
package ch.ethz.idsc.owl.math.planar;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.sca.Clips;
import junit.framework.TestCase;

public class QuadTreeTest extends TestCase {
  private static final Tensor POINTS = Tensors.of( //
      Tensors.fromString("{100,200,0}"), //
      Tensors.fromString("{101,201,1}"), //
      Tensors.fromString("{101,202,2}"), //
      Tensors.fromString("{101,203,3}"), //
      Tensors.fromString("{101,204,4}"), //
      Tensors.fromString("{101,205,5}"), //
      Tensors.fromString("{101,206,6}"), //
      Tensors.fromString("{101,207,7}"), //
      Tensors.fromString("{101,208,8}"), //
      Tensors.fromString("{101,209,9}"), //
      Tensors.fromString("{110,210,10}")).unmodifiable();

  public void testSimple() {
    QuadTree quadTree = new QuadTree(Clips.interval(100, 110), Clips.interval(200, 210), 2);
    POINTS.stream().forEach(quadTree::insert);
    assertEquals(POINTS.get(5), quadTree.closest(Tensors.fromString("{103,205,999}")).get());
  }

  public void testUnits() {
    QuadTree quadTree = new QuadTree( //
        Clips.interval(Quantity.of(100, "m"), Quantity.of(110, "m")), //
        Clips.interval(Quantity.of(200, "m"), Quantity.of(210, "m")), 2);
    Tensor points = POINTS.copy();
    points.forEach(point -> point.set(Quantity.of(point.Get(0), "m"), 0));
    points.forEach(point -> point.set(Quantity.of(point.Get(1), "m"), 1));
    points.stream().forEach(quadTree::insert);
    assertEquals(points.get(5), quadTree.closest(Tensors.fromString("{103[m],205[m],999}")).get());
  }
}
