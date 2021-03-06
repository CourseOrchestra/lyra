package ru.curs.lyra.kernel;

import ru.curs.celesta.score.*;

import java.util.Date;
import java.util.HashMap;

/**
 * Тип сериализуемого поля формы.
 */
public enum LyraFieldType {
    /**
     * BLOB.
     */
    BLOB,

    /**
     * BIT.
     */
    BIT,

    /**
     * DATETIME.
     */
    DATETIME,

    /**
     * REAL.
     */
    REAL,

    /**
     * INT.
     */
    INT,

    /**
     * VARCHAR.
     */
    VARCHAR;

    private static final HashMap<String, LyraFieldType> C2L = new HashMap<>();

    private static final HashMap<Class<?>, LyraFieldType> J2L = new HashMap<>();


    static {
        C2L.put(IntegerColumn.CELESTA_TYPE, INT);
        C2L.put(StringColumn.VARCHAR, VARCHAR);
        C2L.put(StringColumn.TEXT, VARCHAR);
        C2L.put(FloatingColumn.CELESTA_TYPE, REAL);
        C2L.put(DateTimeColumn.CELESTA_TYPE, DATETIME);
        C2L.put(BooleanColumn.CELESTA_TYPE, BIT);
        C2L.put(BinaryColumn.CELESTA_TYPE, BLOB);

        J2L.put(boolean.class, BIT);
        J2L.put(int.class, INT);
        J2L.put(ru.curs.celesta.dbutils.BLOB.class, BLOB);
        J2L.put(Date.class, DATETIME);
        J2L.put(String.class, VARCHAR);
        J2L.put(double.class, REAL);
    }

    /**
     * Определяет тип поля по метаданным столбца таблицы (Table).
     *
     * @param c столбец таблицы.
     */
    public static LyraFieldType lookupFieldType(ColumnMeta c) {
        LyraFieldType result = C2L.get(c.getCelestaType());
        if (result == null) {
            throw new RuntimeException(String.format("Invalid table column type: %s", c.getClass().toString()));
        }
        return result;
    }

    /**
     * Lookups respective LyraFieldType for Java class.
     */
    public static LyraFieldType lookupFieldType(Class<?> clazz){
        return J2L.get(clazz);
    }

}
