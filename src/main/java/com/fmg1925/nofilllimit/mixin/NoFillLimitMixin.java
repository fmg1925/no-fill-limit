package com.fmg1925.nofilllimit.mixin;

import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.server.command.FillCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(FillCommand.class)
public class NoFillLimitMixin {
	@Redirect(
			method = "execute",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/GameRules;getInt(Lnet/minecraft/world/GameRules$Key;)I"
			)
	)
	private static int noFillLimit(GameRules ignore, GameRules.Key<GameRules.IntRule> rule) {
		return Integer.MAX_VALUE;
	}

	@ModifyVariable(method = "execute", at = @At("HEAD"), argsOnly = true)
	private static BlockBox modifyCoords(BlockBox range, ServerCommandSource source) {
		int minBuildY = source.getWorld().getBottomY();
		int maxBuildY = source.getWorld().getLogicalHeight() - Math.abs(source.getWorld().getBottomY()) - 1;

		int x1 = range.getMinX();
		int x2 = range.getMaxX();
		int y1 = Math.max(minBuildY, range.getMinY());
		int y2 = Math.min(maxBuildY, range.getMaxY());
		int z1 = range.getMinZ();
		int z2 = range.getMaxZ();
		range = null;

		return new BlockBox(x1, y1, z1, x2, y2, z2);
	}

	@Inject(method = "execute", at = @At("HEAD"))
	private static void mensaje(
			ServerCommandSource source,
			BlockBox range,
			BlockStateArgument block,
			@Coerce Object mode,
			@Nullable Predicate<CachedBlockPosition> filter,
			boolean strict,
			CallbackInfoReturnable<Integer> cir
	) {
		int x1 = range.getMinX();
		int x2 = range.getMaxX();
		int y1 = range.getMinY();
		int y2 = range.getMaxY();
		int z1 = range.getMinZ();
		int z2 = range.getMaxZ();

		int width = x2 - x1 + 1;
		int height = y2 - y1 + 1;
		int depth = z2 - z1 + 1;

		String cmdStart = "/tp %d %d %d".formatted(x1, y1, z1);
		String cmdEnd = "/tp %d %d %d".formatted(x2, y2, z2);

		Text hintStart = Text.literal("Tp to start");
		Text hintEnd = Text.literal("Tp to end");

		MutableText message = Text.literal("Filling from ")
				.formatted(Formatting.WHITE)

				.append(Text.literal("[%d, %d, %d]".formatted(x1, y1, z1))
						.setStyle(Style.EMPTY
								.withColor(Formatting.GREEN)
								.withClickEvent(new ClickEvent.SuggestCommand(cmdStart))
								.withHoverEvent(new HoverEvent.ShowText(hintStart))
						))

				.append(Text.literal(" to ").formatted(Formatting.WHITE))

				.append(Text.literal("[%d, %d, %d]".formatted(x2, y2, z2))
						.setStyle(Style.EMPTY
								.withColor(Formatting.GREEN)
								.withClickEvent(new ClickEvent.SuggestCommand(cmdEnd))
								.withHoverEvent(new HoverEvent.ShowText(hintEnd))
						));

		MutableText message2 = Text.literal("Width: %d, Height: %d, Depth: %d".formatted(width, height, depth));

		source.sendFeedback(() -> message, true);
		source.sendFeedback(() -> message2, true);
	}
}