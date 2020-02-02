package com.example.substandard.cover;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An LRU disk cache to be used with Bitmaps. Use getInstance() to access.
 *
 * I learned about this from the example linked at
 * https://developer.android.com/topic/performance/graphics/cache-bitmap
 */
public class BitmapDiskCache {
    private static final String TAG = BitmapDiskCache.class.getSimpleName();

    private static final String JOURNAL = "journal";
    private static final String READ = "READ";
    private static final String CREATE = "CREATE";
    private static final String REMOVE = "REMOVE";

    private static final int IMAGE_QUALITY = 100;
    private static final Bitmap.CompressFormat IMAGE_FORMAT = Bitmap.CompressFormat.PNG;
    private static final String FILE_EXTENSION = ".png";


    /*
     * Keep track of cache using a journal file. Lines will look like
     *
     * READ [id]
     * CREATE [id] [fileSize]
     * REMOVE [id]
     *
     * The read event tracks usage, the create event signifies a new file
     * has been added, and remove indicated removal from the cache. The
     * journal is appended to whenever one of these events is performed on
     * the cache.
     */
    // this is cache in memory. Just keeps file IDs and file size, NOT actual
    // files, in memory. The LinkedHashMap can be made to order based on access automatically!
    private final LinkedHashMap<String, Entry> entries;
    private final long maxSize;
    private long size;
    private final File journalFile;
    private final File cacheDirectory;
    private FileWriter journalWriter;

    private static BitmapDiskCache instance;

    /*
     * Used to clear up cache space on a separate thread when we get past
     * the max space. It makes zero sense to make the user wait for space
     * to be cleared before having the new file inserted.
     */
    private final ExecutorService cacheCleanService = Executors.newSingleThreadExecutor();
    private final Callable<Void> cacheCleanCallable = () -> {
        synchronized (BitmapDiskCache.this) {
            clearCacheSpace();
        }

        return null;
    };


    private BitmapDiskCache(long maxSize, File cacheDirectory) {
        this.maxSize = maxSize;
        // .75f is default, but you can't just pass in initial capacity and accessOrder...
        entries = new LinkedHashMap<>(0, .75f, true);
        journalFile = new File(cacheDirectory.getAbsolutePath() + "/" + JOURNAL);
        this.cacheDirectory = cacheDirectory;
    }

    /**
     * Only one instance of the cache is kept to avoid issues with thread access.
     * @param cacheDirectory location on disk to cache files
     * @param maxSize max space given of the cache. Will delete files based on least recent
     *                access once the space used exceeds maxSize
     * @return
     * @throws IOException
     */
    public synchronized static BitmapDiskCache getCache(File cacheDirectory, long maxSize) throws IOException {
        Log.d(TAG, "Getting cache");
        if (null == instance) {
            instance = new BitmapDiskCache(maxSize, cacheDirectory);
            instance.loadJournal();
        }

        return instance;
    }

    /**
     * If the journal file already exists, read from it to initialize entries. If not, or if
     * the journal is corrupt, set up a fresh directory.
     * @throws IOException
     */
    private void loadJournal() throws IOException {
        if (journalFile.exists()) {
            Log.d(TAG, "cache journal found");
            try {
                readJournal();
                processJournal();
            } catch (IOException e) {
                // This means journal is corrupt. Just kill everything
                e.printStackTrace();
                delete();
                initializeCacheDirectory();
            } finally {
                journalWriter = new FileWriter(instance.journalFile, true);
            }
        } else {
            Log.d(TAG, "no cache journal found");
            initializeCacheDirectory();
        }
    }

    /**
     * Sets up a fresh cache directory, with a blank journal file.
     * @throws IOException
     */
    private void initializeCacheDirectory() throws IOException {
        cacheDirectory.mkdirs();
        journalFile.createNewFile();
        journalWriter = new FileWriter(instance.journalFile, true);
        entries.clear();
    }


    /**
     * Literally just reconstructs the entries map from the journal file, line by line.
     */
    private void readJournal() throws IOException {
        Log.d(TAG, "reading cache journal");
        BufferedReader reader = new BufferedReader(new FileReader(journalFile));
        String currentLine;
        while (null != (currentLine = reader.readLine())) {
            readJournalLine(currentLine);
        }
    }

    /**
     * Reads a line from the journal and adds it to the entry list.
     * @param line
     * @throws IOException File is corrupted
     */
    private void readJournalLine(String line) throws IOException {
        final String errorMessage = "unexpected journal line: " + line;

        String[] words = line.split(" ");
        if (words.length < 2) {
            throw new IOException(errorMessage);
        }
        String key = words[1];
        Entry entry = entries.get(key);
        if (null == entry) {
            entry = new Entry(key);
            entries.put(key, entry);
        }

        if (line.startsWith(CREATE) && words.length == 3) {
            long size = Long.parseLong(words[2]);
            entry.setSize(size);
        } else if (line.startsWith(READ) && words.length == 2) {
            // line was accessed and needs to be moved to back of queue
            // BUT LinkedHashMap already takes care of this
        } else if (line.startsWith(REMOVE) && words.length == 2) {
            entries.remove(key);
        } else {
            throw new IOException(errorMessage);
        }
    }

