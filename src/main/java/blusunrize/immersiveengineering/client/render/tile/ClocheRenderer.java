/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.blocks.metal.ClocheTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class ClocheRenderer extends TileEntityRenderer<ClocheTileEntity>
{
	private static final Map<BlockState, List<BakedQuad>> plantQuads = new HashMap<>();

	public ClocheRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(ClocheTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(tile.dummy!=0||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();

		// Render particles in the TER rather than using the standard particle engine to avoid depth issues/the
		// particles not rendering at all outside of fabulous mode
		matrixStack.push();
		ActiveRenderInfo activeInfo = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
		matrixStack.translate(activeInfo.getProjectedView().x, activeInfo.getProjectedView().y, activeInfo.getProjectedView().z);
		IVertexBuilder baseBuffer = IERenderTypes.disableLighting(bufferIn)
				.getBuffer(RenderType.getEntityCutout(AtlasTexture.LOCATION_PARTICLES_TEXTURE));
		TransformingVertexBuilder particleBuilder = new TransformingVertexBuilder(baseBuffer, matrixStack);
		// Need to fix *some* normal, so just use "up" for all quads
		particleBuilder.setNormal(0, 1, 0);
		particleBuilder.setOverlay(OverlayTexture.NO_OVERLAY);
		for(Particle p : tile.particles)
			p.renderParticle(particleBuilder, activeInfo, Minecraft.getInstance().getRenderPartialTicks());
		matrixStack.pop();

		ClocheRecipe recipe = tile.getRecipe();
		if(recipe!=null)
		{
			IVertexBuilder baseBuilder = bufferIn.getBuffer(RenderType.getCutout());
			matrixStack.push();
			matrixStack.translate(0, 1.0625, 0);

			NonNullList<ItemStack> inventory = tile.getInventory();
			ItemStack seed = inventory.get(ClocheTileEntity.SLOT_SEED);
			float growth = MathHelper.clamp(tile.renderGrowth/recipe.time, 0, 1);
			float scale = recipe.renderFunction.getScale(seed, growth);
			matrixStack.translate((1-scale)/2, 0, (1-scale)/2);
			matrixStack.scale(scale, scale, scale);

			Collection<Pair<BlockState, TransformationMatrix>> blocks = recipe.renderFunction.getBlocks(seed, growth);
			for(Pair<BlockState, TransformationMatrix> block : blocks)
			{
				BlockState state = block.getLeft();
				List<BakedQuad> plantQuadList = plantQuads.get(state);
				if(plantQuadList==null)
				{
					IBakedModel plantModel = blockRenderer.getModelForState(state);
					plantQuadList = new ArrayList<>(plantModel.getQuads(state, null, Utils.RAND, EmptyModelData.INSTANCE));
					for(Direction f : Direction.values())
						plantQuadList.addAll(plantModel.getQuads(state, f, Utils.RAND, EmptyModelData.INSTANCE));
					plantQuads.put(state, plantQuadList);
				}
				int col = ClientUtils.mc().getBlockColors().getColor(state, null, blockPos, -1);
				block.getRight().push(matrixStack);
				ClientUtils.renderModelTESRFancy(plantQuadList, new TransformingVertexBuilder(baseBuilder, matrixStack),
						tile.getWorldNonnull(), blockPos, false, col, combinedLightIn);
				matrixStack.pop();
			}
			matrixStack.pop();
		}
	}

	public static void reset()
	{
		plantQuads.clear();
	}
}