package fr.rsommerard.madeleine;

import android.accounts.Account;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "playlist")
public class Playlists {

    @DatabaseField(generatedId = true)
    public int id;

    @DatabaseField(canBeNull=false)
    public String title;
}
