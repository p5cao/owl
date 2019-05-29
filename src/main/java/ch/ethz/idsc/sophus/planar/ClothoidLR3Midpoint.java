// code by jph
package ch.ethz.idsc.sophus.planar;

import ch.ethz.idsc.sophus.curve.ClothoidCurve;
import ch.ethz.idsc.sophus.curve.LaneRiesenfeldCurveSubdivision;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.opt.TensorUnaryOperator;

/* package */ enum ClothoidLR3Midpoint implements MidpointInterface {
  INSTANCE;
  // ---
  private static final TensorUnaryOperator TENSOR_UNARY_OPERATOR = //
      new LaneRiesenfeldCurveSubdivision(ClothoidCurve.INSTANCE, 3)::string;

  @Override // from MidpointInterface
  public Tensor midpoint(Tensor p, Tensor q) {
    return TENSOR_UNARY_OPERATOR.apply(Tensors.of(p, q)).get(1);
  }
}
