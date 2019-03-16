package its_meow.webprojector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import its_meow.webprojector.block.WebProjectionBlock;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.block.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class WebProjectorMod implements ModInitializer {
	
	public static final String MOD_ID = "webprojector";
	
	public static final WebProjectionBlock PROJECTOR_BLOCK = new WebProjectionBlock();
	
	public static final Logger LOG = LogManager.getLogger(MOD_ID);
	
	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "webprojector"), PROJECTOR_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "webprojector"), new BlockItem(PROJECTOR_BLOCK, new Item.Settings().itemGroup(ItemGroup.REDSTONE)));
	}
	
}