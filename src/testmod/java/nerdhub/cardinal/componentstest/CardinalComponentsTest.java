package nerdhub.cardinal.componentstest;

public class CardinalComponentsTest {
    public static void init() {
        System.out.println("Hello, Components!");
        ComponentType<FluidStorage> fluids = ComponentRegistry.INSTANCE.registerIfAbsent(new Identifier("componenttest:fluid"), FluidStorage.class);
        BlockComponentCallback.event(Blocks.CAULDRON).register((BlockState state, BlockView view, BlockPos pos, Direction side, ComponentType<T> type) -> {
            if (type == fluids && side == Direction.UP) {
                return type.getComponentClass().cast(CauldronFluidStorage.CACHE[state.getProperty(CauldronBlock.LEVEL)]);
            }
            return null;
        });
    }
}

interface FluidStorage extends Component {

    double getFluidAmount();
}

class CauldronFluidStorage implements FluidStorage {
    public static final FluidStorage[] CACHE = Stream.of(1, 2, 3).map(CauldronFluidStorage::new).toArray(CauldronFluidStorage[]::new);

    private double fluidAmount;

    CauldronFluidStorage(int level) {
        this.fluidAmount = 1 / level;
    }

    public double getFluidAmount() {
        return this.fluidAmount;
    }
}
