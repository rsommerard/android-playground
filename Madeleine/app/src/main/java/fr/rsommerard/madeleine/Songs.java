package fr.rsommerard.madeleine;

import android.accounts.Account;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "song")
public class Songs {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull=false)
    public String title;

    @DatabaseField(foreign = true, canBeNull=false)
    public Playlists playlist;
}
