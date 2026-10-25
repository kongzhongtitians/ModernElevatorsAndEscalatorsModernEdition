package org.mtr.mod.screen;

import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.ScreenExtension;
import org.mtr.mod.data.IGui;

public class DashboardListSelectorScreen extends MTRScreenBase implements IGui {

	protected final LongArrayList selectedIds;
	protected final DashboardList list;

	public DashboardListSelectorScreen(ObjectImmutableList<DashboardListItem> items, LongArrayList selectedIds, boolean multiSelect, boolean showColors, ScreenExtension previousScreenExtension) {
		super(previousScreenExtension);
		this.selectedIds = selectedIds;
		this.list = new DashboardList((item, index) -> {
			if (multiSelect) {
				if (selectedIds.contains(index)) {
					selectedIds.rem(index);
				} else {
					selectedIds.add(index);
				}
			} else {
				selectedIds.clear();
				selectedIds.add(index);
			}
			updateList();
		}, null, null, null, null, null, showColors, () -> "", text -> {});
	}

	@Override
	protected void init2() {
		super.init2();
		list.x = width / 2 - PANEL_WIDTH;
		list.y = SQUARE_SIZE;
		list.width = PANEL_WIDTH * 2;
		list.height = height - SQUARE_SIZE * 2;
		list.init();
	}

	@Override
	public void render(GraphicsHolder graphicsHolder, int mouseX, int mouseY, float delta) {
		renderBackground(graphicsHolder);
		list.render(graphicsHolder);
		super.render(graphicsHolder, mouseX, mouseY, delta);
	}

	@Override
	public void tick2() {
		list.tick();
	}

	protected void updateList() {
	}
}
