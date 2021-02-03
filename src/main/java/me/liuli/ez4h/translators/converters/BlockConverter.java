package me.liuli.ez4h.translators.converters;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.steveice10.mc.protocol.data.game.chunk.NibbleArray3d;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockState;
import lombok.Getter;
import me.liuli.ez4h.utils.BedrockUtils;

import java.util.HashMap;
import java.util.Map;

public class BlockConverter {
    @Getter
    private final NibbleArray3d fullLight;
    @Getter
    private final NibbleArray3d noLight;

    private final Map<String, BlockState> blockStateMap = new HashMap<>();
    private final Map<String, Integer> blockLightMap = new HashMap<>();
    private final JSONObject runtimeBlockMap;
    private final BlockState nullBlock = new BlockState(1, 0);

    public BlockConverter(JSONArray blockArray, JSONObject blockRuntimeData) {
        for (Object jsonObject : blockArray) {
            JSONObject json = (JSONObject) jsonObject;
            blockLightMap.put(json.getString("name"), json.getInteger("light"));
            blockStateMap.put(json.getString("name"), new BlockState(json.getInteger("id"), json.getInteger("meta")));
        }
        runtimeBlockMap = blockRuntimeData;
        fullLight = new NibbleArray3d(4096);
        noLight = new NibbleArray3d(4096);
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    fullLight.set(x, y, z, 15);
                    noLight.set(x, y, z, 0);
                }
            }
        }
    }

    public String getBedrockNameByRuntime(int runtime) {
        String result = (String) runtimeBlockMap.get(runtime);
        if (result == null) {
            result = "minecraft:stone[stone_type=stone]";
        }
        return result;
    }

    public BlockState getBlockByName(String name) {
        BlockState result = blockStateMap.get(name);
        if (result == null) {
            result = nullBlock;
        }
        return result;
    }

    public int getBlockLightByName(String name) {
        Integer result = blockLightMap.get(name);
        if (result == null) {
            result = 0;
        }
        return result;
    }

    //TODO:Make this faster
    //Process time TOO LONG :(
    public void addLight(NibbleArray3d lightArray, int light, int X, int Y, int Z) {
        int posX = Math.max((X - light), 0), posXend = Math.min(X + light, 15),
                posY = Math.max((Y - light), 0), posYend = Math.min(Y + light, 15),
                posZ = Math.max((Z - light), 0), posZend = Math.min(Z + light, 15);
        for (int x = posX; x <= posXend; x++) {
            for (int y = posY; y <= posYend; y++) {
                for (int z = posZ; z <= posZend; z++) {
                    int reLight = (int) Math.max(light - BedrockUtils.calcDistance(X, Y, Z, x, y, z), 0);
                    if (lightArray.get(x, y, z) < reLight) {
                        lightArray.set(x, y, z, reLight);
                    }
                }
            }
        }
    }
}
