package com.gaiagps.iburn.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;

import static com.gaiagps.iburn.database.Art.TABLE_NAME;

/**
 * Created by dbro on 6/8/17.
 */

@Entity(tableName = TABLE_NAME)
public class Art extends PlayaItem {
    public static final String TABLE_NAME = "arts";

    public static final String ARTIST = "artist";
    public static final String ARTIST_LOCATION = "a_loc";
    public static final String IMAGE_URL = "i_url";
    public static final String AUDIO_TOUR_URL = "a_url";


    @ColumnInfo(name = ARTIST)
    public String artist;

    @ColumnInfo(name = ARTIST_LOCATION)
    public String artistLocation;

    @ColumnInfo(name = IMAGE_URL)
    public String imageUrl;

    @ColumnInfo(name = AUDIO_TOUR_URL)
    public String audioTourUrl;
}