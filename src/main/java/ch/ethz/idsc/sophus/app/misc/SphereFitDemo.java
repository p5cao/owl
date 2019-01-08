// code by jph
package ch.ethz.idsc.sophus.app.misc;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Optional;

import javax.swing.JTextField;

import ch.ethz.idsc.owl.gui.GraphicsUtil;
import ch.ethz.idsc.owl.gui.win.GeometricLayer;
import ch.ethz.idsc.owl.math.map.Se2Utils;
import ch.ethz.idsc.sophus.app.api.AbstractDemo;
import ch.ethz.idsc.sophus.app.api.ControlPointsDemo;
import ch.ethz.idsc.sophus.app.api.CurveRender;
import ch.ethz.idsc.sophus.app.api.DubinsGenerator;
import ch.ethz.idsc.sophus.app.api.GeodesicDisplay;
import ch.ethz.idsc.sophus.app.api.GeodesicDisplays;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;
import ch.ethz.idsc.tensor.img.ColorDataLists;
import ch.ethz.idsc.tensor.lie.CirclePoints;
import ch.ethz.idsc.tensor.opt.ConvexHull;
import ch.ethz.idsc.tensor.opt.HungarianAlgorithm;
import ch.ethz.idsc.tensor.opt.SpatialMedian;
import ch.ethz.idsc.tensor.opt.SphereFit;
import ch.ethz.idsc.tensor.red.Norm;

/* package */ class SphereFitDemo extends ControlPointsDemo {
  private static final ColorDataIndexed COLOR_DATA_INDEXED = ColorDataLists._097.cyclic();
  private static final Tensor CIRCLE = CirclePoints.of(10).multiply(RealScalar.of(3));

  SphereFitDemo() {
    super(true, false, GeodesicDisplays.R2_ONLY);
    // ---
    JTextField jTextField = new JTextField(10);
    jTextField.setPreferredSize(new Dimension(100, 28));
    timerFrame.jToolBar.add(jTextField);
    // ---
    Tensor blub = Tensors.fromString("{{1,0,0},{1,0,0},{2,0,2.5708},{1,0,2.1},{1.5,0,0},{2.3,0,-1.2},{1.5,0,0},{4,0,3.14159},{2,0,3.14159},{2,0,0}}");
    setControl(DubinsGenerator.of(Tensors.vector(0, 0, 2.1), //
        Tensor.of(blub.stream().map(row -> row.pmul(Tensors.vector(2, 1, 1))))));
  }

  @Override // from RenderInterface
  public void render(GeometricLayer geometricLayer, Graphics2D graphics) {
    GraphicsUtil.setQualityHigh(graphics);
    Tensor control = control();
    Optional<Tensor> optional = SphereFit.of(control);
    if (optional.isPresent()) {
      Tensor center = optional.get().get(0);
      Scalar radius = optional.get().Get(1);
      geometricLayer.pushMatrix(Se2Utils.toSE2Translation(center));
      new CurveRender(CirclePoints.of(40).multiply(radius), true, COLOR_DATA_INDEXED.getColor(0), 1.5f) //
          .render(geometricLayer, graphics);
      geometricLayer.popMatrix();
    }
    new CurveRender(ConvexHull.of(control), true, COLOR_DATA_INDEXED.getColor(1), 1.5f).render(geometricLayer, graphics);
    {
      new CurveRender(CIRCLE, true, Color.GRAY).render(geometricLayer, graphics);
      Tensor matrix = Tensors.matrix((i, j) -> //
      Norm._2.between(control.get(i), CIRCLE.get(j)), control.length(), CIRCLE.length());
      HungarianAlgorithm hungarianAlgorithm = HungarianAlgorithm.of(matrix);
      int[] matching = hungarianAlgorithm.matching();
      graphics.setColor(Color.RED);
      for (int index = 0; index < matching.length; ++index)
        if (matching[index] != HungarianAlgorithm.UNASSIGNED) {
          Path2D path2d = geometricLayer.toPath2D(Tensors.of(control.get(index), CIRCLE.get(matching[index])));
          graphics.draw(path2d);
        }
    }
    {
      GeodesicDisplay geodesicDisplay = geodesicDisplay();
      Tensor weiszfeld = SpatialMedian.with(1e-4).uniform(control).get();
      geometricLayer.pushMatrix(Se2Utils.toSE2Translation(weiszfeld));
      Path2D path2d = geometricLayer.toPath2D(geodesicDisplay.shape());
      path2d.closePath();
      graphics.setColor(new Color(128, 128, 255, 64));
      graphics.fill(path2d);
      graphics.setColor(new Color(128, 128, 255, 255));
      graphics.draw(path2d);
      geometricLayer.popMatrix();
    }
    renderControlPoints(geometricLayer, graphics);
  }

  public static void main(String[] args) {
    AbstractDemo abstractDemo = new SphereFitDemo();
    abstractDemo.timerFrame.jFrame.setBounds(100, 100, 1000, 600);
    abstractDemo.timerFrame.jFrame.setVisible(true);
  }
}
