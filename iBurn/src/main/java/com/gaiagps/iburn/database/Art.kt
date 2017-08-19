package com.gaiagps.iburn.database

//import android.arch.persistence.room.*
//import com.gaiagps.iburn.database.Art.Companion.ColAudioTourUrl
//import com.gaiagps.iburn.database.Art.Companion.TableName
//import com.gaiagps.iburn.database.PlayaItem.Companion.ColFavorite
//import com.gaiagps.iburn.database.PlayaItem.Companion.ColName
//import io.reactivex.Flowable
//import io.reactivex.Observable

/**
 * Created by dbro on 6/6/17.
 */
//@Entity(tableName = TableName)
//class Art(
//        id:             Int = 0,
//        name:           String,
//        description:    String,
//        url:            String,
//        contact:        String,
//        playaAddress:   String,
//        playaId:        String,
//        location:       Location,
//        isFavorite:     Boolean,
//
//        @ColumnInfo(name = ColArtist)           val artist: String,
//        @ColumnInfo(name = ColArtistLocation)   val artistLocation: String,
//        @ColumnInfo(name = ColImageUrl)         val imageUrl: String,
//        @ColumnInfo(name = ColAudioTourUrl)     val audioTourUrl: String)
//
//    : PlayaItem(id, name, description, url, contact, playaAddress, playaId, location, isFavorite) {
//
//    companion object {
//        const val TableName = "arts"
//        const val ColArtist = "artist"
//        const val ColArtistLocation = "artist_location"
//        const val ColImageUrl = "image_url"
//        const val ColAudioTourUrl = "audio_tour_url"
//    }
//}

//@Dao
//interface ArtDao {
//
//    @Query("SELECT * FROM $TableName")
//    fun getAll(): Flowable<List<Art>>
//
//    @Query("SELECT * FROM $TableName WHERE $ColAudioTourUrl IS NOT NULL")
//    fun getAllWithAudioTour(): Flowable<List<Art>>
//
//    @Query("SELECT * FROM $TableName WHERE $ColFavorite = 1")
//    fun getFavorites(): Flowable<List<Art>>
//
//    // TODO : 'p0' is used vs 'name' b/c Kotlin isn't preserving function parameter names properly
//    // https://youtrack.jetbrains.com/issue/KT-17959
//    @Query("SELECT * FROM $TableName WHERE $ColName LIKE :p0")
//    fun findByName(p0: String): Flowable<List<Art>>
//
//    @Insert
//    fun insert(vararg art: Art)
//
//    @Update
//    fun update(vararg art: Art)
//
//    @Delete
//    fun delete(art: Art)
//}