package com.hisaab_khata.hisaab_khata.domain.support;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import org.postgresql.util.PGobject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * Maps a Java enum to a PostgreSQL custom enum type so Hibernate binds as PGobject
 * instead of VARCHAR, avoiding "column is of type X but expression is of type character varying".
 */
public abstract class PostgreSQLEnumType<E extends Enum<E>> implements UserType<E> {

    private final Class<E> enumClass;
    private final String pgEnumTypeName;

    protected PostgreSQLEnumType(Class<E> enumClass, String pgEnumTypeName) {
        this.enumClass = enumClass;
        this.pgEnumTypeName = pgEnumTypeName;
    }

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<E> returnedClass() {
        return enumClass;
    }

    @Override
    public boolean equals(E x, E y) {
        return Objects.equals(x, y);
    }

    @Override
    public int hashCode(E x) {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public E nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String value = rs.getString(position);
        if (rs.wasNull() || value == null || value.isBlank()) {
            return null;
        }
        return Enum.valueOf(enumClass, value.trim());
    }

    @Override
    public void nullSafeSet(PreparedStatement st, E value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
            return;
        }
        PGobject pgObject = new PGobject();
        pgObject.setType(pgEnumTypeName);
        pgObject.setValue(value.name());
        st.setObject(index, pgObject, Types.OTHER);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public E deepCopy(E value) {
        return value;
    }

    @Override
    public Serializable disassemble(E value) {
        return value;
    }

    @Override
    public E assemble(Serializable cached, Object owner) {
        return enumClass.cast(cached);
    }
}
