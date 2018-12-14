// code by ob
package ch.ethz.idsc.owl.subdiv.curve;

import ch.ethz.idsc.owl.math.GeodesicInterface;
import ch.ethz.idsc.owl.math.group.LieDifferences;
import ch.ethz.idsc.owl.math.group.LieExponential;
import ch.ethz.idsc.owl.math.group.LieGroup;
import ch.ethz.idsc.owl.math.group.LieGroupGeodesic;
import ch.ethz.idsc.owl.math.group.Se2CoveringExponential;
import ch.ethz.idsc.owl.math.group.Se2Group;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Norm;
import ch.ethz.idsc.tensor.red.Total;

class GeodesicCausalFiltering {
  public static GeodesicCausalFiltering se2(Tensor measurements, Tensor reference, int order) {
    return new GeodesicCausalFiltering(Se2Group.INSTANCE, Se2CoveringExponential.INSTANCE, measurements, reference, order);
  }

  private final LieDifferences lieDifferences;
  // = //
  //
  private final GeodesicInterface geodesicInterface;
  // = //
  // new LieGroupGeodesic(Se2Group.INSTANCE::element, Se2CoveringExponential.INSTANCE);
  // ---
  /** raw data */
  private final Tensor measurements;
  /** filtered data which we use as 'truth' */
  public Tensor reference;

  GeodesicCausalFiltering(LieGroup lieGroup, LieExponential lieExponential, Tensor measurements, Tensor reference, int order) {
    this.lieDifferences = new LieDifferences(lieGroup, lieExponential);
    this.geodesicInterface = new LieGroupGeodesic(lieGroup::element, lieExponential);
    this.measurements = measurements;
    // TODO it would be sufficient to pass in reference as a tensor for instance
    this.reference = reference;
  }

  //
  public Tensor filteredSignal(Scalar alpha) {
    return Tensor.of(measurements.stream() //
        .map(new GeodesicCausal1Filter(geodesicInterface, alpha)));
  }

  /** filter Lie Group elements and perform check
   * 
   * @param alpha
   * @return */
  public Scalar evaluate0Error(Scalar alpha) {
    Tensor errors = Tensors.empty();
    GeodesicCausal1Filter geodesicCausal1Filter = //
        new GeodesicCausal1Filter(geodesicInterface, alpha);
    for (int i = 0; i < measurements.length(); ++i) {
      Tensor result = geodesicCausal1Filter.apply(measurements.get(i));
      Scalar scalar = Norm._2.ofVector(lieDifferences.pair(reference.get(i), result));
      errors.append(scalar);
    }
    return Total.of(errors).Get();
  }

  public Scalar evaluate1Error(Scalar alpha) {
    Tensor errors = Tensors.of(RealScalar.of(0));
    GeodesicCausal1Filter geodesicCausal1Filter = //
        new GeodesicCausal1Filter(geodesicInterface, alpha);
    Tensor result_prev = geodesicCausal1Filter.apply(measurements.get(0));
    Tensor ref_prev = reference.get(0);
    for (int i = 2; i < measurements.length(); ++i) {
      Tensor pair1 = lieDifferences.pair(ref_prev, reference.get(i));
      Tensor pair2 = lieDifferences.pair(result_prev, geodesicCausal1Filter.apply(measurements.get(i)));
      result_prev = geodesicCausal1Filter.apply(measurements.get(i));
      ref_prev = reference.get(i);
      Scalar scalar = Norm._2.between(pair1, pair2);
      errors.append(scalar);
    }
    return Total.of(errors).Get();
  }
}
