package com.example.dat.testsift.PhotoAlbum;

import java.io.File;

/**
 * Created by DAT on 27-Dec-15.
 */
public abstract class AlbumStorageDirFactory {
    public abstract File getAlbumStorageDir(String albumName);
}
