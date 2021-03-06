/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * This file is part of the LX Studio software library. By using
 * LX, you agree to the terms of the LX Studio Software License
 * and Distribution Agreement, available at: http://lx.studio/license
 *
 * Please note that the LX license is not open-source. The license
 * allows for free, non-commercial use.
 *
 * HERON ARTS MAKES NO WARRANTY, EXPRESS, IMPLIED, STATUTORY, OR
 * OTHERWISE, AND SPECIFICALLY DISCLAIMS ANY WARRANTY OF
 * MERCHANTABILITY, NON-INFRINGEMENT, OR FITNESS FOR A PARTICULAR
 * PURPOSE, WITH RESPECT TO THE SOFTWARE.
 *
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.clip;

import com.google.gson.JsonObject;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXUtils;
import heronarts.lx.parameter.LXNormalizedParameter;

public class ParameterClipLane extends LXClipLane {

  public final LXNormalizedParameter parameter;

  ParameterClipLane(LXClip clip, LXNormalizedParameter parameter) {
    super(clip);
    this.parameter = parameter;
  }

  @Override
  public String getLabel() {
    LXComponent component = this.parameter.getComponent();
    if (component != this.clip.bus) {
      return this.parameter.getComponent().getLabel() + " | " + this.parameter.getLabel();
    }
    return this.parameter.getLabel();
  }

  public ParameterClipLane appendEvent(ParameterClipEvent event) {
    super.appendEvent(event);
    return this;
  }

  public ParameterClipLane insertEvent(double basis, double normalized) {
    super.insertEvent(
      new ParameterClipEvent(this, this.parameter, normalized)
      .setCursor(basis * this.clip.length.getValue())
    );
    return this;
  }

  @Override
  void advanceCursor(double from, double to) {
    if (this.events.size() == 0) {
      return;
    }
    LXClipEvent prior = null;
    LXClipEvent next = null;
    for (LXClipEvent event : this.events) {
      prior = next;
      next = event;
      if (next.cursor > to) {
        break;
      }
    }
    if (prior == null) {
      this.parameter.setNormalized(((ParameterClipEvent) next).getNormalized());
    } else {
      this.parameter.setNormalized(LXUtils.lerp(
        ((ParameterClipEvent) prior).getNormalized(),
        ((ParameterClipEvent) next).getNormalized(),
        (to - prior.cursor) / (next.cursor - prior.cursor)
      ));
    }
  }

  @Override
  public void save(LX lx, JsonObject obj) {
    super.save(lx, obj);
    obj.addProperty(LXComponent.KEY_COMPONENT_ID, this.parameter.getComponent().getId());
    obj.addProperty(LXComponent.KEY_PARAMETER_PATH, this.parameter.getPath());
  }

  @Override
  protected LXClipEvent loadEvent(LX lx, JsonObject eventObj) {
    double normalized = eventObj.get(ParameterClipEvent.KEY_NORMALIZED).getAsDouble();
    return new ParameterClipEvent(this, this.parameter, normalized);
  }
}

