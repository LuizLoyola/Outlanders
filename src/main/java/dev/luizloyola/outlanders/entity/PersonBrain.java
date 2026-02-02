package dev.luizloyola.outlanders.entity;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import dev.luizloyola.outlanders.helper.ParticleHelper;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.luizloyola.outlanders.entity.PersonBrain.Pathfinder.NodeType.*;

public class PersonBrain {
    private static final Gson gson = new Gson();
    private final PersonEntity entity;
    private final World world;
    private final PersonIdentity identity;
    private long tickNumber;
    private final int maxDrop = 3;

    private BlockPos walkTarget;
    private boolean walkTargetFailed;
    private Pathfinder.Path path;
    private Pathfinder pathfinder;

    public PersonBrain(PersonEntity entity, PersonIdentity identity) {
        this.entity = entity;
        this.world = entity.getEntityWorld();
        this.identity = identity;
    }

    public PersonBrain(PersonEntity entity) {
        this(entity, PersonIdentity.random());
    }

    public static PersonBrain fromJson(PersonEntity entity, final String json) {
        var identity = gson.fromJson(json, PersonIdentity.class);
        return new PersonBrain(entity, identity);
    }

    public String toJson() {
        return gson.toJson(this.identity);
    }

    public PersonIdentity getIdentity() {
        return this.identity;
    }

    public void tick() {
        if (this.isClient()) {
            return;
        }

        this.tickNumber++;

        if (this.walkTarget != null && !this.walkTargetFailed) {
            // want to go somewhere
            if (this.path == null) {
                // has no path there

                if (this.pathfinder == null) {
                    this.pathfinder = new Pathfinder(this.entity.getBlockPos(), this.walkTarget);
                    this.walkTargetFailed = false;
                }

//                if (this.tickNumber % 1 != 0) {
//                    return; // pathfinder tick rate limit
//                }

                Pathfinder.Path path = null;
                for (int i = 0; i < 20; i++) {
                    path = this.pathfinder.tick();
                    if (path != null) {
                        break;
                    }
                }

                if (path != null) {
                    this.path = path;
                    this.pathfinder = null;
                } else if (this.pathfinder.failed) {
                    this.walkTargetFailed = true;
                    this.pathfinder = null;
                }
            }
        }

        this.tickDebugVisuals();
    }

    private boolean isClient() {
        return this.world.isClient();
    }

    private void tickDebugVisuals() {
        var targetPos = PersonBrain.this.getWalkTarget();
        if (targetPos != null) {
            if (this.tickNumber % 5 == 0) {
                ParticleHelper.particleBox(this.getServerWorld(), ParticleTypes.SOUL_FIRE_FLAME, targetPos);
            }
        }

        if (this.path != null) {
            for (var node : this.path.nodes) {
                var prevPos = node.parent != null ? node.parent.blockPos.toBottomCenterPos() : this.entity.getEntityPos();
                ParticleHelper.particleLine(this.getServerWorld(), ParticleTypes.SMALL_FLAME, node.blockPos.toBottomCenterPos(), prevPos, 2);
            }
        }
    }

    private ServerWorld getServerWorld() {
        return (ServerWorld) this.entity.getEntityWorld();
    }

    public BlockPos getWalkTarget() {
        return this.walkTarget;
    }

    public void setWalkTarget(BlockPos walkTarget) {
        this.walkTarget = walkTarget;
        this.path = null;
        this.pathfinder = null;
    }

    public class Pathfinder {
        private final BlockPos originPos;
        private final BlockPos targetPos;
        private final List<Node> nodes = new ArrayList<>();
        private final List<Long> open = new ArrayList<>();
        private final List<Long> closed = new ArrayList<>();
        private long tickCount;
        private long nodeCounter = 0;
        private boolean failed = false;

        public Pathfinder(BlockPos originPos, BlockPos targetPos) {
            this.originPos = originPos;
            this.targetPos = targetPos;

            var node = this.nodeAt(originPos, null);
            this.open.add(node.id);
        }

        private Node nodeAt(BlockPos pos, Node parent) {
            var node = this.nodes.stream().filter(n -> n.blockPos.equals(pos)).findFirst().orElse(null);

            if (node != null) {
                return node;
            }

            node = new Node(this.nodeCounter++, pos, parent);
            this.nodes.add(node);
            return node;
        }

        private ServerWorld getWorld() {
            return (ServerWorld) PersonBrain.this.entity.getEntityWorld();
        }

