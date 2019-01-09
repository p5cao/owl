// code by jph
package ch.ethz.idsc.sophus.app.util;

import java.awt.event.MouseEvent;

public interface LazyMouseListener {
  void lazyClicked(MouseEvent myMouseEvent);

  default void lazyDragged(MouseEvent myMouseEvent) {
    // empty by default
  }
}
