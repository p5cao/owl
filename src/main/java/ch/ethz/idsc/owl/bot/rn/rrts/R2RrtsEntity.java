// code by jph, gjoel
package ch.ethz.idsc.owl.bot.rn.rrts;

import ch.ethz.idsc.owl.ani.adapter.FallbackControl;
import ch.ethz.idsc.owl.ani.api.AbstractRrtsEntity;
import ch.ethz.idsc.owl.bot.rn.RnTransitionSpace;
import ch.ethz.idsc.owl.bot.rn.glc.R2TrajectoryControl;
import ch.ethz.idsc.owl.math.SingleIntegratorStateSpaceModel;
import ch.ethz.idsc.owl.math.StateSpaceModel;
import ch.ethz.idsc.owl.math.flow.EulerIntegrator;
import ch.ethz.idsc.owl.math.sample.BoxRandomSample;
import ch.ethz.idsc.owl.math.sample.ConstantRandomSample;
import ch.ethz.idsc.owl.math.sample.RandomSampleInterface;
import ch.ethz.idsc.owl.math.state.SimpleEpisodeIntegrator;
import ch.ethz.idsc.owl.math.state.StateTime;
import ch.ethz.idsc.owl.rrts.DefaultRrtsPlannerServer;
import ch.ethz.idsc.owl.rrts.RrtsFlowHelper;
import ch.ethz.idsc.owl.rrts.RrtsNodeCollections;
import ch.ethz.idsc.owl.rrts.core.RrtsNodeCollection;
import ch.ethz.idsc.owl.rrts.core.TransitionRegionQuery;
import ch.ethz.idsc.sophus.math.Extract2D;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Array;

// LONGTERM the redundancy in R2****Entity shows that re-factoring is needed!
/* package */ class R2RrtsEntity extends AbstractRrtsEntity {
  /** preserve 0.5[s] of the former trajectory */
  private static final Scalar DELAY_HINT = RealScalar.of(.5);
  private static final StateSpaceModel STATE_SPACE_MODEL = SingleIntegratorStateSpaceModel.INSTANCE;
  static final Tensor SHAPE = Tensors.fromString("{{0,.1},{.1,0},{0,-.1},{-.1,0}}").unmodifiable();

  // ---
  /** @param stateTime initial position of entity */
  public R2RrtsEntity(StateTime stateTime, TransitionRegionQuery transitionRegionQuery, Tensor lbounds, Tensor ubounds) {
    super( //
        new DefaultRrtsPlannerServer( //
            RnTransitionSpace.INSTANCE, //
            transitionRegionQuery, //
            RationalScalar.of(1, 10), //
            STATE_SPACE_MODEL) {
          @Override
          protected RrtsNodeCollection rrtsNodeCollection() {
            return RrtsNodeCollections.rn(lbounds, ubounds);
          }

          @Override
          protected RandomSampleInterface spaceSampler(Tensor state) {
            return BoxRandomSample.of(lbounds, ubounds);
          }

          @Override
          protected RandomSampleInterface goalSampler(Tensor goal) {
            return new ConstantRandomSample(Extract2D.FUNCTION.apply(goal));
          }

          @Override
          protected Tensor uBetween(StateTime orig, StateTime dest) {
            return RrtsFlowHelper.U_R2.apply(orig, dest);
          }
        }, //
        new SimpleEpisodeIntegrator( //
            STATE_SPACE_MODEL, //
            EulerIntegrator.INSTANCE, //
            stateTime), //
        new R2TrajectoryControl());
    add(FallbackControl.of(Array.zeros(2)));
  }

  protected Tensor shape() {
    return SHAPE;
  }

  /* why Norm2Squared?
  @Override // from TensorMetrix
  public Scalar distance(Tensor x, Tensor y) {
    return Norm2Squared.between(x, y); // non-negative
  }
  */

  @Override // from TrajectoryEntity
  public Scalar delayHint() {
    return DELAY_HINT;
  }
}
