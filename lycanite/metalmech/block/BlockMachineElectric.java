package lycanite.metalmech.block;

import java.util.Random;

import universalelectricity.core.item.IItemElectric;
import universalelectricity.prefab.implement.IToolConfigurator;
import lycanite.metalmech.GuiHandler;
import lycanite.metalmech.MetalMech;
import lycanite.metalmech.client.ClientProxy;
import lycanite.metalmech.item.ItemBlockMachineCrusher;
import lycanite.metalmech.item.ItemBlockMachineElectric;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMachineElectric extends BlockMachineBasic {
		
	// Constructor
	public BlockMachineElectric(int id, String texture) {
		super(id, texture);
	}
	
	
	// Get Names:
	@Override
	public String[] getNames() {
		return MetalMech.electricMachineBlockNames;
	}
	
	
	// Get Titles:
	@Override
	public String[] getTitles() {
		return MetalMech.electricMachineBlockTitles;
	}
	
	
	// Register Block:
	public void registerBlock() {
		Item.itemsList[this.blockID] = new ItemBlockMachineElectric(this.blockID - 256);
		for(int subBlock = 0; subBlock < this.getNames().length; subBlock++) {
			LanguageRegistry.instance().addStringLocalization(this.getNames()[subBlock] + ".name", this.getTitles()[subBlock]);
		}
	}
	
	
	// Tile Entity:
	@Override
	public TileEntity createTileEntity(World world, int metadata) {
		TileEntityMachineElectric tileEntity = new TileEntityMachineElectric();
		return tileEntity;
	}
    
    
	// On Block Placed:
    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLiving entity, ItemStack itemStack) {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    	int rotation = MathHelper.floor_double((double)(entity.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int targetFacing = 3;
    	
        switch(rotation) {
        	case 0:
        		targetFacing = 2;
        		break;
        	case 1:
        		targetFacing = 5;
        		break;
        	case 2:
        		targetFacing = 3;
        		break;
        	case 3:
        		targetFacing = 4;
        		break;
        }
        
        if(tileEntity instanceof TileEntityMachineElectric)
        	((TileEntityMachineElectric)tileEntity).setFacing(targetFacing);
    	((TileEntityMachineElectric)tileEntity).initiate();
    }
	
	
	// Block Rendering:
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	
	// Is Opaque:
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	
	// Get Render Type:
	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType() {
		return ClientProxy.RENDER_ID;
	}
	
	
	// Block Activated:
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
            return true;
        }
		
		if(player.isSneaking()) {
			return false;
		}
		
		if(player.inventory.getCurrentItem() != null) {
			if((player.inventory.getCurrentItem().getItem() instanceof IToolConfigurator)) {
				world.notifyBlocksOfNeighborChange(x, y, z, this.blockID);
				((IToolConfigurator)player.inventory.getCurrentItem().getItem()).wrenchUsed(player, x, y, z);
				
				return onUseWrench(world, x, y, z, player, side, hitX, hitY, hitZ);
			}
			if((player.inventory.getCurrentItem().getItem() instanceof IItemElectric)) {
				if(onUseElectricItem(world, x, y, z, player, side, hitX, hitY, hitZ)) return true;
			}
		}
	
	    return onMachineActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
	}
	
	
	// Machine Activated:
	public boolean onMachineActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		player.openGui(MetalMech.instance, GuiHandler.GUIType.ELECTRIC_MACHINE.id + world.getBlockMetadata(x, y, z), world, x, y, z);
		return true;
	}
	
	
	// Machine Wrenched:
	public boolean onUseWrench(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		TileEntityMachineElectric tileEntity = (TileEntityMachineElectric)world.getBlockTileEntity(x, y, z);
		int facing = tileEntity.facing - 2;
		
		switch (facing) {
			case 0:
				facing = 3;
				break;
			case 3:
				facing = 1;
				break;
			case 1:
				facing = 2;
				break;
			case 2:
				facing = 0;
		}
		
		tileEntity.setFacingWithUpdate(facing + 2);
		return true;
	}
	@Deprecated
	public boolean onSneakUseWrench(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	
	// Machine Electric Item:
	public boolean onUseElectricItem(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	
	// Random Display Tick:
	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random random) {
		TileEntityMachineElectric tileEntity = (TileEntityMachineElectric)world.getBlockTileEntity(x, y, z);
        if(tileEntity.isActive()) {
        	float zPos = (float)x + 0.5F;
	        float yPos = (float)y + 0.0F + random.nextFloat() * 6.0F / 16.0F;
	        float xPos = (float)z + 0.5F;
	        float fixed = 0.52F;
	        float random2 = random.nextFloat() * 0.6F - 0.3F;
	
	        switch(tileEntity.facing) {
	        	case 2:
	        		world.spawnParticle("smoke", (double)(zPos + random2), (double)yPos, (double)(xPos - fixed), 0.0D, 0.0D, 0.0D);
		            world.spawnParticle("reddust", (double)(zPos + random2), (double)yPos, (double)(xPos - fixed), 0.0D, 0.0D, 0.0D);
		            break;
	        	case 3:
	        		world.spawnParticle("smoke", (double)(zPos + random2), (double)yPos, (double)(xPos + fixed), 0.0D, 0.0D, 0.0D);
		            world.spawnParticle("reddust", (double)(zPos + random2), (double)yPos, (double)(xPos + fixed), 0.0D, 0.0D, 0.0D);
		            break;
	        	case 4:
	        		world.spawnParticle("smoke", (double)(zPos - fixed), (double)yPos, (double)(xPos + random2), 0.0D, 0.0D, 0.0D);
		            world.spawnParticle("reddust", (double)(zPos - fixed), (double)yPos, (double)(xPos + random2), 0.0D, 0.0D, 0.0D);
		            break;
	        	case 5:
	        		world.spawnParticle("smoke", (double)(zPos + fixed), (double)yPos, (double)(xPos + random2), 0.0D, 0.0D, 0.0D);
		            world.spawnParticle("reddust", (double)(zPos + fixed), (double)yPos, (double)(xPos + random2), 0.0D, 0.0D, 0.0D);
		            break;
	        }
        }
    }
}
