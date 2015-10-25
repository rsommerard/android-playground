package fr.rsommerard.madeleine;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

public class MadeleineDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "madeleine.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Playlists, Integer> playlistsDao;
    private Dao<Songs, Integer> songsDao;

    private final static String LOG_TAG = MadeleineDatabaseHelper.class.getSimpleName();

    public MadeleineDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
        try {
            Log.d(LOG_TAG, "Tables created in " + DATABASE_NAME);
            TableUtils.createTable(connectionSource, Playlists.class);
            TableUtils.createTable(connectionSource, Songs.class);

        } catch (SQLException e) {
            Log.e(LOG_TAG, "Can't create database", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource,
                          int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Songs.class, true);
            TableUtils.dropTable(connectionSource, Playlists.class, true);
            onCreate(db);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Impossible to drop database", e);
            throw new RuntimeException(e);
        }
    }

    public Dao<Playlists, Integer> getPlaylistsDao() throws SQLException {
        if (playlistsDao == null) {
            playlistsDao = getDao(Playlists.class);
        }
        return playlistsDao;
    }

    public Dao<Songs, Integer> getSongsDao() throws SQLException {
        if (songsDao == null) {
            songsDao = getDao(Songs.class);
        }
        return songsDao;
    }
}