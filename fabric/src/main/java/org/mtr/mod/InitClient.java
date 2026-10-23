package org.mtr.mod;

import org.mtr.core.data.Depot;
import org.mtr.core.data.Platform;
import org.mtr.core.data.Position;
import org.mtr.core.data.Station;
import org.mtr.core.operation.DataRequest;
import org.mtr.core.servlet.WebServlet;
import org.mtr.core.servlet.Webserver;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.mtr.libraries.javax.servlet.MultipartConfigElement;
import org.mtr.libraries.org.eclipse.jetty.servlet.ServletHolder;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.GraphicsHolder;
import org.mtr.mapping.mapper.MinecraftClientHelper;
import org.mtr.mapping.registry.RegistryClient;
import org.mtr.mod.client.CustomResourceLoader;
import org.mtr.mod.client.DynamicTextureCache;
import org.mtr.mod.client.MinecraftClientData;
import org.mtr.mod.config.Config;
import org.mtr.mod.entity.EntityRendering;
import org.mtr.mod.generated.WebserverResources;
import org.mtr.mod.item.ItemBlockClickingBase;
import org.mtr.mod.packet.PacketRequestData;
import org.mtr.mod.render.*;
import org.mtr.mod.resource.CachedResource;
import org.mtr.mod.screen.BetaWarningScreen;
import org.mtr.mod.servlet.ClientServlet;
import org.mtr.mod.servlet.ResourcePackCreatorOperationServlet;
import org.mtr.mod.servlet.ResourcePackCreatorUploadServlet;
import org.mtr.mod.sound.ScheduledSound;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;

public final class InitClient {

	private static Webserver webserver;
	private static int serverPort;
	private static long lastMillis = 0;
	private static long gameMillis = 0;
	private static long lastUpdatePacketMillis = 0;
	private static Runnable movePlayer;
	private static ClientWorld lastClientWorld;

	public static final RegistryClient REGISTRY_CLIENT = new RegistryClient(Init.REGISTRY);

	static {
		Init.createWebserverSetup(InitClient::setupWebserver);
	}

