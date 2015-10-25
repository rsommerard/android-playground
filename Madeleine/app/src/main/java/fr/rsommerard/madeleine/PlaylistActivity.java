package fr.rsommerard.madeleine;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {

    private final static String LOG_TAG = PlaylistActivity.class.getSimpleName();

    private ArrayAdapter<String> playlistsAdapter;
    private ArrayList<String> playlists;

    private MadeleineDatabaseHelper madeleineDatabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        ListView playlistListView = (ListView) findViewById(R.id.playlist_listview);

        List<Playlists> allPlaylists = null;
        try {
            Dao<Playlists, Integer> playlistsDao = getHelper().getPlaylistsDao();

            allPlaylists = playlistsDao.queryForAll();
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Can't recover all playlists from database");
        }

        playlists = new ArrayList<String>();
        for(Playlists playlistsEntry : allPlaylists) {
            playlists.add(playlistsEntry.title);
        }

        Collections.sort(playlists);

        playlistsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, playlists);

        playlistListView.setAdapter(playlistsAdapter);

        playlistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String playlistName = (String) parent.getItemAtPosition(position);
                Log.d(LOG_TAG, "Playlist selected: " + playlistName);

                Intent intent = new Intent(PlaylistActivity.this, SongActivity.class);
                intent.putExtra("playlistName", playlistName);

                startActivity(intent);
            }
        });

        registerForContextMenu(playlistListView);

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
            case R.id.action_add:
                addPlaylist();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addPlaylist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.add_playlist);

        final EditText playlistEditText = new EditText(this);
        playlistEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        playlistEditText.setHint(R.string.name);

        builder.setView(playlistEditText);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String playlistNameEditText = playlistEditText.getText().toString().trim();

                if(playlistNameEditText.isEmpty() || "".equals(playlistNameEditText)) {
                    return;
                }

                try {
                    Dao<Playlists, Integer> playlistsDao = getHelper().getPlaylistsDao();

                    Playlists newPlaylists = new Playlists();
                    newPlaylists.title = playlistNameEditText;
                    playlistsDao.create(newPlaylists);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Can't add new playlists");
                }

                playlists.add(playlistEditText.getText().toString());

                Collections.sort(playlists);

                playlistsAdapter.notifyDataSetChanged();
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
                                    ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:
                editPlaylist(info.position);
                return true;
            case R.id.delete:
                deletePlaylist(playlistsAdapter.getItem(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deletePlaylist(final String playlistName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.dialog_delete);
        builder.setTitle(R.string.delete);

        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    Dao<Playlists, Integer> playlistsDao = getHelper().getPlaylistsDao();

                    Playlists playlistsEntry = playlistsDao.queryForEq("title", playlistName).get(0);

                    Dao<Songs, Integer> songsDao = getHelper().getSongsDao();

                    List<Songs> songsEntries = songsDao.queryForEq("playlist_id", playlistsEntry.id);

                    songsDao.delete(songsEntries);

                    playlistsDao.delete(playlistsEntry);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Can't delete playlists and songs associated");
                }


                playlistsAdapter.remove(playlistName);
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

    private void editPlaylist(final int position) {
        final String playlistName = playlistsAdapter.getItem(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.rename);

        final EditText playlistEditText = new EditText(this);
        playlistEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        playlistEditText.setText(playlistName);

        builder.setView(playlistEditText);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String playlistNameEditText = playlistEditText.getText().toString().trim();

                if(playlistNameEditText.isEmpty() || "".equals(playlistNameEditText)) {
                    dialog.cancel();
                }

                try {
                    Dao<Playlists, Integer> playlistsDao = getHelper().getPlaylistsDao();

                    Playlists playlistsEntry = playlistsDao.queryForEq("title", playlistName).get(0);

                    playlistsEntry.title = playlistNameEditText;
                    playlistsDao.update(playlistsEntry);
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Can't rename playlists");
                }

                playlists.remove(position);
                playlists.add(playlistEditText.getText().toString());

                Collections.sort(playlists);

                playlistsAdapter.notifyDataSetChanged();
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
