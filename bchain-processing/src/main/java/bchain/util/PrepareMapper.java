package bchain.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface PrepareMapper<T> {
    void prepare(PreparedStatement ps, int n, T value) throws SQLException;
}
