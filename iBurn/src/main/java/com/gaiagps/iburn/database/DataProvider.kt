package com.gaiagps.iburn.database

import android.content.ContentValues
import android.content.Context
import com.gaiagps.iburn.PrefsHelper
import com.gaiagps.iburn.api.typeadapter.PlayaDateTypeAdapter
import com.mapbox.mapboxsdk.geometry.VisibleRegion
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Flowables
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Class for interaction with our database via Reactive streams.
 * This is intended as an experiment to replace our use of [android.content.ContentProvider]
 * as it does not meet all of our needs (e.g: Complex UNION queries not possible with Schematic's
 * generated version, and I believe manually writing a ContentProvider is too burdensome and error-prone)
 *
 *
 * Created by davidbrodsky on 6/22/15.
 */
class DataProvider private constructor(private val db: AppDatabase, private val interceptor: DataProvider.QueryInterceptor?) {

    interface QueryInterceptor {
        fun onQueryIntercepted(query: String, tables: Iterable<String>): String
    }

    private val upgradeLock = AtomicBoolean(false)

    fun beginUpgrade() {
        upgradeLock.set(true)
    }

    fun endUpgrade() {
        upgradeLock.set(false)

        // TODO : Trigger Room observers
        // Trigger all SqlBrite observers via reflection (uses private method)
        //        try {
        //            Method method = db.getClass().getDeclaredMethod("sendTableTrigger", Set.class);
        //            method.setAccessible(true);
        //            method.invoke(db, new HashSet<>(PlayaDatabase.ALL_TABLES));
        //        } catch (SecurityException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        //            Timber.w(e, "Failed to notify observers on endUpgrade");
        //        }
    }

    fun deleteCamps(): Int {
        return clearTable(Camp.TABLE_NAME)
    }

    private fun clearTable(tablename: String): Int {
        return db.openHelper.writableDatabase.delete(tablename, null, null)
    }

    fun observeCamps(): Flowable<List<Camp>> {
        return db.campDao().all
    }

    fun observeCampFavorites(): Flowable<List<Camp>> {

        // TODO : Honor upgradeLock?
        return db.campDao().favorites
    }

    fun observeCampsByName(query: String): Flowable<List<Camp>> {

        // TODO : Honor upgradeLock
        // TODO : Return structure with metadata on how many art, camps, events etc?
        return db.campDao().findByName(query)
    }

    fun observeCampByPlayaId(playaId: String): Flowable<Camp> {
        return db.campDao().findByPlayaId(playaId)
    }

    fun beginTransaction() {
        db.beginTransaction()
        //        BriteDatabase.Transaction t = db.newTransaction();
        //        transactionStack.push(t);
    }

    fun setTransactionSuccessful() {
        if (!db.inTransaction()) {
            return
        }

        db.setTransactionSuccessful()
    }

    fun endTransaction() {
        if (!db.inTransaction()) {
            return
        }

        // TODO: Don't allow this call to proceed without prior call to beginTransaction
        db.endTransaction()
    }

    fun insert(table: String, values: ContentValues) {
        db.openHelper.writableDatabase.insert(table, 0, values) // TODO : wtf is the int here?
    }

    fun delete(table: String): Int {
        when (table) {
            Camp.TABLE_NAME -> return deleteCamps()
            Art.TABLE_NAME -> return deleteArt()
            Event.TABLE_NAME -> return deleteEvents()
            else -> Timber.w("Cannot clear unknown table name '%s'", table)
        }
        return 0
    }

    fun deleteEvents(): Int {
        return clearTable(Event.TABLE_NAME)

        //        return db.getOpenHelper().getWritableDatabase().delete(Event.TABLE_NAME, "*", null);
        //        Cursor result = db.query("DELETE FROM event; VACUUM", null);
        //        if (result != null) result.close();
    }

    fun observeEventsOnDayOfTypes(day: String,
                                  types: ArrayList<String>?): Flowable<List<Event>> {

        // TODO : Honor upgradeLock?
        val wildDay = addWildcardsToQuery(day)
        if (types == null || types.isEmpty()) {
            return db.eventDao().findByDay(wildDay)
        } else {
            return db.eventDao().findByDayAndType(wildDay, types)
        }
    }

