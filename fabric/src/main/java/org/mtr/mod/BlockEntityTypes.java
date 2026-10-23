package org.mtr.mod;

import org.mtr.mapping.holder.Identifier;
import org.mtr.mapping.registry.BlockEntityTypeRegistryObject;
import org.mtr.mod.block.*;

@SuppressWarnings("unchecked")
public final class BlockEntityTypes {

	static {
		LIFT_BUTTONS_1 = Init.REGISTRY.registerBlockEntityType(new Identifier(Init.MOD_ID, "lift_buttons_1"), BlockLiftButtons.BlockEntity::new, Blocks.LIFT_BUTTONS_1::get);
		LIFT_DOOR_EVEN_1 = Init.REGISTRY.registerBlockEntityType(new Identifier(Init.MOD_ID, "lift_door_1"), BlockLiftDoor.BlockEntity::new, Blocks.LIFT_DOOR_EVEN_1::get);
		LIFT_DOOR_ODD_1 = Init.REGISTRY.registerBlockEntityType(new Identifier(Init.MOD_ID, "lift_door_odd_1"), BlockLiftDoorOdd.BlockEntity::new, Blocks.LIFT_DOOR_ODD_1::get);
		LIFT_PANEL_EVEN_1 = Init.REGISTRY.registerBlockEntityType(new Identifier(Init.MOD_ID, "lift_panel_even_1"), BlockLiftPanelEven1.BlockEntity::new, Blocks.LIFT_PANEL_EVEN_1::get);
		LIFT_PANEL_EVEN_2 = Init.REGISTRY.registerBlockEntityType(new Identifier(Init.MOD_ID, "lift_panel_even_2"), BlockLiftPanelEven2.BlockEntity::new, Blocks.LIFT_PANEL_EVEN_2::get);
		LIFT_PANEL_ODD_1 = Init.REGISTRY.registerBlockEntityType(new Identifier(Init.MOD_ID, "lift_panel_odd_1"), BlockLiftPanelOdd1.BlockEntity::new, Blocks.LIFT_PANEL_ODD_1::get);
		LIFT_PANEL_ODD_2 = Init.REGISTRY.registerBlockEntityType(new Identifier(Init.MOD_ID, "lift_panel_odd_2"), BlockLiftPanelOdd2.BlockEntity::new, Blocks.LIFT_PANEL_ODD_2::get);
		LIFT_TRACK_FLOOR_1 = Init.REGISTRY.registerBlockEntityType(new Identifier(Init.MOD_ID, "lift_track_floor_1"), BlockLiftTrackFloor.BlockEntity::new, Blocks.LIFT_TRACK_FLOOR_1::get);
	}

	public static final BlockEntityTypeRegistryObject<BlockLiftButtons.BlockEntity> LIFT_BUTTONS_1;
	public static final BlockEntityTypeRegistryObject<BlockLiftDoor.BlockEntity> LIFT_DOOR_EVEN_1;
	public static final BlockEntityTypeRegistryObject<BlockLiftDoorOdd.BlockEntity> LIFT_DOOR_ODD_1;
	public static final BlockEntityTypeRegistryObject<BlockLiftPanelEven1.BlockEntity> LIFT_PANEL_EVEN_1;
	public static final BlockEntityTypeRegistryObject<BlockLiftPanelEven2.BlockEntity> LIFT_PANEL_EVEN_2;
	public static final BlockEntityTypeRegistryObject<BlockLiftPanelOdd1.BlockEntity> LIFT_PANEL_ODD_1;
	public static final BlockEntityTypeRegistryObject<BlockLiftPanelOdd2.BlockEntity> LIFT_PANEL_ODD_2;
	public static final BlockEntityTypeRegistryObject<BlockLiftTrackFloor.BlockEntity> LIFT_TRACK_FLOOR_1;

	public static void init() {
		Init.LOGGER.info("Registering Minecraft Transit Railway block entity types");
	}
}
