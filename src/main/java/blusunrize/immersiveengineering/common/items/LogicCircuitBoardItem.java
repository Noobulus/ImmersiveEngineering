/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.tool.LogicCircuitHandler.LogicCircuitInstruction;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class LogicCircuitBoardItem extends IEBaseItem
{
	public LogicCircuitBoardItem()
	{
		super("logic_circuit", new Properties().stacksTo(1));
	}

	@Nonnull
	@Override
	public String getDescriptionId(ItemStack stack)
	{
		return this.getDescriptionId();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		LogicCircuitInstruction instruction = getInstruction(stack);
		if(instruction!=null)
			list.add(TextUtils.applyFormat(instruction.getFormattedString(), ChatFormatting.GRAY));
	}

	@Nullable
	public static LogicCircuitInstruction getInstruction(ItemStack stack)
	{
		if(stack.hasTag()&&stack.getTag().contains("operator"))
			return LogicCircuitInstruction.deserialize(stack.getTag());
		return null;
	}

	public static ItemStack buildCircuitBoard(LogicCircuitInstruction instruction)
	{
		ItemStack stack = new ItemStack(Misc.logicCircuitBoard);
		stack.setTag(instruction.serialize());
		return stack;
	}
}