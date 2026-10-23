package org.mtr.mod;

import org.apache.commons.io.FileUtils;
import org.mtr.libraries.com.google.gson.JsonParser;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.BlockExtension;
import org.mtr.mapping.mapper.BlockHelper;
import org.mtr.mapping.mapper.BlockItemExtension;
import org.mtr.mapping.registry.BlockRegistryObject;
import org.mtr.mapping.registry.CreativeModeTabHolder;
import org.mtr.mod.block.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;


public final class Blocks {

	static {
		REGISTERED_IDENTIFIERS = new ObjectAVLTreeSet<>();

		// Escalators and lifts
		ESCALATOR_SIDE = registerBlock(new Identifier(Init.MOD_ID, "escalator_side"), () -> new Block(new BlockEscalatorSide()));
		ESCALATOR_STEP = registerBlock(new Identifier(Init.MOD_ID, "escalator_step"), () -> new Block(new BlockEscalatorStep()));
		LIFT_BUTTONS_1 = registerBlockWithBlockItem(new Identifier(Init.MOD_ID, "lift_buttons_1"), () -> new Block(new BlockLiftButtons()), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_PANEL_EVEN_1 = registerBlockWithBlockItem(new Identifier(Init.MOD_ID, "lift_panel_even_1"), () -> new Block(new BlockLiftPanelEven1()), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_PANEL_ODD_1 = registerBlockWithBlockItem(new Identifier(Init.MOD_ID, "lift_panel_odd_1"), () -> new Block(new BlockLiftPanelOdd1()), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_PANEL_EVEN_2 = registerBlockWithBlockItem(new Identifier(Init.MOD_ID, "lift_panel_even_2"), () -> new Block(new BlockLiftPanelEven2()), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_PANEL_ODD_2 = registerBlockWithBlockItem(new Identifier(Init.MOD_ID, "lift_panel_odd_2"), () -> new Block(new BlockLiftPanelOdd2()), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_TRACK_HORIZONTAL_1 = registerBlockWithBlockItem(new Identifier(Init.MOD_ID, "lift_track_horizontal_1"), () -> new Block(new BlockLiftTrackHorizontal()), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_TRACK_VERTICAL_1 = registerBlockWithBlockItem(new Identifier(Init.MOD_ID, "lift_track_1"), () -> new Block(new BlockLiftTrackVertical()), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_TRACK_DIAGONAL_1 = registerBlockWithBlockItem(new Identifier(Init.MOD_ID, "lift_track_diagonal_1"), () -> new Block(new BlockLiftTrackDiagonal()), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_TRACK_FLOOR_1 = registerBlockWithBlockItem(new Identifier(Init.MOD_ID, "lift_track_floor_1"), () -> new Block(new BlockLiftTrackFloor()), CreativeModeTabs.ESCALATORS_LIFTS);
		LIFT_DOOR_EVEN_1 = registerBlock(new Identifier(Init.MOD_ID, "lift_door_1"), () -> new Block(new BlockLiftDoor()));
		LIFT_DOOR_ODD_1 = registerBlock(new Identifier(Init.MOD_ID, "lift_door_odd_1"), () -> new Block(new BlockLiftDoorOdd()));
	}

	public static final BlockRegistryObject ESCALATOR_SIDE;
	public static final BlockRegistryObject ESCALATOR_STEP;
	public static final BlockRegistryObject LIFT_BUTTONS_1;
	public static final BlockRegistryObject LIFT_DOOR_EVEN_1;
	public static final BlockRegistryObject LIFT_DOOR_ODD_1;
	public static final BlockRegistryObject LIFT_PANEL_EVEN_1;
	public static final BlockRegistryObject LIFT_PANEL_EVEN_2;
	public static final BlockRegistryObject LIFT_PANEL_ODD_1;
	public static final BlockRegistryObject LIFT_PANEL_ODD_2;
	public static final BlockRegistryObject LIFT_TRACK_HORIZONTAL_1;
	public static final BlockRegistryObject LIFT_TRACK_VERTICAL_1;
	public static final BlockRegistryObject LIFT_TRACK_DIAGONAL_1;
	public static final BlockRegistryObject LIFT_TRACK_FLOOR_1;

	private static final ObjectAVLTreeSet<String> REGISTERED_IDENTIFIERS;

	public static void init() {
		Init.LOGGER.info("Registering Minecraft Transit Railway blocks");
		if (Keys.DEBUG) {
			try {
				// Validate all registered blocks are in pickaxe.json
				final ObjectAVLTreeSet<String> expectedIdentifiers = new ObjectAVLTreeSet<>();
				JsonParser.parseString(FileUtils.readFileToString(
						MinecraftClient.getInstance().getRunDirectoryMapped().toPath().resolve("../src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json").toFile(),
						StandardCharsets.UTF_8
				)).getAsJsonObject().getAsJsonArray("values").forEach(identifier -> expectedIdentifiers.add(identifier.getAsString()));
				REGISTERED_IDENTIFIERS.forEach(identifier -> {
					if (!expectedIdentifiers.contains(identifier)) {
						Init.LOGGER.warn("Identifier missing in pickaxe.json! [{}]", identifier);
					}
				});
				expectedIdentifiers.forEach(identifier -> {
					if (!REGISTERED_IDENTIFIERS.contains(identifier)) {
						Init.LOGGER.warn("Extra identifier in pickaxe.json! [{}]", identifier);
					}
				});

				// Validate json file exists in loot_tables/blocks
				final ObjectAVLTreeSet<String> expectedFiles = new ObjectAVLTreeSet<>();
				try (final Stream<Path> stream = Files.list(MinecraftClient.getInstance().getRunDirectoryMapped().toPath().resolve("../src/main/resources/data/mtr/loot_tables/blocks"))) {
					stream.forEach(path -> expectedFiles.add("mtr:" + path.getFileName().toString().split(".json")[0]));
				}
				try (final Stream<Path> stream = Files.list(MinecraftClient.getInstance().getRunDirectoryMapped().toPath().resolve("../src/main/resources/data/mtrsteamloco/loot_tables/blocks"))) {
					stream.forEach(path -> expectedFiles.add("mtrsteamloco:" + path.getFileName().toString().split(".json")[0]));
				}
				REGISTERED_IDENTIFIERS.forEach(identifier -> {
					if (!expectedFiles.contains(identifier)) {
						Init.LOGGER.warn("Loot table file missing! [{}]", identifier);
					}
				});
				expectedFiles.forEach(identifier -> {
					if (!REGISTERED_IDENTIFIERS.contains(identifier)) {
						Init.LOGGER.warn("Extra loot table file! [{}]", identifier);
					}
				});
			} catch (Exception e) {
				Init.LOGGER.error("", e);
			}
		}
	}

	public static BlockSettings createDefaultBlockSettings(boolean blockPiston) {
		return BlockHelper.createBlockSettings(blockPiston, true).strength(3);
	}

	public static BlockSettings createDefaultBlockSettings(boolean blockPiston, ToIntFunction<BlockState> luminanceFunction) {
		return BlockHelper.createBlockSettings(blockPiston, true, luminanceFunction).strength(3);
	}

	private static BlockRegistryObject registerBlock(Identifier identifier, Supplier<Block> supplier) {
		REGISTERED_IDENTIFIERS.add(identifier.data.toString());
		return Init.REGISTRY.registerBlock(identifier, supplier);
	}

	private static BlockRegistryObject registerBlockWithBlockItem(Identifier identifier, Supplier<Block> supplier, CreativeModeTabHolder... creativeModeTabHolders) {
		REGISTERED_IDENTIFIERS.add(identifier.data.toString());
		return Init.REGISTRY.registerBlockWithBlockItem(identifier, supplier, creativeModeTabHolders);
	}

	private static BlockRegistryObject registerBlockWithBlockItem(Identifier identifier, Supplier<Block> supplier, BiFunction<Block, ItemSettings, BlockItemExtension> function, CreativeModeTabHolder... creativeModeTabHolders) {
		REGISTERED_IDENTIFIERS.add(identifier.data.toString());
		return Init.REGISTRY.registerBlockWithBlockItem(identifier, supplier, function, creativeModeTabHolders);
	}
}
