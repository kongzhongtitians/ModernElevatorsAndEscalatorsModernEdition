package org.mtr.mod.screen;

import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.mapping.holder.ClickableWidget;
import org.mtr.mapping.mapper.GuiDrawing;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mod.data.IGui;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DashboardList implements IGui {

	public int x;
	public int y;
	public int width;
	public int height;

	private ObjectArrayList<DashboardListItem> data = new ObjectArrayList<>();
	private int scrollOffset = 0;
	private int hoverIndex = -1;

	@Nullable
	private final Callback onPress;

	private static final int TOP_OFFSET = SQUARE_SIZE;

	public DashboardList(@Nullable Callback onFind, @Nullable Runnable onDrawArea, @Nullable Callback onEdit,
						 @Nullable Runnable onSort, @Nullable Callback onAdd, @Nullable Callback onDelete,
						 @Nullable Supplier<?> getList, Supplier<String> getSearch, Consumer<String> setSearch) {
		this.onPress = onFind;
	}

	public void init(Consumer<ClickableWidget> addDrawableChild) {
	}

	public void tick() {
	}

	public void setData(ObjectArrayList<DashboardListItem> dataList, boolean hasFind, boolean hasDrawArea,
						boolean hasEdit, boolean hasSort, boolean hasAdd, boolean hasDelete) {
		this.data = dataList;
		scrollOffset = 0;
	}

	public void setData(org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectSet<DashboardListItem> dataSet,
						boolean hasFind, boolean hasDrawArea, boolean hasEdit, boolean hasSort, boolean hasAdd, boolean hasDelete) {
		this.data = new ObjectArrayList<>(dataSet);
		scrollOffset = 0;
	}

	public void render(GraphicsHolder graphicsHolder) {
		render(graphicsHolder, true);
	}

	public void render(GraphicsHolder graphicsHolder, boolean formatted) {
		final int itemsToShow = itemsToShow();
		for (int i = 0; i < itemsToShow; i++) {
			final int dataIndex = i + scrollOffset;
			if (dataIndex < data.size()) {
				final DashboardListItem item = data.get(dataIndex);
				final int drawY = y + TOP_OFFSET + i * SQUARE_SIZE;

				// Draw color square
				final GuiDrawing guiDrawing = new GuiDrawing(graphicsHolder);
				guiDrawing.beginDrawingRectangle();
				guiDrawing.drawRectangle(x + TEXT_PADDING, drawY, x + TEXT_PADDING + TEXT_HEIGHT, drawY + TEXT_HEIGHT,
						ARGB_BLACK | item.getColor(formatted));
				guiDrawing.finishDrawingRectangle();

				// Draw hover highlight
				if (dataIndex == hoverIndex) {
					guiDrawing.beginDrawingRectangle();
					guiDrawing.drawRectangle(x, drawY, x + width, drawY + SQUARE_SIZE, 0x55FFFFFF);
					guiDrawing.finishDrawingRectangle();
				}

				// Draw item name
				final String name = IGui.formatStationName(item.getName(formatted));
				graphicsHolder.drawText(name, x + TEXT_PADDING * 2 + TEXT_HEIGHT, drawY + 1, ARGB_WHITE, false, GraphicsHolder.getDefaultLight());
			}
		}
	}

	public void mouseMoved(double mouseX, double mouseY) {
		hoverIndex = -1;
		if (mouseX >= x && mouseX < x + width && mouseY >= y + TOP_OFFSET && mouseY < y + height) {
			final int index = (int) ((mouseY - y - TOP_OFFSET) / SQUARE_SIZE) + scrollOffset;
			if (index >= 0 && index < data.size()) {
				hoverIndex = index;
			}
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && onPress != null) {
			final int index = hoverIndex;
			if (index >= 0 && index < data.size()) {
				onPress.accept(data.get(index), index);
				return true;
			}
		}
		return false;
	}

	public void mouseScrolled(double mouseX, double mouseY, double amount) {
		if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
			final int maxScroll = Math.max(0, data.size() - itemsToShow());
			scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(amount)));
		}
	}

	private int itemsToShow() {
		return Math.max(1, (height - TOP_OFFSET) / SQUARE_SIZE);
	}

	@FunctionalInterface
	public interface Callback {
		void accept(DashboardListItem dashboardListItem, int index);
	}
}
