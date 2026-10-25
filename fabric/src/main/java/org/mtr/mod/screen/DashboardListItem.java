package org.mtr.mod.screen;

public class DashboardListItem implements Comparable<DashboardListItem> {

	public final long id;
	private final String name;
	private final int color;

	public DashboardListItem(long id, String name, int color) {
		this.id = id;
		this.name = name;
		this.color = color;
	}

	public String getName(boolean formatted) {
		return name;
	}

	public int getColor(boolean formatted) {
		return color;
	}

	@Override
	public int compareTo(DashboardListItem compare) {
		return (name + color + id).compareTo(compare.name + compare.color + compare.id);
	}
}