    fun observeEventsHostedByCamp(camp: Camp): Flowable<List<Event>> {
        return db.eventDao().findByCampPlayaId(camp.playaId)
    }

    fun observeOtherOccurrencesOfEvent(event: Event): Flowable<List<Event>> {
        return db.eventDao().findOtherOccurrences(event.playaId, event.id)
    }

    fun observeEventFavorites(): Flowable<List<Event>> {

        // TODO : Honor upgradeLock?
        return db.eventDao().favorites
    }

    fun observeEventBetweenDates(start: Date, end: Date): Flowable<List<Event>> {

        val startDateStr = PlayaDateTypeAdapter.iso8601Format.format(start)
        val endDateStr = PlayaDateTypeAdapter.iso8601Format.format(end)
        // TODO : Honor upgradeLock?
        Timber.d("Start time between %s and %s", startDateStr, endDateStr)
        return db.eventDao().findInDateRange(startDateStr, endDateStr)
    }

    fun deleteArt(): Int {
        return clearTable(Art.TABLE_NAME)
        //        return db.getOpenHelper().getWritableDatabase().delete(Art.TABLE_NAME, null, null);
        //        Cursor result = db.query("DELETE FROM art; VACUUM", null);
        //        if (result != null) result.close();
    }

    fun observeArt(): Flowable<List<Art>> {

        // TODO : Honor upgradeLock?
        return db.artDao().all
    }

    fun observeArtFavorites(): Flowable<List<Art>> {

        // TODO : Honor upgradeLock?
        return db.artDao().favorites
    }

    fun observeArtWithAudioTour(): Flowable<List<Art>> {

        // TODO : Honor upgradeLock?
        return db.artDao().allWithAudioTour
    }

    /**
     * Observe all favorites.
     *
     *
     * Note: This query automatically adds in Event.startTime (and 0 values for all non-events),
     * since we always want to show this data for an event.
     */
    fun observeFavorites(): Flowable<List<PlayaItem>> {

        // TODO : Honor upgradeLock
        // TODO : Return structure with metadata on how many art, camps, events etc?
        return Flowables.combineLatest(
                db.artDao().favorites,
                db.campDao().favorites,
                db.eventDao().favorites)
        { arts, camps, events ->
            val all = ArrayList<PlayaItem>(arts.size + camps.size + events.size)
            all.addAll(arts)
            all.addAll(camps)
            all.addAll(events)
            all
        }
    }

    /**
     * Observe all results for a name query.
     *
     *
     * Note: This query automatically adds in Event.startTime (and 0 values for all non-events),
     * since we always want to show this data for an event.
     */
    fun observeNameQuery(query: String): Flowable<List<PlayaItem>> {

        // TODO : Honor upgradeLock
        // TODO : Return structure with metadata on how many art, camps, events etc?
        val wildQuery = addWildcardsToQuery(query)
        return Flowables.combineLatest(
                db.artDao().findByName(wildQuery),
                db.campDao().findByName(wildQuery),
                db.eventDao().findByName(wildQuery))
        { arts, camps, events ->
            val all = ArrayList<PlayaItem>(arts.size + camps.size + events.size)
            all.addAll(arts)
            all.addAll(camps)
            all.addAll(events)
            all
        }
    }

    /**
     * Returns ongoing events in [region], favorites, and user-added markers
     */
    fun observeAllMapItemsInVisibleRegion(region: VisibleRegion): Flowable<List<PlayaItem>> {
        // TODO : Honor upgradeLock

        // Warning: The following is very ethnocentric to Earth C-137 North-Western ... Quadrasphere(?)
        val maxLat = region.farRight.latitude.toFloat()
        val minLat = region.nearRight.latitude.toFloat()
        val maxLon = region.farRight.longitude.toFloat()
        val minLon = region.farLeft.longitude.toFloat()

        return Flowables.combineLatest(
                db.artDao().favorites,
                db.campDao().favorites,
                db.eventDao().findInRegionOrFavorite(minLat, maxLat, minLon, maxLon),
                db.userPoiDao().all)
        { arts, camps, events, userpois ->
            val all = ArrayList<PlayaItem>(arts.size + camps.size + events.size + userpois.size)
            all.addAll(arts)
            all.addAll(camps)
            all.addAll(events)
            all.addAll(userpois)
            all
        }
    }

