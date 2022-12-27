
package me.mioclient.mod.modules.impl.combat;

import me.mioclient.api.events.impl.Render3DEvent;
import me.mioclient.api.events.impl.UpdateWalkingPlayerEvent;
import me.mioclient.api.managers.Managers;
import me.mioclient.api.util.entity.EntityUtil;
import me.mioclient.api.util.math.MathUtil;
import me.mioclient.api.util.math.Timer;
import me.mioclient.api.util.render.ColorUtil;
import me.mioclient.api.util.render.RenderUtil;
import me.mioclient.mod.modules.Category;
import me.mioclient.mod.modules.Module;
import me.mioclient.mod.modules.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class Aura extends Module {

    public static Aura INSTANCE;
    
    private final Setting<Page> page =
            add(new Setting<>("Settings", Page.GLOBAL));

    //Global

    private final Setting<TargetMode> targetMode =
            add(new Setting<>("Filter", TargetMode.SMART, v -> page.getValue() == Page.GLOBAL));
    public final Setting<Float> range =
            add(new Setting<>("Range", 6.0f, 0.1f, 7.0f, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Float> wallRange =
            add(new Setting<>("WallRange", 6.0f, 0.1f, 7.0f, v -> page.getValue() == Page.GLOBAL));

    private final Setting<Boolean> rotate =
            add(new Setting<>("Rotate", true, v -> page.getValue() == Page.GLOBAL).setParent());
    private final Setting<Boolean> lookBack =
            add(new Setting<>("LookBack", true, v -> page.getValue() == Page.GLOBAL && rotate.isOpen()));
    private final Setting<Float> yawStep =
            add(new Setting<>("YawStep", 0.3f, 0.1f, 1.0f, v -> page.getValue() == Page.GLOBAL && rotate.isOpen()));
    private final Setting<Float> pitchAdd =
            add(new Setting<>("PitchAdd", 0.0f, 0.0f, 25.0f, v -> page.getValue() == Page.GLOBAL && rotate.isOpen()));
    private final Setting<Boolean> randomPitch =
            add(new Setting<>("RandomizePitch", false, v -> page.getValue() == Page.GLOBAL && rotate.isOpen()));
    private final Setting<Float> amplitude =
            add(new Setting<>("Amplitude", 3.0f, -5.0f, 5.0f, v -> page.getValue() == Page.GLOBAL && rotate.isOpen() && randomPitch.getValue()));

    private final Setting<Boolean> oneEight =
            add(new Setting<>("OneEight", false, v -> page.getValue() == Page.GLOBAL).setParent());
    private final Setting<Float> minCps =
            add(new Setting<>("MinCps", 6.0f, 0.0f, 20.0f, v -> page.getValue() == Page.GLOBAL && oneEight.isOpen()));
    private final Setting<Float> maxCps =
            add(new Setting<>("MaxCps", 9.0f, 0.0f, 20.0f, v -> page.getValue() == Page.GLOBAL && oneEight.isOpen()));

    private final Setting<Float> randomDelay =
            add(new Setting<>("RandomDelay", 0.0f, 0.0f, 5.0f, v -> page.getValue() == Page.GLOBAL));

    private final Setting<Boolean> fovCheck =
            add(new Setting<>("FovCheck", false, v -> page.getValue() == Page.GLOBAL).setParent());
    private final Setting<Float> angle =
            add(new Setting<>("Angle", 180.0f, 0.0f, 180.0f, v -> page.getValue() == Page.GLOBAL && fovCheck.isOpen()));
    private final Setting<Boolean> stopSprint =
            add(new Setting<>("StopSprint", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> armorBreak =
            add(new Setting<>("ArmorBreak", false, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> whileEating =
            add(new Setting<>("WhileEating", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> weaponOnly =
            add(new Setting<>("WeaponOnly", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> tpsSync =
            add(new Setting<>("TpsSync", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> packet =
            add(new Setting<>("Packet", false, v -> page.getValue() == Page.ADVANCED));
    private final Setting<Boolean> swing =
            add(new Setting<>("Swing", true, v -> page.getValue() == Page.GLOBAL));
    private final Setting<Boolean> sneak =
            add(new Setting<>("Sneak", false, v -> page.getValue() == Page.ADVANCED));

    private final Setting<RenderMode> render =
            add(new Setting<>("Render", RenderMode.JELLO, v -> page.getValue() == Page.GLOBAL));

    //Targets

    private final Setting<Float> targetHealth =
            add(new Setting<>("Health", 6.0f, 0.1f, 36.0f, v -> targetMode.getValue() == TargetMode.SMART && page.getValue() == Page.TARGETS));

    private final Setting<Boolean> players =
            add(new Setting<>("Players", true, v -> page.getValue() == Page.TARGETS));
    private final Setting<Boolean> animals =
            add(new Setting<>("Animals", false, v -> page.getValue() == Page.TARGETS));
    private final Setting<Boolean> neutrals =
            add(new Setting<>("Neutrals", false, v -> page.getValue() == Page.TARGETS));
    private final Setting<Boolean> others =
            add(new Setting<>("Others", false, v -> page.getValue() == Page.TARGETS));
    private final Setting<Boolean> projectiles =
            add(new Setting<>("Projectiles", false, v -> page.getValue() == Page.TARGETS));
    private final Setting<Boolean> hostiles =
            add(new Setting<>("Hostiles", true, v -> page.getValue() == Page.TARGETS).setParent());
    private final Setting<Boolean> onlyGhasts =
            add(new Setting<>("OnlyGhasts", false, v -> hostiles.isOpen() && page.getValue() == Page.TARGETS));

    //Advanced

    private final Setting<Boolean> teleport =
            add(new Setting<>("Teleport", false, v -> page.getValue() == Page.ADVANCED).setParent());
    private final Setting<Float> teleportRange =
            add(new Setting<>("TpRange", 15.0f, 0.1f, 50.0f, v -> teleport.isOpen() && page.getValue() == Page.ADVANCED));
    private final Setting<Boolean> lagBack =
            add(new Setting<>("LagBack", true, v -> teleport.isOpen() && page.getValue() == Page.ADVANCED));

    private final Setting<Boolean> delay32k =
            add(new Setting<>("32kDelay", false, v -> page.getValue() == Page.ADVANCED));
    private final Setting<Integer> time32k =
            add(new Setting<>("32kTime", 5, 1, 50, v -> page.getValue() == Page.ADVANCED));
    private final Setting<Boolean> multi32k =
            add(new Setting<>("Multi32k", false, v -> page.getValue() == Page.ADVANCED));
    private final Setting<Integer> packetAmount32k =
            add(new Setting<>("32kPackets", 2, v -> !delay32k.getValue() && page.getValue() == Page.ADVANCED));

    private final Timer timer = new Timer();

    protected static Entity target;

    public Aura() {
        super("Aura", "Attacks entities in radius.", Category.COMBAT, true);
        INSTANCE = this;
    }

    private enum Page {
        GLOBAL,
        TARGETS,
        ADVANCED
    }

    private enum RenderMode {
        OLD,
        JELLO,
        OFF
    }

    private enum TargetMode {
        FOCUS,
        HEALTH,
        SMART
    }

    @Override
    public String getInfo() {
        String modeInfo = Managers.TEXT.normalizeCases(targetMode.getValue());
        String targetInfo = target instanceof EntityPlayer ? ", " + (target.getName()) : "";

        return modeInfo + targetInfo;
    }

    @Override
    public void onRender3D(Render3DEvent event) {

        if (target != null) {

            if (render.getValue() == RenderMode.OLD) {

                RenderUtil.drawEntityBoxESP(
                        target,
                        Managers.COLORS.getCurrent(),
                        true,
                        new Color(255, 255, 255, 130),
                        0.7f,
                        true,
                        true,
                        35);

            } else if (render.getValue() == RenderMode.JELLO) {

                double everyTime = 1500;
                double drawTime = (System.currentTimeMillis() % everyTime);
                boolean drawMode = drawTime > (everyTime / 2);
                double drawPercent = drawTime / (everyTime / 2);

                if (!drawMode) {
                    drawPercent = 1 - drawPercent;
                } else {
                    drawPercent -= 1;
                }

                drawPercent = easeInOutQuad(drawPercent);

                mc.entityRenderer.disableLightmap();
                glPushMatrix();
                glDisable(GL_TEXTURE_2D);
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                glEnable(GL_LINE_SMOOTH);
                glEnable(GL_BLEND);

                glDisable(GL_DEPTH_TEST);
                glDisable(GL_CULL_FACE);
                glShadeModel(7425);
                mc.entityRenderer.disableLightmap();

                double radius = target.width;
                double height = target.height + 0.1;

                double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * mc.getRenderPartialTicks() - mc.renderManager.viewerPosX;
                double y = (target.lastTickPosY + (target.posY - target.lastTickPosY) * mc.getRenderPartialTicks() - mc.renderManager.viewerPosY) + height * drawPercent;
                double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * mc.getRenderPartialTicks() - mc.renderManager.viewerPosZ;
                double eased = (height / 3) * ((drawPercent > 0.5) ? 1 - drawPercent : drawPercent) * ((drawMode) ? -1 : 1);

                for (int segments = 0; segments < 360; segments += 5) {
                    Color color = Managers.COLORS.isRainbow() ? Managers.COLORS.getRainbow() : Managers.COLORS.getCurrent();

                    double x1 = x - Math.sin(segments * Math.PI / 180F) * radius;
                    double z1 = z + Math.cos(segments * Math.PI / 180F) * radius;
                    double x2 = x - Math.sin((segments - 5) * Math.PI / 180F) * radius;
                    double z2 = z + Math.cos((segments - 5) * Math.PI / 180F) * radius;

                    glBegin(GL_QUADS);

                    glColor4f(ColorUtil.pulseColor(color, 200, 1).getRed() / 255.0f, ColorUtil.pulseColor(color, 200, 1).getGreen() / 255.0f, ColorUtil.pulseColor(color, 200, 1).getBlue() / 255.0f, 0.0f);
                    glVertex3d(x1, y + eased, z1);
                    glVertex3d(x2, y + eased, z2);

                    glColor4f(ColorUtil.pulseColor(color, 200, 1).getRed() / 255.0f, ColorUtil.pulseColor(color, 200, 1).getGreen() / 255.0f, ColorUtil.pulseColor(color, 200, 1).getBlue() / 255.0f, 200.0f);

                    glVertex3d(x2, y, z2);
                    glVertex3d(x1, y, z1);
                    glEnd();

                    glBegin(GL_LINE_LOOP);
                    glVertex3d(x2, y, z2);
                    glVertex3d(x1, y, z1);
                    glEnd();
                }

                glEnable(GL_CULL_FACE);
                glShadeModel(7424);
                glColor4f(1f, 1f, 1f, 1f);
                glEnable(GL_DEPTH_TEST);
                glDisable(GL_LINE_SMOOTH);
                glDisable(GL_BLEND);
                glEnable(GL_TEXTURE_2D);
                glPopMatrix();
            }
        }
    }

    @Override
    public void onTick() {
        if (!rotate.getValue()) {
            doAura();
        }

        if (maxCps.getValue() < minCps.getValue()) {
            maxCps.setValue(minCps.getValue());
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent event) {
        if (event.getStage() == 0 && rotate.getValue()) {
            
            if (target != null) {
                float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), target.getPositionEyes(mc.getRenderPartialTicks()));
                float[] newAngle = Managers.ROTATIONS.injectYawStep(angle, yawStep.getValue());

                Managers.ROTATIONS.setRotations(newAngle[0],
                        newAngle[1] + pitchAdd.getValue() + (randomPitch.getValue() ? ((float) Math.random() * amplitude.getValue()) : 0.0f));
            }
        }
        doAura();
    }

    private void doAura() {
        if (weaponOnly.getValue() && !EntityUtil.isHoldingWeapon(mc.player)) {
            target = null;
            return;
        }

        int wait = (oneEight.getValue() || (EntityUtil.isHolding32k(mc.player) && !delay32k.getValue())) ?
                (int) (MathUtil.randomBetween(minCps.getValue(), maxCps.getValue()) - new Random().nextInt(10) + new Random().nextInt(10) * 100
                        * (tpsSync.getValue() ? Managers.SERVER.getTpsFactor() : 1.0f)) :

                ((int) (EntityUtil.getHitCoolDown(mc.player) + ((float) Math.random() * randomDelay.getValue() * 100)
                        * (tpsSync.getValue() ? Managers.SERVER.getTpsFactor() : 1.0f)));

        if (!timer.passedMs(wait) || (!whileEating.getValue() && mc.player.isHandActive() && (!mc.player.getHeldItemOffhand().getItem().equals(Items.SHIELD) || mc.player.getActiveHand() != EnumHand.OFF_HAND))) {
            return;
        }

        if (targetMode.getValue() != TargetMode.FOCUS || target == null || (mc.player.getDistanceSq(target) >= MathUtil.square(range.getValue()) && (!teleport.getValue() || mc.player.getDistanceSq(target) >= MathUtil.square(teleportRange.getValue()))) || (!mc.player.canEntityBeSeen(target) && !EntityUtil.isFeetVisible(target) && mc.player.getDistanceSq(target) >= MathUtil.square(wallRange.getValue()) && !teleport.getValue())) {
            target = getTarget();
        }

        if (target == null) {
            return;
        }

        if (teleport.getValue()) {
            Managers.POSITION.setPositionPacket(target.posX, EntityUtil.isFeetVisible(target) ? target.posY : (target.posY + target.getEyeHeight()), target.posZ, true, true, !lagBack.getValue());
        }

        if (EntityUtil.isHolding32k(mc.player) && !delay32k.getValue()) {
            if (multi32k.getValue()) {
                for (EntityPlayer player : mc.world.playerEntities) {
                    if (EntityUtil.isValid(player, range.getValue())) {
                        teekayAttack(player);
                    }
                }
            } else {
                teekayAttack(target);
            }
            timer.reset();
            return;
        }

        if (armorBreak.getValue()) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 9, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
            Managers.INTERACTIONS.attackEntity(target, packet.getValue(), swing.getValue());

            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, 9, mc.player.inventory.currentItem, ClickType.SWAP, mc.player);
            Managers.INTERACTIONS.attackEntity(target, packet.getValue(), swing.getValue());

        } else {
            boolean sneaking = mc.player.isSneaking();
            boolean sprinting = mc.player.isSprinting();

            if (sneak.getValue()) {
                if (sneaking) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                }
                if (sprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                }
            }

            Managers.INTERACTIONS.attackEntity(target, packet.getValue(), swing.getValue());

            if (sneak.getValue()) {
                if (sprinting) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                }
                if (sneaking) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }
            }

            if (stopSprint.getValue()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }
        timer.reset();

        if (rotate.getValue() && lookBack.getValue()) {
            Managers.ROTATIONS.resetRotations();
        }
    }

    private void teekayAttack(Entity entity) {
        for (int i = 0; i < packetAmount32k.getValue(); ++i) {
            startEntityAttackThread(entity, i * time32k.getValue());
        }
    }

    private void startEntityAttackThread(Entity entity, int time) {
        new Thread(() -> {
            Timer timer = new Timer();
            timer.reset();
            try {
                Thread.sleep(time);
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            Managers.INTERACTIONS.attackEntity(entity, true, swing.getValue());
        }).start();
    }

    private Entity getTarget() {
        Entity target = null;
        double distance = (teleport.getValue() ? teleportRange.getValue() : ((double) (float) range.getValue()));
        double maxHealth = 36.0;

        for (Entity entity : mc.world.loadedEntityList) {

            if ((players.getValue() && entity instanceof EntityPlayer) || (animals.getValue() && EntityUtil.isPassive(entity)) || (neutrals.getValue() && EntityUtil.isNeutralMob(entity)) || (hostiles.getValue() && EntityUtil.isMobAggressive(entity)) || (hostiles.getValue() && onlyGhasts.getValue() && entity instanceof EntityGhast) || (others.getValue() && EntityUtil.isVehicle(entity)) || (projectiles.getValue() && EntityUtil.isProjectile(entity))) {

                if (EntityUtil.isLiving(entity) && !EntityUtil.isValid(entity, distance)) {
                    continue;
                }

                if (!teleport.getValue() && !mc.player.canEntityBeSeen(entity) && !EntityUtil.isFeetVisible(entity) && mc.player.getDistanceSq(entity) > MathUtil.square(wallRange.getValue())) {
                    continue;
                }

                if (fovCheck.getValue() && !isInFov(entity, angle.getValue().intValue())) {
                    continue;
                }

                if (target == null) {
                    target = entity;
                    distance = mc.player.getDistanceSq(entity);
                    maxHealth = EntityUtil.getHealth(entity);

                } else {
                    if (entity instanceof EntityPlayer && EntityUtil.isArmorLow((EntityPlayer)entity, 15)) {
                        target = entity;
                        break;
                    }

                    if (targetMode.getValue() == TargetMode.SMART && EntityUtil.getHealth(entity) < targetHealth.getValue()) {
                        target = entity;
                        break;
                    }

                    if (targetMode.getValue() != TargetMode.HEALTH && mc.player.getDistanceSq(entity) < distance) {
                        target = entity;
                        distance = mc.player.getDistanceSq(entity);
                        maxHealth = EntityUtil.getHealth(entity);
                    }

                    if (targetMode.getValue() != TargetMode.HEALTH || EntityUtil.getHealth(entity) >= maxHealth) {
                        continue;
                    }

                    target = entity;
                    distance = mc.player.getDistanceSq(entity);
                    maxHealth = EntityUtil.getHealth(entity);
                }
            }
        }
        return target;
    }

    private boolean isInFov(Entity entity, float angle) {
        double x = entity.posX - mc.player.posX;
        double z = entity.posZ - mc.player.posZ;
        double yaw = Math.atan2(x, z) * 57.29577951308232D;
        yaw = -yaw;
        angle = (float)(angle * 0.5D);
        double angleDifference = ((mc.player.rotationYaw - yaw) % 360.0D + 540.0D) % 360.0D - 180.0D;
        return ((angleDifference > 0.0D) && (angleDifference < angle)) || ((-angle < angleDifference) && (angleDifference < 0.0D));
    }

    private double easeInOutQuad(double x) {
        return (x < 0.5) ? 2 * x * x : 1 - Math.pow((-2 * x + 2), 2) / 2;
    }
}