package TFC.Blocks.Vanilla;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.stats.StatList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import TFC.Reference;
import TFC.TFCBlocks;
import TFC.Blocks.BlockTerra;
import TFC.Core.TFC_Climate;

public class BlockCustomSnow extends BlockTerra
{
	public BlockCustomSnow()
	{
		super(Material.snow);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
		this.setTickRandomly(true);
	}

	@Override
	public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4)
	{
		Block block = par1World.getBlock(par2, par3 - 1, par4);
		if (block == Blocks.ice ||
			block != TFCBlocks.worldItem ||
			block == Blocks.leaves ||
			block.isOpaqueCube())
			return true;
		
		return false;
	}

	private boolean canSnowStay(World par1World, int par2, int par3, int par4)
	{
		if (!this.canPlaceBlockAt(par1World, par2, par3, par4))
		{
			int meta = par1World.getBlockMetadata(par2, par3, par4);
			if(meta <= 1)
				par1World.setBlockToAir(par2, par3, par4);
			else
				par1World.setBlockMetadataWithNotify(par2, par3,par4, meta - 1, 1);

			return false;
		}
		else
		{
			return true;
		}
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		int l = par1World.getBlockMetadata(par2, par3, par4) & 7;
		float f = 0.125F;
		return AxisAlignedBB.getAABBPool().getAABB((double)par2 + this.minX, (double)par3 + this.minY, (double)par4 + this.minZ, (double)par2 + this.maxX, (double)((float)par3 + (float)l * f), (double)par4 + this.maxZ);
	}
	@Override
	public int getRenderType()
	{
		return TFCBlocks.snowRenderId;
	}

	@Override
	public void harvestBlock(World par1World, EntityPlayer par2EntityPlayer, int par3, int par4, int par5, int par6)
	{
		dropBlockAsItem(par1World, par3, par4, par5, par6, 0);
		par2EntityPlayer.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
	}

	@Override
	public Item getItemDropped(int par1, Random par2Random, int par3) {
		return Items.snowball;
	}
	
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity par5Entity)
	{
		int var5 = par1World.getBlockMetadata(par2, par3, par4);
		if(var5 > 0)
		{
			double speed = 0.58D + 0.4D * (15/var5/15);

			par5Entity.motionX *= speed;
			par5Entity.motionZ *= speed;
		}
	}

	@Override
	public void onNeighborBlockChange(World par1World, int par2, int par3, int par4, Block par5)
	{
		this.canSnowStay(par1World, par2, par3, par4);
	}

	@Override
	public int quantityDropped(Random par1Random)
	{
		return 1;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int par2, int par3, int par4)
	{
		int var5 = par1IBlockAccess.getBlockMetadata(par2, par3, par4) & 7;
		float var6 = 2 * (1 + var5) / 16.0F;
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, var6, 1.0F);
	}

	@Override
	public int tickRate(World world)
	{
		return 50;
	}

	@Override
	public void updateTick(World par1World, int par2, int par3, int par4, Random par5Random)
	{
		if (!this.canSnowStay(par1World, par2, par3, par4))
		{
			return;
		}
		int meta = par1World.getBlockMetadata(par2, par3, par4);
		if (par1World.getSavedLightValue(EnumSkyBlock.Block, par2, par3, par4) > 11)
		{
			if(meta > 1 && par5Random.nextInt(5) == 0) {
				par1World.setBlockMetadataWithNotify(par2, par3, par4, meta-1, 2);
			} else if(meta == 1 && par5Random.nextInt(5) == 0) {
				par1World.setBlockToAir(par2, par3, par4);
			}
		}
		if(par1World.isRaining() && TFC_Climate.getHeightAdjustedTemp(par2, par3, par4) <= 0)//Raining and Below Freezing
		{
			if(meta < 3 && par1World.getBlock(par2, par3-1, par4).getMaterial() != Material.leaves) 
			{
				if (canAddSnow(par1World, par2, par3, par4, meta)) {
					par1World.setBlockMetadataWithNotify(par2, par3, par4, meta+1, 2);
				}
			} 
			else if(meta < 15 && par5Random.nextInt(8) == 0 && par1World.getBlock(par2, par3-1, par4).getMaterial() != Material.leaves)
			{
				if (canAddSnow(par1World, par2, par3, par4, meta)) {
					par1World.setBlockMetadataWithNotify(par2, par3, par4, meta+1, 2);
				}
			}
			else if(meta < 3 && par5Random.nextInt(3) == 0 && par1World.getBlock(par2, par3-1, par4).getMaterial() == Material.leaves)
			{
				if (canAddSnow(par1World, par2, par3, par4, meta)) {
					par1World.setBlockMetadataWithNotify(par2, par3, par4, meta+1, 2);
				}
			}
		}
		else if(par1World.isRaining() && TFC_Climate.getHeightAdjustedTemp(par2, par3, par4) >= 0)//Raining and above freezing
		{      
			if(meta <= 15 && par1World.getBlock(par2, par3-1, par4).getMaterial() != Material.leaves) 
			{
				if(meta > 1) 
				{
					par1World.setBlockMetadataWithNotify(par2, par3, par4, meta-1, 2);
				} 
				else 
				{
					par1World.setBlockToAir(par2, par3, par4);
				}
			} 
			else if(meta <= 15 && par1World.getBlock(par2, par3-1, par4).getMaterial() == Material.leaves)
			{
				if(meta > 1) {
					par1World.setBlockMetadataWithNotify(par2, par3, par4, meta-1, 2);
				} else {
					par1World.setBlockToAir(par2, par3, par4);
				}
			}
		}
		else if(TFC_Climate.getHeightAdjustedTemp(par2, par3, par4) >= 0F)//Above fReezing
		{
			if(meta > 0 ) {
				par1World.setBlockMetadataWithNotify(par2, par3, par4, meta-1, 2);
			} else {
				par1World.setBlockToAir(par2, par3, par4);
			}
		}
//		else//Below Freezing
//		{
//		    if(meta > 1 && par5Random.nextInt(5) == 0)
//		    {
//              par1World.setBlockMetadataWithNotify(par2, par3, par4, meta-1, 2);
//          }
//		    else if(meta == 1 && par5Random.nextInt(5) == 0)
//          {
//          	par1World.setBlockToAir(par2, par3, par4);
//          }
//		}
	}

	@Override
	public void registerBlockIcons(IIconRegister registerer)
	{
		this.blockIcon = registerer.registerIcon(Reference.ModID + ":"+"snow");
	}

	private boolean canAddSnowCheckNeighbors(World world, int x, int y, int z, int meta)
	{
		if (!this.canPlaceBlockAt(world, x, y, z))
			return true;
		if (world.getBlock(x, y, z).getMaterial() != Material.snow)
			return false;
		if ( world.getBlock(x, y, z).getMaterial() == Material.snow && meta > world.getBlockMetadata(x, y, z))
			return false;

		return true;
	}

	private boolean canAddSnow(World world, int x, int y, int z, int meta)
	{
		if (!canAddSnowCheckNeighbors(world, x+1, y, z, meta))
			return false;
		if (!canAddSnowCheckNeighbors(world, x-1, y, z, meta))
			return false;
		if (!canAddSnowCheckNeighbors(world, x, y, z+1, meta))
			return false;
		if (!canAddSnowCheckNeighbors(world, x, y, z-1, meta))
			return false;

		return true;
	}
}