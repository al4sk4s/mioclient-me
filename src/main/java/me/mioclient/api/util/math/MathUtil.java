package me.mioclient.api.util.math;

import me.mioclient.api.util.Wrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class MathUtil implements Wrapper {

    public static float randomBetween(float min, float max) {
        return min + (new Random().nextFloat() * (max - min));
    }

    public static int randomBetween(int min, int max) {
        return min + (new Random().nextInt() * (max - min));
    }

    public static int clamp(int num, int min, int max) {
        return num < min ? min : Math.min(num, max);
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double clamp(double num, double min, double max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double square(double input) {
        return input * input;
    }

    public static Vec3d roundVec(Vec3d vec3d, int places) {
        return new Vec3d(round(vec3d.x, places), round(vec3d.y, places), round(vec3d.z, places));
    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.FLOOR);
        return bd.doubleValue();
    }

    public static float round(float value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.FLOOR);
        return bd.floatValue();
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean descending) {
        LinkedList<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        if (descending) {
            list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        } else {
            list.sort(Map.Entry.comparingByValue());
        }
        LinkedHashMap result = new LinkedHashMap();
        for (Map.Entry entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static float[] calcAngleNoY(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difZ = to.z - from.z;
        return new float[] { (float)MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0) };
    }

    public static float[] calcAngle(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt(difX * difX + difZ * difZ);
        return new float[]{(float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0), (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)))};
    }

    public static List<Vec3d> getBlockBlocks(Entity entity) {
        ArrayList<Vec3d> vec3ds = new ArrayList<>();
        AxisAlignedBB bb = entity.getEntityBoundingBox();

        double y = entity.posY;
        double minX = round(bb.minX, 0);
        double minZ = round(bb.minZ, 0);
        double maxX = round(bb.maxX, 0);
        double maxZ = round(bb.maxZ, 0);

        if (minX != maxX) {

            vec3ds.add(new Vec3d(minX, y, minZ));
            vec3ds.add(new Vec3d(maxX, y, minZ));

            if (minZ != maxZ) {

                vec3ds.add(new Vec3d(minX, y, maxZ));
                vec3ds.add(new Vec3d(maxX, y, maxZ));
                return vec3ds;
            }

        } else if (minZ != maxZ) {

            vec3ds.add(new Vec3d(minX, y, minZ));
            vec3ds.add(new Vec3d(minX, y, maxZ));
            return vec3ds;
        }
        vec3ds.add(entity.getPositionVector());
        return vec3ds;
    }

    public static Vec3d[] convertVectors(Vec3d vec3d, Vec3d[] input) {

        Vec3d[] out = new Vec3d[input.length];

        for (int i = 0; i < input.length; ++i) {
            out[i] = vec3d.add(input[i]);
        }
        return out;
    }

    public static float animate(float in, float target, float delta) {

        float out = (target - in) / Math.max((float) Minecraft.getDebugFPS(), 5.0f) * 15.0f;

        if (out > 0.0f) {
            out = Math.max(delta, out);
            out = Math.min(target - in, out);
        } else if (out < 0.0f) {
            out = Math.min(-delta, out);
            out = Math.max(target - in, out);
        }

        return in + out;
    }

    public static double animate(double target, double current, double delta) {
        boolean larger;

        larger = target > current;

        if (delta < 0.0) {
            delta = 0.0;
        } else if (delta > 1.0) {
            delta = 1.0;
        }
        double dif = Math.max(target, current) - Math.min(target, current);
        double factor = dif * delta;

        if (factor < 0.1) {
            factor = 0.1;
        }

        if (larger) {
            current += factor;
        } else {
            current -= factor;
        }

        return current;
    }

    public static Integer increaseNumber(int input, int target, int delta) {
        if (input < target)
            return input + delta;
        return target;
    }

    public static Integer decreaseNumber(int input, int target, int delta) {
        if (input > target)
            return input - delta;
        return target;
    }

    public static Float increaseNumber(float input, float target, float delta) {
        if (input < target)
            return input + delta;
        return target;
    }

    public static Float decreaseNumber(float input, float target, float delta) {
        if (input > target)
            return input - delta;
        return target;
    }

    public static double normalize(double value, double min, double max) {
        return (value - min) / (max - min);
    }
}