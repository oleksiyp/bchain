package bchain_poc.tree;

import java.util.ArrayList;
import java.util.List;

public class Tree {
    private Tree root;
    private final String[] sequence;
    private final List<Tree> endings;

    public Tree(String[] sequence) {
        this.root = this;
        this.sequence = sequence;
        endings = new ArrayList<>();
    }

    public static Tree seq(String... items) {
        return new Tree(items);
    }

    public Tree alt(Tree tree) {
        tree.root = this.root;
        endings.add(tree);
        return tree;
    }

    public Tree alt(String... items) {
        alt(seq(items));
        return this;
    }

    public Tree visitRoot(TreeVisitor visitor) {
        root().visit(visitor);
        return root;
    }

    private Tree visit(TreeVisitor visitor) {
        for (int i = 0; i < sequence.length; i++) {
            visitor.enter(sequence[i]);
        }
        for (Tree ending : endings) {
            ending.visit(visitor);
        }
        for (int i = sequence.length - 1; i >= 0; i--) {
            visitor.leave(sequence[i]);
        }
        return this;
    }

    public Tree root() {
        return root;
    }
}
