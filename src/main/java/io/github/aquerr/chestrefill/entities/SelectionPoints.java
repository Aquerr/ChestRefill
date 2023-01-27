package io.github.aquerr.chestrefill.entities;

import org.spongepowered.math.vector.Vector3i;

/**
 * A mapping class for two instances of {@link org.spongepowered.math.vector.Vector3i} which in this case are selected points in the world.
 */
public class SelectionPoints
{
	private Vector3i firstPoint;
	private Vector3i secondPoint;

	public SelectionPoints(final Vector3i firstPoint, final Vector3i secondPoint)
	{
		this.firstPoint = firstPoint;
		this.secondPoint = secondPoint;
	}

	public Vector3i getFirstPoint()
	{
		return this.firstPoint;
	}

	public Vector3i getSecondPoint()
	{
		return this.secondPoint;
	}

	public void setFirstPoint(final Vector3i firstPoint)
	{
		this.firstPoint = firstPoint;
	}

	public void setSecondPoint(final Vector3i secondPoint)
	{
		this.secondPoint = secondPoint;
	}
}
