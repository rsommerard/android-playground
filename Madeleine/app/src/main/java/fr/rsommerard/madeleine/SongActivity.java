package fr.rsommerard.madeleine;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongActivity extends AppCompatActivity {

    private final static String LOG_TAG = SongActivity.class.getSimpleName();

    private String playlistName;
    private ArrayList<String> songs;
    private ArrayAdapter<String> songsAdapter;

    private MadeleineDatabaseHelper madeleineDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);

        Intent intent = getIntent();
        playlistName = intent.getStringExtra("playlistName");

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setTitle(playlistName);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        ListView songListView = (ListView) findViewById(R.id.song_listview);

        List<Songs> allSongs = null;
        try {
            Dao<Songs, Integer> songsDao = getHelper().getSongsDao();
            Dao<Playlists, Integer> playlistsDao = getHelper().getPlaylistsDao();

            Playlists playlistsEntry = playlistsDao.queryForEq("title", playlistName).get(0);

            allSongs = songsDao.queryForEq("playlist_id", playlistsEntry.id);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Can't recover all songs from database");
        }

        songs = new ArrayList<String>();
        for(Songs songsEntry : allSongs) {
            songs.add(songsEntry.title);
        }

        Collections.sort(songs);

        songsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, songs);

        songListView.setAdapter(songsAdapter);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String songName = (String) parent.getItemAtPosition(position);
                Log.d(LOG_TAG, "Song selected: " + songName);

                Intent intent = new Intent(Intent.ACTION_SEARCH);
                intent.setPackage("com.google.android.youtube");
                intent.putExtra("query", songName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        registerForContextMenu(songListView);
    }

    private MadeleineDatabaseHelper getHelper() {
        if (madeleineDatabaseHelper == null) {
            madeleineDatabaseHelper = OpenHelperManager.getHelper(this, MadeleineDatabaseHelper.class);
        }
        return madeleineDatabaseHelper;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_add:
                addSong();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addSong() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_song);

        final EditText songEditText = new EditText(this);
        songEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        songEditText.setHint(R.string.name);

        builder.setView(songEditText);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String songNameEditText = songEditText.getText().toString().trim();

                if(songNameEditText.isEmpty() || "".equals(songNameEditText)) {
                    return;
                }

                try {
                    Dao<Songs, Integer> songsDao = getHelper().getSongsDao();
                    Dao<Playlists, Integer> playlistsDao = getHelper().getPlaylistsDao();

                    Playlists playlistsEntry = playlistsDao.queryForEq("title", playlistName).get(0);

                    Songs newSongs = new Songs();
                    newSongs.title = songNameEditText;
                    newSongs.playlist = playlistsEntry;
                    songsDao.create(newSongs);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Can't add new songs");
                }

                songs.add(songEditText.getText().toString());

                Collections.sort(songs);

                songsAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:
                editSong(info.position);
                return true;
            case R.id.delete:
                deleteSong(songsAdapter.getItem(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteSong(final String songName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete);
        builder.setTitle(R.string.delete);

        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    Dao<Songs, Integer> songsDao = getHelper().getSongsDao();

                    Songs songsEntry = songsDao.queryForEq("title", songName).get(0);

                    songsDao.delete(songsEntry);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Can't delete songs");
                }

                songsAdapter.remove(songName);
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void editSong(final int position) {
        final String songName = songsAdapter.getItem(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rename);

        final EditText songEditText = new EditText(this);
        songEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        songEditText.setText(songName);

        builder.setView(songEditText);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String songNameEditText = songEditText.getText().toString().trim();

                if(songNameEditText.isEmpty() || "".equals(songNameEditText)) {
                    return;
                }

                try {
                    Dao<Songs, Integer> songsDao = getHelper().getSongsDao();

                    Songs songsEntry = songsDao.queryForEq("title", songName).get(0);

                    songsEntry.title = songNameEditText;
                    songsDao.update(songsEntry);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Can't rename songs");
                }

                songs.remove(position);
                songs.add(songEditText.getText().toString());

                Collections.sort(songs);

                songsAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
