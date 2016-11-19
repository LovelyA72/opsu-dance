package awlex.ospu.polymover.factory;

import awlex.ospu.polymover.ArcMover;
import awlex.ospu.polymover.LineMover;
import awlex.ospu.polymover.PolyMover;
import itdelatrisu.opsu.objects.DummyObject;
import itdelatrisu.opsu.objects.GameObject;

import java.util.Arrays;

/**
 * Created by Awlex on 18.11.2016.
 */
public class ArcFactory implements PolyMoverFactory {

	private static final int PREFFERED_BUFFER_SIZE = 3;
	private PolyMover current, previous;
	private int lastIndex;

	public double[] getPointAt(int time) {
		if (previous == null) {
			return current.getPointAt(time);
		}
		if (current == null) {
			return previous.getPointAt(time);
		}

		double[] point1 = current.getPointAt(time);
		double[] point2 = previous.getPointAt(time);

		return new double[]{
			(point1[0] + point2[0]) * 0.5,
			(point1[1] + point2[1]) * 0.5
		};
	}


	public void init(GameObject[] objects, int startIndex) {
		if (objects == null)
			throw new NullPointerException("Objects musn't be null");
		boolean b;
		if (b = startIndex == -1) {
			objects = new GameObject[]{
				new DummyObject(),
				objects[0],
				objects[1],
			};
			startIndex = 0;
		}
		GameObject middle = objects[startIndex + 1];
		if (!ArcMover.canCricleExistBetweenItems(objects[startIndex], middle, objects[startIndex + 2]))
			current = new LineMover(objects, startIndex, 3);
		else
			current = new ArcMover(objects[startIndex], middle, objects[startIndex + 2]);
		lastIndex = startIndex + 2;
		if (b)
			lastIndex--;
		previous = null;
	}

	public void update(GameObject p) {
		GameObject[] items = (previous == null ? current : previous).getItems();
		GameObject last = items[items.length - 1];
		if (last != p) {
			previous = current;
			if (ArcMover.canCricleExistBetweenItems(items[items.length - 2], last, p)) {
				current = new ArcMover(items[items.length - 2], last, p);
			}  else {
				GameObject[] lineObjects = new GameObject[PREFFERED_BUFFER_SIZE];
				System.arraycopy(items, items.length - 2, lineObjects, 0, PREFFERED_BUFFER_SIZE - 2);
				lineObjects[lineObjects.length - 1] = p;
				current = new LineMover(lineObjects, 0, PREFFERED_BUFFER_SIZE);
			}
		}
		lastIndex++;
	}

	@Override
	public int getPrefferedBufferSize() {
		return PREFFERED_BUFFER_SIZE;
	}

	@Override
	public String toString() {
		return "Arcs";
	}

	@Override
	public boolean isInitialized() {
		return current != null;
	}

	@Override
	public int getLatestIndex() {
		return lastIndex;
	}

}
