package com.sandymandy.pleasurehorizons.settlement.building;

import com.sandymandy.pleasurehorizons.settlement.Settlement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class BuildingScanner {
    private final Settlement settlement;

    public BuildingScanner(Settlement settlement) {
        this.settlement = settlement;
    }

    public void scanForBuilding(Level world, BlockPos origin, BlockPos doorPos, BlockPos tagPos, BuildingType type, Player player) {
        // Stub
    }

    public boolean reScanVerify(Level world, BlockPos doorPos, BuildingType type, Direction tagFacing) {
        return false;
    }
}
