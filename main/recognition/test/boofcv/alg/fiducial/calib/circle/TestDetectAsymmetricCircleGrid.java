/*
 * Copyright (c) 2011-2016, Peter Abeles. All Rights Reserved.
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

package boofcv.alg.fiducial.calib.circle;

import boofcv.abst.filter.binary.InputToBinary;
import boofcv.alg.fiducial.calib.circle.EllipseClustersIntoAsymmetricGrid.Grid;
import boofcv.alg.shapes.ellipse.BinaryEllipseDetector;
import boofcv.factory.filter.binary.FactoryThresholdBinary;
import boofcv.factory.shape.FactoryShapeDetector;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayU8;
import georegression.struct.affine.Affine2D_F64;
import georegression.struct.point.Point2D_F64;
import georegression.struct.shapes.EllipseRotated_F64;
import org.ddogleg.struct.FastQueue;
import org.junit.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Peter Abeles
 */
public class TestDetectAsymmetricCircleGrid {
	@Test
	public void process_easy() {
		DetectAsymmetricCircleGrid<GrayU8> alg = createAlg(5,4);

		List<Point2D_F64> locations = new ArrayList<>();
		GrayU8 image = render(5,4, 20, new Affine2D_F64(1,0,0,1,100,100), locations);

		alg.process(image);

		List<Grid> found = alg.getGrids();

		assertEquals(1, found.size());

		Grid g = found.get(0);
		assertEquals(5 , g.rows );
		assertEquals(4 , g.columns );

		int N = g.rows*g.columns;
		for (int i = 0,j=0; i < N; i+=2,j++) {
			EllipseRotated_F64 f = g.ellipses.get(i);
			Point2D_F64 e = locations.get(j);

			assertEquals( e.x , f.center.x , 2.0 );
			assertEquals( e.y , f.center.y , 2.0 );
		}

	}

	@Test
	public void process_rotated() {
		fail("Implement");
	}

	@Test
	public void process_negative() {
		fail("Implement");
	}

	private DetectAsymmetricCircleGrid<GrayU8> createAlg( int numRows , int numCols ) {

		InputToBinary<GrayU8> threshold = FactoryThresholdBinary.globalFixed(100,true,GrayU8.class);
		BinaryEllipseDetector<GrayU8> detector = FactoryShapeDetector.ellipse(null, GrayU8.class);
		EllipsesIntoClusters cluster = new EllipsesIntoClusters(2.0,0.8);
		return new DetectAsymmetricCircleGrid<>( numRows, numCols,threshold, detector,  cluster);
	}

	@Test
	public void pruneIncorrectSize() {
		List<List<EllipsesIntoClusters.Node>> clusters = new ArrayList<>();
		clusters.add( createListNodes(4));
		clusters.add( createListNodes(10));
		clusters.add( createListNodes(11));


		DetectAsymmetricCircleGrid.pruneIncorrectSize(clusters, 10);

		assertEquals(1,clusters.size());
		assertEquals(10,clusters.get(0).size());
	}

	private static List<EllipsesIntoClusters.Node> createListNodes( int N ) {
		List<EllipsesIntoClusters.Node> list = new ArrayList<>();

		for (int i = 0; i < N; i++) {
			list.add( new EllipsesIntoClusters.Node() );
		}

		return list;
	}

	@Test
	public void pruneIncorrectShape() {
		FastQueue<Grid> grids = new FastQueue<>(Grid.class,true);
		grids.grow().setShape(4,5);
		grids.grow().setShape(5,4);
		grids.grow().setShape(4,3);
		grids.grow().setShape(5,5);

		DetectAsymmetricCircleGrid.pruneIncorrectShape(grids, 4, 5);

		assertEquals( 2 , grids.size );
	}

	/**
	 * Vertical flip is needed to put it into the correct order
	 */
	@Test
	public void putGridIntoCanonical_vertical() {
		DetectAsymmetricCircleGrid<?> alg = new DetectAsymmetricCircleGrid<>(5,2,null,null,null);

		Grid g = createGrid(5,2);
		List<EllipseRotated_F64> original = new ArrayList<>();
		original.addAll(g.ellipses);

		alg.putGridIntoCanonical(g);

		assertEquals(5,g.rows);
		assertEquals(2,g.columns);
		assertTrue(original.get(0) == g.get(0,0));

		alg.putGridIntoCanonical(flipVertical(g));

		assertEquals(5,g.rows);
		assertEquals(2,g.columns);
		assertTrue(original.get(0) == g.get(0,0));
	}

	/**
	 * Horizontal flip is needed to put it into the correct order
	 */
	@Test
	public void putGridIntoCanonical_horizontal() {
		DetectAsymmetricCircleGrid<?> alg = new DetectAsymmetricCircleGrid<>(2,5,null,null,null);

		Grid g = createGrid(2,5);
		List<EllipseRotated_F64> original = new ArrayList<>();
		original.addAll(g.ellipses);

		alg.putGridIntoCanonical(g);

		assertEquals(2,g.rows);
		assertEquals(5,g.columns);
		assertTrue(original.get(0) == g.get(0,0));

		alg.putGridIntoCanonical(flipHorizontal(g));

		assertEquals(2,g.rows);
		assertEquals(5,g.columns);
		assertTrue(original.get(0) == g.get(0,0));
	}

