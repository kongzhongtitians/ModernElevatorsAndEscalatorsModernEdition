package org.mtr.mod.screen;

import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongCollection;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.ScreenExtension;
import org.mtr.mod.data.IGui;

import javax.annotation.Nullable;

public class DashboardListSelectorScreen extends MTRScreenBase implements IGui {

	protected final ObjectImmutableList<DashboardListItem> allData;
	protected final LongCollection selectedIds;
	protected final DashboardList list;
	private final boolean isSingleSelect;

	public DashboardListSelectorScreen(ObjectImmutableList<DashboardListItem> allData, LongCollection selectedIds,
									   boolean isSingleSelect, boolean canRepeat, @Nullable ScreenExtension previousScreenExtension) {
		super(previousScreenExtension);
		this.allData = allData;
		this.selectedIds = selectedIds;
		this.isSingleSelect = isSingleSelect;

		list = new DashboardList((item, index) -> {
			if (isSingleSelect) {
				selectedIds.clear();
			}
			if (!selectedIds.contains(item.id)) {
				selectedIds.add(item.id);
			}
			updateList();
		}, null, null, null, null, null, null, () -> "", text -> {});
	}

	@Override
	protected void init2() {
		super.init2();
		list.x = width / 2 - PANEL_WIDTH;
		list.y = SQUARE_SIZE;
		list.width = PANEL_WIDTH * 2;
		list.height = height - SQUARE_SIZE * 2;
		list.init(this::addChild);
		updateList();
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

	@Override
	public boolean mouseScrolled2(double mouseX, double mouseY, double amount) {
		list.mouseScrolled(mouseX, mouseY, amount);
		return super.mouseScrolled2(mouseX, mouseY, amount);
	}

	@Override
	public boolean isPauseScreen2() {
		return false;
	}

	protected void updateList() {
		final ObjectArrayList<DashboardListItem> selectedData = new ObjectArrayList<>();
		for (final long selectedId : selectedIds) {
			allData.stream().filter(data -> data.id == selectedId).findFirst().ifPresent(selectedData::add);
		}
		list.setData(selectedData, false, false, false, false, false, false);
	}
}
