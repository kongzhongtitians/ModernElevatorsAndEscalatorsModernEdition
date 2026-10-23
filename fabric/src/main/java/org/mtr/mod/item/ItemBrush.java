package org.mtr.mod.item;

import org.mtr.mapping.mapper.ItemExtension;
import org.mtr.mapping.holder.ItemSettings;

public class ItemBrush extends ItemExtension {

	public ItemBrush(ItemSettings itemSettings) {
		super(itemSettings.maxCount(1));
	}
}
