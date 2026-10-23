package org.mtr.mod.client;

import com.logisticscraft.occlusionculling.util.Vec3d;
import org.mtr.core.data.*;
import org.mtr.core.data.Position;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.Long2ObjectAVLTreeMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import org.mtr.libraries.it.unimi.dsi.fastutil.longs.LongArrayList;
import org.mtr.libraries.it.unimi.dsi.fastutil.objects.*;
import org.mtr.mapping.holder.*;
import org.mtr.mapping.mapper.EntityHelper;
import org.mtr.mod.Init;
import org.mtr.mod.data.PersistentVehicleData;
import org.mtr.mod.data.VehicleExtension;
import org.mtr.mod.screen.DashboardListItem;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MinecraftClientData extends ClientData {

	public final Long2ObjectOpenHashMap<SimplifiedRoute> simplifiedRouteIdMap = new Long2ObjectOpenHashMap<>();
	public final ObjectArraySet<VehicleExtension> vehicles = new ObjectArraySet<>();
	public final Long2ObjectAVLTreeMap<PersistentVehicleData> vehicleIdToPersistentVehicleData = new Long2ObjectAVLTreeMap<>();
	public final Long2ObjectAVLTreeMap<LiftWrapper> liftWrapperList = new Long2ObjectAVLTreeMap<>();
	public final Object2ObjectArrayMap<String, RailWrapper> railWrapperList = new Object2ObjectArrayMap<>();
	public final Object2ObjectAVLTreeMap<String, LongArrayList> railIdToPreBlockedSignalColors = new Object2ObjectAVLTreeMap<>();
	public final Object2ObjectAVLTreeMap<String, LongArrayList> railIdToCurrentlyBlockedSignalColors = new Object2ObjectAVLTreeMap<>();
	public final ObjectArraySet<String> blockedRailIds = new ObjectArraySet<>();
	public final ObjectArrayList<DashboardListItem> railActions = new ObjectArrayList<>();

	private final LongAVLTreeSet routeIdsWithDisabledAnnouncements = new LongAVLTreeSet();

	private static MinecraftClientData instance = new MinecraftClientData();
	private static MinecraftClientData dashboardInstance = new MinecraftClientData();

	public static String DASHBOARD_SEARCH = "";
	public static String ROUTES_PLATFORMS_SEARCH = "";
	public static String ROUTES_PLATFORMS_SELECTED_SEARCH = "";
	public static String TRAINS_SEARCH = "";
	public static String EXIT_PARENTS_SEARCH = "";
	public static String EXIT_DESTINATIONS_SEARCH = "";

	private static boolean pressingAccelerate = false;
	private static boolean pressingBrake = false;
	private static boolean pressingDoors = false;

	@Override
	public void sync() {
		super.sync();
		checkAndRemoveFromMap(vehicleIdToPersistentVehicleData, vehicles, NameColorDataBase::getId);

		checkAndRemoveFromMap(liftWrapperList, lifts, Lift::getId);
		lifts.forEach(lift -> {
			final LiftWrapper liftWrapper = liftWrapperList.get(lift.getId());
			if (liftWrapper == null) {
				liftWrapperList.put(lift.getId(), new LiftWrapper(lift));
			} else {
				liftWrapper.lift = lift;
			}
		});

		checkAndRemoveFromMap(railWrapperList, rails, Rail::getHexId);
		positionsToRail.forEach((startPosition, railMap) -> railMap.forEach((endPosition, rail) -> {
			final String hexId = rail.getHexId();
			final RailWrapper railWrapper = railWrapperList.get(hexId);
			if (railWrapper == null) {
				railWrapperList.put(hexId, new RailWrapper(rail, hexId));
			} else {
				railWrapper.rail = rail;
			}
		}));

		simplifiedRoutes.forEach(simplifiedRoute -> simplifiedRouteIdMap.put(simplifiedRoute.getId(), simplifiedRoute));
	}

	@Nullable
	public ObjectObjectImmutablePair<Rail, BlockPos> getFacingRailAndBlockPos(boolean includeCableType) {
		return null;
	}

	public boolean getRouteIdHasDisabledAnnouncements(long routeId) {
		return routeIdsWithDisabledAnnouncements.contains(routeId);
	}

	public void setRouteIdHasDisabledAnnouncements(long routeId, boolean isDisabled) {
		if (isDisabled) {
			routeIdsWithDisabledAnnouncements.add(routeId);
		} else {
			routeIdsWithDisabledAnnouncements.remove(routeId);
		}
	}

	public static MinecraftClientData getInstance() {
		return instance;
	}

	public static MinecraftClientData getDashboardInstance() {
		return dashboardInstance;
	}

	public static void reset() {
		MinecraftClientData.instance = new MinecraftClientData();
		MinecraftClientData.dashboardInstance = new MinecraftClientData();
	}

	public static boolean hasPermission() {
		final ClientPlayerEntity player = MinecraftClient.getInstance().getPlayerMapped();
		if (player == null) {
			return false;
		}
		final ClientPlayNetworkHandler clientPlayNetworkHandler = MinecraftClient.getInstance().getNetworkHandler();
		if (clientPlayNetworkHandler == null) {
			return false;
		}
		final PlayerListEntry playerListEntry = clientPlayNetworkHandler.getPlayerListEntry(player.getUuid());
		if (playerListEntry == null) {
			return false;
		}
		final GameMode gameMode = playerListEntry.getGameMode();
		return gameMode == GameMode.getCreativeMapped() || gameMode == GameMode.getSurvivalMapped();
	}

	@Nullable
	public static Lift getLift(long liftId) {
		// Don't use liftIdMap
		for (final Lift lift : MinecraftClientData.getInstance().lifts) {
			if (lift.getId() == liftId) {
				return lift;
			}
		}
		return null;
	}

	public static <T extends NameColorDataBase> ObjectArraySet<DashboardListItem> getFilteredDataSet(TransportMode transportMode, ObjectArraySet<T> dataSet) {
		return convertDataSet(dataSet.stream().filter(data -> data.isTransportMode(transportMode)).collect(Collectors.toCollection(ObjectArraySet::new)));
	}

	public static <T extends NameColorDataBase> ObjectArraySet<DashboardListItem> convertDataSet(ObjectArraySet<T> dataSet) {
		return dataSet.stream().map(data -> new DashboardListItem(data.getId(), data.getName(), data.getColor())).collect(Collectors.toCollection(ObjectArraySet::new));
	}

	public static <T extends AreaBase<T, U>, U extends SavedRailBase<U, T>> Object2ObjectAVLTreeMap<Position, ObjectArrayList<U>> getFlatPositionToSavedRails(ObjectArraySet<U> savedRails, TransportMode transportMode) {
		final Object2ObjectAVLTreeMap<Position, ObjectArrayList<U>> map = new Object2ObjectAVLTreeMap<>();
		savedRails.forEach(savedRail -> {
			if (savedRail.isTransportMode(transportMode)) {
				final Position position = savedRail.getMidPosition();
				Data.put(map, new Position(position.getX(), 0, position.getZ()), savedRail, ObjectArrayList::new);
			}
		});
		map.forEach((position, newSavedRails) -> newSavedRails.sort((savedRail1, savedRail2) -> {
			if (savedRail1.getId() == savedRail2.getId()) {
				return 0;
			} else {
				final long y1 = savedRail1.getMidPosition().getY();
				final long y2 = savedRail2.getMidPosition().getY();
				return y1 == y2 ? savedRail1.getId() > savedRail2.getId() ? 1 : -1 : y1 > y2 ? 1 : -1;
			}
		}));
		return map;
	}

	private static <T, U, V> void checkAndRemoveFromMap(Map<T, U> map, ObjectSet<V> dataSet, Function<V, T> getId) {
		final ObjectAVLTreeSet<T> idSet = dataSet.stream().map(getId).collect(Collectors.toCollection(ObjectAVLTreeSet::new));
		final ObjectArrayList<T> idsToRemove = new ObjectArrayList<>();
		map.keySet().forEach(id -> {
			if (!idSet.contains(id)) {
				idsToRemove.add(id);
			}
		});
		idsToRemove.forEach(map::remove);
	}

	public static class LiftWrapper {

		public boolean shouldRender;
		private Lift lift;

		private LiftWrapper(Lift lift) {
			this.lift = lift;
		}

		public Lift getLift() {
			return lift;
		}
	}

	public static class RailWrapper {

		public boolean shouldRender;
		public final String hexId;
		public final Vec3d startVector;
		public final Vec3d endVector;
		private Rail rail;

		private RailWrapper(Rail rail, String hexId) {
			this.rail = rail;
			this.hexId = hexId;
			startVector = new Vec3d(rail.railMath.minX, rail.railMath.minY, rail.railMath.minZ);
			endVector = new Vec3d(rail.railMath.maxX, rail.railMath.maxY, rail.railMath.maxZ);
		}

		public Rail getRail() {
			return rail;
		}
	}
}
