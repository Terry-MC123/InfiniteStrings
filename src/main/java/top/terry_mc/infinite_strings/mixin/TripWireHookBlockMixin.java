package top.terry_mc.infinite_strings.mixin;

import com.google.common.base.MoreObjects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(TripWireHookBlock.class)
public abstract class TripWireHookBlockMixin {
    @Shadow
    @Final
    public static EnumProperty<Direction> FACING;

    @Shadow
    @Final
    public static BooleanProperty ATTACHED;

    @Shadow
    @Final
    public static BooleanProperty POWERED;

    @Shadow
    private static void notifyNeighbors(Block block, Level level, BlockPos pos, Direction p_direction) {}

    @Shadow
    private static void emitState(Level level, BlockPos pos, boolean attached, boolean powered, boolean wasAttached, boolean wasPowered) {}

    @Inject(method = "calculateState", at = @At("HEAD"))
    private static void infinite_strings$calculateStateInject(Level level, BlockPos pos, BlockState hookState, boolean attaching, boolean shouldNotifyNeighbours, int searchRange, BlockState state, CallbackInfo ci) {
        Optional<Direction> optional = hookState.getOptionalValue(FACING);
        if (optional.isPresent()) {
            Direction direction = optional.get();
            boolean flag = hookState.getOptionalValue(ATTACHED).orElse(false);
            boolean flag1 = hookState.getOptionalValue(POWERED).orElse(false);
            Block block = hookState.getBlock();
            boolean flag2 = !attaching;
            boolean flag3 = false;
            int i = 0;
            BlockState[] ablockstate = new BlockState[42];

            for(int j = 1; j < 42; ++j) {
                BlockPos blockpos = pos.relative(direction, j);
                BlockState blockstate = level.getBlockState(blockpos);
                if (blockstate.is(Blocks.TRIPWIRE_HOOK)) {
                    if (blockstate.getValue(FACING) == direction.getOpposite()) {
                        i = j;
                    }
                    break;
                }

                if (!blockstate.is(Blocks.TRIPWIRE) && j != searchRange) {
                    ablockstate[j] = null;
                    flag2 = false;
                } else {
                    if (j == searchRange) {
                        blockstate = MoreObjects.firstNonNull(state, blockstate);
                    }

                    boolean flag4 = !(Boolean)blockstate.getValue(TripWireBlock.DISARMED);
                    boolean flag5 = blockstate.getValue(TripWireBlock.POWERED);
                    flag3 |= flag4 && flag5;
                    ablockstate[j] = blockstate;
                    if (j == searchRange) {
                        level.scheduleTick(pos, block, 10);
                        flag2 &= flag4;
                    }
                }
            }

            flag2 &= i > 1;
            flag3 &= flag2;
            BlockState blockstate1 = block.defaultBlockState().trySetValue(ATTACHED, flag2).trySetValue(POWERED, flag3);
            if (i > 0) {
                BlockPos blockpos1 = pos.relative(direction, i);
                Direction direction1 = direction.getOpposite();
                level.setBlock(blockpos1, blockstate1.setValue(FACING, direction1), 3);
                notifyNeighbors(block, level, blockpos1, direction1);
                emitState(level, blockpos1, flag2, flag3, flag, flag1);
            }

            emitState(level, pos, flag2, flag3, flag, flag1);
            if (!attaching) {
                level.setBlock(pos, blockstate1.setValue(FACING, direction), 3);
                if (shouldNotifyNeighbours) {
                    notifyNeighbors(block, level, pos, direction);
                }
            }

            if (flag != flag2) {
                for(int k = 1; k < i; ++k) {
                    BlockPos blockpos2 = pos.relative(direction, k);
                    BlockState blockstate2 = ablockstate[k];
                    if (blockstate2 != null) {
                        level.setBlock(blockpos2, blockstate2.trySetValue(ATTACHED, flag2), 3);
                        level.getBlockState(blockpos2);
                    }
                }
            }
        }
    }
}
