package org.mtr.mod;

import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.holder.Item;
import org.mtr.mapping.registry.ItemRegistryObject;
import org.mtr.mod.item.*;

public final class Items {

	static {
		// Brush
		BRUSH = Init.REGISTRY.registerItem(new Identifier(Init.MOD_ID, "brush"), itemSettings -> new Item(new ItemBrush(itemSettings)), CreativeModeTabs.CORE);

		// Escalators and lifts
		ESCALATOR = Init.REGISTRY.registerItem(new Identifier(Init.MOD_ID, "escalator"), itemSettings -> new Item(new ItemEscalator(itemSettings)), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_BUTTONS_LINK_CONNECTOR = Init.REGISTRY.registerItem(new Identifier(Init.MOD_ID, "lift_buttons_link_connector"), itemSettings -> new Item(new ItemLiftButtonsLinkModifier(true, itemSettings)), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_BUTTONS_LINK_REMOVER = Init.REGISTRY.registerItem(new Identifier(Init.MOD_ID, "lift_buttons_link_remover"), itemSettings -> new Item(new ItemLiftButtonsLinkModifier(false, itemSettings)), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_DOOR_1 = Init.REGISTRY.registerItem(new Identifier(Init.MOD_ID, "lift_door_1"), itemSettings -> new Item(new ItemPSDAPGBase(ItemPSDAPGBase.EnumPSDAPGItem.PSD_APG_DOOR, ItemPSDAPGBase.EnumPSDAPGType.LIFT_DOOR_1, itemSettings)), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_DOOR_ODD_1 = Init.REGISTRY.registerItem(new Identifier(Init.MOD_ID, "lift_door_odd_1"), itemSettings -> new Item(new ItemPSDAPGBase(ItemPSDAPGBase.EnumPSDAPGItem.PSD_APG_DOOR, ItemPSDAPGBase.EnumPSDAPGType.LIFT_DOOR_ODD_1, itemSettings)), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_REFRESHER = Init.REGISTRY.registerItem(new Identifier(Init.MOD_ID, "lift_refresher"), itemSettings -> new Item(new ItemLiftRefresher(itemSettings)), CreativeModeTabs.ESCALATORS_LIFTS);
	}

	public static final ItemRegistryObject BRUSH;
	public static final ItemRegistryObject ESCALATOR;
	public static final ItemRegistryObject LIFT_BUTTONS_LINK_CONNECTOR;
	public static final ItemRegistryObject LIFT_BUTTONS_LINK_REMOVER;
	public static final ItemRegistryObject LIFT_DOOR_1;
	public static final ItemRegistryObject LIFT_DOOR_ODD_1;
	public static final ItemRegistryObject LIFT_REFRESHER;

	public static void init() {
		Init.LOGGER.info("Registering Minecraft Transit Railway items");
	}
}
