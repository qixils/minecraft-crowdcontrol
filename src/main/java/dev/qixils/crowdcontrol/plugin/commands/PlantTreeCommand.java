package dev.qixils.crowdcontrol.plugin.commands;

import dev.qixils.crowdcontrol.plugin.CrowdControlPlugin;
import dev.qixils.crowdcontrol.plugin.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.utils.RandomUtil;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.bukkit.Axis;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class PlantTreeCommand extends ImmediateCommand {
	private final String effectName = "plant_tree";
	private final String displayName = "Plant Tree";

	public PlantTreeCommand(CrowdControlPlugin plugin) {
		super(plugin);
	}

	@Override
	protected @NotNull Response.Builder executeImmediately(@NotNull List<@NotNull Player> players, @NotNull Request request) {
		Tree tree = RandomUtil.randomElementFrom(Tree.values());
		Bukkit.getScheduler().runTask(plugin, () -> players.forEach(tree::place));
		return request.buildResponse().type(ResultType.SUCCESS).message("SUCCESS");
	}

	private enum Tree {
		OAK(new TreeBlock(Material.OAK_LOG, Axis.Y).modify(treeBlock -> treeBlock
				.thenUp(new TreeBlock(Material.OAK_LOG, Axis.Y))
				.thenUp(new TreeBlock(Material.OAK_LOG, Axis.Y))
				.addNorth(new TreeBlock(Material.OAK_LEAVES)
						.addNorth(new TreeBlock(Material.OAK_LEAVES)
								.addEast(new TreeBlock(Material.OAK_LEAVES)))
						.addEast(new TreeBlock(Material.OAK_LEAVES)
								.addEast(new TreeBlock(Material.OAK_LEAVES))))
				.addEast(new TreeBlock(Material.OAK_LEAVES)
						.addEast(new TreeBlock(Material.OAK_LEAVES)
								.addSouth(new TreeBlock(Material.OAK_LEAVES)))
						.addSouth(new TreeBlock(Material.OAK_LEAVES)
								.addSouth(new TreeBlock(Material.OAK_LEAVES))))
				.addSouth(new TreeBlock(Material.OAK_LEAVES)
						.addSouth(new TreeBlock(Material.OAK_LEAVES)
								.addWest(new TreeBlock(Material.OAK_LEAVES)))
						.addWest(new TreeBlock(Material.OAK_LEAVES)
								.addWest(new TreeBlock(Material.OAK_LEAVES))))
				.addWest(new TreeBlock(Material.OAK_LEAVES)
						.addWest(new TreeBlock(Material.OAK_LEAVES)
								.addNorth(new TreeBlock(Material.OAK_LEAVES)))
						.addNorth(new TreeBlock(Material.OAK_LEAVES)
								.addNorth(new TreeBlock(Material.OAK_LEAVES))))
				.thenUp(new TreeBlock(Material.OAK_LOG, Axis.Y))
				.addNorth(new TreeBlock(Material.OAK_LEAVES)
						.addNorth(new TreeBlock(Material.OAK_LEAVES)
								.addEast(new TreeBlock(Material.OAK_LEAVES)))
						.addEast(new TreeBlock(Material.OAK_LEAVES)
								.addEast(new TreeBlock(Material.OAK_LEAVES))))
				.addEast(new TreeBlock(Material.OAK_LEAVES)
						.addEast(new TreeBlock(Material.OAK_LEAVES)
								.addSouth(new TreeBlock(Material.OAK_LEAVES)))
						.addSouth(new TreeBlock(Material.OAK_LEAVES)
								.addSouth(new TreeBlock(Material.OAK_LEAVES))))
				.addSouth(new TreeBlock(Material.OAK_LEAVES)
						.addSouth(new TreeBlock(Material.OAK_LEAVES)
								.addWest(new TreeBlock(Material.OAK_LEAVES)))
						.addWest(new TreeBlock(Material.OAK_LEAVES)
								.addWest(new TreeBlock(Material.OAK_LEAVES))))
				.addWest(new TreeBlock(Material.OAK_LEAVES)
						.addWest(new TreeBlock(Material.OAK_LEAVES)
								.addNorth(new TreeBlock(Material.OAK_LEAVES)))
						.addNorth(new TreeBlock(Material.OAK_LEAVES)
								.addNorth(new TreeBlock(Material.OAK_LEAVES))))
				.thenUp(new TreeBlock(Material.OAK_LOG, Axis.Y))
				.addNorth(new TreeBlock(Material.OAK_LEAVES))
				.addEast(new TreeBlock(Material.OAK_LEAVES))
				.addSouth(new TreeBlock(Material.OAK_LEAVES))
				.addWest(new TreeBlock(Material.OAK_LEAVES))
				.thenUp(new TreeBlock(Material.OAK_LEAVES))
				.addNorth(new TreeBlock(Material.OAK_LEAVES))
				.addEast(new TreeBlock(Material.OAK_LEAVES))
				.addSouth(new TreeBlock(Material.OAK_LEAVES))
				.addWest(new TreeBlock(Material.OAK_LEAVES))
		)),
		BIRCH(new TreeBlock(Material.BIRCH_LOG, Axis.Y).modify(treeBlock -> treeBlock
				.thenUp(new TreeBlock(Material.BIRCH_LOG, Axis.Y))
				.thenUp(new TreeBlock(Material.BIRCH_LOG, Axis.Y))
				.addNorth(new TreeBlock(Material.BIRCH_LEAVES)
						.addNorth(new TreeBlock(Material.BIRCH_LEAVES)
								.addEast(new TreeBlock(Material.BIRCH_LEAVES)))
						.addEast(new TreeBlock(Material.BIRCH_LEAVES)
								.addEast(new TreeBlock(Material.BIRCH_LEAVES))))
				.addEast(new TreeBlock(Material.BIRCH_LEAVES)
						.addEast(new TreeBlock(Material.BIRCH_LEAVES)
								.addSouth(new TreeBlock(Material.BIRCH_LEAVES)))
						.addSouth(new TreeBlock(Material.BIRCH_LEAVES)
								.addSouth(new TreeBlock(Material.BIRCH_LEAVES))))
				.addSouth(new TreeBlock(Material.BIRCH_LEAVES)
						.addSouth(new TreeBlock(Material.BIRCH_LEAVES)
								.addWest(new TreeBlock(Material.BIRCH_LEAVES)))
						.addWest(new TreeBlock(Material.BIRCH_LEAVES)
								.addWest(new TreeBlock(Material.BIRCH_LEAVES))))
				.addWest(new TreeBlock(Material.BIRCH_LEAVES)
						.addWest(new TreeBlock(Material.BIRCH_LEAVES)
								.addNorth(new TreeBlock(Material.BIRCH_LEAVES)))
						.addNorth(new TreeBlock(Material.BIRCH_LEAVES)
								.addNorth(new TreeBlock(Material.BIRCH_LEAVES))))
				.thenUp(new TreeBlock(Material.BIRCH_LOG, Axis.Y))
				.addNorth(new TreeBlock(Material.BIRCH_LEAVES)
						.addNorth(new TreeBlock(Material.BIRCH_LEAVES)
								.addEast(new TreeBlock(Material.BIRCH_LEAVES)))
						.addEast(new TreeBlock(Material.BIRCH_LEAVES)
								.addEast(new TreeBlock(Material.BIRCH_LEAVES))))
				.addEast(new TreeBlock(Material.BIRCH_LEAVES)
						.addEast(new TreeBlock(Material.BIRCH_LEAVES)
								.addSouth(new TreeBlock(Material.BIRCH_LEAVES)))
						.addSouth(new TreeBlock(Material.BIRCH_LEAVES)
								.addSouth(new TreeBlock(Material.BIRCH_LEAVES))))
				.addSouth(new TreeBlock(Material.BIRCH_LEAVES)
						.addSouth(new TreeBlock(Material.BIRCH_LEAVES)
								.addWest(new TreeBlock(Material.BIRCH_LEAVES)))
						.addWest(new TreeBlock(Material.BIRCH_LEAVES)
								.addWest(new TreeBlock(Material.BIRCH_LEAVES))))
				.addWest(new TreeBlock(Material.BIRCH_LEAVES)
						.addWest(new TreeBlock(Material.BIRCH_LEAVES)
								.addNorth(new TreeBlock(Material.BIRCH_LEAVES)))
						.addNorth(new TreeBlock(Material.BIRCH_LEAVES)
								.addNorth(new TreeBlock(Material.BIRCH_LEAVES))))
				.thenUp(new TreeBlock(Material.BIRCH_LOG, Axis.Y))
				.addNorth(new TreeBlock(Material.BIRCH_LEAVES))
				.addEast(new TreeBlock(Material.BIRCH_LEAVES))
				.addSouth(new TreeBlock(Material.BIRCH_LEAVES))
				.addWest(new TreeBlock(Material.BIRCH_LEAVES))
				.thenUp(new TreeBlock(Material.BIRCH_LEAVES))
				.addNorth(new TreeBlock(Material.BIRCH_LEAVES))
				.addEast(new TreeBlock(Material.BIRCH_LEAVES))
				.addSouth(new TreeBlock(Material.BIRCH_LEAVES))
				.addWest(new TreeBlock(Material.BIRCH_LEAVES))
		)),
		JUNGLE(new TreeBlock(Material.JUNGLE_LOG, Axis.Y).modify(treeBlock -> treeBlock
				.thenUp(new TreeBlock(Material.JUNGLE_LOG, Axis.Y))
				.thenUp(new TreeBlock(Material.JUNGLE_LOG, Axis.Y))
				.addNorth(new TreeBlock(Material.JUNGLE_LEAVES)
						.addNorth(new TreeBlock(Material.JUNGLE_LEAVES)
								.addEast(new TreeBlock(Material.JUNGLE_LEAVES)))
						.addEast(new TreeBlock(Material.JUNGLE_LEAVES)
								.addEast(new TreeBlock(Material.JUNGLE_LEAVES))))
				.addEast(new TreeBlock(Material.JUNGLE_LEAVES)
						.addEast(new TreeBlock(Material.JUNGLE_LEAVES)
								.addSouth(new TreeBlock(Material.JUNGLE_LEAVES)))
						.addSouth(new TreeBlock(Material.JUNGLE_LEAVES)
								.addSouth(new TreeBlock(Material.JUNGLE_LEAVES))))
				.addSouth(new TreeBlock(Material.JUNGLE_LEAVES)
						.addSouth(new TreeBlock(Material.JUNGLE_LEAVES)
								.addWest(new TreeBlock(Material.JUNGLE_LEAVES)))
						.addWest(new TreeBlock(Material.JUNGLE_LEAVES)
								.addWest(new TreeBlock(Material.JUNGLE_LEAVES))))
				.addWest(new TreeBlock(Material.JUNGLE_LEAVES)
						.addWest(new TreeBlock(Material.JUNGLE_LEAVES)
								.addNorth(new TreeBlock(Material.JUNGLE_LEAVES)))
						.addNorth(new TreeBlock(Material.JUNGLE_LEAVES)
								.addNorth(new TreeBlock(Material.JUNGLE_LEAVES))))
				.thenUp(new TreeBlock(Material.JUNGLE_LOG, Axis.Y))
				.addNorth(new TreeBlock(Material.JUNGLE_LEAVES)
						.addNorth(new TreeBlock(Material.JUNGLE_LEAVES)
								.addEast(new TreeBlock(Material.JUNGLE_LEAVES)))
						.addEast(new TreeBlock(Material.JUNGLE_LEAVES)
								.addEast(new TreeBlock(Material.JUNGLE_LEAVES))))
				.addEast(new TreeBlock(Material.JUNGLE_LEAVES)
						.addEast(new TreeBlock(Material.JUNGLE_LEAVES)
								.addSouth(new TreeBlock(Material.JUNGLE_LEAVES)))
						.addSouth(new TreeBlock(Material.JUNGLE_LEAVES)
								.addSouth(new TreeBlock(Material.JUNGLE_LEAVES))))
				.addSouth(new TreeBlock(Material.JUNGLE_LEAVES)
						.addSouth(new TreeBlock(Material.JUNGLE_LEAVES)
								.addWest(new TreeBlock(Material.JUNGLE_LEAVES)))
						.addWest(new TreeBlock(Material.JUNGLE_LEAVES)
								.addWest(new TreeBlock(Material.JUNGLE_LEAVES))))
				.addWest(new TreeBlock(Material.JUNGLE_LEAVES)
						.addWest(new TreeBlock(Material.JUNGLE_LEAVES)
								.addNorth(new TreeBlock(Material.JUNGLE_LEAVES)))
						.addNorth(new TreeBlock(Material.JUNGLE_LEAVES)
								.addNorth(new TreeBlock(Material.JUNGLE_LEAVES))))
				.thenUp(new TreeBlock(Material.JUNGLE_LOG, Axis.Y))
				.addNorth(new TreeBlock(Material.JUNGLE_LEAVES))
				.addEast(new TreeBlock(Material.JUNGLE_LEAVES))
				.addSouth(new TreeBlock(Material.JUNGLE_LEAVES))
				.addWest(new TreeBlock(Material.JUNGLE_LEAVES))
				.thenUp(new TreeBlock(Material.JUNGLE_LEAVES))
				.addNorth(new TreeBlock(Material.JUNGLE_LEAVES))
				.addEast(new TreeBlock(Material.JUNGLE_LEAVES))
				.addSouth(new TreeBlock(Material.JUNGLE_LEAVES))
				.addWest(new TreeBlock(Material.JUNGLE_LEAVES))
		)),
		;

		private final TreeBlock treeBlock;

		Tree(TreeBlock treeBlock) {
			this.treeBlock = treeBlock;
		}

		/**
		 * Places this block and its children onto an entity.
		 * <p>
		 * <b>WARNING:</b> Must be run synchronously!
		 * @param entity entity to place the block on
		 */
		public void place(@NotNull Entity entity) {
			treeBlock.place(entity.getLocation());
		}

		/**
		 * Places this block and its children into the world.
		 * <p>
		 * <b>WARNING:</b> Must be run synchronously!
		 * @param location location to place the block
		 */
		public void place(@NotNull Location location) {
			treeBlock.place(location);
		}
	}

	// wow, uh, my code is kinda overkill isn't it
	private static final class TreeBlock {
		private final @NotNull Material blockType;
		private final @Nullable Axis woodAxis;
		private final Map<BlockFace, TreeBlock> children = new EnumMap<>(BlockFace.class);

		private TreeBlock(@NotNull Material block) {
			this.blockType = block;
			this.woodAxis = null;
		}

		private TreeBlock(@NotNull Material block, @Nullable Axis woodAxis) {
			this.blockType = block;
			this.woodAxis = woodAxis;
		}

		/**
		 * Adds a child and returns that child
		 */
		@Contract("_, _ -> param2")
		@NotNull
		public TreeBlock then(@NotNull BlockFace face, @NotNull TreeBlock child) {
			if (children.containsKey(face))
				throw new IllegalArgumentException(face.name() + " has already been set");
			children.put(face, child);
			return child;
		}

		/**
		 * Adds a child and returns the current TreeBlock
		 */
		@Contract("_, _ -> this")
		@NotNull
		public TreeBlock add(@NotNull BlockFace face, @NotNull TreeBlock child) {
			then(face, child);
			return this;
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenUp(@NotNull TreeBlock child) {
			return then(BlockFace.UP, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addUp(@NotNull TreeBlock child) {
			return add(BlockFace.UP, child);
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenDown(@NotNull TreeBlock child) {
			return then(BlockFace.DOWN, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addDown(@NotNull TreeBlock child) {
			return add(BlockFace.DOWN, child);
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenNorth(@NotNull TreeBlock child) {
			return then(BlockFace.NORTH, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addNorth(@NotNull TreeBlock child) {
			return add(BlockFace.NORTH, child);
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenEast(@NotNull TreeBlock child) {
			return then(BlockFace.EAST, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addEast(@NotNull TreeBlock child) {
			return add(BlockFace.EAST, child);
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenSouth(@NotNull TreeBlock child) {
			return then(BlockFace.SOUTH, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addSouth(@NotNull TreeBlock child) {
			return add(BlockFace.SOUTH, child);
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenWest(@NotNull TreeBlock child) {
			return then(BlockFace.WEST, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addWest(@NotNull TreeBlock child) {
			return add(BlockFace.WEST, child);
		}

		/**
		 * Places this block and its children into the world.
		 * <p>
		 * <b>WARNING:</b> Must be run synchronously!
		 * @param location location to place the block
		 */
		public void place(@NotNull Location location) {
			Block block = location.getBlock();
			block.setType(blockType);
			if (woodAxis != null && block.getBlockData() instanceof Orientable orientable)
				orientable.setAxis(woodAxis);
			children.forEach((blockFace, treeBlock) -> treeBlock.place(location.clone().add(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ())));
		}

		/**
		 * Allows for easily building tree blocks by modifying this object and then returning itself
		 * @param selfConsumer function to modify this tree block (and its children)
		 * @return this tree block
		 */
		@Contract("_ -> this")
		@NotNull
		public TreeBlock modify(@NotNull Consumer<TreeBlock> selfConsumer) {
			selfConsumer.accept(this);
			return this;
		}
	}
}