	/**
	 * Horizontal flip is needed to put it into the correct order
	 */
	@Test
	public void putGridIntoCanonical_rotate() {
		DetectAsymmetricCircleGrid<?> alg = new DetectAsymmetricCircleGrid<>(3,3,null,null,null);

		Grid g = createGrid(3,3);
		List<EllipseRotated_F64> original = new ArrayList<>();
		original.addAll(g.ellipses);

		alg.putGridIntoCanonical(g);
		assertEquals(3,g.rows);
		assertEquals(3,g.columns);
		assertTrue(original.get(0) == g.get(0,0));

		alg.rotateGridCCW(g);
		alg.putGridIntoCanonical(g);
		assertTrue(original.get(0) == g.get(0,0));

		alg.rotateGridCCW(g);
		alg.rotateGridCCW(g);
		alg.putGridIntoCanonical(g);
		assertTrue(original.get(0) == g.get(0,0));

		alg.rotateGridCCW(g);
		alg.rotateGridCCW(g);
		alg.rotateGridCCW(g);
		alg.putGridIntoCanonical(g);
		assertTrue(original.get(0) == g.get(0,0));
	}

	@Test
	public void closestCorner4() {
		Grid g = new Grid();

		g.rows = 3;
		g.columns = 3;

		g.ellipses.add(new EllipseRotated_F64(20,20, 0,0,0));
		g.ellipses.add(null);
		g.ellipses.add(new EllipseRotated_F64(20,100, 0,0,0));

		g.ellipses.add(null);
		g.ellipses.add(new EllipseRotated_F64());
		g.ellipses.add(null);

		g.ellipses.add(new EllipseRotated_F64(100,20, 0,0,0));
		g.ellipses.add(null);
		g.ellipses.add(new EllipseRotated_F64(100,100, 0,0,0));

		assertEquals(0, DetectAsymmetricCircleGrid.closestCorner4(g));

		g.ellipses.get(0).center.set(20,100);
		g.ellipses.get(2).center.set(100,20);
		g.ellipses.get(6).center.set(100,100);
		g.ellipses.get(8).center.set(20,20);
		assertEquals(2, DetectAsymmetricCircleGrid.closestCorner4(g));

		g.ellipses.get(0).center.set(100,20);
		g.ellipses.get(2).center.set(100,100);
		g.ellipses.get(6).center.set(20,20);
		g.ellipses.get(8).center.set(20,100);
		assertEquals(1, DetectAsymmetricCircleGrid.closestCorner4(g));

		g.ellipses.get(0).center.set(100,100);
		g.ellipses.get(2).center.set(20,20);
		g.ellipses.get(6).center.set(20,100);
		g.ellipses.get(8).center.set(100,20);
		assertEquals(3, DetectAsymmetricCircleGrid.closestCorner4(g));
	}

	private Grid createGrid(int numRows, int numCols) {
		Grid g = new Grid();

		g.rows = numRows;
		g.columns = numCols;

		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				if( i%2 == 0 ) {
					if( j%2 == 0 )
						g.ellipses.add(new EllipseRotated_F64(i*20,j*20, 0,0,0));
					else
						g.ellipses.add(null);
				} else {
					if( j%2 == 0 )
						g.ellipses.add(null);
					else
						g.ellipses.add(new EllipseRotated_F64(i*20,j*20, 0,0,0));
				}
			}
		}

		return g;
	}

	@Test
	public void rotateGridCCW() {
		Grid g = createGrid(3,3);
		List<EllipseRotated_F64> original = new ArrayList<>();
		original.addAll(g.ellipses);

		DetectAsymmetricCircleGrid<?> alg = new DetectAsymmetricCircleGrid<>(3,3,null,null,null);

		alg.rotateGridCCW(g);
		assertEquals(9,g.ellipses.size());
		assertTrue( original.get(6) == g.get(0,0));
		assertTrue( original.get(0) == g.get(0,2));
		assertTrue( original.get(2) == g.get(2,2));
		assertTrue( original.get(8) == g.get(2,0));

	}

	private Grid flipHorizontal( Grid g ) {
		Grid out = new Grid();

		for (int i = 0; i < g.rows; i++) {
			for (int j = 0; j < g.columns; j++) {
				out.ellipses.add( g.get(i,g.columns-j-1) );
			}
		}

		out.columns = g.columns;
		out.rows = g.rows;

		return out;
	}

	private Grid flipVertical( Grid g ) {
		Grid out = new Grid();

		for (int i = 0; i < g.rows; i++) {
			for (int j = 0; j < g.columns; j++) {
				out.ellipses.add( g.get(g.rows-i-1,j) );
			}
		}

		out.columns = g.columns;
		out.rows = g.rows;

		return out;
	}

	public static GrayU8 render(int rows , int cols , double radius , Affine2D_F64 affine,
								List<Point2D_F64> locations )
	{
		BufferedImage buffered = new BufferedImage(400,350, BufferedImage.TYPE_INT_BGR);
		Graphics2D g2 = buffered.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0,0,buffered.getWidth(),buffered.getHeight());
		g2.setColor(Color.BLACK);

		locations.clear();

		for (int row = 0; row < rows; row++) {
			double y = row*radius*2;
			for (int col = 0; col < cols; col++) {
				double x = col*radius*2;

				if( row%2 == 1 && col%2 ==0 )
					continue;
				if( row%2 == 0 && col%2 ==1 )
					continue;

				double xx = affine.a11*x + affine.a12*y + affine.tx;
				double yy = affine.a21*x + affine.a22*y + affine.ty;

				g2.fillOval((int)(xx-radius+0.5),(int)(yy-radius+0.5),(int)(radius*2),(int)(radius*2));

				locations.add( new Point2D_F64(xx,yy));
			}
		}

		GrayU8 ret = new GrayU8(buffered.getWidth(), buffered.getHeight());
		ConvertBufferedImage.convertFrom(buffered, ret);

		return ret;
	}
}