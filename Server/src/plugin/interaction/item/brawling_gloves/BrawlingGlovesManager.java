package plugin.interaction.item.brawling_gloves;

import org.crandor.cache.def.impl.ItemDefinition;
import org.crandor.game.node.entity.player.Player;
import org.crandor.game.node.entity.player.info.login.SavingModule;
import org.crandor.game.node.item.Item;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Objects;

/**
 * Manages brawling gloves for a player
 * @author ceik
 */
public class BrawlingGlovesManager implements SavingModule {

    final Player player;
    public HashMap<Integer, Integer> GloveCharges = new HashMap<Integer,Integer>();
    public void registerGlove(int id) {
        try {
            registerGlove(id, Objects.requireNonNull(BrawlingGloves.forId(id)).getCharges());
        } catch (Exception e){
            System.out.println(e);
        }
    }
    public void registerGlove(int id, int charges) {GloveCharges.putIfAbsent(id,charges);}

    public BrawlingGlovesManager(Player player){this.player = player;}

    @Override
    public void save(ByteBuffer buffer) {
        if(!GloveCharges.isEmpty()){
            GloveCharges.forEach((key,value) -> {
                buffer.put(BrawlingGloves.forId(key).getIndicator());
                buffer.putInt(value);
            });
        }
        buffer.put((byte) 0);
    }

    @Override
    public void parse(ByteBuffer buffer) {
        int opcode;
        int charges;
        while ((opcode = buffer.get() & 0xFF) != 0){
            charges = buffer.getInt();
            registerGlove(BrawlingGloves.forIndicator((byte)opcode).getId(),charges);
        }
    }

    public boolean updateCharges(int glove, int charges){
        if(GloveCharges.get(glove) != null){
            if(GloveCharges.get(glove) - charges <= 0) {
                GloveCharges.remove(glove);
                player.getEquipment().remove(new Item(glove));
                player.getPacketDispatch().sendMessage("<col=ff0000>You use the last charge of your " + ItemDefinition.forId(glove).getName() + " and they vanish.</col>");
                return false;
            }
            int currentCharges = GloveCharges.get(glove);
            GloveCharges.replace(glove,currentCharges - charges);
            player.debug("Glove charges: " + (currentCharges - 1));
            if((currentCharges - 1) % 50 == 0) {
                player.getPacketDispatch().sendMessage("<col=1fbd0d>Your " + ItemDefinition.forId(glove).getName() + " have " + GloveCharges.get(glove) + " charges left.</col>");
            }
        }
        return true;
    }

    public double getExperienceBonus(){
        double bonus;
        final double BONUS_PER_DEPTH = 5.3191489362;
        int level = player.getSkullManager().getLevel();
        if(level >= 47){
            bonus = 250.0;
        }else if(level == 0){
            bonus = 50.0;
        }else{
            bonus = 50.0 + level * BONUS_PER_DEPTH;
        }
        return bonus / 100;
    }
}
