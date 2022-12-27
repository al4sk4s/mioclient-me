package me.mioclient.mod.modules.impl.player;

import me.mioclient.api.events.impl.BreakBlockEvent;
import me.mioclient.api.util.interact.BlockUtil;
import me.mioclient.api.util.math.MathUtil;
import me.mioclient.api.util.math.Timer;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.DecimalFormat;
import java.util.Random;

public class Announcer extends Module {

    private final Setting<Boolean> move =
            add(new Setting<>("Move", true));
    private final Setting<Boolean> breakBlock =
            add(new Setting<>("Break", true));
    private final Setting<Boolean> eat =
            add(new Setting<>("Eat", true));

    private final Setting<Double> delay =
            add(new Setting<>("Delay", 10d, 1d, 30d));

    private double lastPositionX;
    private double lastPositionY;
    private double lastPositionZ;

    private int eaten;

    private int broken;

    private final Timer delayTimer = new Timer();

    public Announcer() {
        super("Announcer", "announces yo shit", Category.PLAYER, true);
    }

    @Override
    public void onEnable() {
        eaten = 0;
        broken = 0;

        delayTimer.reset();
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck() || !spawnCheck()) return;

        double traveledX = lastPositionX - mc.player.lastTickPosX;
        double traveledY = lastPositionY - mc.player.lastTickPosY;
        double traveledZ = lastPositionZ - mc.player.lastTickPosZ;

        double traveledDistance = Math.sqrt(traveledX * traveledX + traveledY * traveledY + traveledZ * traveledZ);

        if (move.getValue()
                && traveledDistance >= 1
                && traveledDistance <= 1000
                && delayTimer.passedS(delay.getValue())) {

            mc.player.sendChatMessage(getWalkMessage()
                    .replace("{blocks}", new DecimalFormat("0.00").format(traveledDistance)));

            lastPositionX = mc.player.lastTickPosX;
            lastPositionY = mc.player.lastTickPosY;
            lastPositionZ = mc.player.lastTickPosZ;

            delayTimer.reset();
        }
    }

    @SubscribeEvent
    public void onUseItem(LivingEntityUseItemEvent.Finish event) {
        if (fullNullCheck() || !spawnCheck()) return;

        int random = MathUtil.randomBetween(1, 6);

        if (eat.getValue()
                && event.getEntity() == mc.player
                && event.getItem().getItem() instanceof ItemFood
                || event.getItem().getItem() instanceof ItemAppleGold) {

            ++eaten;

            if (eaten >= random && delayTimer.passedS(delay.getValue())) {

                mc.player.sendChatMessage(getEatMessage()
                        .replace("{amount}", "" + eaten)
                        .replace("{name}", "" + event.getItem().getDisplayName()));

                eaten = 0;

                delayTimer.reset();
            }
        }
    }

    @SubscribeEvent
    public void onBreakBlock(BreakBlockEvent event) {
        if (fullNullCheck() || !spawnCheck()) return;

        int random = MathUtil.randomBetween(1, 6);

        ++broken;

        if (breakBlock.getValue()
                && broken >= random
                && delayTimer.passedS(delay.getValue())) {

            mc.player.sendChatMessage(getBreakMessage()
                    .replace("{amount}", "" + broken)
                    .replace("{name}", "" + BlockUtil.getBlock(event.getPos()).getLocalizedName()));

            broken = 0;

            delayTimer.reset();
        }
    }

    private String getWalkMessage() {

        String[] walkMessage = {
                "I just flew over {blocks} blocks thanks to mioclient.me!",
                "Я только что пролетел над {blocks} блоками с помощью mioclient.me!",
                "mioclient.me sayesinde {blocks} blok u\u00E7tum!",
                "\u6211\u521A\u521A\u7528 mioclient.me \u8D70\u4E86 {blocks} \u7C73!",
                "Dank mioclient.me bin ich gerade über {blocks} Blöcke geflogen!",
                "Jag hoppade precis över {blocks} blocks tack vare mioclient.me!",
                "Właśnie przeleciałem ponad {blocks} bloki dzięki mioclient.me!",
                "Es tikko nolidoju {blocks} blokus, paldies mioclient.me!",
                "Я щойно пролетів над {blocks} блоками завдяки mioclient.me!",
                "I just fwew ovew {blocks} bwoccs thanks to miocwient.me! :3",
                "Ho appena camminato per {blocks} blocchi grazie a mioclient.me!",
                "עכשיו עפתי {blocks} הודות ל mioclient.me!",
                "Právě jsem proletěl {blocks} bloků díky mioclient.me!"
        };

        return walkMessage[new Random().nextInt(walkMessage.length)];
    }

    private String getBreakMessage() {

        String[] breakMessage = {
                "I just destroyed {amount} {name} with the power of mioclient.me!",
                "Я только что разрушил {amount} {name} с помощью mioclient.me!",
                "Az \u00F6nce {amount} tane {name} k\u0131rd\u0131m. Te\u015Eekk\u00FCrler mioclient.me!",
                "\u6211\u521A\u521A\u7528 mioclient.me \u7834\u574F\u4E86 {amount} {name}!",
                "Ich habe gerade {amount} {name} mit der Kraft von mioclient.me zerstört!",
                "Jag förstörde precis {amount} {name} tack vare mioclient.me!",
                "Właśnie zniszczyłem {amount} {name} za pomocą mioclient.me",
                "Es tikko salauzu {amount} {name} ar spēku mioclient.me!",
                "Я щойно знищив {amount} {name} за допомогою mioclient.me!",
                "I just destwoyed {amount} {name} with the powew of miocwient.me! :3",
                "Ho appena distrutto {amount} {name} grazie al potere di mioclient.me!",
                "בדיוק חצבתי {amount} {name} בעזרת הכוח של mioclient.me!",
                "Právě jsem zničil {amount} {name} s mocí mioclient.me!"
        };

        return breakMessage[new Random().nextInt(breakMessage.length)];
    }

    private String getEatMessage() {

        String[] eatMessage = {
                "I just ate {amount} {name} thanks to mioclient.me!",
                "Я только что съел {amount} {name} с помощью mioclient.me!",
                "Tam olarak {amount} tane {name} yedim. Te\u015Eekk\u00FCrler mioclient.me",
                "\u6211\u521A\u7528 mioclient.me \u5403\u4E86 {amount} \u4E2A {name}!",
                "Ich habe gerade {amount} {name} dank mioclient.me gegessen!",
                "Jag åt precis {amount} {name} tack vare mioclient.me",
                "Właśnie zjadłem {amount} {name} dzięki mioclient.me",
                "Es tikko apēdu {amount} {name} paldies mioclient.me",
                "Я щойно з’їв {amount} {name} завдяки mioclient.me!",
                "I just ate {amount} {name} thanks to miocwient.me! ^-^",
                "Ho appena mangiato {amount} {name} grazie a mioclient.me!",
                "כרגע אכלתי {amount} {name} הודות לmioclient.me!" ,
                "Právě jsem snědl {amount} {name} díky mioclient.me"
        };

        return eatMessage[new Random().nextInt(eatMessage.length)];
    }
}