    /**
     * Returns favorites and user-added markers only
     */
    fun observeUserAddedMapItemsOnly(): Flowable<List<PlayaItem>> {
        // TODO : Honor upgradeLock
        return Flowables.combineLatest(
                db.artDao().favorites,
                db.campDao().favorites,
                db.eventDao().favorites,
                db.userPoiDao().all)
        { arts, camps, events, userpois ->
            val all = ArrayList<PlayaItem>(arts.size + camps.size + events.size + userpois.size)
            all.addAll(arts)
            all.addAll(camps)
            all.addAll(events)
            all.addAll(userpois)
            all
        }
    }


    private fun update(item: PlayaItem) {
        if (item is Art) {
            db.artDao().update(item)
        } else if (item is Event) {
            db.eventDao().update(item)
        } else if (item is Camp) {
            db.campDao().update(item)
        } else {
            Timber.e("Cannot update item of unknown type")
        }
    }

    fun toggleFavorite(item: PlayaItem) {
        // TODO : Really don't like mutable DBB objects, so hide the field twiddling here in case
        // I can remove it from the PlayaItem API
        item.isFavorite = !item.isFavorite
        Timber.d("Setting item %s favorite %b", item.name, item.isFavorite)
        update(item)
    }

    private fun interceptQuery(query: String, table: String): String {
        return interceptQuery(query, setOf(table))
    }

    private fun interceptQuery(query: String, tables: Iterable<String>): String {
        if (interceptor == null) return query
        return interceptor.onQueryIntercepted(query, tables)
    }

    companion object {

        /**
         * Version of database schema
         */
        const val BUNDLED_DATABASE_VERSION: Long = 1

        /**
         * Version of database data and mbtiles. This is basically the unix time at which bundled data was provided to this build.
         */
        val RESOURCES_VERSION: Long = 0 //1472093065000L; // Unix time of creation

        /**
         * If true, use a bundled pre-populated database (see [DBWrapper]. Else start with a fresh database.
         */
        private val USE_BUNDLED_DB = true

        private var provider: DataProvider? = null

        //    private ArrayDeque<BriteDatabase.Transaction> transactionStack = new ArrayDeque<>();

        fun getInstance(context: Context): Observable<DataProvider> {

            // TODO : This ain't thread safe

            if (provider != null) return Observable.just(provider!!)

            val prefs = PrefsHelper(context)

            // TODO : How to use bundled DB?
            //        SQLiteOpenHelper openHelper = USE_BUNDLED_DB ? new DBWrapper(context) : com.gaiagps.iburn.database.generated.PlayaDatabase.getInstance(context);


            return Observable.just(getSharedDb(context))
                    .subscribeOn(Schedulers.io())
                    .doOnNext { database ->
                        prefs.databaseVersion = BUNDLED_DATABASE_VERSION
                        prefs.setBaseResourcesVersion(RESOURCES_VERSION)
                    }
                    .map { sqlBrite -> DataProvider(sqlBrite, Embargo(prefs)) }
                    .doOnNext { dataProvider -> provider = dataProvider }
        }

        fun makeProjectionString(projection: Array<String>): String {
            val builder = StringBuilder()
            for (column in projection) {
                builder.append(column)
                builder.append(',')
            }
            // Remove the last comma
            return builder.substring(0, builder.length - 1)
        }

        /**
         * Add wildcards to the beginning and end of a query term

         * @return "%{@param query}%"
         */
        private fun addWildcardsToQuery(query: String): String {
            return "%$query%"
        }
    }
}