        public Path tick() {
            // get closest (to target) open node
            var node = this.getCheaperOpenNode(this.targetPos);

            if (node == null) {
                // TODO: what to do?
                this.failed = true;
                return null;
            }

            this.open.remove(node.id);
            this.closed.add(node.id);

            if (node.blockPos.equals(this.targetPos)) {
                // this is the target node!
                return this.pathTo(node);
            }

            for (Node neighbor : this.getNeighbors(node)) {
                if (this.closed.contains(neighbor.id)) continue;

                if (this.pathCostTo(node) + this.getNodeEnterCost(neighbor) < this.pathCostTo(neighbor) || !this.open.contains(neighbor.id)) {
                    // replace parent if cheaper path found
                    neighbor.parent = node;
                    if (!this.open.contains(neighbor.id)) this.open.add(neighbor.id);
                }
            }

            return null;
        }

        private Path pathTo(Node node) {
            List<Node> nodes = new ArrayList<>();
            Node current = node;
            while (true) {
                nodes.addFirst(current);
                if (current.parent == null) break;
                current = current.parent;
            }

            return new Path(ImmutableList.copyOf(nodes));
        }

        private double pathCostTo(Node node) {
            return node != null ? this.getNodeEnterCost(node) + this.pathCostTo(node.parent) : 0;
        }

        private List<Node> getNeighbors(Node node) {
            BlockPos pos = node.blockPos;

            List<Node> neighbors = new ArrayList<>();

            Consumer<BlockPos> addNeighbor = (BlockPos neighPos) -> {
                if (neighPos == null) return;
                neighbors.add(this.nodeAt(neighPos, node));
            };

            addNeighbor.accept(this.getCardinalNeighbor(pos, pos.north()));
            addNeighbor.accept(this.getCardinalNeighbor(pos, pos.east()));
            addNeighbor.accept(this.getCardinalNeighbor(pos, pos.south()));
            addNeighbor.accept(this.getCardinalNeighbor(pos, pos.west()));

            addNeighbor.accept(this.getDiagonalNeighbor(pos, pos.north(), pos.east(), pos.north().east()));
            addNeighbor.accept(this.getDiagonalNeighbor(pos, pos.east(), pos.south(), pos.south().east()));
            addNeighbor.accept(this.getDiagonalNeighbor(pos, pos.south(), pos.west(), pos.south().west()));
            addNeighbor.accept(this.getDiagonalNeighbor(pos, pos.west(), pos.north(), pos.north().west()));


            // ignoring as it is unstable
//            addNeighbor.accept(getCardinalLeapNeighbor(pos, pos.north(), pos.north().north()));
//            addNeighbor.accept(getCardinalLeapNeighbor(pos, pos.east(), pos.east().east()));
//            addNeighbor.accept(getCardinalLeapNeighbor(pos, pos.south(), pos.south().south()));
//            addNeighbor.accept(getCardinalLeapNeighbor(pos, pos.west(), pos.west().west()));
//
//            addNeighbor.accept(getCardinalLongLeapNeighbor(pos, pos.north(), pos.north().north(), pos.north().north().north()));
//            addNeighbor.accept(getCardinalLongLeapNeighbor(pos, pos.east(), pos.east().east(), pos.east().east().east()));
//            addNeighbor.accept(getCardinalLongLeapNeighbor(pos, pos.south(), pos.south().south(), pos.south().south().south()));
//            addNeighbor.accept(getCardinalLongLeapNeighbor(pos, pos.west(), pos.west().west(), pos.west().west().west()));

            return neighbors;
        }

