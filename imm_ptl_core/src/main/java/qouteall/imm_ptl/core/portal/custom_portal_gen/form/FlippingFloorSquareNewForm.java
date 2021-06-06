package qouteall.imm_ptl.core.portal.custom_portal_gen.form;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import qouteall.imm_ptl.core.McHelper;
import qouteall.q_misc_util.my_util.IntBox;
import qouteall.imm_ptl.core.portal.custom_portal_gen.PortalGenInfo;
import qouteall.imm_ptl.core.portal.custom_portal_gen.SimpleBlockPredicate;
import qouteall.imm_ptl.core.portal.nether_portal.BlockPortalShape;
import qouteall.imm_ptl.core.portal.nether_portal.BreakablePortalEntity;
import qouteall.imm_ptl.core.portal.nether_portal.NetherPortalGeneration;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class FlippingFloorSquareNewForm extends HeterogeneousForm {
    public static final Codec<FlippingFloorSquareNewForm> codec = RecordCodecBuilder.create(instance -> {
        return instance.group(
            Codec.BOOL.fieldOf("generate_frame_if_not_found").forGetter(o -> o.generateFrameIfNotFound),
            SimpleBlockPredicate.codec.fieldOf("area_block").forGetter(o -> o.areaBlock),
            SimpleBlockPredicate.codec.fieldOf("frame_block").forGetter(o -> o.frameBlock)
        ).apply(instance, FlippingFloorSquareNewForm::new);
    });
    
    public FlippingFloorSquareNewForm(
        boolean generateFrameIfNotFound, SimpleBlockPredicate areaBlock,
        SimpleBlockPredicate frameBlock
    ) {
        super(generateFrameIfNotFound, areaBlock, frameBlock);
    }
    
    @Override
    public BreakablePortalEntity[] generatePortalEntitiesAndPlaceholder(PortalGenInfo info) {
        ServerWorld fromWorld = McHelper.getServerWorld(info.from);
        ServerWorld toWorld = McHelper.getServerWorld(info.to);
        NetherPortalGeneration.fillInPlaceHolderBlocks(fromWorld, info.fromShape);
        NetherPortalGeneration.fillInPlaceHolderBlocks(toWorld, info.toShape);
        return FlippingFloorSquareForm.createPortals(
            fromWorld,
            toWorld,
            info.fromShape, info.toShape
        );
    }
    
    @Override
    public boolean testThisSideShape(ServerWorld fromWorld, BlockPortalShape fromShape) {
        // only horizontal shape
        if (fromShape.axis != Direction.Axis.Y) {
            return false;
        }
    
        IntBox box = fromShape.innerAreaBox;
        BlockPos boxSize = box.getSize();
        // must be square
        return boxSize.getX() == boxSize.getZ() &&
            boxSize.getX() * boxSize.getZ() == fromShape.area.size();
    }
    
    @Override
    public PortalGenInfo getNewPortalPlacement(
        ServerWorld toWorld, BlockPos toPos,
        ServerWorld fromWorld, BlockPortalShape fromShape
    ) {
        IntBox portalPlacement = FlippingFloorSquareForm.findPortalPlacement(
            toWorld,
            fromShape.totalAreaBox.getSize(),
            toPos
        );
    
        BlockPortalShape placedShape = fromShape.getShapeWithMovedTotalAreaBox(portalPlacement);
    
        return new PortalGenInfo(
            fromWorld.getRegistryKey(), toWorld.getRegistryKey(),
            fromShape, placedShape,
            new Quaternion(
                new Vec3f(1, 0, 0),
                180,
                true
            ), 1.0
        );
        
    }
    
    @Override
    public Codec<? extends PortalGenForm> getCodec() {
        return codec;
    }
    
    @Override
    public PortalGenForm getReverse() {
        return new FlippingFloorSquareNewForm(
            generateFrameIfNotFound,
            areaBlock, frameBlock
        );
    }
}
