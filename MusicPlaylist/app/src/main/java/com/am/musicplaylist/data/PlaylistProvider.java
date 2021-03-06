package com.am.musicplaylist.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import com.am.musicplaylist.data.PlaylistContract.PlaylistEntry;

public class PlaylistProvider extends ContentProvider {

  private static final String LOG_TAG = PlaylistProvider.class.getName();

  // URI matcher code for the content uri for the table "playlists"
  private static final int PLAYLISTS = 1;

  // URI matcher code for the content uri for the table "songs"
  private static final int SONGS = 3;

  // URI matcher code for the content uri for the specific row for the table "songs"
  private static final int PATH_SONGS_PLAYLIST_NAME = 4;

  private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

  static {
    mUriMatcher.addURI(PlaylistContract.CONTENT_AUTHORITY, PlaylistContract.PATH_PLAYLISTS, PLAYLISTS);
    mUriMatcher.addURI(PlaylistContract.CONTENT_AUTHORITY, PlaylistContract.PATH_SONGS, SONGS);
    mUriMatcher.addURI(PlaylistContract.CONTENT_AUTHORITY, PlaylistContract.PATH_SONGS + "/#", PATH_SONGS_PLAYLIST_NAME);
  }

  private PlaylistDbHelper mPlaylistDbHelper;

  @Override
  public boolean onCreate() {
    mPlaylistDbHelper = new PlaylistDbHelper(getContext());
    return true;
  }

  @Nullable
  @Override
  public Cursor query(Uri uri,  String[] projection, String selection,
      String[] selectionArgs,  String sortOrder) {

    SQLiteDatabase db = mPlaylistDbHelper.getReadableDatabase();

    Cursor cursor;

    int match = mUriMatcher.match(uri);
    switch (match) {
      case PLAYLISTS:
        // query the whole "playlists" table
        cursor = db.query(PlaylistEntry.TABLE_NAME_PLAYLISTS, projection, selection, selectionArgs, null, null, sortOrder);
        break;
      case SONGS:
        // query the whole "songs" table
        cursor = db.query(PlaylistEntry.TABLE_NAME_SONGS, projection, selection, selectionArgs, null, null, sortOrder);
        break;
      case PATH_SONGS_PLAYLIST_NAME:
        // query the specific row in "songs" table
        selection = PlaylistEntry.COLUMN_SONG_PLAYLIST_ID + "=?";
        selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
        cursor = db.query(PlaylistEntry.TABLE_NAME_SONGS, projection, selection, selectionArgs, null, null, sortOrder);
        break;
      default:
        throw new IllegalArgumentException("Cannot query unknown URI " + uri);
    }

    // register cursor for this URI
    // so if in any changes will occure in this URI cursorloader should be refreshed
    cursor.setNotificationUri(getContext().getContentResolver(), uri);
    return cursor;
  }

  /**
   * return MIME type of the Content URI
   */
  @Override
  public String getType(Uri uri) {
    int match = mUriMatcher.match(uri);
    switch (match) {
      case PLAYLISTS:
        return PlaylistEntry.CONTENT_LIST_TYPE;
      case SONGS:
        return PlaylistEntry.CONTENT_SONG_LIST_TYPE;
      case PATH_SONGS_PLAYLIST_NAME:
        return PlaylistEntry.CONTENT_SONG_ITEM_TYPE;
      default:
        throw new IllegalArgumentException("Illegal URI" + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    int match = mUriMatcher.match(uri);
    switch (match) {
      case PLAYLISTS:
        return insertPlaylist(uri, values);
      case SONGS:
        return insertSong(uri, values);
      default:
        throw new IllegalArgumentException("Cannot query unknown URI " + uri);
    }
  }

  /**
   * Insert song into db
   */
  private Uri insertSong(Uri uri, ContentValues values) {

    SQLiteDatabase db = mPlaylistDbHelper.getWritableDatabase();
    long id = db.insert(PlaylistEntry.TABLE_NAME_SONGS, null, values);

    if (id == -1) {
      Log.e(LOG_TAG, "Error in inserting a new row");
    } else {
      Log.i(LOG_TAG, "Row is inserted");
    }

    getContext().getContentResolver().notifyChange(uri, null);
    return ContentUris.withAppendedId(uri, id);
  }

  /**
   * Insert playlist into db
   */
  private Uri insertPlaylist(Uri uri, ContentValues values) {

    SQLiteDatabase db = mPlaylistDbHelper.getWritableDatabase();
    long id = db.insert(PlaylistEntry.TABLE_NAME_PLAYLISTS, null, values);

    if (id == -1) {
      Log.e(LOG_TAG, "Error in inserting a new row");
    } else {
      Log.i(LOG_TAG, "Row is inserted");
    }

    getContext().getContentResolver().notifyChange(uri, null);
    return ContentUris.withAppendedId(uri, id);
  }

  @Override
  public int delete( Uri uri,  String selection,
       String[] selectionArgs) {
    int deletedRows;

    // get writeable databse
    SQLiteDatabase db = mPlaylistDbHelper.getWritableDatabase();

    int match = mUriMatcher.match(uri);
    switch (match) {
      case PLAYLISTS:

        // delete all rows from the "playlists" table
        deletedRows = db.delete(PlaylistEntry.TABLE_NAME_PLAYLISTS, selection, selectionArgs);

        // if rows are deleted
        if (deletedRows != 0) {
          getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
//      case SONGS:
//        // delete all songs from the "songs" table
//        deletedRows = db.delete(PlaylistEntry.TABLE_NAME_SONGS, selection, selectionArgs);
//        if (deletedRows != 0) {
//          getContext().getContentResolver().notifyChange(uri, null);
//        }
//        return deletedRows;
      case PATH_SONGS_PLAYLIST_NAME:

        // delete specific song from the "songs" table
        selection = PlaylistEntry._ID + "=?";
        selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

        deletedRows = db.delete(PlaylistEntry.TABLE_NAME_SONGS, selection, selectionArgs);

        if (deletedRows != 0) {
          getContext().getContentResolver().notifyChange(uri, null);
        }
        return deletedRows;
      default:
        throw new IllegalArgumentException("Illegal uri");
    }
  }

  @Override
  public int update( Uri uri,  ContentValues values,  String selection,
       String[] selectionArgs) {
    return 0;
  }

}
