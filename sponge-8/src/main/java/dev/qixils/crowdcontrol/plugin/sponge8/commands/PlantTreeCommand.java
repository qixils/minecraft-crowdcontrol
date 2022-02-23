package dev.qixils.crowdcontrol.plugin.sponge8.commands;

import dev.qixils.crowdcontrol.common.util.RandomUtil;
import dev.qixils.crowdcontrol.plugin.sponge8.ImmediateCommand;
import dev.qixils.crowdcontrol.plugin.sponge8.SpongeCrowdControlPlugin;
import dev.qixils.crowdcontrol.socket.Request;
import dev.qixils.crowdcontrol.socket.Response.Builder;
import dev.qixils.crowdcontrol.socket.Response.ResultType;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class PlantTreeCommand extends ImmediateCommand {
	private final String effectName = "plant_tree";
	private final String displayName = "Plant Tree";

	public PlantTreeCommand(SpongeCrowdControlPlugin plugin) {
		super(plugin);
	}

	@NotNull
	@Override
	public Builder executeImmediately(@NotNull List<@NotNull ServerPlayer> players, @NotNull Request request) {
		// TODO: mixin impl
		Tree tree = RandomUtil.randomElementFrom(Tree.values());
		sync(() -> players.forEach(tree::place));
		return request.buildResponse().type(ResultType.SUCCESS);
	}

	private enum Tree {
		OAK(new TreeBlock(BlockTypes.OAK_LOG, Axis.Y).modify(treeBlock -> treeBlock
				.thenUp(new TreeBlock(BlockTypes.OAK_LOG, Axis.Y))
				.thenUp(new TreeBlock(BlockTypes.OAK_LOG, Axis.Y))
				.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES)
						.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addEast(new TreeBlock(BlockTypes.OAK_LEAVES)))
						.addEast(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addEast(new TreeBlock(BlockTypes.OAK_LEAVES))))
				.addEast(new TreeBlock(BlockTypes.OAK_LEAVES)
						.addEast(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES)))
						.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES))))
				.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES)
						.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addWest(new TreeBlock(BlockTypes.OAK_LEAVES)))
						.addWest(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addWest(new TreeBlock(BlockTypes.OAK_LEAVES))))
				.addWest(new TreeBlock(BlockTypes.OAK_LEAVES)
						.addWest(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES)))
						.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES))))
				.thenUp(new TreeBlock(BlockTypes.OAK_LOG, Axis.Y))
				.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES)
						.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addEast(new TreeBlock(BlockTypes.OAK_LEAVES)))
						.addEast(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addEast(new TreeBlock(BlockTypes.OAK_LEAVES))))
				.addEast(new TreeBlock(BlockTypes.OAK_LEAVES)
						.addEast(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES)))
						.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES))))
				.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES)
						.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addWest(new TreeBlock(BlockTypes.OAK_LEAVES)))
						.addWest(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addWest(new TreeBlock(BlockTypes.OAK_LEAVES))))
				.addWest(new TreeBlock(BlockTypes.OAK_LEAVES)
						.addWest(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES)))
						.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES))))
				.thenUp(new TreeBlock(BlockTypes.OAK_LOG, Axis.Y))
				.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES))
				.addEast(new TreeBlock(BlockTypes.OAK_LEAVES))
				.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES))
				.addWest(new TreeBlock(BlockTypes.OAK_LEAVES))
				.thenUp(new TreeBlock(BlockTypes.OAK_LEAVES))
				.addNorth(new TreeBlock(BlockTypes.OAK_LEAVES))
				.addEast(new TreeBlock(BlockTypes.OAK_LEAVES))
				.addSouth(new TreeBlock(BlockTypes.OAK_LEAVES))
				.addWest(new TreeBlock(BlockTypes.OAK_LEAVES))
		)),
		BIRCH(new TreeBlock(BlockTypes.BIRCH_LOG, Axis.Y).modify(treeBlock -> treeBlock
				.thenUp(new TreeBlock(BlockTypes.BIRCH_LOG, Axis.Y))
				.thenUp(new TreeBlock(BlockTypes.BIRCH_LOG, Axis.Y))
				.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
						.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES)))
						.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES))))
				.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES)
						.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES)))
						.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES))))
				.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
						.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES)))
						.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES))))
				.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES)
						.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES)))
						.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES))))
				.thenUp(new TreeBlock(BlockTypes.BIRCH_LOG, Axis.Y))
				.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
						.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES)))
						.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES))))
				.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES)
						.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES)))
						.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES))))
				.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
						.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES)))
						.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES))))
				.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES)
						.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES)))
						.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES))))
				.thenUp(new TreeBlock(BlockTypes.BIRCH_LOG, Axis.Y))
				.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES))
				.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES))
				.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES))
				.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES))
				.thenUp(new TreeBlock(BlockTypes.BIRCH_LEAVES))
				.addNorth(new TreeBlock(BlockTypes.BIRCH_LEAVES))
				.addEast(new TreeBlock(BlockTypes.BIRCH_LEAVES))
				.addSouth(new TreeBlock(BlockTypes.BIRCH_LEAVES))
				.addWest(new TreeBlock(BlockTypes.BIRCH_LEAVES))
		)),
		JUNGLE(new TreeBlock(BlockTypes.JUNGLE_LOG, Axis.Y).modify(treeBlock -> treeBlock
				.thenUp(new TreeBlock(BlockTypes.JUNGLE_LOG, Axis.Y))
				.thenUp(new TreeBlock(BlockTypes.JUNGLE_LOG, Axis.Y))
				.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
						.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES)))
						.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES))))
				.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
						.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)))
						.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES))))
				.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
						.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES)))
						.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES))))
				.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
						.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)))
						.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES))))
				.thenUp(new TreeBlock(BlockTypes.JUNGLE_LOG, Axis.Y))
				.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
						.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES)))
						.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES))))
				.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
						.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)))
						.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES))))
				.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
						.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES)))
						.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES))))
				.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
						.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)))
						.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES)
								.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES))))
				.thenUp(new TreeBlock(BlockTypes.JUNGLE_LOG, Axis.Y))
				.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES))
				.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES))
				.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES))
				.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES))
				.thenUp(new TreeBlock(BlockTypes.JUNGLE_LEAVES))
				.addNorth(new TreeBlock(BlockTypes.JUNGLE_LEAVES))
				.addEast(new TreeBlock(BlockTypes.JUNGLE_LEAVES))
				.addSouth(new TreeBlock(BlockTypes.JUNGLE_LEAVES))
				.addWest(new TreeBlock(BlockTypes.JUNGLE_LEAVES))
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
		 *
		 * @param entity entity to place the block on
		 */
		public void place(@NotNull Entity entity) {
			treeBlock.place(entity.serverLocation());
		}

		/**
		 * Places this block and its children into the world.
		 * <p>
		 * <b>WARNING:</b> Must be run synchronously!
		 *
		 * @param location location to place the block
		 */
		public void place(@NotNull ServerLocation location) {
			treeBlock.place(location);
		}
	}

	// wow, uh, my code is kinda overkill isn't it
	private static final class TreeBlock {
		private final @NotNull BlockType blockType;
		private final @Nullable Axis woodAxis;
		private final Map<Direction, TreeBlock> children = new EnumMap<>(Direction.class);

		private TreeBlock(@NotNull BlockType block) {
			this.blockType = block;
			this.woodAxis = null;
		}

		private TreeBlock(@NotNull Supplier<BlockType> block) {
			this.blockType = block.get();
			this.woodAxis = null;
		}

		private TreeBlock(@NotNull BlockType block, @Nullable Axis woodAxis) {
			this.blockType = block;
			this.woodAxis = woodAxis;
		}

		private TreeBlock(@NotNull Supplier<BlockType> block, @Nullable Axis woodAxis) {
			this.blockType = block.get();
			this.woodAxis = woodAxis;
		}

		/**
		 * Adds a child and returns that child
		 */
		@Contract("_, _ -> param2")
		@NotNull
		public TreeBlock then(@NotNull Direction face, @NotNull TreeBlock child) {
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
		public TreeBlock add(@NotNull Direction face, @NotNull TreeBlock child) {
			then(face, child);
			return this;
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenUp(@NotNull TreeBlock child) {
			return then(Direction.UP, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addUp(@NotNull TreeBlock child) {
			return add(Direction.UP, child);
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenDown(@NotNull TreeBlock child) {
			return then(Direction.DOWN, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addDown(@NotNull TreeBlock child) {
			return add(Direction.DOWN, child);
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenNorth(@NotNull TreeBlock child) {
			return then(Direction.NORTH, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addNorth(@NotNull TreeBlock child) {
			return add(Direction.NORTH, child);
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenEast(@NotNull TreeBlock child) {
			return then(Direction.EAST, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addEast(@NotNull TreeBlock child) {
			return add(Direction.EAST, child);
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenSouth(@NotNull TreeBlock child) {
			return then(Direction.SOUTH, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addSouth(@NotNull TreeBlock child) {
			return add(Direction.SOUTH, child);
		}

		@Contract("_ -> param1")
		@NotNull
		public TreeBlock thenWest(@NotNull TreeBlock child) {
			return then(Direction.WEST, child);
		}

		@Contract("_ -> this")
		@NotNull
		public TreeBlock addWest(@NotNull TreeBlock child) {
			return add(Direction.WEST, child);
		}

		/**
		 * Places this block and its children into the world.
		 * <p>
		 * <b>WARNING:</b> Must be run synchronously!
		 *
		 * @param location location to place the block
		 */
		public void place(@NotNull ServerLocation location) {
			BlockState.Builder builder = BlockState.builder().blockType(blockType);
			if (woodAxis != null)
				builder.add(Keys.AXIS, woodAxis);
			location.setBlock(builder.build());
			children.forEach((direction, treeBlock) -> treeBlock.place(location.add(direction.asBlockOffset())));
		}

		/**
		 * Allows for easily building tree blocks by modifying this object and then returning itself
		 *
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