        private BlockPos getDiagonalNeighbor(BlockPos pos, BlockPos left, BlockPos right, BlockPos p) {
            if (this.isDanger(p)) return null;

            if (!this.isPassable(left) || !this.isPassable(right) || !this.enoughClearance(p))
                return null; //cant go diagonal if these are blocked

            if (this.isPassable(p)) {
                // same or down

                // are cardinals ok with this?
                if (!this.enoughClearance(left) || !this.enoughClearance(right)) return null; // nope, not enough clearance

                // block immediately down
                if (this.isSolidGround(p.down())) {
                    // can walk
                    return p;
                } else {
                    // down

                    var currentLeft = left.down();
                    var currentRight = right.down();

                    if (this.isPassable(currentLeft) || this.isPassable(currentRight))
                        return null; // cant go down, these arent clear

                    // check size of the drop
                    var current = p.down();
                    int i;
                    for (i = 1; i <= PersonBrain.this.maxDrop; i++) {
                        current = current.down();
                        currentLeft = currentLeft.down();
                        currentRight = currentRight.down();

                        if (!this.isPassable(currentLeft) || this.isPassable(currentRight)) return null; // wont check any further

                        if (!this.isSolidGround(current)) continue; // keep dropping

                        // drop is passable
                        return current.up();
                    }
                    // cant pass drop
                }
            } else {
                // up
                if (!this.isPassable(pos.up())) return null; // cant even jump here

                if (!this.isPassable(p.up())) return null; // 2 blocks up, cant jump

                // there's air upwards
                if (!this.enoughClearance(p.up())) return null; // theres block and air, but not enough space to stand

                // are cardinals ok?
                if (!this.enoughClearance(left.up()) || !this.enoughClearance(right.up())) return null; //nope

                return p.up();
            }

            return null;
        }

        private boolean isPassable(BlockPos pos) {
            BlockState state = PersonBrain.this.world.getBlockState(pos);
            if (state.isAir()) return true;

            return state.getCollisionShape(PersonBrain.this.world, pos).isEmpty();
        }

        private boolean enoughClearance(BlockPos pos) {
            return this.enoughClearance(pos, PersonBrain.this.entity.getHeight());
        }

        private boolean enoughClearance(BlockPos pos, float height) {
            BlockPos curr = pos;

            for (var i = 0; i < height; i++) {
                if (!this.isPassable(curr)) return false;
                curr = curr.up();
            }

            return true;
        }

        public boolean isSolidGround(BlockPos pos) {
            return PersonBrain.this.world.isTopSolid(pos, PersonBrain.this.entity);
        }

        private boolean isDanger(BlockPos pos) {
            BlockState state = PersonBrain.this.world.getBlockState(pos);

            return false;
        }


        private BlockPos getCardinalNeighbor(BlockPos pos, BlockPos p) {
            if (this.isDanger(p)) return null;

            if (this.isPassable(p)) {
                // straight or down
                if (!this.enoughClearance(p)) return null;  // not enough clearance to go forward at all


                // block immediately down
                if (this.isSolidGround(p.down())) {
                    // can pass
                    return p;
                }

                // check size of the drop
                var current = p.down();
                int i;
                for (i = 1; i <= PersonBrain.this.maxDrop; i++) {
                    current = current.down();
                    if (!this.isSolidGround(current)) continue;

                    // drop is passable
                    return current.up();
                }
                // cant pass drop
            } else {
                // up?
                if (!this.enoughClearance(pos.up())) return null; // cant even jump here

                if (!this.isPassable(p.up())) return null; // 2 blocks up, cant jump

                // there's air upwards
                if (!this.enoughClearance(p.up())) return null; // theres block and air, but not enough space to stand

                return p.up();
            }

            return null;
        }

        private @Nullable Node getCheaperOpenNode(BlockPos targetPos) {
            return this.nodes.stream().filter(n -> this.open.contains(n.id)).min(Comparator.comparingDouble(node -> {
                var costToTarget = this.getNodeCostToTarget(node, targetPos);
                var enterCost = this.getNodeEnterCost(node);
                return costToTarget + enterCost;
            })).orElse(null);
        }

        private double getNodeEnterCost(Node node) {
            return this.getCostForNodeType(this.getNodeType(node));
        }
        public static double getCostForNodeType(NodeType type) {
            if (type == CARDINAL_WALK) return 1d;
            if (type == CARDINAL_JUMP) return 1.1d;
            if (type == CARDINAL_DROP_1) return 0.9d;
            if (type == CARDINAL_DROP_2) return 0.9d;
            if (type == CARDINAL_DROP_3) return 1.3d;
            if (type == DIAGONAL_WALK) return Math.sqrt(1d * 1d * 2d);
            if (type == DIAGONAL_JUMP) return Math.sqrt(1.1d * 1.1d * 2d);
            if (type == DIAGONAL_DROP_1) return Math.sqrt(0.9d * 0.9d * 2d);
            if (type == DIAGONAL_DROP_2) return Math.sqrt(0.9d * 0.9d * 2d);
            if (type == DIAGONAL_DROP_3) return Math.sqrt(1.3d * 1.3d * 2d);
            if (type == CARDINAL_LEAP) return 0.8d * 2d;
            if (type == CARDINAL_LEAP_LONG) return 0.7d * 3d;

            return 0d;
        }

