package com.example.telemetry.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TelemetryDao_Impl implements TelemetryDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TelemetryEntity> __insertionAdapterOfTelemetryEntity;

  private final SharedSQLiteStatement __preparedStmtOfClear;

  public TelemetryDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTelemetryEntity = new EntityInsertionAdapter<TelemetryEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `telemetry` (`id`,`rfidTag`,`temperatura`,`humedad`,`luzDetectada`,`fechaCreacion`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TelemetryEntity entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getRfidTag() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getRfidTag());
        }
        if (entity.getTemperatura() == null) {
          statement.bindNull(3);
        } else {
          statement.bindDouble(3, entity.getTemperatura());
        }
        if (entity.getHumedad() == null) {
          statement.bindNull(4);
        } else {
          statement.bindDouble(4, entity.getHumedad());
        }
        final Integer _tmp = entity.getLuzDetectada() == null ? null : (entity.getLuzDetectada() ? 1 : 0);
        if (_tmp == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, _tmp);
        }
        if (entity.getFechaCreacion() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getFechaCreacion());
        }
      }
    };
    this.__preparedStmtOfClear = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM telemetry";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<TelemetryEntity> items,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTelemetryEntity.insert(items);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clear(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClear.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClear.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<TelemetryEntity> observeLatest() {
    final String _sql = "SELECT * FROM telemetry ORDER BY fechaCreacion DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"telemetry"}, new Callable<TelemetryEntity>() {
      @Override
      @Nullable
      public TelemetryEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRfidTag = CursorUtil.getColumnIndexOrThrow(_cursor, "rfidTag");
          final int _cursorIndexOfTemperatura = CursorUtil.getColumnIndexOrThrow(_cursor, "temperatura");
          final int _cursorIndexOfHumedad = CursorUtil.getColumnIndexOrThrow(_cursor, "humedad");
          final int _cursorIndexOfLuzDetectada = CursorUtil.getColumnIndexOrThrow(_cursor, "luzDetectada");
          final int _cursorIndexOfFechaCreacion = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCreacion");
          final TelemetryEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpRfidTag;
            if (_cursor.isNull(_cursorIndexOfRfidTag)) {
              _tmpRfidTag = null;
            } else {
              _tmpRfidTag = _cursor.getString(_cursorIndexOfRfidTag);
            }
            final Double _tmpTemperatura;
            if (_cursor.isNull(_cursorIndexOfTemperatura)) {
              _tmpTemperatura = null;
            } else {
              _tmpTemperatura = _cursor.getDouble(_cursorIndexOfTemperatura);
            }
            final Double _tmpHumedad;
            if (_cursor.isNull(_cursorIndexOfHumedad)) {
              _tmpHumedad = null;
            } else {
              _tmpHumedad = _cursor.getDouble(_cursorIndexOfHumedad);
            }
            final Boolean _tmpLuzDetectada;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfLuzDetectada)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfLuzDetectada);
            }
            _tmpLuzDetectada = _tmp == null ? null : _tmp != 0;
            final String _tmpFechaCreacion;
            if (_cursor.isNull(_cursorIndexOfFechaCreacion)) {
              _tmpFechaCreacion = null;
            } else {
              _tmpFechaCreacion = _cursor.getString(_cursorIndexOfFechaCreacion);
            }
            _result = new TelemetryEntity(_tmpId,_tmpRfidTag,_tmpTemperatura,_tmpHumedad,_tmpLuzDetectada,_tmpFechaCreacion);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<TelemetryEntity>> observeHistory() {
    final String _sql = "SELECT * FROM telemetry ORDER BY fechaCreacion DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"telemetry"}, new Callable<List<TelemetryEntity>>() {
      @Override
      @NonNull
      public List<TelemetryEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRfidTag = CursorUtil.getColumnIndexOrThrow(_cursor, "rfidTag");
          final int _cursorIndexOfTemperatura = CursorUtil.getColumnIndexOrThrow(_cursor, "temperatura");
          final int _cursorIndexOfHumedad = CursorUtil.getColumnIndexOrThrow(_cursor, "humedad");
          final int _cursorIndexOfLuzDetectada = CursorUtil.getColumnIndexOrThrow(_cursor, "luzDetectada");
          final int _cursorIndexOfFechaCreacion = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCreacion");
          final List<TelemetryEntity> _result = new ArrayList<TelemetryEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TelemetryEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpRfidTag;
            if (_cursor.isNull(_cursorIndexOfRfidTag)) {
              _tmpRfidTag = null;
            } else {
              _tmpRfidTag = _cursor.getString(_cursorIndexOfRfidTag);
            }
            final Double _tmpTemperatura;
            if (_cursor.isNull(_cursorIndexOfTemperatura)) {
              _tmpTemperatura = null;
            } else {
              _tmpTemperatura = _cursor.getDouble(_cursorIndexOfTemperatura);
            }
            final Double _tmpHumedad;
            if (_cursor.isNull(_cursorIndexOfHumedad)) {
              _tmpHumedad = null;
            } else {
              _tmpHumedad = _cursor.getDouble(_cursorIndexOfHumedad);
            }
            final Boolean _tmpLuzDetectada;
            final Integer _tmp;
            if (_cursor.isNull(_cursorIndexOfLuzDetectada)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(_cursorIndexOfLuzDetectada);
            }
            _tmpLuzDetectada = _tmp == null ? null : _tmp != 0;
            final String _tmpFechaCreacion;
            if (_cursor.isNull(_cursorIndexOfFechaCreacion)) {
              _tmpFechaCreacion = null;
            } else {
              _tmpFechaCreacion = _cursor.getString(_cursorIndexOfFechaCreacion);
            }
            _item = new TelemetryEntity(_tmpId,_tmpRfidTag,_tmpTemperatura,_tmpHumedad,_tmpLuzDetectada,_tmpFechaCreacion);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
