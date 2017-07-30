package bchain.processing.path;

import bchain.domain.Hash;
import bchain.dao.BlockDAO;
import bchain.dao.BlockLevelDAO;
import bchain.tree.TreeVisitor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Stack;

import static bchain.processing.path.PathItem.Type.POP;
import static bchain.processing.path.PathItem.Type.PUSH;
import static bchain.tree.Tree.seq;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class PathFinderTest {
    @Mock
    BlockDAO blockDAO;

    @Mock
    BlockLevelDAO levelDAO;

    PathFinder pathFinder;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        pathFinder = new PathFinder(blockDAO, levelDAO);

    }

    @Test
    public void findPath() throws Exception {
        // given
        seq("a", "b", "c")
                .alt("d", "e", "f")
                .alt("g", "h", "i")
                .alt(seq("j", "k", "l")
                        .alt("m", "n", "o")
                        .alt("p", "q", "r"))
        .visitRoot(setup(blockDAO))
        .visitRoot(setup(levelDAO));

        // when
        List<PathItem> path = pathFinder.findPath(Hash.of("h"), Hash.of("n"));

        // then
        assertThat(path)
                .containsExactly(
                        pop("h"),
                        pop("g"),
                        push("j"),
                        push("k"),
                        push("l"),
                        push("m"),
                        push("n"));
    }

    private PathItem push(String name) {
        return new PathItem(PUSH, Hash.of(name));
    }

    private PathItem pop(String name) {
        return new PathItem(POP, Hash.of(name));
    }

    private static TreeVisitor setup(BlockLevelDAO levelDAO) {
        class Visitor implements TreeVisitor {
            int level;

            @Override
            public void enter(String s) {
                Hash current = Hash.of(s);

                when(levelDAO.getBlockLevel(eq(current))).thenReturn(level);

                level++;
            }

            @Override
            public void leave(String s) {
                level--;
            }
        }

        return new Visitor();
    }

    private static TreeVisitor setup(BlockDAO blockDAO) {
        class Visitor implements TreeVisitor {
            Stack<Hash> parents = new Stack<>();

            @Override
            public void enter(String s) {
                Hash current = Hash.of(s);

                if (!parents.isEmpty()) {
                    when(blockDAO.parentBlockHash(eq(current))).thenReturn(parents.peek());
                }

                parents.push(current);
            }

            @Override
            public void leave(String s) {
                parents.pop();
            }
        }

        return new Visitor();
    }

}