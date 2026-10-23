package org.mtr.mod.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.mapping.holder.GraphicsHolder;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mod.data.IGui;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DashboardList implements IGui {

	public int x, y, width, height;

	private final BiConsumer<DashboardListItem, Integer> onPress;
	private final Supplier<String> searchTextSupplier;
	private final Consumer<String> searchTextConsumer;
	private ObjectArrayList<DashboardListItem> data = new ObjectArrayList<>();
	private int scrollOffset = 0;
	private Consumer<Widget> addChild;

	public DashboardList(BiConsumer<DashboardListItem, Integer> onPress,
						 Runnable onAdd, Runnable onRemove, Runnable onEdit, Runnable onSelect,
						 ObjectArrayList<?> customButtons, Boolean showColors,
						 Supplier<String> searchTextSupplier, Consumer<String> searchTextConsumer) {
		this.onPress = onPress;
		this.searchTextSupplier = searchTextSupplier;
		this.searchTextConsumer = searchTextConsumer;
	}

	public void init(Consumer<Widget> addChild) {
		this.addChild = addChild;
	}

	public void tick() {
	}

	public void setData(ObjectArrayList<DashboardListItem> data, boolean bool1, boolean bool2, boolean bool3, boolean bool4, boolean bool5, boolean bool6) {
		this.data = data;
	}

	public void render(GraphicsHolder graphicsHolder) {
		final int rowHeight = SQUARE_SIZE * 2;
		final int maxRows = Math.max(1, height / rowHeight);
		for (int i = scrollOffset; i < Math.min(data.size(), scrollOffset + maxRows); i++) {
			final DashboardListItem item = data.get(i);
			final int rowY = y + (i - scrollOffset) * rowHeight;
			graphicsHolder.drawText(item.name, x + PADDING, rowY + PADDING, ARGB_WHITE, false);
		}
	}

	public void mouseMoved(double mouseX, double mouseY) {
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		scrollOffset = Math.max(0, scrollOffset - (int) Math.signum(amount));
		return true;
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		final int rowHeight = SQUARE_SIZE * 2;
		final int row = (int) ((mouseY - y) / rowHeight) + scrollOffset;
		if (row >= 0 && row < data.size() && mouseX >= x && mouseX < x + width) {
			onPress.accept(data.get(row), row);
			return true;
		}
		return false;
	}
}