    /**
     * All setup needed after loading entries into memory. Right now, this is just
     * updating the size member variable.
     */
    private void processJournal() {
        for (Entry entry : entries.values()) {
            size += entry.getSize();
        }
    }

    /**
     * Add an image to the cache
     * @param key
     * @param bitmap
     * @throws IOException
     */
    public synchronized void put(String key, @NonNull Bitmap bitmap) throws IOException {
        String filename = getImageFilename(key);
        Log.d(TAG, "adding item to cache: " + filename);
        try (FileOutputStream outputStream = new FileOutputStream(filename)) {
            bitmap.compress(IMAGE_FORMAT, IMAGE_QUALITY, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        long bitmapSize = bitmap.getByteCount();

        Entry entry = new Entry(key, bitmapSize);
        entries.put(key, entry);
        size += bitmapSize;
        writeToJournal(entry, CREATE);

        if (size > maxSize) {
            cacheCleanService.submit(cacheCleanCallable);
        }
    }

    private String getImageFilename(String key) {
        return cacheDirectory.getAbsolutePath() + "/" + key + FILE_EXTENSION;
    }

    // It is ridiculous to me that this isn't a LinkedHashMap method...
    private synchronized String getOldestKey() {
        String oldestKey = null;
        for (String key : entries.keySet()) {
            oldestKey = key;
        }

        return oldestKey;
    }

    /**
     * Removes the file that was least recently accessed
     * @throws IOException
     */
    private void removeOldest() throws IOException {
        removeEntry(getOldestKey());
    }

    /**
     * Deletes file with given key from the cache
     * @param key
     * @throws IOException
     */
    private synchronized void removeEntry(String key) throws IOException {
        Log.d(TAG, "removing entry from cache");
        Entry toRemove = entries.get(key);
        if (null == toRemove) {
            return;
        }
        entries.remove(key);
        size -= toRemove.getSize();

        new File(getImageFilename(key)).delete();
        writeToJournal(toRemove, REMOVE);
    }

    /**
     * Use this to check whether the given image is stored locally.
     * @param key
     * @return
     */
    public boolean contains(String key) {
        return entries.containsKey(key);
    }

    /**
     * Load the cached bitmap from the disk. You should check with contains
     * before calling this.
     * @param key
     * @return
     * @throws IOException
     */
    public synchronized Bitmap get(String key) throws IOException {
        Log.d(TAG, "retrieving image from cache");
        Entry entry = entries.get(key);
        writeToJournal(entry, READ);
        return loadImageFromDisk(entry);
    }

    private Bitmap loadImageFromDisk(Entry entry) {
        if (null == entry) {
            return null;
        }
        String filename = getImageFilename(entry.getId());
        return BitmapFactory.decodeFile(filename);
    }

    private void clearCacheSpace() throws IOException {
        Log.d(TAG, "making room in cache");
        while (size > maxSize) {
            removeOldest();
        }
    }

    /**
     * Log an action into the journal
     * @param entry
     * @param action Must be one of CREATE, READ, or REMOVE. If CREATE, also must supply a file size.
     * @throws IOException
     */
    private synchronized void writeToJournal(Entry entry, String action) throws IOException {
        Log.d(TAG, "writing to cache journal");
        if (!CREATE.equals(action) && !READ.equals(action) && !REMOVE.equals(action)){
            throw new IllegalArgumentException("Not a valid journal entry: " +  action);
        }

        if (null == journalWriter) {
            journalWriter = new FileWriter(journalFile, true);
        }

        String toWrite = action + " " + entry.getId();
        if (CREATE.equals(action)) {
            toWrite += " " + entry.getSize();
        }

        journalWriter.write(toWrite + '\n');
        journalWriter.flush();
    }

    /**
     * Deletes everything in the cache, e.g. if the journal is corrupt.
     * @throws IOException
     */
    private void delete() throws IOException {
        close();
        clearDirectory(cacheDirectory);
    }

    /**
     * Deletes all files in the directory, and then removes the directory
     * @param directory directory to delete
     */
    private void clearDirectory(File directory) {
        for (File file : directory.listFiles()) {
            file.delete();
        }

        directory.delete();
    }

    /**
     * Closes all open file operations.
     * @throws IOException
     */
    private void close() throws IOException {
        if (null != journalWriter) {
            journalWriter.close();
        }

        journalWriter = null;
    }

    /**
     * Represents an entry in the cache.
     */
    class Entry {
        private String id;
        private long size;

        public Entry(String id) {
            this.id = id;
        }

        public Entry(String id, long size) {
            this.id = id;
            this.size = size;
        }

        public String getId() {
            return id;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (null == id || !(obj instanceof Entry)) {
                return false;
            }

            Entry that = (Entry) obj;
            return id.equals(that.getId());
        }
    }
}
