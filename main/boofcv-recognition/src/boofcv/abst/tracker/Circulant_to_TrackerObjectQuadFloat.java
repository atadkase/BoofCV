/*
 * Copyright (c) 2011-2017, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.abst.tracker;

import boofcv.alg.tracker.circulant.CirculantTracker;
import boofcv.alg.tracker.circulant.CirculantTrackerFloat;
import boofcv.struct.image.ImageGray;
import boofcv.struct.image.ImageType;
import georegression.geometry.UtilPolygons2D_F32;
import georegression.geometry.UtilPolygons2D_F64;
import georegression.struct.shapes.*;

/**
 * Wrapper around {@link CirculantTracker} for {@link TrackerObjectQuad}.
 *
 * @author Peter Abeles
 */
public class Circulant_to_TrackerObjectQuadFloat<T extends ImageGray<T>> implements TrackerObjectQuadFloat<T> {

	CirculantTrackerFloat<T> tracker;
	Rectangle2D_F32 rect = new Rectangle2D_F32();

	ImageType<T> imageType;

	public Circulant_to_TrackerObjectQuadFloat(CirculantTrackerFloat<T> tracker , ImageType<T> imageType) {
		this.tracker = tracker;
		this.imageType = imageType;

	}

	@Override
	public boolean initialize(T image, Quadrilateral_F32 location) {


		UtilPolygons2D_F32.bounding(location, rect);

		int width = (int)(rect.p1.x - rect.p0.x);
		int height = (int)(rect.p1.y - rect.p0.y);

		tracker.initialize(image,(int)rect.p0.x,(int)rect.p0.y,width,height);

		return true;
	}

	@Override
	public boolean process(T image, Quadrilateral_F32 location) {

		tracker.performTracking(image);
		RectangleLength2D_F32 r = tracker.getTargetLocation();

		if( r.x0 >= image.width || r.y0 >= image.height )
			return false;
		if( r.x0+r.width < 0 || r.y0+r.height < 0 )
			return false;

		float x0 = r.x0;
		float y0 = r.y0;
		float x1 = r.x0 + r.width;
		float y1 = r.y0 + r.height;

		location.a.x = x0;
		location.a.y = y0;
		location.b.x = x1;
		location.b.y = y0;
		location.c.x = x1;
		location.c.y = y1;
		location.d.x = x0;
		location.d.y = y1;

		return true;
	}

	@Override
	public ImageType<T> getImageType() {
		return imageType;
	}
}
