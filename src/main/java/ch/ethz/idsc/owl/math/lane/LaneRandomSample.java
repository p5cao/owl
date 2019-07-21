// code by gjoel
package ch.ethz.idsc.owl.math.lane;

import java.io.Serializable;
import java.util.Random;

import ch.ethz.idsc.owl.math.sample.RandomSampleInterface;
import ch.ethz.idsc.owl.math.sample.SphereRandomSample;
import ch.ethz.idsc.sophus.math.Extract2D;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.pdf.NormalDistribution;
import ch.ethz.idsc.tensor.pdf.RandomVariate;
import ch.ethz.idsc.tensor.qty.Degree;

public class LaneRandomSample implements RandomSampleInterface, Serializable {
  // public static RandomSampleInterface along(SplitInterface geodesicInterface, Scalar width, Tensor... controlPoints) {
  // return along(geodesicInterface, width, Tensors.of(controlPoints));
  // }
  //
  // public static RandomSampleInterface along(SplitInterface geodesicInterface, Scalar width, Collection<Tensor> controlPoints) {
  // return along(geodesicInterface, width, Tensor.of(controlPoints.stream()));
  // }
  //
  // public static RandomSampleInterface along(SplitInterface geodesicInterface, Scalar width, Tensor controlPoints) {
  // return new LaneRandomSample(StableLane.of(geodesicInterface, controlPoints, width));
  // }
  public static RandomSampleInterface along(LaneInterface laneInterface) {
    return new LaneRandomSample(laneInterface);
  }

  public static RandomSampleInterface startSample(LaneInterface laneInterface) {
    return new LaneRandomSample(laneInterface).around(0);
  }

  public static RandomSampleInterface endSample(LaneInterface laneInterface) {
    return new LaneRandomSample(laneInterface).around(laneInterface.midLane().length() - 1);
  }

  private RandomSampleInterface around(int index) {
    return around(laneInterface.midLane().get(index), laneInterface.margins().Get(index));
  }

  // ---
  // TODO GJOEL/JPH magic const
  private final static Scalar MU_A = Degree.of(18);
  // ---
  public final LaneInterface laneInterface;

  private LaneRandomSample(LaneInterface lane) {
    this.laneInterface = lane;
  }

  @Override // from RandomSampleInterface
  public Tensor randomSample(Random random) {
    // RandomVariate.of(UniformDistribution.of(0, laneInterface.midLane().length() - 1)).number().intValue();
    int index = random.nextInt(laneInterface.midLane().length());
    return around(index).randomSample(random);
  }

  private static RandomSampleInterface around(Tensor point, Scalar radius) {
    return new RandomSampleInterface() {
      @Override // from RandomSampleInterface
      public Tensor randomSample(Random random) {
        Tensor xy = SphereRandomSample.of(Extract2D.FUNCTION.apply(point), radius).randomSample(random);
        Scalar a = RandomVariate.of(NormalDistribution.of(point.Get(2), MU_A));
        return xy.append(a);
      }
    };
  }
}