        private double getNodeCostToTarget(Node node, BlockPos targetPos) {
            return Math.sqrt(node.blockPos.getSquaredDistance(targetPos));
        }

        public void tickDebugVisuals() {
        }

        private NodeType getNodeType(Node node) {
            if (node.parent == null) return NodeType.NONE;

            var pos = node.blockPos;
            var parentPos = node.parent.blockPos;

            BlockPos pN = parentPos.north();
            BlockPos pE = parentPos.east();
            BlockPos pS = parentPos.south();
            BlockPos pW = parentPos.west();
            if (pN.equals(pos) || pE.equals(pos) || pS.equals(pos) || pW.equals(pos)) return CARDINAL_WALK;
            if (pN.north().equals(pos) || pE.east().equals(pos) || pS.south().equals(pos) || pW.west().equals(pos)) return NodeType.CARDINAL_LEAP;
            if (pN.north().north().equals(pos) || pE.east().east().equals(pos) || pS.south().south().equals(pos) || pW.west().west().equals(pos)) return NodeType.CARDINAL_LEAP_LONG;
            if (pN.up().equals(pos) || pE.up().equals(pos) || pS.up().equals(pos) || pW.up().equals(pos)) return NodeType.CARDINAL_JUMP;
            if (pN.down().equals(pos) || pE.down().equals(pos) || pS.down().equals(pos) || pW.down().equals(pos)) return NodeType.CARDINAL_DROP_1;
            if (pN.down().down().equals(pos) || pE.down().down().equals(pos) || pS.down().down().equals(pos) || pW.down().down().equals(pos)) return NodeType.CARDINAL_DROP_2;
            if (pN.down().down().down().equals(pos) || pE.down().down().down().equals(pos) || pS.down().down().down().equals(pos) || pW.down().down().down().equals(pos)) return NodeType.CARDINAL_DROP_3;
            if (pN.east().equals(pos) || pS.east().equals(pos) || pS.west().equals(pos) || pN.west().equals(pos)) return NodeType.DIAGONAL_WALK;
            if (pN.east().down().equals(pos) || pS.east().down().equals(pos) || pS.west().down().equals(pos) || pN.west().down().equals(pos)) return NodeType.DIAGONAL_DROP_1;
            if (pN.east().down().down().equals(pos) || pS.east().down().down().equals(pos) || pS.west().down().down().equals(pos) || pN.west().down().down().equals(pos)) return NodeType.DIAGONAL_DROP_2;
            if (pN.east().down().down().down().equals(pos) || pS.east().down().down().down().equals(pos) || pS.west().down().down().down().equals(pos) || pN.west().down().down().down().equals(pos)) return NodeType.DIAGONAL_DROP_3;
            if (pN.east().up().equals(pos) || pS.east().up().equals(pos) || pS.west().up().equals(pos) || pN.west().up().equals(pos)) return NodeType.DIAGONAL_JUMP;

            return NodeType.NONE;
        }

        public record Path(ImmutableList<Node> nodes) {}

        public static final class Node {
            public final long id;
            public final BlockPos blockPos;
            public @Nullable Node parent;

            public Node(long id, BlockPos blockPos, @Nullable Node parent) {
                this.id = id;
                this.blockPos = blockPos;
                this.parent = parent;
            }


            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                var that = (Node) obj;
                return this.id == that.id &&
                        Objects.equals(this.blockPos, that.blockPos) &&
                        Objects.equals(this.parent, that.parent);
            }
        }

        public enum NodeType {
            CARDINAL_WALK(true),
            CARDINAL_JUMP(false),
            CARDINAL_DROP_1(false),
            CARDINAL_DROP_2(false),
            CARDINAL_DROP_3(false),
            DIAGONAL_WALK(true),
            DIAGONAL_JUMP(false),
            DIAGONAL_DROP_1(false),
            DIAGONAL_DROP_2(false),
            DIAGONAL_DROP_3(false),
            CARDINAL_LEAP(false),
            CARDINAL_LEAP_LONG(false),
            NONE(false);

            private final boolean canAnticipate;

            NodeType(boolean canAnticipate) {
                this.canAnticipate = canAnticipate;
            }

            public boolean canAnticipate() {
                return canAnticipate;
            }

        }
    }

}