	public static void init() {
		KeyBindings.init();
		Init.writeFromClient();

		REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.LIFT_DOOR_EVEN_1);
		REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.LIFT_DOOR_ODD_1);
		REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.LIFT_PANEL_EVEN_1);
		REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.LIFT_PANEL_ODD_1);
		REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.LIFT_PANEL_EVEN_2);
		REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.LIFT_PANEL_ODD_2);
		REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.ESCALATOR_STEP);
		REGISTRY_CLIENT.registerBlockRenderType(RenderLayer.getCutout(), Blocks.ESCALATOR_SIDE);

		REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.LIFT_BUTTONS_1, RenderLiftButtons::new);
		REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.LIFT_PANEL_EVEN_1, dispatcher -> new RenderLiftPanel<>(dispatcher, false, false));
		REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.LIFT_PANEL_ODD_1, dispatcher -> new RenderLiftPanel<>(dispatcher, true, false));
		REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.LIFT_PANEL_EVEN_2, dispatcher -> new RenderLiftPanel<>(dispatcher, false, true));
		REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.LIFT_PANEL_ODD_2, dispatcher -> new RenderLiftPanel<>(dispatcher, true, true));
		REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.LIFT_DOOR_EVEN_1, dispatcher -> new RenderPSDAPGDoor<>(dispatcher, 3));
		REGISTRY_CLIENT.registerBlockEntityRenderer(BlockEntityTypes.LIFT_DOOR_ODD_1, dispatcher -> new RenderPSDAPGDoor<>(dispatcher, 4));

		REGISTRY_CLIENT.registerEntityRenderer(EntityTypes.RENDERING, MainRenderer::new);

		REGISTRY_CLIENT.registerItemModelPredicate(Items.LIFT_BUTTONS_LINK_CONNECTOR, new Identifier(Init.MOD_ID, "selected"), checkItemPredicateTag());
		REGISTRY_CLIENT.registerItemModelPredicate(Items.LIFT_BUTTONS_LINK_REMOVER, new Identifier(Init.MOD_ID, "selected"), checkItemPredicateTag());

		REGISTRY_CLIENT.setupPackets(new Identifier(Init.MOD_ID, "packet"));

		REGISTRY_CLIENT.eventRegistryClient.registerClientJoin(() -> {
			MinecraftClientData.reset();
			DynamicTextureCache.instance = new DynamicTextureCache();
			lastMillis = System.currentTimeMillis();
			gameMillis = 0;
			lastUpdatePacketMillis = 0;
			DynamicTextureCache.instance.refresh();

			// Clientside webserver for locally hosting the online system map
			// Only start clientside webserver if not in singleplayer
			if (Init.getServerPort() == 0) {
				serverPort = Init.findFreePort(0);
				webserver = new Webserver(serverPort);
				webserver.addServlet(new ServletHolder(new ClientServlet()), "/");
				setupWebserver(webserver);
				webserver.start();
			} else {
				serverPort = Math.max(Init.getServerPort(), 0);
			}
			if (serverPort > 0) {
				Init.LOGGER.info("Open the Transport System Map at http://localhost:{}", serverPort);
				Init.LOGGER.info("Open the Resource Pack Creator at http://localhost:{}/creator/", serverPort);
			} else {
				Init.LOGGER.info("Transport System Map and Resource Pack Creator disabled");
			}
		});

		REGISTRY_CLIENT.eventRegistryClient.registerClientDisconnect(() -> {
			if (webserver != null) {
				webserver.stop();
				webserver = null;
			}
			serverPort = 0;
		});

		REGISTRY_CLIENT.eventRegistryClient.registerStartClientTick(() -> {
			final long currentMillis = System.currentTimeMillis();
			final long millisElapsed = currentMillis - lastMillis;
			lastMillis = currentMillis;
			gameMillis += millisElapsed;
			CachedResource.tick();
			BetaWarningScreen.handle();

			final ClientWorld clientWorld = MinecraftClient.getInstance().getWorldMapped();
			if (clientWorld != null) {
				final boolean[] shouldCreateEntity = {true};
				MinecraftClientHelper.getEntities(entity -> {
					if (entity.data instanceof EntityRendering) {
						shouldCreateEntity[0] = false;
						((EntityRendering) entity.data).update();
					}
				});
				if (shouldCreateEntity[0]) {
					MinecraftClientHelper.addEntity(new EntityRendering(new World(clientWorld.data)));
				}

				// If world or dimension changed, reset the data
				if (lastClientWorld == null || !lastClientWorld.equals(clientWorld)) {
					lastClientWorld = clientWorld;
					MinecraftClientData.reset();
				}
			}

			ResourcePackCreatorOperationServlet.tick(millisElapsed);

			// If player is moving, send a request every 0.5 seconds to the server to fetch any new nearby data
			final ClientPlayerEntity clientPlayerEntity = MinecraftClient.getInstance().getPlayerMapped();
			if (clientPlayerEntity != null && lastUpdatePacketMillis > 0 && getGameMillis() > lastUpdatePacketMillis) {
				final DataRequest dataRequest = new DataRequest(clientPlayerEntity.getUuid(), Init.blockPosToPosition(MinecraftClient.getInstance().getGameRendererMapped().getCamera().getBlockPos()), MinecraftClientHelper.getRenderDistance() * 16L);
				dataRequest.writeExistingIds(MinecraftClientData.getInstance());
				InitClient.REGISTRY_CLIENT.sendPacketToServer(new PacketRequestData(dataRequest));
				lastUpdatePacketMillis = 0;
			}
		});

		REGISTRY_CLIENT.eventRegistryClient.registerEndClientTick(() -> {
			if (movePlayer != null) {
				movePlayer.run();
				movePlayer = null;
			}
			ScheduledSound.playScheduledSounds();
		});

		REGISTRY_CLIENT.eventRegistryClient.registerChunkLoad((clientWorld, worldChunk) -> {
			if (lastUpdatePacketMillis == 0) {
				lastUpdatePacketMillis = getGameMillis() + 500;
			}
		});

		REGISTRY_CLIENT.eventRegistryClient.registerResourceReloadEvent(CustomResourceLoader::reload);

		Config.init(MinecraftClient.getInstance().getRunDirectoryMapped());

		// Finish registration
		REGISTRY_CLIENT.init();
	}

	public static void scheduleMovePlayer(Runnable movePlayer) {
		InitClient.movePlayer = movePlayer;
	}

	public static Station findStation(BlockPos blockPos) {
		return MinecraftClientData.getInstance().stations.stream().filter(station -> station.inArea(Init.blockPosToPosition(blockPos))).findFirst().orElse(null);
	}

	public static void findClosePlatform(BlockPos blockPos, int radius, Consumer<Platform> consumer) {
		final Position position = Init.blockPosToPosition(blockPos);
		MinecraftClientData.getInstance().platforms.stream().filter(platform -> platform.closeTo(Init.blockPosToPosition(blockPos), radius)).min(Comparator.comparingDouble(platform -> platform.getApproximateClosestDistance(position, MinecraftClientData.getInstance()))).ifPresent(consumer);
	}

	@Nullable
	public static Depot findDepot(BlockPos blockPos) {
		final Position position = Init.blockPosToPosition(blockPos);
		for (final Depot depot : MinecraftClientData.getInstance().depots) {
			if (depot.inArea(position)) {
				return depot;
			}
		}
		return null;
	}

	public static void transformToFacePlayer(GraphicsHolder graphicsHolder, double x, double y, double z) {
		final Vector3d cameraPosition = MinecraftClient.getInstance().getGameRendererMapped().getCamera().getPos();
		final double differenceX = cameraPosition.getXMapped() - x;
		final double differenceY = cameraPosition.getYMapped() - y;
		final double differenceZ = cameraPosition.getZMapped() - z;
		graphicsHolder.rotateYRadians((float) (Math.atan2(differenceX, differenceZ) + Math.PI));
		graphicsHolder.rotateXRadians((float) Math.atan2(differenceY, Math.sqrt(differenceZ * differenceZ + differenceX * differenceX)));
	}

	public static String getShiftText() {
		return MinecraftClient.getInstance().getOptionsMapped().getKeySneakMapped().getBoundKeyLocalizedText().getString();
	}

	public static String getRightClickText() {
		return MinecraftClient.getInstance().getOptionsMapped().getKeyUseMapped().getBoundKeyLocalizedText().getString();
	}

	public static float getGameTick() {
		return gameMillis / 50F;
	}

	public static long getGameMillis() {
		return gameMillis;
	}

	/**
	 * @return the port of the clientside webserver (multiplayer) or the webserver started by Transport Simulation Core (singleplayer).
	 * <br>{@code 0} means the webserver is not running
	 */
	public static int getServerPort() {
		return serverPort;
	}

	private static RegistryClient.ModelPredicateProvider checkItemPredicateTag() {
		return (itemStack, clientWorld, livingEntity) -> itemStack.getOrCreateTag().contains(ItemBlockClickingBase.TAG_POS) ? 1 : 0;
	}

	private static void setupWebserver(Webserver webserver) {
		webserver.addServlet(new ServletHolder(new ResourcePackCretorWebServlet(WebserverResources::get, "/creator/")), "/creator/*");
		webserver.addServlet(new ServletHolder(new ResourcePackCreatorOperationServlet()), "/mtr/api/creator/operation/*");
		final ServletHolder resourcePackCreatorUploadServletHolder = new ServletHolder(new ResourcePackCreatorUploadServlet());
		resourcePackCreatorUploadServletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement((String) null));
		webserver.addServlet(resourcePackCreatorUploadServletHolder, "/mtr/api/creator/upload/*");
	}

	private static class ResourcePackCretorWebServlet extends WebServlet {

		public ResourcePackCretorWebServlet(Function<String, String> contentProvider, String expectedPath) {
			super(contentProvider, expectedPath);
		}
	}
}
