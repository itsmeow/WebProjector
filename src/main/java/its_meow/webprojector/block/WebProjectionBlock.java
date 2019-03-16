package its_meow.webprojector.block;

import static net.minecraft.state.property.Properties.FACING_HORIZONTAL;

import its_meow.webprojector.light.VideoLight;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateFactory.Builder;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import therealfarfetchd.illuminate.client.api.Lights;

public class WebProjectionBlock extends Block {

	public WebProjectionBlock() {
		super(Block.Settings.of(Material.METAL));
	}


	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING_HORIZONTAL, ctx.getPlayerHorizontalFacing().getOpposite());
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.get(FACING_HORIZONTAL)));
	}

	@Override
	public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState state2) {
		MinecraftClient.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				Lights.getInstance().add(new VideoLight(pos, state.getBlock()));
			}
		});
		super.onBlockAdded(state, world, pos, state2);
	}
	
	

	@Override
	public void onBlockRemoved(BlockState blockState_1, World world_1, BlockPos blockPos_1, BlockState blockState_2,
			boolean boolean_1) {
		/*MinecraftClient.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				Lights.getInstance().remove(new BlockLight(blockPos_1));
			}
		});*/
		super.onBlockRemoved(blockState_1, world_1, blockPos_1, blockState_2, boolean_1);
	}


	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.with(FACING_HORIZONTAL, rotation.rotate(state.get(FACING_HORIZONTAL)));
	}

	@Override
	protected void appendProperties(Builder<Block, BlockState> builder) {
		builder.with(FACING_HORIZONTAL);
		super.appendProperties(builder);
	}

}