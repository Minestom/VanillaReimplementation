package net.minestom.vanilla.commands; 

import fr.themode.command.Command;
import fr.themode.command.Arguments;
import fr.themode.command.arguments.Argument; 
import fr.themode.command.arguments.ArgumentType; 
import net.minestom.server.entity.type.*;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Entity; 
import net.minestom.server.entity.Player;
import net.minestom.server.entity.ExperienceOrb; 
import net.minestom.server.utils.Position; 
import net.minestom.server.instance.Instance;



/**
 * Command that spawns a specifed entity. 
 *
 * <p> Usage: <pre>/summon &lt;entity&gt; [pos] [nbt]<pre> <br> 
 * where pos and nbt are  optional arguments, 
 * and entity is required.
 *
 * Entity should be the name of the entity that you want to spawn, 
 * to spawn a zombie you would write: <br>     
 *
 * /summon Zombie 
 * </p> 
 *
 * TODO: allow the user to specify a compound NBT tag  
 * TODO: implement relative positioning arguments.  
 * see: <a href="https://minecraft.gamepedia.com/Commands/summon">
 */
public class SummonCommand extends Command<Player> { 

    public SummonCommand() {
        super("summon"); 

        setCondition( this::isAllowed ); 
        setDefaultExecutor( this::usage ); 

        EntityType[] entities = EntityType.values();
        String[] names = new String[entities.length];
        for (int i = 0; i < entities.length; i++) {
            names[i] = entities[i].name().toLowerCase();
        }
        Argument entity = ArgumentType.Word("entity").from(names); 
        Argument x = ArgumentType.Float("x");  
        Argument y = ArgumentType.Float("y"); 
        Argument z = ArgumentType.Float("z"); 

        addSyntax( this::executeHere, entity ); 
        addSyntax( this::executeThere, entity, x, y, z); 
        //addSyntax( this::execute, entity, pos, nbt)
    }

    /**
     * executeHere: spawn the entity at the player's position. 
     * @param player - the player sending the command.  
     * @param arguments - the arguments given to /summon 
     *
     *  /summon <entity>
     */
    private void executeHere( Player player, Arguments arguments ){

        String entityName = arguments.getWord("entity");   
        EntityType entityType  = EntityType.valueOf( entityName.toUpperCase()); 
        assert entityType != null;

        Position pos = player.getPosition();  
        Instance instance = player.getInstance(); 

        Entity summoned = createEntity( entityType, pos ); 
        summoned.setInstance( instance ); 


        player.sendMessage("Summoned a " + entityName );
    } 

    /**
     * executeThere: spawn the entity at a specified x,y,z position. 
     * @param player - the player sending the command.  
     * @param arguments - the arguments given to /summon 
     *
     *  /summon <entity> [pos]
     */
    private void executeThere( Player player, Arguments arguments ){

        String entityName = arguments.getWord("entity");   
        EntityType entityType  = EntityType.valueOf( entityName.toUpperCase()); 
        assert entityType != null;

        float x = arguments.getFloat("x");  
        float y = arguments.getFloat("y"); 
        float z = arguments.getFloat("z"); 

        Position pos = new Position( x, y, z ); 
        Instance instance = player.getInstance(); 

        Entity summoned = createEntity( entityType, pos ); 
        summoned.setInstance( instance ); 
        
        player.sendMessage("Summoned a " + entityName );
    }

    private void summonCallback(Player player, String entity, int error) {
        player.sendMessage("'" + entity + "' is not a valid entity!");
    }

    private void usage(Player player, Arguments arguments) {
        player.sendMessage("Usage: /summon <entity> [pos]" );  
    }

    private boolean isAllowed( Player player ){
        return true; // TODO: permissions 
    }

    /** 
     * Spawn a new enitty based on the entity type. 
     * @param entityType - the type of entity to spawn.
     * @param pos - the position of the entity to spawn. 
     *
     * <p> All of the entities that are implemented in the vanillia 
     * reimplementation are here. But there are more entities 
     * that will need to be added once they are implemented. </p>   
     */
    private Entity createEntity( EntityType entityType, Position pos ){
        Entity result = null; 
        switch( entityType ){
            case BAT:
                result = new EntityBat(pos);
                break; 
            case BLAZE:
                result = new EntityBlaze(pos);
                break; 
            case CAVE_SPIDER:
                result = new EntityCaveSpider(pos);
                break; 
            case CHICKEN:
                result = new EntityChicken(pos);
                break; 
            case COW:
                result = new EntityCow(pos); 
                break;
            case CREEPER:
                result = new EntityCreeper(pos); 
                break; 
            case ENDERMITE:
                result = new EntityEndermite(pos); 
                break;
            case GHAST:
                result = new EntityGhast(pos); 
                break;
            case GIANT:
                result = new EntityGiant(pos); 
                break;
            case GUARDIAN:
                result = new EntityGuardian(pos); 
                break;
            case IRON_GOLEM:
                result = new EntityIronGolem(pos); 
                break;
            case MOOSHROOM:
                result = new EntityMooshroom(pos); 
                break;
            case PHANTOM:
                result = new EntityPhantom(pos); 
                break;
            case PIG:
                result = new EntityPig(pos); 
                break;
            case ZOMBIFIED_PIGLIN:
                result = new EntityPigZombie(pos); 
                break;
            case POLAR_BEAR:
                result = new EntityPolarBear(pos); 
                break;
            case RABBIT:
                result = new EntityRabbit(pos); 
                break;
            case SILVERFISH:
                result = new EntitySilverfish(pos); 
                break;
            case SLIME:
                result = new EntitySlime(pos); 
                break;
            case SNOW_GOLEM:
                result = new EntitySnowman(pos); 
                break;
            case SPIDER:
                result = new EntitySpider(pos); 
                break;
            case WITCH:
                result = new EntityWitch(pos); 
                break;
            case ZOMBIE:
                result = new EntityZombie(pos); 
                break;
            case AREA_EFFECT_CLOUD:
                result = new EntityAreaEffectCloud(pos); 
                break;
            case ARMOR_STAND:
                result = new EntityArmorStand(pos); 
                break;
            case BOAT:
                result = new EntityBoat(pos); 
                break;
            case POTION:
                //result = new EntityPotion(pos); 
                break;
            case ITEM:
                //result = new ItemEntity(pos); 
                break;
            case EXPERIENCE_ORB:
                result = new ExperienceOrb( (short) 1 ); 
                break;
            default: 
                System.out.printf("did not recognize the entityType: %s\n", entityType.name()); 
                break; 
        } 
        return result; 
    } 
}
